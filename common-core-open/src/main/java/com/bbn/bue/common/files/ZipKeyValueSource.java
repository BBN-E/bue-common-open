package com.bbn.bue.common.files;

import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteSource;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An {@link ImmutableKeyValueSource} based on the contents of a zip file.
 *
 * @author Constantine Lignos, Ryan Gabbard
 */
final class ZipKeyValueSource extends AbstractImmutableKeyValueSource<Symbol, ByteSource> {

  private final ZipFile zipFile;
  private final ImmutableMap<Symbol, String> keyFiles;

  ZipKeyValueSource(final ZipFile zipFile, final ImmutableMap<Symbol, String> keyFiles) {
    this.zipFile = checkNotNull(zipFile);
    this.keyFiles = checkNotNull(keyFiles);
  }

  @Nonnull
  @Override
  public Iterable<Symbol> keys() throws IOException {
    return keyFiles.keySet();
  }

  @Override
  @Nonnull
  public Optional<ByteSource> get(final Symbol key) throws IOException {
    final String entryName = keyFiles.get(key);
    if (entryName == null) {
      // Key is not present
      return Optional.absent();
    }
    final ZipEntry entry = zipFile.getEntry(entryName);
    if (entry == null) {
      // As we have indexed the entry before, this would an IO issue so we pass it on as such
      throw new IOException("Could not open zip entry " + entryName);
    }
    final ByteSource source = ZipFiles.entryAsByteSource(zipFile, entry);
    return Optional.of(source);
  }

  @Override
  public void close() {
    // No resources to close. The zip file is managed by the caller.
  }
}
