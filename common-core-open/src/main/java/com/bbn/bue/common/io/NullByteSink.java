package com.bbn.bue.common.io;

import com.google.common.io.ByteSink;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.OutputStream;

public final class NullByteSink extends ByteSink {
    @Override
    public OutputStream openStream() throws IOException {
        return ByteStreams.nullOutputStream();
    }
}
