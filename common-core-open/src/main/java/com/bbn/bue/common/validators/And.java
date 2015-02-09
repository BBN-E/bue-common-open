package com.bbn.bue.common.validators;

import com.google.common.collect.Lists;

import java.util.List;

public class And<T> implements Validator<T> {

  public And(Validator<T> a, Validator<T> b) {
    validators.add(a);
    validators.add(b);
  }

  public And(List<Validator<T>> validators) {
    this.validators.addAll(validators);
  }

  /**
   * Calls each of its child validators on the input, short-circuiting and propagating if one throws
   * an exception.
   */
  @Override
  public void validate(T arg) throws ValidationException {
    for (final Validator<T> validator : validators) {
      validator.validate(arg);
    }
  }

  private final List<Validator<T>> validators = Lists.newArrayList();
}
