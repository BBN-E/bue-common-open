package com.bbn.bue.common.symbols;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Symbol (adapted from Serif) is represents an interned String. Symbol-ize Strings can be used to
 * reduce memory requirements for frequently repeated Strings (e.g. parse node names) and to speed
 * up comparisons.
 *
 * It is guaranteed that for any <code>String</code>s <code>a</code> and <code>b</code>,
 * <pre>
 * Symbol aSym = Symbol.from(a);
 * Symbol bSym = Symbol.from(b);
 * </pre>
 * it is true that <code>(aSym == bSym) == a.equals(b)</code>.
 *
 * The hashcode is not stable across program runs.
 *
 * @author rgabbard
 */
@Beta
public final class Symbol implements Serializable {

  private static Map<String, WeakReference<Symbol>> symbols =
      new HashMap<String, WeakReference<Symbol>>();
  private final String string;

  private Symbol(String string) {
    this.string = checkNotNull(string);
  }

  /**
   * Creates a <code>Symbol</code> representing this string.
   *
   * @param string Must be non-null.
   */
  public static synchronized Symbol from(final String string) {
    final WeakReference<Symbol> ref = symbols.get(checkNotNull(string));

    if (ref != null) {
      final Symbol sym = ref.get();
      if (sym != null) {
        return sym;
      }
    }

    final Symbol sym = new Symbol(string);
    symbols.put(string, new WeakReference<Symbol>(sym));
    return sym;
  }

  /**
   * (non-Javadoc) Returns the <code>String</code> this <code>Symbol</code> was created from.
   */
  @Override
  public String toString() {
    return string;
  }

  public static final Function<? super String, Symbol> FromString = new Function<String, Symbol>() {
    @Override
    public Symbol apply(String input) {
      return Symbol.from(input);
    }
  };

  // serialization support
  private static final long serialVersionUID = 1L;

  // when a Symbol is deserialized, this method is called and
  // the return value *replaces* this object. We use this to
  // ensure that the constraint that there is always at most
  // one Symbol representing a given String remains true
  // even after deserialization.
  private Object readResolve() throws ObjectStreamException {
    return Symbol.from(this.string);
  }
}

