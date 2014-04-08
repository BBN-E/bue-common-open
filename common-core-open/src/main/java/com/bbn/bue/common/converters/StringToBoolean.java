package com.bbn.bue.common.converters;

import static com.google.common.base.Preconditions.*;

public class StringToBoolean implements StringConverter<Boolean> {
    public StringToBoolean() { }

    public Class<Boolean> getValueClass() { return Boolean.class; }

    public Boolean decode(final String s) { 
    	return Boolean.parseBoolean(checkNotNull(s)); 
    }
}
