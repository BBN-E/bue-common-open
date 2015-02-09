package com.bbn.bue.common.converters;

import static com.google.common.base.Preconditions.checkNotNull;

public class StringToInteger implements StringConverter<Integer> {

  public StringToInteger() {
  }

  public Class<Integer> getValueClass() {
    return Integer.class;
  }

  public Integer decode(final String s) {
    try {
      return Integer.parseInt(checkNotNull(s));
    } catch (NumberFormatException nfe) {
      throw new ConversionException("Not an integer: " + s, nfe);
    }
  }
}
