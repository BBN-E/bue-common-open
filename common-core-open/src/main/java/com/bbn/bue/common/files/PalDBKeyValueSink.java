package com.bbn.bue.common.files;

import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Throwables;
import com.linkedin.paldb.api.Configuration;
import com.linkedin.paldb.api.PalDB;
import com.linkedin.paldb.api.StoreWriter;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A PalDB-backed {@link KeyValueSink}
 *
 * Internally, {@link String}s are used for the keys to avoid serializing {@link Symbol}s.
 */
final class PalDBKeyValueSink implements KeyValueSink<Symbol, byte[]> {

  private final StoreWriter writer;

  private PalDBKeyValueSink(final StoreWriter writer) {
    this.writer = checkNotNull(writer);
  }

  @Nonnull
  static KeyValueSink<Symbol, byte[]> forFile(final File dbFile, final boolean compressValues)
      throws IOException {
    final Configuration configuration = PalDB.newConfiguration()
        .set("compression.enabled", Boolean.toString(compressValues));
    final StoreWriter writer;
    try {
      writer = PalDB.createWriter(dbFile, configuration);
    } catch (Exception e) {
      // The writer throws all underlying IOExceptions as unchecked exceptions, so we undo this,
      // providing checked exceptions from the cause if it is an IOException.
      Throwables.propagateIfInstanceOf(e.getCause(), IOException.class);
      // Just throw as-is if it wasn't IOException
      throw Throwables.propagate(e);
    }
    return new PalDBKeyValueSink(writer);
  }

  @Override
  public void put(final Symbol key, final byte[] value) throws IOException {
    try {
      // We use the string value as the key to simplify serialization
      writer.put(key.asString(), value);
    } catch (Exception e) {
      // The writer throws all underlying IOExceptions as unchecked exceptions, so we undo this,
      // providing checked exceptions from the cause if it is an IOException.
      Throwables.propagateIfInstanceOf(e.getCause(), IOException.class);
      // Just throw as-is if it wasn't IOException
      throw Throwables.propagate(e);
    }
  }

  @Override
  public void close() throws IOException {
    try {
      writer.close();
    } catch (Exception e) {
      // The writer throws all underlying IOExceptions as unchecked exceptions, so we undo this,
      // providing checked exceptions from the cause if it is an IOException.
      Throwables.propagateIfInstanceOf(e.getCause(), IOException.class);
      // Just throw as-is if it wasn't IOException
      throw Throwables.propagate(e);
    }
  }
}
