package com.bbn.bue.common.parameters.serifstyle;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bbn.bue.common.parameters.ParameterFileLoader;
import com.bbn.bue.common.parameters.exceptions.ErrorInIncludedParameterFileException;
import com.bbn.bue.common.parameters.exceptions.ParseFailureException;
import com.google.common.annotations.Beta;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import static com.google.common.base.Preconditions.checkState;

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
 *
 */
@Beta
public class SerifStyleParameterFileLoader implements ParameterFileLoader {

	/**
	 * Creates a <code>SerifStyleParameterFileLoader</code> which crashes on undeclared overrides.
	 */
	public SerifStyleParameterFileLoader() {
		this(true);
	}

	/**
	 * Creates a <code>SerifStyleParameterFileLoader</code> which crashes or not on undeclared overrides as specified.
	 * The standard behavior of BBN parameter files is to crash if a parameter file redefines an
	 * already defined parameter without explicitly specifying the OVERRIDE prefix.  Sometimes
	 * this behavior is inconvenient, so this constructor provides a way to turn it off, and allow
	 * silent redefinition.
	 */
	public SerifStyleParameterFileLoader(final boolean crashOnUndeclaredOverrides) {
		this.crashOnUndeclaredOverrides = crashOnUndeclaredOverrides;
	}

	/** Parses a BBN-style parameter file to a Map.
	 */
	@Override
	public Map<String, String> load(final File configFile) throws IOException {
		final Map<String, String> ret = Maps.newHashMap();
		load(configFile, ret);
		return ret;
	}

    private String interpolateLine(String line, final Map<String, String> ret) {
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
					throw new InterpolationException(key, ret);
				}
			}
		}
		return line;
	}

	private void load(final File configFile, final Map<String, String> ret) throws IOException {
		final File absConfigFile = configFile.getAbsoluteFile();

		int i = 1;
		for (String line : Files.readLines(absConfigFile, Charsets.UTF_8)) {
			if (line.startsWith("INCLUDE ")) {
				if (line.length() < 9) {
					throw new ParseFailureException("INCLUDE must be followed by a filename",
						line, absConfigFile, i);
				}

                // Fix our slashes if we are on Windows
                if (System.getProperty("os.name").startsWith("Windows")) {
                    line = line.replace("/", File.separator);
                }

                // Check if we have any interpolation
                line = interpolateLine(line, ret);
				final String includedFilename = line.substring(line.indexOf(' ') + 1);
                File includedFile = new File(includedFilename);
                if (!includedFile.isAbsolute()) {
                    final File curDir = absConfigFile.getParentFile();
                    includedFile = new File(curDir, includedFilename); //relative path
                }
				try {
					load(includedFile, ret);
				} catch (final ErrorInIncludedParameterFileException e) {
					throw  ErrorInIncludedParameterFileException.fromNextLevel(configFile, i, e);
				} catch (final Exception e) {
					throw ErrorInIncludedParameterFileException.fromException(configFile, i, e);
				}
			} else if ((line.length() > 0 && line.charAt(0) == '#') || line.isEmpty()) {
                            // comment, do nothing
            } else if (line.startsWith("UNSET ")) {
                final String key = line.substring(line.indexOf(' ')+1).trim();
                ret.remove(key);
            } else {
				boolean override = false;
				if (line.startsWith("OVERRIDE ")) {
					if (line.length() < 10) {
						throw new ParseFailureException("OVERRIDE must be followed by a parameter setting",
							line, absConfigFile, i);
					}
					line=line.substring(line.indexOf(' ')+1);
					override = true;
				}

                // Check if we have any interpolation
                line = interpolateLine(line, ret);

				// we limit the split to 2 so we only split on the first :, since our parameters may themselves contain :s
				final String[] parts = line.split(":", 2);
				if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
					throw new ParseFailureException("Lines must be of the format key: value",
						line, absConfigFile, i);
				}
				final String key = parts[0].trim();
				final String value = parts[1].trim();

				if (ret.containsKey(key) && !override && crashOnUndeclaredOverrides) {
					throw new ParseFailureException("Attempting to override a parameter without OVERRIDE prefix",
						line, absConfigFile, i);
				}

				checkState(value != null);
				ret.put(key, value);
			}
			++i;
		}
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("crashOnUndeclareOverrides", crashOnUndeclaredOverrides)
				.toString();
	}

	private static final Pattern INTERPOLATE_REGEX = Pattern.compile("%((\\w|\\.)+)%", Pattern.CASE_INSENSITIVE);

	private final boolean crashOnUndeclaredOverrides;
}

