package com.bbn.nlp.edl;

import com.bbn.bue.common.strings.offsets.OffsetRange;
import com.bbn.bue.common.symbols.Symbol;
import com.bbn.bue.common.symbols.SymbolUtils;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.io.CharSource;

import java.io.IOException;
import java.util.List;

/**
 * Loads files in the submission format for the TAC KBP Entity Detection and Linking eval.
 */
public final class EDLLoader {

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

  /**
   * Loads EDL mentions, grouped by document. Multimap keys are in alphabetical order
   * by document ID.
   */
  public ImmutableListMultimap<Symbol, EDLMention> loadEDLMentionsByDocFrom(CharSource source) throws IOException {
    final ImmutableList<EDLMention> edlMentions = loadEDLMentionsFrom(source);
    final ImmutableListMultimap.Builder<Symbol, EDLMention> byDocs =
        ImmutableListMultimap.<Symbol, EDLMention>builder()
            .orderKeysBy(SymbolUtils.byStringOrdering());
    for (final EDLMention edlMention : edlMentions) {
      byDocs.put(edlMention.documentID(), edlMention);
    }
    return byDocs.build();
  }

  // We don't use StringUtils.onTabs because it sets omitEmptyStrings, which would lead to a
  // misleading crash if there are empty fields in the input.
  private static final Splitter TAB_SPLITTER = Splitter.on('\t').trimResults();
  private static final int RUN_ID = 0;
  private static final int MENTION_ID = 1;
  private static final int HEAD_STRING = 2;
  private static final int DOC_ID_AND_OFFSETS = 3;
  private static final int KB_ID = 4;
  private static final int ENTITY_TYPE = 5;
  private static final int MENTION_TYPE = 6;
  private static final int CONFIDENCE = 7;

  private EDLMention parseMention(final String line) throws IOException {
    final List<String> parts = TAB_SPLITTER.splitToList(line);
    if (parts.size() != 8 && parts.size() != 11) {
      throw new IOException(
          "Expected 8 fields (or 11 for assessment file) but got " + parts.size());
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

    return new EDLMention.Builder()
        .runId(Symbol.from(parts.get(RUN_ID)))
        .mentionId(parts.get(MENTION_ID))
        .documentID(Symbol.from(documentId))
        .headString(parts.get(HEAD_STRING))
        .headOffsets(OffsetRange.charOffsetRange(startOffset, endOffset))
        .mentionType(Symbol.from(parts.get(MENTION_TYPE)))
        .entityType(Symbol.from(parts.get(ENTITY_TYPE)))
        .kbId(parts.get(KB_ID))
        .confidence(confidence)
        .build();
  }
}
