package com.bbn.nlp.edl;

import com.google.common.collect.Lists;
import com.google.common.io.CharSink;

import java.io.IOException;
import java.util.List;

public final class EDLWriter {

  private EDLWriter() {
  }

  public static EDLWriter create() {
    return new EDLWriter();
  }

  public void writeEDLMentions(Iterable<EDLMention> edlMentions, CharSink sink) throws IOException {
    final List<String> lines = Lists.newArrayList();

    for (final EDLMention edlMention : edlMentions) {
      lines.add(toLine(edlMention));
    }

    sink.writeLines(lines, "\n");
  }

  private String toLine(final EDLMention edlMention) {
    return edlMention.runId() + "\t" + edlMention.mentionId() + "\t" + edlMention.headString()
        + "\t" + edlMention.documentID() + ":" + edlMention.headOffsets().startInclusive().asInt()
        + "-" + edlMention.headOffsets().endInclusive().asInt() + "\t" + "NIL\t"
        + edlMention.entityType() + "\t" + edlMention.mentionType() + "\t"
        + edlMention.confidence();
  }
}
