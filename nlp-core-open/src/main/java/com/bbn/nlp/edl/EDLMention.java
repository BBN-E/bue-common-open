package com.bbn.nlp.edl;

import com.bbn.bue.common.strings.offsets.CharOffset;
import com.bbn.bue.common.strings.offsets.OffsetRange;

import com.google.common.base.Objects;
import com.google.common.collect.Range;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a mention for the Entity Detection and Linking scorer to the degree
 * necessary for the Lorelei NER evaluations.  Does not yet address KB linking.
 */
public final class EDLMention {
  private final String runId;
  private final String mentionId;
  private final String documentID;
  private final String headString;
  private final OffsetRange<CharOffset> headOffsets;
  private final String mentionType;
  private final String entityType;
  private final double confidence;

  private EDLMention(final String runId, final String mentionId,
      final String documentID, final String headString,
      final OffsetRange<CharOffset> headOffsets,
      final String mentionType, final String entityType, final double confidence) {
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

  public static EDLMention create(final String runId, final String mentionId,
      final String documentID, final String headString,
      final OffsetRange<CharOffset> headOffsets,
      final String mentionType, final String entityType, final double confidence)
  {
    return new EDLMention(runId, mentionId, documentID, headString, headOffsets,
        mentionType, entityType, confidence);
  }

  public String runId() {
    return runId;
  }

  public String mentionId() {
    return mentionId;
  }

  public String documentID() {
    return documentID;
  }

  public String headString() {
    return headString;
  }

  public OffsetRange<CharOffset> headOffsets() {
    return headOffsets;
  }

  public String mentionType() {
    return mentionType;
  }

  public String entityType() {
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
}
