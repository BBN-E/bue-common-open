package com.bbn.nlp.indri;

import com.bbn.bue.common.files.FileUtils;
import com.bbn.bue.common.parameters.Parameters;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * An {@link com.bbn.nlp.indri.IndriFileProcessor} for the 'raw' files from Gigaword which
 * concatenate many files together. This is the preferred way of indexing GW because it is much much
 * faster.
 */
public final class RawGigawordAsTrecTextFileProcessor extends AbstractIndriFileProcessor {

  // this param exists because not all Chinese gigaword documents are processed into serifxml.
  // Rather than solve the hard problem and attempt to fix CSerif
  private final static String OPTIONAL_DOCID_WHITE_LIST_PARAM = "optionalDocIdWhiteList";
  private final Optional<? extends Set<String>> optionalDocIdWhiteList;

  private RawGigawordAsTrecTextFileProcessor(
      final Optional<? extends Set<String>> optionalDocIdWhiteList) {
    this.optionalDocIdWhiteList = optionalDocIdWhiteList;
  }

  public static RawGigawordAsTrecTextFileProcessor fromParameters(Parameters params)
      throws IOException {
    final Optional<? extends Set<String>> docIdsToProcess;
    if (params.isPresent(OPTIONAL_DOCID_WHITE_LIST_PARAM)) {
      docIdsToProcess = Optional.of(ImmutableSet
          .copyOf(FileUtils.loadStringList(Files
              .asCharSource(params.getExistingFile(OPTIONAL_DOCID_WHITE_LIST_PARAM),
                  Charsets.UTF_8))));
    } else {
      docIdsToProcess = Optional.absent();
    }
    return new RawGigawordAsTrecTextFileProcessor(docIdsToProcess);
  }

  @Override
  public Iterator<String> documentsForString(String s) {
    return new RawGigawordAsTrecTextIterator(s);
  }


  private final class RawGigawordAsTrecTextIterator
      extends AbstractIterator<String> {

    private final String fullText;
    private int startNextSearchAt = 0;

    private RawGigawordAsTrecTextIterator(String fullText) {
      this.fullText = checkNotNull(fullText);
    }

    private static final String END_OF_DOCUMENT_MARKER = "</DOC>";

    @Override
    protected String computeNext() {
      if (startNextSearchAt >= fullText.length()) {
        return endOfData();
      }
      int endOfNextDocumentClosingElement =
          fullText.indexOf(END_OF_DOCUMENT_MARKER, startNextSearchAt);

      while (endOfNextDocumentClosingElement >= 0) {
        final int endOfDoc = endOfNextDocumentClosingElement + END_OF_DOCUMENT_MARKER.length();
        final String docString = fullText.substring(startNextSearchAt, endOfDoc);
        startNextSearchAt = endOfDoc + 1;
        // do not search the entire document ...
        final Matcher m = GIGAWORD_DOC_ELEMENT_PATTERN
            .matcher(docString.substring(0, Math.min(100, docString.length())));
        checkState(m.find());
        final String docId = m.group(1);
        // if our whitelist doesn't contain this docid
        if (!optionalDocIdWhiteList.isPresent() || optionalDocIdWhiteList.get().contains(docId)) {
          return gigawordToTrecText(docString);
        }
        endOfNextDocumentClosingElement =
            fullText.indexOf(END_OF_DOCUMENT_MARKER, startNextSearchAt);
      }
      return endOfData();
    }

    // Gigaword does not fit the trectext format because it puts the document ID in an
    // attribute rather than a <DOCNO> element as required. This function fixes this.
    private final Pattern GIGAWORD_DOC_ELEMENT_PATTERN = Pattern.compile(
        "<DOC id=\"(.*?)\".*>");

    private String gigawordToTrecText(String gigawordDoc) {
      return GIGAWORD_DOC_ELEMENT_PATTERN.matcher(gigawordDoc)
          .replaceFirst("<DOC>\n\t<DOCNO>$1</DOCNO>\n");
    }
  }

}
