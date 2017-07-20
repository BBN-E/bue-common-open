package com.bbn.nlp.edl;

import com.bbn.bue.common.io.StringSink;
import com.bbn.bue.common.strings.offsets.OffsetRange;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;

import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

public class EDLTest {

  @Test
  public void edlLoadingTest() throws IOException {
    final EDLLoader edlLoader = EDLLoader.create();
    final ImmutableList<EDLMention> edlMentions =
        edlLoader.loadEDLMentionsFrom(Resources.asCharSource(
            Resources.getResource(EDLTest.class, "edl-sample.txt"), Charsets.UTF_8));
    assertEquals(2, edlMentions.size());
    final EDLMention secondMention = edlMentions.get(1);
    assertEquals("BBN1", secondMention.runId().asString());
    assertEquals("ment-2", secondMention.mentionId());
    assertEquals("BBN Technologies", secondMention.headString());
    assertEquals("12345678", secondMention.kbId().get());
    assertEquals("NYT20081231002", secondMention.documentID().asString());
    assertEquals(OffsetRange.charOffsetRange(0, 15), secondMention.headOffsets());
    assertEquals(0.765, secondMention.confidence(), .0001);
    final EDLMention firstMention = edlMentions.get(0);
    assertEquals("NIL", firstMention.kbId().get());
  }

  @Test
  public void edlRoundtripTest() throws IOException {
    final EDLLoader edlLoader = EDLLoader.create();
    final EDLWriter edlWriter = EDLWriter.create();

    final ImmutableList<EDLMention> edlMentions =
        edlLoader.loadEDLMentionsFrom(Resources.asCharSource(
            Resources.getResource(EDLTest.class, "edl-sample.txt"), Charsets.UTF_8));

    final StringSink tmp = StringSink.createEmpty();
    edlWriter.writeEDLMentions(edlMentions, tmp);
    final ImmutableList<EDLMention> reloadedEDLMentions =
        edlLoader.loadEDLMentionsFrom(CharSource.wrap(tmp.getString()));
    assertEquals(edlMentions, reloadedEDLMentions);
  }

  @Test
  public void edlKbIdTest() {
    final EDLMention ment = new EDLMention.Builder()
        .confidence(1.0)
        .entityType(Symbol.from("ORG"))
        .mentionType(Symbol.from("NAM"))
        .documentID(Symbol.from("NW_1234"))
        .headOffsets(OffsetRange.charOffsetRange(0, 2))
        .runId(Symbol.from("test"))
        .mentionId("ment-1")
        .headString("BBN")
        .build();

    assertEquals(Optional.absent(), ment.kbId());
    assertEquals(Optional.of("123"),
        new EDLMention.Builder().from(ment).kbId("123").build().kbId());
    assertEquals(Optional.of("NIL123"),
        new EDLMention.Builder().from(ment).nilKbId("123").build().kbId());
    // Verify an empty ID throws an exception
    try {
      new EDLMention.Builder().from(ment).kbId("").build();
      fail();
    } catch (IllegalArgumentException e) {
      // Pass
    }
  }
}
