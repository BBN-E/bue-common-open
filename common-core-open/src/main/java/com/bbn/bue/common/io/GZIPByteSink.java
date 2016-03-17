package com.bbn.bue.common.io;

import com.google.common.io.ByteSink;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

public final class GZIPByteSink extends ByteSink {

  private GZIPByteSink(final ByteSink wrappedByteSink) {
    this.wrappedByteSink = checkNotNull(wrappedByteSink);
  }

  public static ByteSink gzipCompress(final ByteSink byteSink) {
    return new GZIPByteSink(byteSink);
  }

  public static ByteSink gzipCompress(final File f) {
    return gzipCompress(Files.asByteSink(f));
  }

  @Override
  public OutputStream openStream() throws IOException {
    return new GZIPOutputStream(wrappedByteSink.openBufferedStream());
  }

  private final ByteSink wrappedByteSink;
}
