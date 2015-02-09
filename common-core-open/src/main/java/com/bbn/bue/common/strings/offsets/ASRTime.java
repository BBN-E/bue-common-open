package com.bbn.bue.common.strings.offsets;

import com.google.common.primitives.Ints;

public final class ASRTime extends AbstractOffset implements Comparable<ASRTime> {

  public ASRTime(int val) {
    super(val);
  }

  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public int compareTo(ASRTime o) {
    return Ints.compare(value(), o.value());
  }
}
