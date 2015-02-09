package com.bbn.bue.common.strings.offsets;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class AbstractOffset implements Offset {

  private final int value;

  protected AbstractOffset(final int value) {
    checkArgument(value >= 0);
    this.value = value;
  }

  public int value() {
    return value;
  }

  public int asInt() {
    return value;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (getClass() != o.getClass()) {
      return false;
    }

    return value == ((Offset) o).value();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }


}
