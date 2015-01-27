package com.bbn.bue.common.validators;

import com.google.common.collect.Range;

import static com.google.common.base.Preconditions.checkNotNull;

public class IsInRange<T extends Comparable<T>> implements Validator<T> {

  public IsInRange(Range<T> range) {
    this.range = checkNotNull(range);
  }

  @Override
  public void validate(T arg) throws ValidationException {
    if (!range.contains(arg)) {
      throw new ValidationException(String.format(
          "%s not in range %s", arg, range));
    }
  }

  private final Range<T> range;

}
