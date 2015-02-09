package com.bbn.bue.common.validators;

import java.io.File;

public final class FileExists implements Validator<File> {

  @Override
  public void validate(File f) throws ValidationException {
    if (!f.exists()) {
      throw new ValidationException(String.format("File %s does not exist", f.getAbsolutePath()));
    }
  }
}
