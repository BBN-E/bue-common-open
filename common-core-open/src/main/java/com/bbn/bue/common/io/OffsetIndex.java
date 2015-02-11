package com.bbn.bue.common.io;

import com.bbn.bue.common.strings.offsets.ByteOffset;
import com.bbn.bue.common.strings.offsets.OffsetRange;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Optional;

import java.util.Set;

/**
 * Maps keys to offset ranges.  This is useful when, for example, many documents are concatenated
 * together and you wish to pull on eout of the middle.
 */
public interface OffsetIndex {

  public Optional<OffsetRange<ByteOffset>> byteOffsetsOf(Symbol key);

  public Set<Symbol> keySet();
}
