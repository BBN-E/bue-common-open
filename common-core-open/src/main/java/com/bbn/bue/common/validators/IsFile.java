package com.bbn.bue.common.validators;

import java.io.File;

public class IsFile implements Validator<File> {

  @Override
  public void validate(File arg) throws ValidationException {
    if (!arg.isFile()) {
      throw new ValidationException(String.format(
          "%s either does not exist or is not a file",
          arg.getAbsolutePath()));
    }
  }
}
