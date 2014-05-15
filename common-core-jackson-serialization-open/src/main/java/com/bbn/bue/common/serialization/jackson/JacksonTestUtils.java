package com.bbn.bue.common.serialization.jackson;

import java.io.IOException;
import java.io.OutputStream;

import com.bbn.bue.common.io.ByteArraySink;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;

public final class JacksonTestUtils  {
	private JacksonTestUtils () { throw new UnsupportedOperationException(); }

	public static <T> T roundTripThroughSerializer(final T o, final JacksonSerializer jackson) throws IOException {
		final ByteArraySink sink = new ByteArraySink();
		jackson.serializeTo(o, sink);
		final ByteSource source = ByteSource.wrap(sink.toByteArray());
		return (T)jackson.deserializeFrom(source);
	}
}
