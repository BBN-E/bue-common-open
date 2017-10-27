package com.bbn.bue.common.files;

import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An immutable key-value source built from a symbol to file map, such as those created by
 * {@link com.bbn.bue.common.files.FileUtils#loadSymbolToFileMap(File)}.
 *
 * @author Constantine Lignos, Ryan Gabbard
 */
final class FileMapKeyToByteSource extends AbstractImmutableKeyValueSource<Symbol, ByteSource> {

  final ImmutableMap<Symbol, File> fileMap;

  FileMapKeyToByteSource(final Map<Symbol, File> fileMap) {
    this.fileMap = ImmutableMap.copyOf(fileMap);
  }

  @Nonnull
  @Override
  public Iterable<Symbol> keys() {
    return fileMap.keySet();
  }

  @Nonnull
  @Override
  public Optional<ByteSource> get(final Symbol key) throws IOException {
    checkNotNull(key);
    final File file = fileMap.get(key);
    if (file == null) {
      return Optional.absent();
    } else {
      return Optional.of(Files.asByteSource(file));
    }
  }

  @Override
  public void close() {
    // No resources to close
  }
}
