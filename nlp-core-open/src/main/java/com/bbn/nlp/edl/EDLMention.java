package com.bbn.nlp.edl;

import com.bbn.bue.common.strings.offsets.CharOffset;
import com.bbn.bue.common.strings.offsets.OffsetRange;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Range;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a mention for the Entity Detection and Linking scorer to the degree
 * necessary for the Lorelei NER evaluations.  Does not yet address KB linking.
 */
public final class EDLMention {

  private final Symbol runId;
  private final String mentionId;
  private final Symbol documentID;
  private final String headString;
  private final OffsetRange<CharOffset> headOffsets;
  private final Symbol mentionType;
  private final Symbol entityType;
  private final double confidence;

  private EDLMention(final Symbol runId, final String mentionId,
      final Symbol documentID, final String headString,
      final OffsetRange<CharOffset> headOffsets,
      final Symbol mentionType, final Symbol entityType, final double confidence) {
    this.runId = checkNotNull(runId);
    this.mentionId = checkNotNull(mentionId);
    this.documentID = checkNotNull(documentID);
    this.headString = checkNotNull(headString);
    this.headOffsets = checkNotNull(headOffsets);
    this.mentionType = checkNotNull(mentionType);
    this.entityType = checkNotNull(entityType);
    this.confidence = confidence;
    checkArgument(Range.openClosed(0.0, 1.0).contains(confidence),
        "Confidence must be in (0,1.0] but got %s", confidence);
  }

  public static EDLMention create(final Symbol runId, final String mentionId,
      final Symbol documentID, final String headString,
      final OffsetRange<CharOffset> headOffsets,
      final Symbol mentionType, final Symbol entityType, final double confidence)
  {
    return new EDLMention(runId, mentionId, documentID, headString, headOffsets,
        mentionType, entityType, confidence);
  }

  public Symbol runId() {
    return runId;
  }

  public String mentionId() {
    return mentionId;
  }

  public Symbol documentID() {
    return documentID;
  }

  public String headString() {
    return headString;
  }

  public OffsetRange<CharOffset> headOffsets() {
    return headOffsets;
  }

  public Symbol mentionType() {
    return mentionType;
  }

  public Symbol entityType() {
    return entityType;
  }

  public double confidence() {
    return confidence;
  }

  @Override
  public int hashCode() {
    return Objects
        .hashCode(runId, mentionId, documentID, headString, headOffsets, mentionType, entityType,
            confidence);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final EDLMention other = (EDLMention) obj;
    return Objects.equal(this.runId, other.runId)
        && Objects.equal(this.mentionId, other.mentionId)
        && Objects.equal(this.documentID, other.documentID)
        && Objects.equal(this.headString, other.headString)
        && Objects.equal(this.headOffsets, other.headOffsets)
        && Objects.equal(this.mentionType, other.mentionType)
        && Objects.equal(this.entityType, other.entityType)
        && Objects.equal(this.confidence, other.confidence);
  }

  public static Function<EDLMention, Symbol> entityTypeFunction() {
    return EntityTypeFunction.INSTANCE;
  }

  public static Function<EDLMention, Symbol> mentionTypeFunction() {
    return MentionTypeFunction.INSTANCE;
  }

  private enum EntityTypeFunction implements Function<EDLMention, Symbol> {
    INSTANCE {
      @Override
      public Symbol apply(final EDLMention input) {
        return input.entityType();
      }
    };
  }

  private enum MentionTypeFunction implements Function<EDLMention, Symbol> {
    INSTANCE {
      @Override
      public Symbol apply(final EDLMention input) {
        return input.mentionType();
      }
    };
  }
}
