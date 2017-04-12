package com.bbn.bue.common.strings.offsets;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A range between two points in text, defined by the full offsets groups of its
 * two inclusive end points.
 */
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


  // these are so frequent it makes sense to make convenience accessors for them
  public CharOffset startCharOffsetInclusive() {
    return startInclusive().charOffset();
  }

  public CharOffset endCharOffsetInclusive() {
    return endInclusive().charOffset();
  }

  public EDTOffset startEdtOffsetInclusive() {
    return startInclusive().edtOffset();
  }

  public EDTOffset endEdtOffsetInclusive() {
    return endInclusive().edtOffset();
  }


  private OffsetGroupRange(final OffsetGroup startInclusive, final OffsetGroup endInclusive) {
    this.startInclusive = checkNotNull(startInclusive);
    this.endInclusive = checkNotNull(endInclusive);
    checkArgument(startInclusive.charOffset().asInt() <= endInclusive.charOffset().asInt(),
        "Starting char offset %s of OffsetGroupRange exceeds ending char offset %s",
        startInclusive.charOffset().asInt(), endInclusive.charOffset().asInt());
    checkArgument(startInclusive.edtOffset().asInt() <= endInclusive.edtOffset().asInt(),
        "Starting EDT offset %s of OffsetGroupRange exceeds ending EDT offset %s",
        startInclusive.edtOffset().asInt(), endInclusive.edtOffset().asInt());
  }

  @Override
  public String toString() {
    return startInclusive + "-" + endInclusive;
  }

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

  public OffsetRange<EDTOffset> asEdtOffsetRange() {
    return OffsetRange
        .fromInclusiveEndpoints(startInclusive().edtOffset(), endInclusive().edtOffset());
  }

  public Optional<OffsetRange<ByteOffset>> asByteOffsetRange() {
    if (startInclusive().byteOffset().isPresent() && endInclusive().byteOffset().isPresent()) {
      return Optional.of(OffsetRange.fromInclusiveEndpoints(startInclusive().byteOffset().get(),
          endInclusive().byteOffset().get()));
    } else {
      return Optional.absent();
    }
  }

  public Optional<OffsetRange<ASRTime>> asAsrTimeRange() {
    if (startInclusive().asrTime().isPresent() && endInclusive().asrTime().isPresent()) {
      return Optional.of(OffsetRange.fromInclusiveEndpoints(startInclusive().asrTime().get(),
          endInclusive().asrTime().get()));
    } else {
      return Optional.absent();
    }
  }
}
