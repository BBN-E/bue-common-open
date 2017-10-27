package com.bbn.bue.common.files;

import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link KeyValueSink} based on the contents of a zip file.
 *
 * @author Constantine Lignos, Ryan Gabbard
 */
final class ZipKeyValueSink implements KeyValueSink<Symbol, byte[]> {

  private final OutputStream output;
  private final ZipOutputStream zip;
  private final Function<Symbol, String> keyEntryFunction;

  private ZipKeyValueSink(final OutputStream outputStream, final ZipOutputStream zipOutputStream,
      final Function<Symbol, String> keyEntryFunction) {
    this.output = checkNotNull(outputStream);
    this.zip = checkNotNull(zipOutputStream);
    this.keyEntryFunction = keyEntryFunction;
  }

  @Nonnull
  static ZipKeyValueSink forFile(final File zipfile,
      final Function<Symbol, String> keyEntryFunction) throws IOException {
    final OutputStream outputStream = Files.asByteSink(zipfile).openBufferedStream();
    final ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream, Charsets.UTF_8);
    return new ZipKeyValueSink(outputStream, zipOutputStream, keyEntryFunction);
  }

  @Override
  public void put(final Symbol key, final byte[] value) throws IOException {
    final String entryPath = keyEntryFunction.apply(key);
    if (entryPath == null || entryPath.isEmpty()) {
      throw new IllegalArgumentException("Key to entry function returned an invalid string");
    }
    final ZipEntry entry = new ZipEntry(entryPath);
    zip.putNextEntry(entry);
    zip.write(value);
    zip.closeEntry();
  }

  @Override
  public void close() throws IOException {
    zip.close();
    output.close();
  }
}
