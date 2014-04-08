package com.bbn.bue.common.converters;

import java.util.List;

import com.bbn.bue.common.symbols.Symbol;
import com.bbn.bue.common.symbols.SymbolUtils;

import static com.google.common.base.Preconditions.checkNotNull;

public class StringToSymbolList implements StringConverter<List<Symbol>> {
	public StringToSymbolList(String delimiter) {
		this.subConverter = 
			new StringToStringList(checkNotNull(delimiter));
	}

	@Override
	public List<Symbol> decode(String s) {
		checkNotNull(s);
		return SymbolUtils.listFrom(subConverter.decode(s));
	}
	
	private final StringConverter<? extends List<String>> subConverter;
}
