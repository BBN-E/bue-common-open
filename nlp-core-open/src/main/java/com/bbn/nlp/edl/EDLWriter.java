package com.bbn.nlp.edl;

import com.bbn.bue.common.TextGroupImmutable;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.io.CharSink;

import org.immutables.value.Value;

import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Writes files in the submission format for the TAC KBP Entity Detection and Linking eval.
 */
@TextGroupImmutable
@Value.Immutable
public abstract class EDLWriter {

  /**
   * A default KB ID to assign to {@link EDLMention}s with absent KB IDs.
   */
  public abstract Optional<String> defaultKbId();

  /**
   * @deprecated Prefer {@link Builder}. To preserve legacy behavior, the constructed object
   * uses a default KB ID of {@code NIL}.
   */
  @Deprecated
  public static EDLWriter create() {
    return new EDLWriter.Builder().defaultKbId("NIL").build();
  }

  /**
   * Writes out the specified {@link EDLMention}s. If a {@link #defaultKbId} was not specified,
   * an exception will be thrown if any EDL mentions have an absent KB ID.
   */
  public void writeEDLMentions(Iterable<EDLMention> edlMentions, CharSink sink) throws IOException {
    final List<String> lines = Lists.newArrayList();

    for (final EDLMention edlMention : edlMentions) {
      lines.add(toLine(edlMention));
    }

    sink.writeLines(lines, "\n");
  }

  public static class Builder extends ImmutableEDLWriter.Builder {}

  private String toLine(final EDLMention edlMention) {
    final Optional<String> kbId = edlMention.kbId().or(defaultKbId());
    checkArgument(kbId.isPresent(),
        "EDL mention %s does not have a KB ID and no default was specified", kbId);
    return edlMention.runId() + "\t" + edlMention.mentionId() + "\t" + edlMention.headString()
        + "\t" + edlMention.documentID() + ":" + edlMention.headOffsets().startInclusive().asInt()
        + "-" + edlMention.headOffsets().endInclusive().asInt() + "\t" + kbId.get() + "\t"
        + edlMention.entityType() + "\t" + edlMention.mentionType() + "\t"
        + edlMention.confidence();
  }
}
