package com.bbn.nlp.corpora.gigaword;

import com.bbn.bue.common.files.FileUtils;
import com.bbn.bue.common.io.OffsetIndex;
import com.bbn.bue.common.io.OffsetIndices;
import com.bbn.bue.common.parameters.Parameters;
import com.bbn.bue.common.strings.offsets.ByteOffset;
import com.bbn.bue.common.strings.offsets.OffsetRange;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Produces document-ID to byte-offset indices for the flat representation of Gigaword (which
 * comes on the CDs). This is often useful because copying the 'exploded' verison of Gigaword with a
 * 12 M separate files is prohibitively expensive.
 */
public final class IndexFlatGigaword {

  private static final Logger log = LoggerFactory.getLogger(IndexFlatGigaword.class);

  private IndexFlatGigaword() {
    throw new UnsupportedOperationException();
  }

  private static void trueMain(String[] argv) throws IOException {
    final Parameters params = Parameters.loadSerifStyle(new File(argv[0]));
    final File gigawordRawRoot = params.getExistingDirectory("rawGigawordRoot");
    final File outputDirectory = params.getCreatableDirectory("rawGigawordOffsetIndexDir");

    for (final File sourceDir : gigawordRawRoot.listFiles()) {
      final File outputDir = new File(outputDirectory, sourceDir.getName());
      outputDir.mkdir();
      for (final File chunkFile : sourceDir.listFiles()) {
        log.info("Building offset map for {}", chunkFile);
        final OffsetIndex offsetIndex = buildOffsetIndex(Files.asByteSource(chunkFile));
        final File indexFile = new File(outputDir, chunkFile.getName() + ".index");
        log.info("Writing {} offsets for {} to {}", offsetIndex.keySet().size(), chunkFile,
            indexFile);
        OffsetIndices.writeBinary(offsetIndex, FileUtils.asCompressedByteSink(indexFile));
      }
    }
  }

  private final static String beginProbeString = "<DOC id=\"";
  private final static byte[] beginProbe = beginProbeString.getBytes(Charsets.UTF_8);
  private final static String docIdEndString = "\"";
  private final static byte[] docIdEnd = docIdEndString.getBytes(Charsets.UTF_8);
  private final static String endProbeString = "</DOC>";
  private final static byte[] endProbe = endProbeString.getBytes(Charsets.UTF_8);

  private static final int NOT_FOUND = 1;

  /* package private for testing */
  static OffsetIndex buildOffsetIndex(ByteSource source) throws IOException {
    final Map<Symbol, OffsetRange<ByteOffset>> ret = Maps.newHashMap();

    // we could be more efficient and deal with the byte stream as it comes along,
    // but these files don't exceed 50 MB, so it's simpler just to read it all in
    final byte[] fileAsBytes = source.read();

    // Okay, kids, don't try this at home. In general searching byte-by-byte through UTF-8 is a
    // bad idea, because multibyte characters will trip you up.  However, in this particular case:
    //   (a) all of the characters in our search strings are single-byte, and
    //   (b) for the begin and end probes, they are long enough that the chances of them occurring
    //         accidentally starting at a non-initial character of a multibyte character
    //         is vanishingly small.  It could be a problem for docIdEnd, being only a single character,
    //         but this will only be a problem if we have non-ASCII unicode characters in the doc IDs
    //         which is not the case for Gigaword.
    int startDocInclusive;
    int nextSearchIdx = 0;
    while ((startDocInclusive = findBytes(fileAsBytes, beginProbe, nextSearchIdx)) != NOT_FOUND) {
      final int startDocIDInclusive = startDocInclusive + beginProbe.length; // double check this!
      final int endDocIDIdxExclusive = findBytes(fileAsBytes, docIdEnd, startDocIDInclusive + 1);
      if (endDocIDIdxExclusive != NOT_FOUND) {
        final String docID = bytesAsString(fileAsBytes, startDocIDInclusive, endDocIDIdxExclusive);
        final int endTagIdx = findBytes(fileAsBytes, endProbe, endDocIDIdxExclusive + 1);
        if (endTagIdx != NOT_FOUND) {
          final int endDocIdxInclusive = endTagIdx + endProbe.length - 1;
          if (ret.containsKey(Symbol.from(docID))) {
            log.warn("Document ID {} occurs more than once; using latest version", docID);
          }
          ret.put(Symbol.from(docID),
              OffsetRange.byteOffsetRange(startDocInclusive, endDocIdxInclusive));
          nextSearchIdx = endDocIdxInclusive + 1;
        } else {
          throw new IOException("Failed to find closing document tag in " + source);
        }
      } else {
        throw new IOException("Failed to find end of document ID in " + source);
      }
    }
    return OffsetIndices.forMap(ret);
  }

  private static String bytesAsString(final byte[] sourceBytes, final int startInclusive,
      final int endExclusive) {
    return new String(Arrays.copyOfRange(sourceBytes, startInclusive, endExclusive),
        Charsets.UTF_8);
  }

  private static int findBytes(byte[] toSearch, byte[] needle, int startIdx) {
    // because our needles are unlikely to overlap with regular text,
    // using K-M-P here is not worth the trouble.
    for (int idx = startIdx; idx < toSearch.length; ++idx) {
      if (matches(toSearch, needle, idx)) {
        return idx;
      }
    }
    return NOT_FOUND;
  }

  private static boolean matches(byte[] toMatch, byte[] pattern, int startIdx) {
    if (startIdx + pattern.length > toMatch.length) {
      // there's not enough room left in the string being matched
      // to fit the pattern
      return false;
    }
    for (int i = 0; i < pattern.length; ++i) {
      int toMatchIdx = startIdx + i;
      if (toMatch[toMatchIdx] != pattern[i]) {
        return false;
      }
    }
    return true;
  }

  public static void main(String[] argv) {
    // we wrap the main method in this way to
    // ensure a non-zero return value on failure
    try {
      System.err.println("Copyright 2015 Raytheon BBN Technologies Corp.");
      System.err.println("All Rights Reserved");
      trueMain(argv);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
