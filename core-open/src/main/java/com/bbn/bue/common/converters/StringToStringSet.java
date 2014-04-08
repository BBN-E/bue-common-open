package com.bbn.bue.common.converters;

import java.util.Set;

import static com.google.common.base.Preconditions.*;

import com.google.common.collect.Sets;

public class StringToStringSet implements StringConverter<Set<String>>{
	public StringToStringSet(String delimiter) {
		this.delimiter = checkNotNull(delimiter);
	}
	
	@Override
	public Set<String> decode(String s) {
		checkNotNull(s);
		return Sets.newHashSet(s.split(delimiter));
	}
	
	private final String delimiter;
}
