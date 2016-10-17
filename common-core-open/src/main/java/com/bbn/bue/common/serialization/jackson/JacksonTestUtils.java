package com.bbn.bue.common.serialization.jackson;

import com.google.common.io.ByteSource;

import com.bbn.bue.common.io.ByteArraySink;

import java.io.IOException;

public final class JacksonTestUtils {

  private JacksonTestUtils() {
    throw new UnsupportedOperationException();
  }

  public static <T> T roundTripThroughSerializer(final T o, final JacksonSerializer jackson)
      throws IOException {
    final ByteArraySink sink = ByteArraySink.create();
    jackson.serializeTo(o, sink);
    final ByteSource source = ByteSource.wrap(sink.toByteArray());
    return (T) jackson.deserializeFrom(source);
  }
}
