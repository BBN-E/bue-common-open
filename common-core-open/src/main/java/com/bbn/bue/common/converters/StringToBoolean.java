package com.bbn.bue.common.converters;

import static com.google.common.base.Preconditions.checkNotNull;

public class StringToBoolean implements StringConverter<Boolean> {

  public StringToBoolean() {
  }

  public Class<Boolean> getValueClass() {
    return Boolean.class;
  }

  @Override
  public Boolean decode(final String s) {
    return Boolean.parseBoolean(checkNotNull(s));
  }
}
