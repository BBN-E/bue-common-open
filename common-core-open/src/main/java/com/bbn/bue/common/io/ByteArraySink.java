package com.bbn.bue.common.io;

import com.google.common.io.ByteSink;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A {@link com.google.common.io.ByteSink} which writes to a byte array.
 * The contents of this array can be recovered by calling toByteArray().
 *
 * Before anything is written to this {@code ByteSink}, the wrapped array
 * is empty.
 */
public class ByteArraySink extends ByteSink {
    private final ByteArrayOutputStream stream = new ByteArrayOutputStream();

    @Override
    public OutputStream openStream() throws IOException {
        stream.reset();
        return stream;
    }

    public byte[] toByteArray() {
        return stream.toByteArray();
    }
}