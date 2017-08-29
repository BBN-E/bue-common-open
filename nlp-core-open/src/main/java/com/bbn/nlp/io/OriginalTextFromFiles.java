package com.bbn.nlp.io;

import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An implementations of an original text source which loads the text from the filesystem.
 *
 *
 * This should be merged into the newer {@link com.bbn.bue.common.files.KeyValueSource}
 * code.
 *
 * @author Ryan Gabbard
 */
public final class OriginalTextFromFiles implements OriginalTextSource {

  private final DocIDToFileMapping docidMap;

  private OriginalTextFromFiles(final DocIDToFileMapping docidMap) {
    this.docidMap = checkNotNull(docidMap);
  }

  @Override
  public Optional<String> getOriginalText(final Symbol docID) throws IOException {
    final Optional<File> f = docidMap.fileForDocID(docID);
    if (f.isPresent()) {
      return Optional.of(Files.asCharSource(f.get(), Charsets.UTF_8).read());
    } else {
      return Optional.absent();
    }
  }

  public static Builder createFromDocIdMap(final DocIDToFileMapping docidToSerifXML) {
    return Builder.fromDocIdMap(docidToSerifXML);
  }


  public static class Builder {

    private Builder() {
    }

    public OriginalTextSource build() throws IOException {
      return CachingOriginalTextSource.from(new OriginalTextFromFiles(docidMap), maxElements);
    }

    private static Builder fromDocIdMap(final DocIDToFileMapping docidToSerifXML) {
      final Builder ret = new Builder();
      ret.docidMap = docidToSerifXML;
      return ret;
    }

    /**
     * Sets a maximum size for the cache. This is only a suggestion and not strictly enforced.
     */
    public Builder setMaxSize(final int maxSize) {
      checkArgument(maxSize >= 0);
      maxElements = maxSize;
      return this;
    }

    private DocIDToFileMapping docidMap = null;

    // a reasonable default
    private int maxElements = 100;
  }
}
