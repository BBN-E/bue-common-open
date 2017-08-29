package com.bbn.bue.common.files;

import com.bbn.bue.common.symbols.Symbol;
import com.bbn.bue.common.symbols.SymbolUtils;

import com.google.common.base.Function;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * Provides factory methods for key-value sinks.
 *
 * @author Constantine Lignos, Ryan Gabbard
 */
public final class KeyValueSinks {

  private KeyValueSinks() {
    throw new UnsupportedOperationException();
  }

  /**
   * Creates a new key-value sink backed by an embedded database.
   *
   * Compression should generally be used unless the values are very small or already compressed.
   *
   * @param dbFile         the file to use for the database
   * @param compressValues whether to compress values
   * @return a key-value sink
   * @throws IOException if the file could not be opened for writing
   */
  @Nonnull
  public static KeyValueSink<Symbol, byte[]> forPalDB(final File dbFile,
      final boolean compressValues)
      throws IOException {
    return PalDBKeyValueSink.forFile(dbFile, compressValues);
  }

  /**
   * Creates a new key-value sink backed by a zip file using the default (identity) mapping
   * between keys and the entry inside the zip used for storing their value. The caller must ensure
   * that the zip file is not closed or modified, otherwise all behavior is undefined. To specify a
   * function that defines the mapping between keys and the zip entry used for their values, see
   * {@link #forZip(File, Function)}.
   *
   * @param zipFile the zip file to use for storage
   * @return a key-value sink
   * @throws IOException if the zip file could not be opened for writing
   * @see #forZip(File, Function)
   */
  @Nonnull
  public static KeyValueSink<Symbol, byte[]> forZip(final File zipFile) throws IOException {
    return ZipKeyValueSink.forFile(zipFile, SymbolUtils.desymbolizeFunction());
  }

  /**
   * Creates a new key-value sink backed by a zip file, using the specified a function to maps each
   * key in the store to the zip entry that will be used to store its value. The caller must ensure
   * that the zip file is not closed or modified, otherwise all behavior is undefined. To use a
   * default function to define the mapping between keys and the zip entry used for their values,
   * see {@link #forZip(File, Function)}.
   *
   * @param zipFile          the zip file to use for storage
   * @param keyEntryFunction the function to be used to transform keys into the entry (path) inside
   *                         the zip file that will be used to store the value
   * @return a key-value sink
   * @throws IOException if the zip file could not be opened for writing
   * @see #forZip(File)
   */
  @Nonnull
  public static KeyValueSink<Symbol, byte[]> forZip(final File zipFile,
      final Function<Symbol, String> keyEntryFunction) throws IOException {
    return ZipKeyValueSink.forFile(zipFile, keyEntryFunction);
  }
}
