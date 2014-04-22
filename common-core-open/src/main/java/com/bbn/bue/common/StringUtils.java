package com.bbn.bue.common;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.annotations.Beta;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Beta
public final class StringUtils {
	private StringUtils() { throw new UnsupportedOperationException() ;}
	/**
	 * Returns a string which is the result of replacing every match of regex in the input string with the results of applying replacementFunction
	 * to the matched string. This is a candidate to be moved to a more general utility package.
	 *
	 * @param input
	 * @param regex
	 * @param replacementFunction May not return null.
	 * @return
	 */
	public static String replaceAll(final String input, final String regex, final Function<MatchResult, String> replacementFunction) {
		return replaceAll(input, Pattern.compile(regex), replacementFunction);
	}

	/**
	 * Returns a string which is the result of replacing every match of regex in the input string with the results of applying replacementFunction
	 * to the matched string. This is a candidate to be moved to a more general utility package.
	 *
	 * @param input
	 * @param regex
	 * @param replacementFunction May not return null.
	 * @return
	 */
	public static String replaceAll(final String input, final Pattern regex, final Function<MatchResult, String> replacementFunction) {
		final StringBuffer output = new StringBuffer();
		final Matcher matcher = regex.matcher(input);
		while (matcher.find()) {
			final MatchResult match = matcher.toMatchResult();
			final String replacement = replacementFunction.apply(match);
			if (replacement == null) {
				throw new IllegalArgumentException(String.format("Replacement function returned null for match %s", match.group()));
			}
			if (!replacement.equals(match.group())) {
				matcher.appendReplacement(output, replacement);
			}
		}
		matcher.appendTail(output);
		return output.toString();
	}

	/**
	 * * Returns the index of the {@code n}-th occurence of {@code needle} in {@code s}. If {@ needle}
	 * does not appear in {@code s}, returns -1.
	 *
	 * @param s The string to search. Cannot be null.
	 * @param needle The character to search for.
	 * @param n Return the {@code n}-th occurence
	 * @return
	 */
	public static int nthOccurrenceOf(final String s, final char needle, int n) {
		checkNotNull(s);
		checkArgument(n>0);
		for (int i=0; i<s.length(); ++i) {
			if (needle == s.charAt(i)) {
				--n;
				if (n==0) {
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
		final LineProcessor<Set<String>> callback = new LineProcessor<Set<String>> () {
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
	 * Convenience wrappers for join. This is not better than just executing the
	 * underlying guava call, but IMO has a simpler syntax.
	 * @param list
	 * @param separator
	 * @return
	 */
	public static String join(final Iterable<?> list, final String separator) {
		return Joiner.on(separator).join(list);
	}

	public static String joinSkipNulls(final Iterable<?> list, final String separator) {
		return Joiner.on(separator).skipNulls().join(list);
	}

	/**
	 * Returns a Function which will join the string with the specified separator
	 * @param separator
	 * @return
	 */
	public static Function<Iterable<?>,String> JoinFunction(final Joiner joiner) {
		return new Function<Iterable<?>,String> () {
			@Override
			public String apply(final Iterable<?> list) {
				return joiner.join(list);
			}
		};
	}

	public static Joiner SpaceJoiner = Joiner.on(" ");
	public static Function<Iterable<?>,String> SpaceJoin =
			JoinFunction(SpaceJoiner);
	public static Joiner NewlineJoiner = Joiner.on("\n");
	public static Function<Iterable<?>,String> NewlineJoin =
			JoinFunction(NewlineJoiner);
	public static Joiner CommaJoiner = Joiner.on(",");
	public static Function<Iterable<?>,String> CommaJoin =
			JoinFunction(CommaJoiner);
	public static Joiner CommaSpaceJoiner = Joiner.on(", ");
	public static Function<Iterable<?>,String> CommaSpaceJoin =
			JoinFunction(CommaSpaceJoiner);
	public static Joiner SemicolonJoiner = Joiner.on(";");
	public static Function<Iterable<?>,String> SemicolonJoin =
			JoinFunction(SemicolonJoiner);
	public static Joiner SemicolonSpaceJoiner = Joiner.on("; ");
	public static Function<Iterable<?>,String> SemicolonSpaceJoin =
			JoinFunction(SemicolonSpaceJoiner);
	public static Joiner DotJoiner = Joiner.on(".");
	public static Function<Iterable<?>, String> DotJoin =
			JoinFunction(DotJoiner);

	/************* Splitters ********************/
	/**
	 * Splits on tab, omitting empty strings and trimming results.
	 */
	public static final Splitter OnTabs = Splitter.on("\t").trimResults().omitEmptyStrings();
	/**
	 * Splits on spaces, omitting empty strings and trimming results.
	 */
	public static final Splitter OnSpaces = Splitter.on(" ").trimResults().omitEmptyStrings();
	/**
	 * Splits on Unix newlines, omitting empty strings and trimming results.
	 */
	public static final Splitter OnUnixNewlines = Splitter.on("\n").trimResults().omitEmptyStrings();

	/**
	 * Splits on commas, omitting empty strings and trimming results.
	 */
	public static final Splitter OnCommas = Splitter.on(",").trimResults().omitEmptyStrings();

	/********************** Wrapping functions ********************/

	/**
	 * Returns a Function which will wrap a string in the specified wrappers
	 * string (e.g. if the wrappers are "[", "]", it will transform "foo" to "[foo]"
	 * @param wrapString
	 * @return
	 */
	public static Function<String,String> WrapFunction(final String leftWrapper,
		final String rightWrapper)
	{
		Preconditions.checkNotNull(leftWrapper);
		Preconditions.checkNotNull(rightWrapper);

		return new Function<String, String> () {
			@Override
			public String apply(final String s) {
				return leftWrapper + s + rightWrapper;
			}
		};
	}

	public static Function<String,String> WrapInDoubleQuotes = WrapFunction("\"","\"");
	public static Function<String,String> WrapInSingleQuotes = WrapFunction("'","'");
	public static Function<String,String> WrapInSquareBrackets = WrapFunction("[","]");
	public static Function<String,String> WrapInAngleBrackets = WrapFunction("<",">");
	public static Function<String,String> WrapInParens = WrapFunction("(",")");

	public static Function<String, String> ToLowerCase = new Function<String, String>() {
		@Override
		public String apply(final String s) {
			return s.toLowerCase();
		}
	};

	public static Predicate<String> ContainsLetterOrDigit = new Predicate<String>() {
		@Override
		public boolean apply(final String s) {
			for (int i=0; i<s.length(); ++i) {
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

	public static Function<String, String> PrefixWith(final String prefix) {
		return new Function<String, String> () {
			@Override
			public String apply(final String s) {
				return prefix + s;
			}
		};
	}

	public static Predicate<String> startsWith(final String prefix) {
		return new Predicate<String>() {
			@Override
			public boolean apply(final String x) {
				return x.startsWith(prefix);
			}
		};
	}

	public static String removeSuffixIfPresent(final String name, final String badSuffix) {
		if (name.endsWith(badSuffix)) {
			return name.substring(0, name.length()-badSuffix.length());
		} else {
			return name;
		}
	}

    public static Predicate<String> Contains(final String probe) {
        checkNotNull(probe);
        return new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return input.contains(probe);
            }
        };
    }

    /**
     * Just like {@link java.lang.String#indexOf(String, int)}, except it searches for all
     * strings in {@code probes}.  If none are found, returns -1. If any are found, returns
     * the earliest index of a match. The current implementation naively searches for each string
     * separately. If speed is important, consider an alternative approach.
     *
     * @param probes
     * @param from
     * @return
     */
    public static int earliestIndexOfAny(String s, Iterable<String> probes, int from) {
        int earliestIdx = -1;

        for (final String probe  : probes) {
            final int probeIdx = s.indexOf(probe, from);
            // if we found something for this probe
            if (probeIdx >= 0
               // and either we haven't found anything else yet or
               // this is earlier than anything we've found yet
               && (earliestIdx == -1 || probeIdx < earliestIdx))
            {
                // then this is our new earliest match
                earliestIdx = probeIdx;
            }
        }

        return earliestIdx;
    }
}
