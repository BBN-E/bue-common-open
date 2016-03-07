package com.bbn.bue.common.strings.offsets;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public final class OffsetGroupRange {

  private final OffsetGroup startInclusive;
  private final OffsetGroup endInclusive;

  public OffsetGroup startInclusive() {
    return startInclusive;
  }

  public OffsetGroup endInclusive() {
    return endInclusive;
  }

  public static OffsetGroupRange from(final OffsetGroup startInclusive,
      final OffsetGroup endInclusive) {
    return new OffsetGroupRange(startInclusive, endInclusive);
  }

	/*public static OffsetGroupRange fromTokenSpan(final Span span) {
                return new OffsetGroupRange(span.startOffsetGroup(), span.endOffsetGroup());
	}*/

  private OffsetGroupRange(final OffsetGroup startInclusive, final OffsetGroup endInclusive) {
    this.startInclusive = checkNotNull(startInclusive);
    this.endInclusive = checkNotNull(endInclusive);
  }

  @Override
  public String toString() {
    return startInclusive + "-" + endInclusive;
  }

	/*private boolean overlapsByCharOffset(final OffsetGroupRange range) {
		// if range starts strictly to the right of this...
		if (startInclusive.charOffset().value() < range.startInclusive().charOffset().value()) {
			// then it must start before our ending...
			return range.startInclusive().charOffset().value() <= endInclusive.charOffset().value();
		} else {
			// range start to the left or at the same offset as this
			// therefore it overlaps only if its end lies beyond our start
			return range.endInclusive().charOffset().value() >= startInclusive.charOffset().value();
		}
	}*/


  @Override
  public int hashCode() {
    return Objects.hashCode(startInclusive, endInclusive);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final OffsetGroupRange other = (OffsetGroupRange) obj;
    return Objects.equal(this.startInclusive, other.startInclusive) && Objects
        .equal(this.endInclusive, other.endInclusive);
  }

  public OffsetRange<CharOffset> asCharOffsetRange() {
    return OffsetRange
        .fromInclusiveEndpoints(startInclusive().charOffset(), endInclusive().charOffset());
  }
}
