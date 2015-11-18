package com.bbn.bue.common.io;

import com.google.common.io.ByteSink;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A {@link ByteSink} which throws away everything written to it.
 */
public final class NullByteSink extends ByteSink {

  /**
   * Prefer {@link #create()}
   */
  @Deprecated
  public NullByteSink() {

  }

  @SuppressWarnings("deprecated")
  public static ByteSink create() {
    return new NullByteSink();
  }

  @Override
  public OutputStream openStream() throws IOException {
    return ByteStreams.nullOutputStream();
  }
}
