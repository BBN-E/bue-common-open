package com.bbn.bue.common.symbols;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Comparator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility methods for {link Symbol}s
 * @author rgabbard
 *
 */
@Beta
public class SymbolUtils {
	private SymbolUtils() { throw new UnsupportedOperationException(); }

  /**
	 * Compares <code>Symbol</code>s by the <code>String</code>s used to create them.
	 * @author rgabbard
	 *
	 */
	public static class ByString implements Comparator<Symbol> {
		@Override
		public int compare(final Symbol s1, final Symbol s2) {
			if (s1 == null) {
				if (s2 == null) {
					return 0;
				} else {
					return -1;
				}
			} else if (s2 == null) {
				return 1;
			}
			return s1.toString().compareTo(s2.toString());
		}
	}

	/**
	 * For every input <code>String s</code>, returns <code>Symbol.from(s)</code>.
	 */
	public static final Function<String, Symbol> Symbolize = new Function<String, Symbol>() {
		@Override
		public Symbol apply(final String s) {
			return Symbol.from(s);
		}
	};

	/**
	 * Creates a <code>Set</code> of Symbols from some strings.
	 * The returned <code>Set</code> is immutable.
	 *
	 * @param strings No string may be null.
	 */
	public static ImmutableSet<Symbol> setFrom(final Iterable<String> strings) {
		checkNotNull(strings);

		return FluentIterable.from(strings)
				.transform(Symbolize)
				.toSet();
	}

	/**
	 * Creates a <code>List</code> of Symbols from some strings.
	 * The returned <code>List</code> is immutable.
	 *
	 * @param strings No string may be null.
	 */
	public static ImmutableList<Symbol> listFrom(final Iterable<String> strings) {
		checkNotNull(strings);
		return FluentIterable.from(strings)
				.transform(Symbolize)
				.toList();
	}

	public static ImmutableSet<String> toStringSet(final Iterable<Symbol> syms) {
		final ImmutableSet.Builder<String> ret = ImmutableSet.builder();

		for (final Symbol sym : syms) {
			ret.add(sym.toString());
		}

		return ret.build();
	}

	public static ImmutableSet<Symbol> setFrom(final String... strings) {
		final ImmutableSet.Builder<Symbol> ret = ImmutableSet.builder();
		for (final String s : strings) {
			ret.add(Symbol.from(s));
		}
		return ret.build();
	}

  /**
   * Returns a lowercased version of the specified symbol, where lowercasing is done by
   * {@link String#toLowerCase()}.
   */
  public static Symbol lowercaseSymbol(Symbol s) {
    return Symbol.from(s.toString().toLowerCase());
  }
}
