package com.bbn.bue.common.files;

import com.google.common.io.ByteSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides utilities for working with zip files.
 *
 * @author Constantine Lignos
 */
public final class ZipFiles {

  private ZipFiles() {
    throw new UnsupportedOperationException();
  }

  /**
   * Get a {@link ZipFile} entry as a Guava {@link ByteSource}
   */
  public static ByteSource entryAsByteSource(final ZipFile file, ZipEntry entry) {
    return new ZipEntryByteSource(file, entry);
  }

  /**
   * Get a {@link ZipFile} entry as a string.
   */
  public static String entryAsString(final ZipFile file, ZipEntry entry, Charset charset)
      throws IOException {
    return new ZipEntryByteSource(file, entry).asCharSource(charset).read();
  }

  /**
   * Provides {@link ByteSource} access to zip entries by wrapping their {@link InputStream}.
   */
  private static class ZipEntryByteSource extends ByteSource {

    private final ZipFile file;
    private final ZipEntry entry;

    private ZipEntryByteSource(ZipFile file, final ZipEntry entry) {
      this.file = checkNotNull(file);
      this.entry = checkNotNull(entry);
    }

    @Override
    @Nonnull
    public InputStream openStream() throws IOException {
      return file.getInputStream(entry);
    }
  }
}
