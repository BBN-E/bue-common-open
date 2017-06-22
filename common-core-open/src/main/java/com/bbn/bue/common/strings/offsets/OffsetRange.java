package com.bbn.bue.common.strings.offsets;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;

import java.util.Comparator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents an inclusive range of offsets.
 */
public class OffsetRange<OffsetType extends Offset<OffsetType>> {

  private final OffsetType startInclusive;
  private final OffsetType endInclusive;

  @JsonCreator
  public static <OffsetType extends Offset<OffsetType>> OffsetRange<OffsetType> fromInclusiveEndpoints(
      @JsonProperty("start") OffsetType startInclusive,
      @JsonProperty("end") OffsetType endInclusive) {
    checkArgument(startInclusive.asInt() <= endInclusive.asInt());
    return new OffsetRange<>(startInclusive, endInclusive);
  }

  private OffsetRange(OffsetType startInclusive, OffsetType endInclusive) {
    checkArgument(startInclusive.asInt() <= endInclusive.asInt());
    this.startInclusive = checkNotNull(startInclusive);
    this.endInclusive = checkNotNull(endInclusive);
  }

  @JsonProperty("start")
  public OffsetType startInclusive() {
    return startInclusive;
  }

  @JsonProperty("end")
  public OffsetType endInclusive() {
    return endInclusive;
  }

  public Range<OffsetType> asRange() {
    return Range.closed(startInclusive, endInclusive);
  }

  public int length() {
    return endInclusive.asInt() - startInclusive().asInt() + 1;
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
    final OffsetRange other = (OffsetRange) obj;
    return Objects.equal(this.startInclusive, other.startInclusive) && Objects
        .equal(this.endInclusive, other.endInclusive);
  }

  public static <T extends Offset<T>> Ordering<OffsetRange<T>> byLengthOrdering() {
    return new Ordering<OffsetRange<T>>() {
      @Override
      public int compare(final OffsetRange<T> left, final OffsetRange<T> right) {
        return Ints.compare(left.length(), right.length());
      }
    };
  }

  private static <T extends Offset<T>> Function<OffsetRange<T>, T> toStartInclusiveFunction() {
    return new Function<OffsetRange<T>, T>() {
      @Override
      public T apply(OffsetRange<T> x) {
        return x.startInclusive();
      }
    };
  }

  private static <T extends Offset<T>> Function<OffsetRange<T>, T> toEndInclusiveFunction() {
    return new Function<OffsetRange<T>, T>() {
      @Override
      public T apply(OffsetRange<T> x) {
        return x.endInclusive();
      }
    };
  }

  /**
   * Provides an {@link Ordering} of {@link OffsetRange}s by their start position. Note that this is
   * not a total ordering because {@link OffsetRange}s with the same start position but different
   * end positions will compare as equal.
   *
   * Consider producing a compound ordering with {@link #byEndOrdering()} using {@link
   * Ordering#compound(Comparator)} or using one of the predefined total orderings.
   */
  public static <T extends Offset<T>> Ordering<OffsetRange<T>> byStartOrdering() {
    return Ordering.<T>natural().onResultOf(OffsetRange.<T>toStartInclusiveFunction());
  }

  /**
   * Provides an {@link Ordering} of {@link OffsetRange}s by their end position. Note that this is
   * not a total ordering because {@link OffsetRange}s with the same end position but different
   * start positions will compare as equal.
   *
   * Consider producing a compound ordering with {@link #byStartOrdering()} using {@link
   * Ordering#compound(Comparator)} or using one of the predefined total orderings.
   */
  public static <T extends Offset<T>> Ordering<OffsetRange<T>> byEndOrdering() {
    return Ordering.<T>natural().onResultOf(OffsetRange.<T>toEndInclusiveFunction());
  }

  /**
   * Provides a total {@link Ordering} over {@link OffsetRange}s by their start position, breaking
   * ties by placing the earlier end position first.
   */
  public static <T extends Offset<T>> Ordering<OffsetRange<T>> byEarlierStartEarlierEndOrdering() {
    return Ordering.<T>natural().onResultOf(OffsetRange.<T>toStartInclusiveFunction())
        .compound(Ordering.<T>natural().onResultOf(OffsetRange.<T>toEndInclusiveFunction()));
  }

  /**
   * Provides a total {@link Ordering} over {@link OffsetRange}s by their start position, breaking
   * ties by placing the later end position first.
   */
  public static <T extends Offset<T>> Ordering<OffsetRange<T>> byEarlierStartLaterEndOrdering() {
    return Ordering.<T>natural().onResultOf(OffsetRange.<T>toStartInclusiveFunction())
        .compound(
            Ordering.<T>natural().onResultOf(OffsetRange.<T>toEndInclusiveFunction()).reverse());
  }

  public static OffsetRange<CharOffset> inclusiveCharOffsetRange(int startInclusive,
      int endInclusive) {
    return fromInclusiveEndpoints(CharOffset.asCharOffset(startInclusive),
        CharOffset.asCharOffset(endInclusive));
  }

  /**
   * Prefer {@link #inclusiveCharOffsetRange(int, int)}
   */
  public static OffsetRange<CharOffset> charOffsetRange(int startInclusive, int endInclusive) {
    return inclusiveCharOffsetRange(startInclusive, endInclusive);
  }

  public static OffsetRange<ByteOffset> byteOffsetRange(final int startInclusive,
      final int endInclusive) {
    return OffsetRange.fromInclusiveEndpoints(ByteOffset.asByteOffset(startInclusive),
        ByteOffset.asByteOffset(endInclusive));
  }


  /**
   * This returns optional because it is not possible to represent an empty offset span
   */
  public static Optional<OffsetRange<CharOffset>> charOffsetsOfWholeString(String s) {
    if (s.isEmpty()) {
      return Optional.absent();
    }
    return Optional.of(charOffsetRange(0, s.length() - 1));
  }

  /**
   * Returns a new {@code OffsetRange} of the same type with the start and end points
   * shifted by the specified amount.
   */
  public OffsetRange<OffsetType> shiftedCopy(final int shiftAmount) {
    return new OffsetRange<>(startInclusive().shiftedCopy(shiftAmount),
        endInclusive().shiftedCopy(shiftAmount));
  }

  public boolean overlaps(OffsetRange<OffsetType> other) {
    return asRange().isConnected(other.asRange());
  }

  public Optional<OffsetRange<OffsetType>> intersection(OffsetRange<OffsetType> other) {
    final Range<OffsetType> meAsRange = asRange();
    final Range<OffsetType> otherAsRange = other.asRange();

    if (meAsRange.isConnected(otherAsRange)) {
      final Range<OffsetType> intersectionRange = meAsRange.intersection(otherAsRange);
      return Optional.of(fromInclusiveEndpoints(intersectionRange.lowerEndpoint(), intersectionRange.upperEndpoint()));
    } else {
      return Optional.absent();
    }
  }

  /**
   * Returns if the given offset is within the inclusive bounds of this range.
   */
  public boolean contains(final OffsetType x) {
    return x.asInt() >= startInclusive().asInt() && x.asInt() <= endInclusive().asInt();
  }

  /**
   * Returns if the given inclusive offset range is within the inclusive bounds of this range.
   */
  public boolean contains(OffsetRange<OffsetType> other) {
    return asRange().encloses(other.asRange());
  }

  public static <OffsetType extends Offset<OffsetType>>
  Predicate<OffsetRange<OffsetType>> containedInPredicate(final OffsetRange<OffsetType> container) {
    return new Predicate<OffsetRange<OffsetType>>() {
      @Override
      public boolean apply(OffsetRange<OffsetType> input) {
        return container.contains(input);
      }
    };
  }

  /**
   * Returns this range with its start and end positions clipped to fit within {@code bounds}. If
   * this does not intersect {@code bounds}, returns {@link com.google.common.base.Optional#absent()}
   * .
   */
  public Optional<OffsetRange<OffsetType>> clipToBounds(OffsetRange<OffsetType> bounds) {
    if (bounds.contains(this)) {
      return Optional.of(this);
    }
    if (!bounds.overlaps(this)) {
      return Optional.absent();
    }
    final OffsetType newLowerBound;
    final OffsetType newUpperBound;
    if (bounds.startInclusive().asInt() > startInclusive().asInt()) {
      newLowerBound = bounds.startInclusive();
    } else {
      newLowerBound = startInclusive();
    }
    if (bounds.endInclusive().asInt() < endInclusive().asInt()) {
      newUpperBound = bounds.endInclusive();
    } else {
      newUpperBound = endInclusive();
    }
    return Optional.of(OffsetRange.fromInclusiveEndpoints(newLowerBound, newUpperBound));
  }

  public static Function<OffsetRange<?>, Integer> lengthFunction() {
    return LengthFunction.INSTANCE;
  }


  private enum LengthFunction implements Function<OffsetRange<?>, Integer> {
    INSTANCE {
      @Override
      public Integer apply(final OffsetRange<?> input) {
        return input.length();
      }
    }
  }

  @Override
  public String toString() {
    return "[" + startInclusive().toString() + "-" + endInclusive().toString() + "]";
  }

}
