package com.bbn.nlp.edl;

import com.bbn.bue.common.StringUtils;
import com.bbn.bue.common.strings.offsets.OffsetRange;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.collect.ImmutableList;
import com.google.common.io.CharSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Loads files in the submission format for the TAC KBP Entity Detection and Linking eval. This is
 * focused on Lorelei NER, so it ignores the KB IDs.
 */
public final class EDLLoader {
  private static final Logger log = LoggerFactory.getLogger(EDLLoader.class);

  private EDLLoader() {
  }

  public static EDLLoader create() {
    return new EDLLoader();
  }

  public ImmutableList<EDLMention> loadEDLMentionsFrom(CharSource source) throws IOException {
    final ImmutableList.Builder<EDLMention> ret = ImmutableList.builder();

    int lineNo = 0;
    for (final String line : source.readLines()) {
      ++lineNo;
      try {
        ret.add(parseMention(line));
      } catch (IOException ioe) {
        throw new IOException("Illegal EDL line #" + lineNo + ":\n" + line + "\n"
            + ioe.getMessage());
      }
    }

    return ret.build();
  }

  private static final int RUN_ID = 0;
  private static final int MENTION_ID = 1;
  private static final int HEAD_STRING = 2;
  private static final int DOC_ID_AND_OFFSETS = 3;
  // this field is deliberately ignored
  private static final int KB_ID = 4;
  private static final int ENTITY_TYPE = 5;
  private static final int MENTION_TYPE = 6;
  private static final int CONFIDENCE = 7;

  private EDLMention parseMention(final String line) throws IOException {
    final List<String> parts = StringUtils.OnTabs.splitToList(line);
    if (parts.size() != 8) {
      throw new IOException("Expected 8 fields but got " + parts.size());
    }

    final double confidence;
    final String confidenceString = parts.get(CONFIDENCE);
    try {
      confidence = Double.parseDouble(confidenceString);
    } catch (NumberFormatException ife) {
      throw new IOException("Illegal confidence " + confidenceString);
    }

    final String docIdAndOffsets = parts.get(DOC_ID_AND_OFFSETS);
    final int colonIndex = docIdAndOffsets.indexOf(":");
    if (colonIndex<0 || colonIndex == docIdAndOffsets.length()-1) {
      throw new IOException("Illegal doc ID and offsets element " + docIdAndOffsets);
    }
    final String documentId = docIdAndOffsets.substring(0, colonIndex);
    final int dashIdx = docIdAndOffsets.indexOf("-", colonIndex);
    if (dashIdx < 0 || dashIdx == docIdAndOffsets.length()-1) {
      throw new IOException("Illegal doc ID and offsets element " + docIdAndOffsets);
    }
    final int startOffset;
    final int endOffset;
    try {
      startOffset = Integer.parseInt(docIdAndOffsets.substring(colonIndex+1, dashIdx));
      endOffset = Integer.parseInt(docIdAndOffsets.substring(dashIdx+1));
    } catch (NumberFormatException ife) {
      throw new IOException("Illegal doc ID and offsets element " + docIdAndOffsets);
    }

    return EDLMention.create(Symbol.from(parts.get(RUN_ID)), parts.get(MENTION_ID),
        Symbol.from(documentId), parts.get(HEAD_STRING),
        OffsetRange.charOffsetRange(startOffset, endOffset),
        Symbol.from(parts.get(MENTION_TYPE)), Symbol.from(parts.get(ENTITY_TYPE)),
        confidence);
  }
}
