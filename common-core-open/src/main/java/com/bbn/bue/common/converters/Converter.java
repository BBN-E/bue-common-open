package com.bbn.bue.common.converters;

import com.google.common.annotations.Beta;

@Beta
public interface Converter<S,T> {
	public T decode(S argumentValue);
}
