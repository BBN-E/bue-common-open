package com.bbn.bue.common.io;

import com.google.common.annotations.Beta;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import static com.google.common.base.Preconditions.checkNotNull;

@Beta
public class GZIPByteSource extends ByteSource {

  private GZIPByteSource(final ByteSource wrappedByteSource) {
    this.wrappedByteSource = checkNotNull(wrappedByteSource);
  }

  public static ByteSource fromCompressed(final ByteSource wrappedByteSource) {
    return new GZIPByteSource(wrappedByteSource);
  }

  public static ByteSource fromCompressed(final File f) {
    return fromCompressed(Files.asByteSource(f));
  }

  @Override
  public InputStream openStream() throws IOException {
    return new GZIPInputStream(wrappedByteSource.openBufferedStream());
  }

  private final ByteSource wrappedByteSource;
}
