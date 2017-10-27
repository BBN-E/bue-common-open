package com.bbn.bue.common.io;

import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Optional;
import com.google.common.io.CharSource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * Something which contains multiple 'channels' of character data, indexed by {@link
 * com.bbn.bue.common.symbols.Symbol}s.
 *
 * This should get merged/replaced with the newer {@link com.bbn.bue.common.files.KeyValueSource}
 * code.
 */
public interface CharacterChannelSet {

  Set<Symbol> channelSet();

  Optional<CharSource> channelAsCharSource(Symbol key, Charset charset) throws IOException;
}
