package com.bbn.bue.common.parameters.serifstyle;

import com.bbn.bue.common.StringUtils;
import com.bbn.bue.common.TextGroupImmutable;
import com.bbn.bue.common.parameters.ParameterFileLoader;
import com.bbn.bue.common.parameters.Parameters;
import com.bbn.bue.common.parameters.exceptions.ParseFailureException;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class for parsing Serif-style parameter files to <code>Map<String, String></code>.
 *
 * Serif-style parameter files contain lines like:
 * <pre>
 * key: value
 * </pre>
 * If key has already been defined, the line must have the the <code>OVERRIDE</code> prefix like:
 * <pre>
 * OVERRIDE key: value
 * </pre>
 *
 * You may include other parameter files with relative or absolute paths as follows:
 * <pre>
 * INCLUDE foo/bar.params
 * INCLUDE /home/bob/foo.params
 * </pre>
 *
 * You may use the value of previously defined parameters in defining other parameters like this:
 * <pre>
 * foo: /home/bob
 * bar: %foo%/data.dat
 * </pre>
 *
 * Lines prefixed with <code>#</code> are treated as comments.
 *
 * @author rgabbard
 */
@TextGroupImmutable
@Value.Immutable
@Value.Enclosing
public abstract class SerifStyleParameterFileLoader implements ParameterFileLoader {

  private static final Logger log = LoggerFactory.getLogger(SerifStyleParameterFileLoader.class);

  @Value.Default
  public boolean crashOnUndeclaredOverrides() {
    return true;
  }

  public static class Builder extends ImmutableSerifStyleParameterFileLoader.Builder {}

  /**
   * Parses a BBN-style parameter file to a Map.
   */
  @Override
  public final Parameters load(final File configFile) throws IOException {
    final Loading loading = new Loading();
    loading.topLoad(configFile);
    return Parameters.fromMap(loading.ret);
  }

  @Value.Immutable
  @TextGroupImmutable
  abstract static class ParseIssue {
    abstract ImmutableList<File> includeStack();
    abstract int line();
    abstract String message();

    final String toUserMessage() {
      return "At " + Joiner.on(" -> ").join(includeStack()) + ":" + line() + ": " + message();
    }
  }

  private static class UnrecoverableParseError extends RuntimeException {}

  // class to encapsulate all the state during the loading of a file
  private final class Loading {

    final Map<String, String> ret = new HashMap<>();
    final List<ParseIssue> warnings = new ArrayList<>();
    final List<ParseIssue> errors = new ArrayList<>();

    private void topLoad(final File configFile) throws IOException {
      try {
        internalLoad(configFile, new Stack<File>());
      } catch (UnrecoverableParseError pfe) {
        // if a PFE was thrown, an error should have gotten added to
        // the errors list and will be reported below
      }

      for (final ParseIssue warning : warnings) {
        log.warn(warning.toUserMessage());
      }
      if (!errors.isEmpty()) {
        final List<String> messages = new ArrayList<>();
        for (final ParseIssue error : errors) {
          messages.add(error.toUserMessage());
        }
        throw new ParseFailureException(StringUtils.unixNewlineJoiner().join(messages));
      }
    }

    private void internalLoad(final File configFile, Stack<File> includeStack) throws IOException {
      includeStack.push(configFile);

      final File absConfigFile = configFile.getAbsoluteFile();
      log.info("Loading parameter file {}", absConfigFile);

      int i = 1;
      for (String line : Files.readLines(absConfigFile, Charsets.UTF_8)) {
        if (line.startsWith("INCLUDE ")) {
          if (line.length() < 9) {
            errors.add(ImmutableSerifStyleParameterFileLoader.ParseIssue.builder()
            .includeStack(includeStack).line(i)
                .message("INCLUDE must be followed by a filename: " + line).build());
            throw new UnrecoverableParseError();
          }

          // Fix our slashes if we are on Windows
          if (System.getProperty("os.name").startsWith("Windows")) {
            line = line.replace("/", File.separator);
          }

          // Check if we have any interpolation
          line = interpolateLine(line, includeStack, i);
          final String includedFilename = line.substring(line.indexOf(' ') + 1);
          File includedFile = new File(includedFilename);
          if (!includedFile.isAbsolute()) {
            final File curDir = absConfigFile.getParentFile();
            includedFile = new File(curDir, includedFilename); //relative path
          }
          internalLoad(includedFile, includeStack);
        } else //noinspection StatementWithEmptyBody
          if ((line.length() > 0 && line.charAt(0) == '#') || line.isEmpty()) {
          // comment, do nothing
        } else if (line.startsWith("UNSET ")) {
          final String key = line.substring(line.indexOf(' ') + 1).trim();
          ret.remove(key);
        } else {
          boolean override = false;
          if (line.startsWith("OVERRIDE ")) {
            if (line.length() < 10) {
              errors.add(ImmutableSerifStyleParameterFileLoader.ParseIssue.builder()
              .includeStack(includeStack).line(i)
                  .message("OVERRIDE must be followed by a parameter setting: " + line)
                  .build());
              throw new UnrecoverableParseError();
            }
            line = line.substring(line.indexOf(' ') + 1);
            override = true;
          }

          // Check if we have any interpolation
          line = interpolateLine(line, includeStack, i);

          // we limit the split to 2 so we only split on the first :, since our parameters may themselves contain :s
          final String[] parts = line.split(":", 2);
          if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            errors.add(ImmutableSerifStyleParameterFileLoader.ParseIssue.builder()
            .includeStack(includeStack).line(i)
                    .message("Lines must be of the format key: value but got " + line).build());
            throw new UnrecoverableParseError();
          }

          final String key = parts[0].trim();
          final String value = parts[1].trim();

          if (ret.containsKey(key) && !override) {
            final ParseIssue issue = ImmutableSerifStyleParameterFileLoader.ParseIssue.builder()
                .includeStack(includeStack).line(i)
                .message("Attempting to override a parameter without OVERRIDE prefix: " + line)
                .build();
            if (crashOnUndeclaredOverrides()) {
              errors.add(issue);
            } else {
              warnings.add(issue);
            }
          }

          ret.put(key, value);
        }
        ++i;
      }
    }

    private String interpolateLine(String line, Stack<File> includeStack, int curLine) {
      boolean changed = true;
      while (changed) {
        changed = false;
        final Matcher matcher = INTERPOLATE_REGEX.matcher(line);
        if (matcher.find()) {
          final String key = matcher.group(1);
          final String value = ret.get(key);
          if (value != null) {
            line = line.replace("%" + matcher.group(1) + "%", value);
            changed = true;
          } else {
            // we treat interpolation errors as recoverable
            errors.add(ImmutableSerifStyleParameterFileLoader.ParseIssue.builder()
                .includeStack(includeStack).line(curLine)
                .message("Could not interpolate for " + key + ". Available parameters are " + ret)
                .build());
          }
        }
      }
      return line;
    }
  }

  private static final Pattern INTERPOLATE_REGEX =
      Pattern.compile("%((\\w|\\.)+)%", Pattern.CASE_INSENSITIVE);

}

