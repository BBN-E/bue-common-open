package com.bbn.bue.common.serialization.jackson;

import com.bbn.bue.common.io.ByteArraySink;

import com.google.common.io.ByteSource;

import java.io.IOException;

public final class JacksonTestUtils {

  private JacksonTestUtils() {
    throw new UnsupportedOperationException();
  }

  public static <T> T roundTripThroughSerializer(final T o, final JacksonSerializer jackson)
      throws IOException {
    final ByteArraySink sink = new ByteArraySink();
    jackson.serializeTo(o, sink);
    final ByteSource source = ByteSource.wrap(sink.toByteArray());
    return (T) jackson.deserializeFrom(source);
  }
}
