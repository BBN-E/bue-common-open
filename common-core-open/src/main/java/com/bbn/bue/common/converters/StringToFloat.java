package com.bbn.bue.common.converters;

import static com.google.common.base.Preconditions.*;

public class StringToFloat implements StringConverter<Float> {
    public StringToFloat() { }

    public Class<Float> getValueClass() { return Float.class; }

    public Float decode(final String s) {
        try {
            return Float.parseFloat(checkNotNull(s));
        } catch (NumberFormatException nfe) {
            throw new ConversionException("Not a float: " + s, nfe);
        }
    }
}
