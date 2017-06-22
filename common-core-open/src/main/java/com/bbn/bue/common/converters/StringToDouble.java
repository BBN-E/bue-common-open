package com.bbn.bue.common.converters;

import static com.google.common.base.Preconditions.checkNotNull;

public class StringToDouble implements StringConverter<Double> {

  public StringToDouble() {
  }

  public Class<Double> getValueClass() {
    return Double.class;
  }

  @Override
  public Double decode(final String s) {
    try {
      return Double.parseDouble(s);
    } catch (NumberFormatException nfe) {
      throw new ConversionException("Not a double: " + s,
          checkNotNull(nfe));
    }
  }
}
