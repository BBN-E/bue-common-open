package com.bbn.bue.common;

import com.bbn.bue.common.strings.offsets.CharOffset;
import com.bbn.bue.common.strings.offsets.OffsetRange;

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Beta
public final class StringUtils {

  private StringUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * Makes a string into a {@link UnicodeFriendlyString}. See that interface's Javadoc for more
   * details.
   */
  public static UnicodeFriendlyString unicodeFriendly(String utf16CodeUnits) {
    if (utf16CodeUnits.codePointCount(0, utf16CodeUnits.length()) == utf16CodeUnits.length()) {
      return StringWithoutNonBmp.of(utf16CodeUnits);
    } else {
      return StringWithNonBmp.of(utf16CodeUnits);
    }
  }

  /**
   * Applies {@link #unicodeFriendly(String)} to the contents of a list.
   */
  public static ImmutableList<UnicodeFriendlyString> unicodeFriendlyList(Iterable<String> strings) {
    final ImmutableList.Builder<UnicodeFriendlyString> ret = ImmutableList.builder();

    for (final String s : strings) {
      ret.add(unicodeFriendly(s));
    }

    return ret.build();
  }

  /**
   * Applies {@link #unicodeFriendly(String)} to the contents of a set.
   */
  public static ImmutableSet<UnicodeFriendlyString> unicodeFriendlySet(Iterable<String> strings) {
    final ImmutableSet.Builder<UnicodeFriendlyString> ret = ImmutableSet.builder();

    for (final String s : strings) {
      ret.add(unicodeFriendly(s));
    }

    return ret.build();
  }

  public static ImmutableSet<UnicodeFriendlyString> unicodeFriendlySet(String s1, String... strings) {
    final ImmutableSet.Builder<UnicodeFriendlyString> ret = ImmutableSet.builder();

    ret.add(unicodeFriendly(s1));
    for (final String s : strings) {
      ret.add(unicodeFriendly(s));
    }

    return ret.build();
  }

  public static String codepointToString(int codePoint) {
    return new String(Character.toChars(codePoint));
  }

  /**
   * Returns a string which is the result of replacing every match of regex in the input string with
   * the results of applying replacementFunction to the matched string. This is a candidate to be
   * moved to a more general utility package.
   *
   * @param replacementFunction May not return null.
   */
  public static String replaceAll(final String input, final String regex,
      final Function<MatchResult, String> replacementFunction) {
    return replaceAll(input, Pattern.compile(regex), replacementFunction);
  }

  /**
   * Returns a string which is the result of replacing every match of regex in the input string with
   * the results of applying replacementFunction to the matched string. This is a candidate to be
   * moved to a more general utility package.
   *
   * @param replacementFunction May not return null.
   */
  public static String replaceAll(final String input, final Pattern regex,
      final Function<MatchResult, String> replacementFunction) {
    final StringBuffer output = new StringBuffer();
    final Matcher matcher = regex.matcher(input);
    while (matcher.find()) {
      final MatchResult match = matcher.toMatchResult();
      final String replacement = replacementFunction.apply(match);
      if (replacement == null) {
        throw new IllegalArgumentException(
            String.format("Replacement function returned null for match %s", match.group()));
      }
      if (!replacement.equals(match.group())) {
        matcher.appendReplacement(output, replacement);
      }
    }
    matcher.appendTail(output);
    return output.toString();
  }

  /**
   * * Returns the index of the {@code n}-th occurence of {@code needle} in {@code s}. If {@code needle}
   * does not appear in {@code s}, returns -1.
   *
   * @param s      The string to search. Cannot be null.
   * @param needle The character to search for.
   * @param n      Return the {@code n}-th occurence
   */
  public static int nthOccurrenceOf(final String s, final char needle, int n) {
    checkNotNull(s);
    checkArgument(n > 0);
    for (int i = 0; i < s.length(); ++i) {
      if (needle == s.charAt(i)) {
        --n;
        if (n == 0) {
          return i;
        }
      }
    }
    return -1;
  }

  public static Set<String> stringSetFrom(final File stringFile) throws IOException {
    return stringSetFrom(Files.asCharSource(stringFile, Charsets.UTF_8));
  }

  public static Set<String> stringSetFrom(final CharSource supplier) throws IOException {
    final LineProcessor<Set<String>> callback = new LineProcessor<Set<String>>() {
      private final ImmutableSet.Builder<String> builder = ImmutableSet.builder();

      @Override
      public boolean processLine(final String s) {
        builder.add(s.trim());
        return true;
      }

      @Override
      public Set<String> getResult() {
        return builder.build();
      }
    };

    supplier.readLines(callback);
    return callback.getResult();
  }

  /**
   * Returns a Function which will join the string with the specified separator
   */
  public static Function<Iterable<?>, String> joinFunction(final Joiner joiner) {
    return new Function<Iterable<?>, String>() {
      @Override
      public String apply(final Iterable<?> list) {
        return joiner.join(list);
      }
    };
  }

  @SuppressWarnings("deprecation")
  public static Joiner spaceJoiner() {
    return SpaceJoiner;
  }

  private static final Joiner OR_JOINER = Joiner.on("|");

  /**
   * A {@link Joiner} which joins on |.  Handy for constructing regular expressions.
   */
  public static Joiner pipeJoiner() {
    return OR_JOINER;
  }

  /**
   * @deprecated Prefer {@link #spaceJoiner()}
   */
  @Deprecated
  public static final Joiner SpaceJoiner = Joiner.on(" ");

  @SuppressWarnings("deprecation")
  public static Joiner unixNewlineJoiner() {
    return NewlineJoiner;
  }

  /**
   * @deprecated Prefer {@link #unixNewlineJoiner()}
   */
  @Deprecated
  public static final Joiner NewlineJoiner = Joiner.on("\n");

  @SuppressWarnings("deprecation")
  public static Joiner commaJoiner() {
    return CommaJoiner;
  }

  @SuppressWarnings("deprecation")
  public static Joiner dotJoiner() {
    return DotJoiner;
  }

  /**
   * @deprecated Prefer {@link #commaJoiner()}.
   */
  @Deprecated
  public static final Joiner CommaJoiner = Joiner.on(",");
  public static final Function<Iterable<?>, String> CommaJoin =
      JoinFunction(CommaJoiner);
  public static final Joiner CommaSpaceJoiner = Joiner.on(", ");
  public static final Function<Iterable<?>, String> CommaSpaceJoin =
      JoinFunction(CommaSpaceJoiner);
  public static final Joiner SemicolonJoiner = Joiner.on(";");
  public static final Function<Iterable<?>, String> SemicolonJoin =
      JoinFunction(SemicolonJoiner);
  public static final Joiner SemicolonSpaceJoiner = Joiner.on("; ");
  public static final Function<Iterable<?>, String> SemicolonSpaceJoin =
      JoinFunction(SemicolonSpaceJoiner);
  @Deprecated
  /**
   * Prefer {@link #dotJoiner()}
   */
  public static final Joiner DotJoiner = Joiner.on(".");


  /************* Splitters ********************/


  /**
   * Splits on tab, omitting empty strings and trimming results.
   */
  public static Splitter onTabs() {
    return OnTabs;
  }


  /**
   * Splits on spaces, omitting empty strings and trimming results.
   */
  public static Splitter onSpaces() {
    return OnSpaces;
  }


  /**
   * Splits on Unix newlines, omitting empty strings and trimming results.
   */
  public static Splitter onUnixNewlines() {
    return OnUnixNewlines;
  }


  /**
   * Splits on commas, omitting empty strings and trimming results.
   */
  public static Splitter onCommas() {
    return OnCommas;
  }

  private static final Splitter onDots = Splitter.on(".").trimResults().omitEmptyStrings();

  /**
   * Splits on periods, omitting empty strings and trimming results.
   */
  public static Splitter onDots() {
    return onDots;
  }

  private static final Splitter onDashes = Splitter.on("-").trimResults().omitEmptyStrings();

  /**
   * Splits on dashes, omitting empty strings and trimming results.
   */
  public static Splitter onDashes() {
    return onDashes;
  }

  /********************** Wrapping functions ********************/

  /**
   * Returns a Function which will wrap a string in the specified wrappers string (e.g. if the
   * wrappers are "[", "]", it will transform "foo" to "[foo]"
   */
  public static Function<String, String> WrapFunction(final String leftWrapper,
      final String rightWrapper) {
    Preconditions.checkNotNull(leftWrapper);
    Preconditions.checkNotNull(rightWrapper);

    return new Function<String, String>() {
      @Override
      public String apply(final String s) {
        return leftWrapper + s + rightWrapper;
      }
    };
  }

  public static final Function<String, String> WrapInDoubleQuotes = WrapFunction("\"", "\"");
  public static final Function<String, String> WrapInSingleQuotes = WrapFunction("'", "'");
  public static final Function<String, String> WrapInSquareBrackets = WrapFunction("[", "]");
  public static final Function<String, String> WrapInAngleBrackets = WrapFunction("<", ">");
  public static final Function<String, String> WrapInParens = WrapFunction("(", ")");

  /**
   * A Guava function for converting strings to lowercase.
   * @param locale
   * @return
   */
  public static Function<String, String> toLowerCaseFunction(final Locale locale) {
    return new Function<String, String>() {
      @Override
      public String apply(final String s) {
        return s.toLowerCase(locale);
      }
    };
  }

  public static final Predicate<String> ContainsLetterOrDigit = new Predicate<String>() {
    @Override
    public boolean apply(final String s) {
      for (int i = 0; i < s.length(); ++i) {
        if (Character.isLetterOrDigit(s.charAt(i))) {
          return true;
        }
      }
      return false;
    }
  };

  public static final Function<String, String> Trim = new Function<String, String>() {
    @Override
    public String apply(final String s) {
      return s.trim();
    }
  };

  public static final Function<String, String> prefixWithFunction(final String prefix) {
    return new Function<String, String>() {
      @Override
      public String apply(final String s) {
        return prefix + s;
      }
    };
  }

  public static final Function<String, String> suffixWithFunction(final String suffix) {
    return new Function<String, String>() {
      @Override
      public String apply(final String s) {
        return s + suffix;
      }
    };
  }

  public static final Predicate<String> startsWith(final String prefix) {
    return new Predicate<String>() {
      @Override
      public boolean apply(final String x) {
        return x.startsWith(prefix);
      }
    };
  }

  public static final String removeSuffixIfPresent(final String name, final String badSuffix) {
    if (name.endsWith(badSuffix)) {
      return name.substring(0, name.length() - badSuffix.length());
    } else {
      return name;
    }
  }

  /**
   * Gets a predicate which returns true for a {@code String} iff it contains {@code probe} as a substring.
   */
  @SuppressWarnings("unchecked")
  public static final Predicate<String> containsPredicate(final String probe) {
    return Contains(probe);
  }

  /**
   * A predicate which returns true for a {@code String} iff at least one of its characters
   * matches the provided {@link CharMatcher}
   */
  public static final Predicate<String> anyCharMatches(final CharMatcher matcher) {
    return new Predicate<String>() {
      @Override
      public boolean apply(@Nullable final String input) {
        return matcher.matchesAnyOf(input);
      }
    };
  }

  public static final Predicate<String> isEmpty() {
    return new Predicate<String>() {
      @Override
      public boolean apply(final String input) {
        checkArgument(input != null);
        return input.isEmpty();
      }
    };
  }

  /**
   * Just like {@link java.lang.String#indexOf(String, int)}, except it searches for all strings in
   * {@code probes}.  If none are found, returns -1. If any are found, returns the earliest index of
   * a match. The current implementation naively searches for each string separately. If speed is
   * important, consider an alternative approach.
   */
  public static int earliestIndexOfAny(String s, Iterable<String> probes, int from) {
    int earliestIdx = -1;

    for (final String probe : probes) {
      final int probeIdx = s.indexOf(probe, from);
      // if we found something for this probe
      if (probeIdx >= 0
          // and either we haven't found anything else yet or
          // this is earlier than anything we've found yet
          && (earliestIdx == -1 || probeIdx < earliestIdx)) {
        // then this is our new earliest match
        earliestIdx = probeIdx;
      }
    }

    return earliestIdx;
  }

  /**
   * Returns the number of codepoints in a string.
   */
  public static int codepointCount(final String input) {
    return CodepointCountFunction.INSTANCE.apply(input);
  }

  /**
   * Returns a function that computes the number of code points in a string.
   */
  public static Function<String, Integer> codepointCountFunction() {
    return CodepointCountFunction.INSTANCE;
  }

  /**
   * Returns the substring of {@code s} which starts at the Unicode codepoint offset at
   * {@code startIndexInclusive} and ends before the Unicode codepoint offset at
   * {@code endIndexExclusive}. You should typically use this instead
   * of {@link String#substring(int)} because the latter will fail badly in the presence of
   * non-BMP characters.
   *
   * Beware this takes linear time according to the end index of the substring, rather than the
   * substring's length, like for {@link String#substring(int)}
   */
  public static String substringByCodepoints(String s, int startIndexInclusive,
      int endIndexExclusive) {
    return substringByCodepoints(s, startIndexInclusive, endIndexExclusive, false);
  }

  /**
   * Returns the substring of {@code s} indicated by {@code substringBounds}, where the character
   * offsets are interpreted as Unicode code point offsets.  You should typically use this instead
   * of {@link String#substring(int)} because the latter will fail badly in the presence of
   * non-BMP characters.
   *
   * Beware this takes linear time according to the end index of the substring, rather than the
   * substring's length, like for {@link String#substring(int)}
   */
  public static String substringByCodepoints(String s, OffsetRange<CharOffset> substringBounds) {
    return substringByCodepoints(s, substringBounds.startInclusive().asInt(),
        // +1 because called method takes exclusive end offset like String#substring
        substringBounds.endInclusive().asInt() + 1 );
  }

  /**
   * Acts just like {@link #laxSubstringByCodepoints(String, int, int)} except that if either
   * index is out-of-bounds, it is clipped to the most extreme legal value.
   * This guarantees that as long as {@code s} is non-null and
   * {@code endIndexExclusive>=startIndexInclusive}, no exception will be thrown when
   * calling this method.
   */
  public static String laxSubstringByCodepoints(String s, int startIndexInclusive,
      int endIndexExclusive) {
    return substringByCodepoints(s, startIndexInclusive, endIndexExclusive, true);
  }

  private static String substringByCodepoints(String s, int startIndexInclusive,
      int endIndexExclusive, boolean lax) {
    checkArgument(startIndexInclusive <= endIndexExclusive);
    if (lax) {
      startIndexInclusive = Math.max(startIndexInclusive, 0);
    }
    checkArgument(startIndexInclusive >= 0);

    final int startCharIdx = s.offsetByCodePoints(0, startIndexInclusive);

    final int substringCodePointLength = endIndexExclusive - startIndexInclusive;
    final int endCharIdxInclusive;
    try {
      endCharIdxInclusive = s.offsetByCodePoints(startCharIdx, substringCodePointLength - 1);
    } catch (IndexOutOfBoundsException ibe) {
      if (lax) {
        // handle clipping at the end in lax mode
        return s.substring(startCharIdx, s.length());
      } else {
        throw ibe;
      }
    }

    // we want an exclusive character offset for toString below, so we need to go one more codepoint.
    // However, that may be one or two characters depending on exactly what the last code point is
    final int lastCodePoint = s.codePointAt(endCharIdxInclusive);
    int endCharIdxExclusive = endCharIdxInclusive + Character.charCount(lastCodePoint);

    if (lax && endCharIdxExclusive > s.length()) {
      // if we are requested to substring "safely", clip the substring to the end of the string
      endCharIdxExclusive = s.length();
    }
    return s.substring(startCharIdx, endCharIdxExclusive);
  }

  /**
   * Checks that the supplied string is non-empty. If it is empty, an {@link
   * java.lang.IllegalArgumentException} is thrown with the supplied message.
   */
  public static String checkNonEmpty(String s, String msg) {
    checkArgument(!s.isEmpty(), msg);
    return s;
  }

  /**
   * Produces a string representation of a positive integer padded with leading zeros. Enough zeros
   * are adding so that the supplied {@code maxValue} would have the same number of digits.
   */
  public static String padWithMax(final int numToPad, final int maxValue) {
    checkArgument(numToPad >= 0);
    checkArgument(numToPad <= maxValue);
    final int maxLength = Integer.toString(maxValue).length();
    final String baseString = Integer.toString(numToPad);
    final String padding = Strings.repeat("0", maxLength - baseString.length());
    return padding + numToPad;
  }

  /**
   * Returns the code points contained in a {@link String}. Use {@link #toCodepointStrings(String)}
   * to get the {@link String} representation for each codepoint.
   *
   * @see #toCodepoints(String)
   */
  public static ImmutableList<Integer> toCodepoints(final String s) {
    final ImmutableList.Builder<Integer> ret = ImmutableList.builder();
    for (int offset = 0; offset < s.length();) {
      final int codePoint = s.codePointAt(offset);
      ret.add(codePoint);
      offset += Character.charCount(codePoint);
    }
    return ret.build();
  }

  /**
   * Returns each code point in a {@link String} converted into a {@link String}. Useful for iterating
   * over {@link String}s in a Unicode-aware fashion. Use {@link #toCodepoints(String)} to get the
   * codepoints themselves.
   *
   * @see #toCodepoints(String)
   */
  public static ImmutableList<String> toCodepointStrings(final String s) {
    final ImmutableList.Builder<String> ret = ImmutableList.builder();
    for (int codePoint : toCodepoints(s)) {
      ret.add(new String(Character.toChars(codePoint)));
    }
    return ret.build();
  }

  /**
   * Guava {@link Function} which runs {@link String#trim()} on all inputs.
   */
  public static Function<String, String> trimFunction() {
    return TrimFunction.INSTANCE;
  }

  private enum TrimFunction implements Function<String, String> {
    INSTANCE;

    @Override
    public String apply(final String input) {
      return input.trim();
    }
  }

  private enum CodepointCountFunction implements Function<String, Integer> {
    INSTANCE;

    @Override
    public Integer apply(final String input) {
      return input.codePointCount(0, input.length());
    }
  }

  // \p{M} means all Unicode "marks"
  private static final Pattern ACCENT_STRIPPER = Pattern.compile("[\\p{M}]");

  /**
   * Removes all Unicode marks from a string. As a side effect, applies NFD normalization.
   */
  public static UnicodeFriendlyString stripAccents(final UnicodeFriendlyString input) {
    // this nifty normalization courtesy of http://stackoverflow.com/questions/3322152/is-there-a-way-to-get-rid-of-accents-and-convert-a-whole-string-to-regular-lette
    return StringUtils.unicodeFriendly(ACCENT_STRIPPER.matcher(
        Normalizer.normalize(input.utf16CodeUnits(), Normalizer.Form.NFD))
        // note this replaceAll is really deleteAll
        .replaceAll(""));
  }

  /******************************** Deprecated code ************************************/

  /**
   * @deprecated Just use Guava's {@link Joiner} as normal.
   */
  @Deprecated
  public static String join(final Iterable<?> list, final String separator) {
    return Joiner.on(separator).join(list);
  }

  /**
   * @deprecated Just use Guava's {@link Joiner} as normal.
   */
  @Deprecated
  public static String joinSkipNulls(final Iterable<?> list, final String separator) {
    return Joiner.on(separator).skipNulls().join(list);
  }

  /**
   * @deprecated Prefer {@link #joinFunction(Joiner)}'s more consistent capitalization.
   */
  @Deprecated
  public static final Function<Iterable<?>, String> JoinFunction(final Joiner joiner) {
    return joinFunction(joiner);
  }

  /**
   * @deprecated Prefer {@link #joinFunction(Joiner)} applies to {@link #spaceJoiner()}.
   */
  @Deprecated
  public static final Function<Iterable<?>, String> SpaceJoin =
      JoinFunction(SpaceJoiner);

  /**
   * @deprecated Prefer {@link #joinFunction(Joiner)} applied to {@link #unixNewlineJoiner()}
   */
  @Deprecated
  public static final Function<Iterable<?>, String> NewlineJoin =
      JoinFunction(NewlineJoiner);

  /**
   * @deprecated Use {@link #onSpaces()}
   */
  @Deprecated
  public static final Splitter OnSpaces = Splitter.on(" ").trimResults().omitEmptyStrings();

  /**
   * @deprecated Use {@link #onTabs()}
   */
  @Deprecated
  public static final Splitter OnTabs = Splitter.on("\t").trimResults().omitEmptyStrings();

  /**
   * @deprecated Use {@link #onUnixNewlines()}
   */
  @Deprecated
  public static final Splitter OnUnixNewlines = Splitter.on("\n").trimResults().omitEmptyStrings();

  /**
   * @deprecated Use {@link #onCommas()}
   */
  @Deprecated
  public static final Splitter OnCommas = Splitter.on(",").trimResults().omitEmptyStrings();

  @Deprecated
  /**
   * @deprecated Prefer {@link #joinFunction(Joiner)} on {@link #dotJoiner()}
   */
  public static final Function<Iterable<?>, String> DotJoin =
      JoinFunction(DotJoiner);

  /**
   * @deprecated Prefer {@link #containsPredicate(String)}
   */
  @Deprecated
  public static final Predicate<String> Contains(final String probe) {
    checkNotNull(probe);
    return new Predicate<String>() {
      @Override
      public boolean apply(String input) {
        return input.contains(probe);
      }
    };
  }

  /**
   * Computes the length of a string in the naive, non-Unicode safe fashion. Use with extreme
   * caution, and prefer {@link #codepointCountFunction()}  ()} for almost every use case.
   *
   * @deprecated Prefer {@link #codepointCountFunction()}, which is Unicode-safe.
   */
  @Deprecated
  public static final Function<String, Integer> ToLength = new Function<String, Integer>() {
    @Override
    public Integer apply(String input) {
      checkNotNull(input);
      return input.length();
    }
  };


  /**
   * Prefer {@link #toLowerCaseFunction(Locale)}, which requires specifying a {@link Locale}.
   */
  @Deprecated
  public static final Function<String, String> ToLowerCase = new Function<String, String>() {
    @Override
    public String apply(final String s) {
      return s.toLowerCase();
    }
  };

  /**
   * @deprecated Prefer {@link #prefixWithFunction(String)}
   */
  @Deprecated
  public static final Function<String, String> PrefixWith(final String prefix) {
    return prefixWithFunction(prefix);
  }

  /**
   * @deprecated Prefer {@link #substringByCodepoints(String, OffsetRange)} for most NLP uses.
   */
  @Deprecated
  public static String substring(String s, OffsetRange<CharOffset> substringBounds) {
    return s.substring(substringBounds.startInclusive().asInt(),
        substringBounds.endInclusive().asInt() + 1);
  }

  /**
   * @deprecated Prefer {@link #laxSubstringByCodepoints(String, int, int)}
   */
  @Deprecated
  public static String safeSubstring(String s, int startIndexInclusive, int endIndexExclusive) {
    final int trueStartIndex = Math.max(0, startIndexInclusive);
    final int trueEndIndex = Math.min(endIndexExclusive, s.length());
    return s.substring(trueStartIndex, trueEndIndex);
  }
}
