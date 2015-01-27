package com.bbn.bue.common.converters;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

public class StringToFile implements StringConverter<File> {

  public StringToFile() {
  }

  public Class<File> getValueClass() {
    return File.class;
  }

  @Override
  public File decode(final String s) {
    return new File(checkNotNull(s));
  }
}
