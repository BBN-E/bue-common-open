package com.bbn.bue.common.strings.offsets;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.primitives.Ints;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractOffset<SelfType extends Offset<SelfType>>
    implements Offset<SelfType> {

  private final int value;

  protected AbstractOffset(final int value) {
    checkArgument(value >= 0);
    this.value = value;
  }

  /**
   * @deprecated Prefer {@link #asInt()}
   */
  @Deprecated
  @Override
  public final int value() {
    return value;
  }

  @JsonProperty("value")
  @Override
  public final int asInt() {
    return value;
  }

  @Override
  public final boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (getClass() != o.getClass()) {
      return false;
    }

    return value == ((Offset) o).asInt();
  }

  @Override
  public final int hashCode() {
    return Objects.hashCode(value);
  }


  @Override
  public final int compareTo(final SelfType o) {
    checkNotNull(o);
    return Ints.compare(asInt(), o.asInt());
  }
}

