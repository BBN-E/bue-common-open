package com.bbn.bue.common.converters;

import static com.google.common.base.Preconditions.*;

public class StringToLong implements StringConverter<Long> {
    public StringToLong() { }

    public Class<Long> getValueClass() { return Long.class; }

    public Long decode(final String s) {
        try {
            return Long.parseLong(checkNotNull(s));
        } catch (NumberFormatException nfe) {
            throw new ConversionException("Not a long: " + s, nfe);
        }
    }
}
