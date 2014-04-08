package com.bbn.bue.common.converters;

import com.google.common.annotations.Beta;


/**
 * Can decode instances of a given type
 * from a string.
 */
@Beta
public interface StringConverter<T> extends Converter<String, T> {

    /**
     * @param argumentValue a string value to decode. May not be null.
     * @return the decoded value. Should not be null.
     * @throws ConversionException if the value cannot be decoded
     * @throws IllegalArgmentException if the value is null
     */
    @Override
	public T decode(String argumentValue);
}
