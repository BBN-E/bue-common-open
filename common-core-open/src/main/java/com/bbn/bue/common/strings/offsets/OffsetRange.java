package com.bbn.bue.common.strings.offsets;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class OffsetRange<OffsetType extends Offset & Comparable<OffsetType>> {
    private final OffsetType startInclusive;
    private final OffsetType endInclusive;

    public static <OffsetType extends Offset & Comparable<OffsetType>> OffsetRange<OffsetType> fromInclusiveEndpoints(
            OffsetType startInclusive, OffsetType endInclusive)
    {
        checkArgument(startInclusive.asInt()<=endInclusive.asInt());
        return new OffsetRange<OffsetType>(startInclusive, endInclusive);
    }

    private OffsetRange(OffsetType startInclusive, OffsetType endInclusive) {
        this.startInclusive = checkNotNull(startInclusive);
        this.endInclusive = checkNotNull(endInclusive);
    }

    public OffsetType startInclusive() {
        return startInclusive;
    }

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
        return Objects.equal(this.startInclusive, other.startInclusive) && Objects.equal(this.endInclusive, other.endInclusive);
    }

    private static final Ordering<OffsetRange> ByLength = new Ordering<OffsetRange>() {
        @Override
        public int compare(final OffsetRange left, final OffsetRange right) {
            return Ints.compare(left.length(), right.length());
        }
    };

    public static final <T extends Offset & Comparable<T>> Ordering<OffsetRange<T>> byLengthOrdering() {
        return new Ordering<OffsetRange<T>>() {
            @Override
            public int compare(final OffsetRange<T> left, final OffsetRange<T> right) {
                return Ints.compare(left.length(), right.length());
            }
        };
    }

    private static final <T extends Offset & Comparable<T>> Function<OffsetRange<T>, T> toStartInclusiveFunction() {
        return new Function<OffsetRange<T>, T>() {
            @Override
            public T apply(OffsetRange<T> x) {
                return x.startInclusive();
            }
        };
    }

    private static final <T extends Offset & Comparable<T>> Function<OffsetRange<T>, T> toEndInclusiveFunction() {
        return new Function<OffsetRange<T>, T>() {
            @Override
            public T apply(OffsetRange<T> x) {
                return x.endInclusive();
            }
        };
    }

    public static final <T extends Offset & Comparable<T>> Ordering<OffsetRange<T>> byStartOrdering() {
        return Ordering.<T>natural().onResultOf(OffsetRange.<T>toStartInclusiveFunction());
    }

    public static final <T extends Offset & Comparable<T>> Ordering<OffsetRange<T>> byEndOrdering() {
        return Ordering.<T>natural().onResultOf(OffsetRange.<T>toEndInclusiveFunction());
    }

    public static OffsetRange<CharOffset> charOffsetRange(int startInclusive, int endInclusive) {
        return fromInclusiveEndpoints(CharOffset.asCharOffset(startInclusive),
                CharOffset.asCharOffset(endInclusive));
    }

    /**
     * This returns optional because it is not possible to represent an empty offset span
     */
    public static Optional<OffsetRange<CharOffset>> charOffsetsOfWholeString(String s) {
        if (s.isEmpty()) {
            return Optional.absent();
        }
        return Optional.of(charOffsetRange(0, s.length()-1));
    }

    public boolean overlaps(OffsetRange<OffsetType> other) {
        return asRange().isConnected(other.asRange());
    }

    public boolean contains(OffsetRange<OffsetType> other) {
        return asRange().encloses(other.asRange());
    }

    public static <OffsetType extends Offset & Comparable<OffsetType>>
    Predicate<OffsetRange<OffsetType>> containedInPredicate(final OffsetRange<OffsetType> container)
    {
        return new Predicate<OffsetRange<OffsetType>>() {
            @Override
            public boolean apply(OffsetRange<OffsetType> input) {
                return container.contains(input);
            }
        };
    }
}
