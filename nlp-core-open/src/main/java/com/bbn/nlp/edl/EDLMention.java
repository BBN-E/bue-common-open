package com.bbn.nlp.edl;

import com.bbn.bue.common.TextGroupImmutable;
import com.bbn.bue.common.evaluation.ScoringTypedOffsetRange;
import com.bbn.bue.common.strings.offsets.CharOffset;
import com.bbn.bue.common.strings.offsets.OffsetRange;
import com.bbn.bue.common.symbols.Symbol;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Function;
import com.google.common.base.Optional;

import org.immutables.func.Functional;
import org.immutables.value.Value;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents a mention for the Entity Detection and Linking scorer to the degree
 * necessary for the Lorelei NER evaluations.  Does not yet address KB linking.
 */
@TextGroupImmutable
@Value.Immutable
@Functional
@JsonSerialize
@JsonDeserialize
public abstract class EDLMention {
  public abstract Symbol runId();
  public abstract String mentionId();
  public abstract Symbol documentID();
  public abstract Optional<String> kbId();
  public abstract String headString();
  public abstract OffsetRange<CharOffset> headOffsets();
  public abstract Symbol mentionType();
  public abstract Symbol entityType();
  public abstract double confidence();

  /**
   * @deprecated Prefer {@link Builder}.
   */
  @Deprecated
  public static EDLMention create(final Symbol runId, final String mentionId,
      final Symbol documentId, final String headString,
      final OffsetRange<CharOffset> headOffsets,
      final Symbol mentionType, final Symbol entityType, final double confidence) {
    return new Builder().runId(runId).mentionId(mentionId).documentID(documentId)
        .headString(headString).headOffsets(headOffsets)
        .mentionType(mentionType).entityType(entityType).confidence(confidence).build();
  }

  @Value.Check
  void check() {
    checkArgument(!runId().asString().isEmpty(), "Run ID cannot be empty");
    checkArgument(!mentionId().isEmpty(), "Mention ID cannot be empty");
    checkArgument(!documentID().asString().isEmpty(), "Document ID cannot be empty");
    checkArgument(!kbId().isPresent() || !kbId().get().isEmpty(),
        "KB ID can be absent but cannot be empty");
    checkArgument(!headString().isEmpty(), "Head string cannot be empty");
    checkArgument(!mentionType().asString().isEmpty(), "Mention type cannot be empty");
    checkArgument(!entityType().asString().isEmpty(), "Entity type cannot be empty");
  }

  public static class Builder extends ImmutableEDLMention.Builder {

    /**
     * Sets the KB id to be a NIL cluster with the specified id.
     */
    public Builder nilKbId(final String id) {
      return this.kbId("NIL" + id);
    }
  }

  /**
   * @deprecated Prefer {@link EDLMentionFunctions#entityType()}
   */
  @Deprecated
  public static Function<EDLMention, Symbol> entityTypeFunction() {
    return EntityTypeFunction.INSTANCE;
  }

  /**
   * @deprecated Prefer {@link EDLMentionFunctions#mentionType()}
   */
  public static Function<EDLMention, Symbol> mentionTypeFunction() {
    return MentionTypeFunction.INSTANCE;
  }

  /**
   * Guava {@link Function} to transform an {@code EDLMention}s to
   * a {@link ScoringTypedOffsetRange} with offsets corresponding to
   * {@link #headOffsets()} and type corresponding to {@link #entityType()}.
   */
  public static Function<EDLMention, ScoringTypedOffsetRange<CharOffset>> asTypedOffsetRangeFunction() {
    return AsTypedOffsetRangeFunction.INSTANCE;
  }

  /**
   * @deprecated Prefer {@link EDLMentionFunctions#entityType()}
   */
  @Deprecated
  private enum EntityTypeFunction implements Function<EDLMention, Symbol> {
    INSTANCE {
      @Override
      public Symbol apply(final EDLMention input) {
        return input.entityType();
      }
    }
  }

  /**
   * @deprecated Prefer {@link EDLMentionFunctions#mentionType()}
   */
  @Deprecated
  private enum MentionTypeFunction implements Function<EDLMention, Symbol> {
    INSTANCE {
      @Override
      public Symbol apply(final EDLMention input) {
        return input.mentionType();
      }
    }
  }

  private enum AsTypedOffsetRangeFunction
      implements Function<EDLMention, ScoringTypedOffsetRange<CharOffset>> {
    INSTANCE {
      @Override
      public ScoringTypedOffsetRange<CharOffset> apply(final EDLMention input) {
        return ScoringTypedOffsetRange
            .create(input.documentID(), input.entityType(), input.headOffsets());
      }
    }
  }
}
