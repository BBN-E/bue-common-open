package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.HasDocID;
import com.bbn.bue.common.strings.offsets.Offset;
import com.bbn.bue.common.strings.offsets.OffsetRange;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.annotations.Beta;
import com.google.common.base.Equivalence;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A struct to wrap an {@link OffsetRange} alongside the corresponding {@link Symbol} type and
 * document id. The type is meant to represent the class label in a multi-class prediction tag,
 * such as a named entity type or a part of speech.
 */
@Beta
public final class ScoringTypedOffsetRange<T extends Offset<T>>
    implements HasDocID, HasScoringType, HasOffsetRange {

  private final Symbol docId;
  private final Symbol type;
  private final OffsetRange<T> offsetRange;

  private ScoringTypedOffsetRange(final Symbol docId, final Symbol type, final OffsetRange<T> offsetRange) {
    this.docId = checkNotNull(docId);
    this.type = checkNotNull(type);
    this.offsetRange = checkNotNull(offsetRange);
  }

  public static <T extends Offset<T>> ScoringTypedOffsetRange<T> create(final Symbol docId,
      final Symbol type, final OffsetRange<T> offsetRange) {
    return new ScoringTypedOffsetRange<T>(docId, type, offsetRange);
  }

  @Override
  public Symbol docID() {
    return docId;
  }

  @Override
  public Symbol scoringType() {
    return type;
  }

  @Override
  public OffsetRange<T> offsetRange() {
    return offsetRange;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(docId, type, offsetRange);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final ScoringTypedOffsetRange other = (ScoringTypedOffsetRange) obj;
    return Objects.equal(this.docId, other.docId)
        && Objects.equal(this.type, other.type)
        && Objects.equal(this.offsetRange, other.offsetRange);
  }

  @Override
  public String toString() {
    return docId + "-" + type + "-" + offsetRange;
  }

  /**
   * Returns an enum-wrapped {@link Equivalence} that uses only the document id and offset range
   * to compare {@link ScoringTypedOffsetRange}s.
   */
  public static DocIdOffsetEquivalence docIdOffsetEquivalence() {
    return new DocIdOffsetEquivalence();
  }

  public static <T extends Offset<T>> Function<ScoringTypedOffsetRange<T>, Symbol> typeFunction() {
    return new TypeFunction<>();
  }

  /**
   * Provides an ordering of {@code ScoringTypedOffsetRange} by their degree of overlap with a
   * reference {@code ScoringTypedOffsetRange}, from least overlap to most.
   */
  public static <OffsetType extends Offset<OffsetType>> Ordering<ScoringTypedOffsetRange<OffsetType>>
    orderByOverlapWith(final ScoringTypedOffsetRange<OffsetType> reference) {
    return new Ordering<ScoringTypedOffsetRange<OffsetType>>() {
      @Override
      public int compare(final ScoringTypedOffsetRange<OffsetType> left, final
      ScoringTypedOffsetRange<OffsetType> right) {
        final int leftIntersectionSize =
            left.offsetRange().intersection(reference.offsetRange())
                .transform(OffsetRange.lengthFunction()).or(0);
        final int rightIntersectionSize = right.offsetRange().intersection(reference.offsetRange())
            .transform(OffsetRange.lengthFunction()).or(0);

        return Ints.compare(leftIntersectionSize, rightIntersectionSize);
      }
    };
  }

  private static final class TypeFunction<T extends Offset<T>>
      implements Function<ScoringTypedOffsetRange<T>, Symbol> {

    @Override
    public Symbol apply(final ScoringTypedOffsetRange<T> input) {
      return input.scoringType();
    }
  }

  /**
   * Equivalence that ignores the scoring type.
   */
  private static final class DocIdOffsetEquivalence extends Equivalence<ScoringTypedOffsetRange> {

    @Override
    protected boolean doEquivalent(ScoringTypedOffsetRange a, ScoringTypedOffsetRange b) {
      return Objects.equal(a.docID(), b.docID()) && Objects.equal(a.offsetRange(), b.offsetRange());
    }

    @Override
    protected int doHash(ScoringTypedOffsetRange scoringTypedOffsetRange) {
      return Objects.hashCode(scoringTypedOffsetRange.docID(), scoringTypedOffsetRange.offsetRange());
    }
  }
}
