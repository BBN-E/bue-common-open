package com.bbn.bue.common.converters;

import com.bbn.bue.common.symbols.Symbol;
import com.bbn.bue.common.symbols.SymbolUtils;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class StringToSymbolSet implements StringConverter<Set<Symbol>> {

  public StringToSymbolSet(String delimiter) {
    this.subConverter = new StringToStringSet(checkNotNull(delimiter));
  }

  @Override
  public Set<Symbol> decode(String s) {
    checkNotNull(s);
    return SymbolUtils.setFrom(subConverter.decode(s));
  }

  private final StringConverter<Set<String>> subConverter;
}
