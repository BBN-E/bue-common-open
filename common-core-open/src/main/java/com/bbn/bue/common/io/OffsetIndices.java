package com.bbn.bue.common.io;

import com.bbn.bue.common.strings.offsets.ByteOffset;
import com.bbn.bue.common.strings.offsets.OffsetRange;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.Closeables;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public final class OffsetIndices {

  private OffsetIndices() {
    throw new UnsupportedOperationException();
  }

  public static OffsetIndex readBinary(ByteSource source) throws IOException {
    final DataInputStream in = new DataInputStream(source.openBufferedStream());
    final ImmutableMap.Builder<Symbol, OffsetRange<ByteOffset>> builder = ImmutableMap.builder();
    final int numEntries = in.readInt();

    try {
      for (int i = 0; i < numEntries; ++i) {
        builder.put(Symbol.from(in.readUTF()),
            OffsetRange.byteOffsetRange(in.readInt(), in.readInt()));
      }
    } finally {
      Closeables.closeQuietly(in);
    }

    return MapOffsetIndex.fromMap(builder.build());
  }

  public static void writeBinary(OffsetIndex offsetIndex, ByteSink sink) throws IOException {
    final DataOutputStream out = new DataOutputStream(sink.openBufferedStream());

    boolean threw = true;
    try {
      out.writeInt(offsetIndex.keySet().size());
      // use a fixed key order to be diff-friendly.
      final Iterable<Symbol> keyOrder =
          Ordering.usingToString().immutableSortedCopy(offsetIndex.keySet());
      for (final Symbol key : keyOrder) {
        out.writeUTF(key.asString());
        // get is safe because we are iterating over the mapping's key set
        final OffsetRange<ByteOffset> range = offsetIndex.byteOffsetsOf(key).get();
        out.writeInt(range.startInclusive().asInt());
        out.writeInt(range.endInclusive().asInt());
      }
      threw = false;
    } finally {
      Closeables.close(out, threw);
    }

  }

  public static OffsetIndex forMap(final Map<Symbol, OffsetRange<ByteOffset>> map) {
    return MapOffsetIndex.fromMap(map);
  }
}
