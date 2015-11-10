package com.bbn.bue.common.symbols;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;

import java.util.Comparator;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility methods for {link Symbol}s
 *
 * @author rgabbard
 */
@Beta
public class SymbolUtils {

  private SymbolUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * An ordering which compares <code>Symbol</code>s by the <code>String</code>s used to create them.
   *
   * @author rgabbard
   */
  public static Ordering<Symbol> byStringOrdering() {
    return Ordering.from(new ByString());
  }

  /**
   * Compares <code>Symbol</code>s by the <code>String</code>s used to create them.
   *
   * @author rgabbard
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
   * Returns a function that transforms a {@link Symbol} into a {@link String} using
   * {@link Symbol#asString()}.
   */
  public static Function<Symbol,String> desymbolizeFunction() {
    return DesymbolizeFunction.INSTANCE;
  }

  private enum DesymbolizeFunction implements Function<Symbol, String> {
    INSTANCE;
    @Override
    public String apply(final Symbol s) {
      return s.asString();
    }
  }

  /**
   * Returns a function that transforms a {@link String} into a {@link Symbol} using
   * {@link Symbol#from(String)}.
   */
  public static Function<String, Symbol> symbolizeFunction() {
    return SymbolizeFunction.INSTANCE;
  }

  private enum SymbolizeFunction implements Function<String, Symbol> {
    INSTANCE;
    @Override
    public Symbol apply(final String s) {
      return Symbol.from(s);
    }
  }

  /**
   * Creates a <code>Set</code> of Symbols from some strings. The returned <code>Set</code> is
   * immutable.
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
   * Creates a <code>List</code> of Symbols from some strings. The returned <code>List</code> is
   * immutable.
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
   * Returns a lowercased version of the specified symbol, where lowercasing is done by {@link
   * String#toLowerCase()}.
   */
  public static Symbol lowercaseSymbol(Symbol s) {
    return Symbol.from(s.toString().toLowerCase());
  }

  /**
   * Creates a map of {@link Symbol}s from a map of {@link String}s.  No keys or values
   * may be null.
   */
  public static ImmutableMap<Symbol, Symbol> mapFrom(Map<String, String> stringMap) {
    final ImmutableMap.Builder<Symbol, Symbol> ret = ImmutableMap.builder();

    for (Map.Entry<String, String> stringEntry : stringMap.entrySet()) {
      ret.put(Symbol.from(stringEntry.getKey()),
          Symbol.from(stringEntry.getValue()));
    }

    return ret.build();
  }
}
