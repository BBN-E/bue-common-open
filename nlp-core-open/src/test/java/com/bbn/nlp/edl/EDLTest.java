package com.bbn.nlp.edl;

import com.bbn.bue.common.io.StringSink;
import com.bbn.bue.common.strings.offsets.OffsetRange;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;

import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;

public class EDLTest {

  @Test
  public void edlLoadingTest() throws IOException {
    final EDLLoader edlLoader = EDLLoader.create();
    final ImmutableList<EDLMention> edlMentions =
        edlLoader.loadEDLMentionsFrom(Resources.asCharSource(
            Resources.getResource(EDLTest.class, "edl-sample.txt"), Charsets.UTF_8));
    assertEquals(2, edlMentions.size());
    final EDLMention secondMention = edlMentions.get(1);
    assertEquals("BBN1", secondMention.runId());
    assertEquals("ment-2", secondMention.mentionId());
    assertEquals("BBN Technologies", secondMention.headString());
    assertEquals("NYT20081231002", secondMention.documentID());
    assertEquals(OffsetRange.charOffsetRange(0, 15), secondMention.headOffsets());
    assertEquals(0.765, secondMention.confidence(), .0001);
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
}
