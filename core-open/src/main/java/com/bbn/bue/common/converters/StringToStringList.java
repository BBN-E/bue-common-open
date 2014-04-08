package com.bbn.bue.common.converters;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Splitter;

import static com.google.common.base.Preconditions.checkNotNull;

public class StringToStringList implements StringConverter<List<String>> {
	public StringToStringList(String delimiter) {
		this.splitter = Splitter.on(delimiter).trimResults().omitEmptyStrings();
	}
	
	@Override
	public List<String> decode(String s) {
		checkNotNull(s);
		final List<String> ret = new ArrayList<String>();
		for (final String part : splitter.split(s)) {
			ret.add(part);
		}
		return ret;
	}
	
	private final Splitter splitter; 
}
