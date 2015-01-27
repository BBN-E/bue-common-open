package com.bbn.bue.common.converters;

import static com.google.common.base.Preconditions.checkNotNull;

public class StringToString implements StringConverter<String> {

  public StringToString() {
  }

  public Class<String> getValueClass() {
    return String.class;
  }

  @Override
  public String decode(final String s) {
    return checkNotNull(s);
  }
}
