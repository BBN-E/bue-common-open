package com.bbn.bue.common.io;

import com.bbn.bue.common.strings.offsets.ByteOffset;
import com.bbn.bue.common.strings.offsets.OffsetRange;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Optional;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A file which contains multiple indexed blocks of character data.
 *
 * This should get merged/replaced with the never {@link com.bbn.bue.common.files.KeyValueSource} code.
 */
public final class IndexedByteSource implements CharacterChannelSet {

  private final ByteSource source;
  private final OffsetIndex offsetIndex;

  private IndexedByteSource(final ByteSource source, final OffsetIndex offsetIndex) {
    this.source = checkNotNull(source);
    this.offsetIndex = checkNotNull(offsetIndex);
  }

  public static IndexedByteSource from(final ByteSource source, final OffsetIndex offsetIndex) {
    return new IndexedByteSource(source, offsetIndex);
  }


  @Override
  public Set<Symbol> channelSet() {
    return offsetIndex.keySet();
  }

  public Optional<ByteSource> channelAsByteSource(final Symbol key) throws IOException {
    final Optional<OffsetRange<ByteOffset>> range =
        offsetIndex.byteOffsetsOf(checkNotNull(key));
    if (range.isPresent()) {
      final ByteOffset startInclusive = range.get().startInclusive();
      final ByteOffset endInclusive = range.get().endInclusive();

      return Optional.of(source.slice(startInclusive.asInt(),
          endInclusive.asInt() - startInclusive.asInt() + 1));
    } else {
      return Optional.absent();
    }
  }

  @Override
  public Optional<CharSource> channelAsCharSource(final Symbol key, Charset charset)
      throws IOException {
    final Optional<ByteSource> channelSource = channelAsByteSource(key);
    if (channelSource.isPresent()) {
      return Optional.of(channelSource.get().asCharSource(charset));
    } else {
      return Optional.absent();
    }
  }

  public ByteSource source() {
    return source;
  }

}
