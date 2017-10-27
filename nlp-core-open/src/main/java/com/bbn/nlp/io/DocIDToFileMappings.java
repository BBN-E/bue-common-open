package com.bbn.nlp.io;

import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility methods for creating {@link com.bbn.nlp.io.DocIDToFileMapping}s.
 *
 *
 * This should be merged into the newer {@link com.bbn.bue.common.files.KeyValueSource}
 * code.
 *
 * @author Ryan Gabbard
 */
public final class DocIDToFileMappings {

  private DocIDToFileMappings() {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a DocIDToFileMapping which does lookup in the provieded map and returns {@link
   * com.google.common.base.Optional#absent()}  if no mapping is present.
   */
  public static DocIDToFileMapping forMap(Map<Symbol, File> map) {
    return new ForMap(map);
  }

  /**
   * Returns a {@code DocIDToFileMapping} which calls the specified function.
   */
  public static DocIDToFileMapping forFunction(Function<Symbol, Optional<File>> function) {
    return new ForFunction(function);
  }

  private static class ForMap implements DocIDToFileMapping {

    private final ImmutableMap<Symbol, File> map;

    public ForMap(Map<Symbol, File> map) {
      this.map = ImmutableMap.copyOf(map);
    }

    @Override
    public Optional<File> fileForDocID(Symbol docID) {
      return Optional.fromNullable(map.get(docID));
    }
  }

  private static class ForFunction implements DocIDToFileMapping {

    private final Function<Symbol, Optional<File>> function;

    public ForFunction(Function<Symbol, Optional<File>> function) {
      this.function = checkNotNull(function);
    }

    @Override
    public Optional<File> fileForDocID(Symbol docID) {
      return function.apply(docID);
    }
  }
}
