package com.bbn.bue.common.io;

import com.google.common.annotations.Beta;
import com.google.common.collect.AbstractIterator;
import com.google.common.io.CharSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Beta
/**
 * Provides a way of iterating over multiple XML files concatenated together. When processing all
 * files in a very large corpus, it is often significantly more efficient to do this.
 *
 * WARNING: The current implementation is limited and not generally correct.
 * It is intended to be used only on XML documents output by BBN Serif.
 */
public final class ConcatenatedXMLIterableFactory {
  private final String splitString;
  private final int maxDocBytes;

  private static final int MEGABYTES = 1024*1024;

  private ConcatenatedXMLIterableFactory(String splitPattern,
                                         int maxDocBytes) {
    this.splitString = checkNotNull(splitPattern);
    checkArgument(maxDocBytes > 0);
    this.maxDocBytes = maxDocBytes;
  }

  public static ConcatenatedXMLIterableFactory splitOnXMLProlog() {
    return new ConcatenatedXMLIterableFactory("<?xml ", 30*MEGABYTES);
  }

  public Iterable<CharSource> filesIn(CharSource source) {
    return new ConcatenatedXMLFile(source);
  }

  /**
   * Since we can't declare an {@link java.io.IOException} on the {@link #iterator()}
   * method, be aware that any exceptions during reading will be wrapped in
   * a {@link ConcatenatedXMLException}.
   */
  private final class ConcatenatedXMLFile implements Iterable<CharSource> {
    private final CharSource source;

    public ConcatenatedXMLFile(CharSource source) {
      this.source = checkNotNull(source);
    }

    @Override
    public Iterator<CharSource> iterator() {
      try {
        return new ConcatenatedXMLIterator(source.openBufferedStream());
      } catch (IOException ioe) {
        throw new ConcatenatedXMLException(ioe);
      }
    }
  }

  public static class ConcatenatedXMLException extends RuntimeException {
    private final Exception wrapped;

    public ConcatenatedXMLException(Exception wrapped) {
      super(wrapped);
      this.wrapped = checkNotNull(wrapped);
    }

    public Exception getWrappedException() {
      return wrapped;
    }
  }

  private class ConcatenatedXMLIterator extends AbstractIterator<CharSource> {
    private final BufferedReader reader;
    private boolean first = true;

    public ConcatenatedXMLIterator(BufferedReader bufferedReader) {
      this.reader = checkNotNull(bufferedReader);
    }

    @Override
    protected CharSource computeNext() {
      try {
        final String firstLine = reader.readLine();
        // no more data - we're done
        if (firstLine == null) {
          return endOfData();
        }

        // the reader should always be starting with the split string
        // note this means the file being split must begin with the split string
        if (!firstLine.startsWith(splitString)) {
          throw new ConcatenatedXMLException(
              new IOException("Block does not start with split string " + splitString));
        }

        final StringBuilder data = new StringBuilder();

        String line;
        // we always mark our position before beginning a new line
        // so that if we find the beginning of the next block, we can back up
        reader.mark(maxDocBytes);
        while ((line = reader.readLine()) != null) {
          // beginning of next block. Back up and return what we've accumulated
          if (line.startsWith(splitString)) {
            reader.reset();
            break;
          } else {
            // not done yet
            data.append(line).append("\n");
            reader.mark(maxDocBytes);
          }
        }

        return CharSource.wrap(data.toString());
      } catch (IOException ioe) {
        throw new ConcatenatedXMLException(ioe);
      }
    }
  }
}
