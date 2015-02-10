package com.bbn.bue.common.strings.offsets;

import com.google.common.primitives.Ints;

public final class ByteOffset extends AbstractOffset implements Comparable<ByteOffset> {

  public ByteOffset(int val) {
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
  public int compareTo(ByteOffset o) {
    return Ints.compare(value(), o.value());
  }

  public static ByteOffset asByteOffset(final int val) {
    return new ByteOffset(val);
  }
}
