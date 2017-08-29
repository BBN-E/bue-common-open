package com.bbn.bue.common.files;

import com.bbn.bue.common.symbols.Symbol;
import com.bbn.bue.common.symbols.SymbolUtils;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.io.ByteSource;
import com.linkedin.paldb.api.NotFoundException;
import com.linkedin.paldb.api.PalDB;
import com.linkedin.paldb.api.StoreReader;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A PalDB-backed {@link KeyValueSource}.
 *
 * @author Constantine Lignos, Ryan Gabbard
 */
final class PalDBKeyValueSource extends AbstractImmutableKeyValueSource<Symbol, ByteSource> {

  private final StoreReader reader;

  private PalDBKeyValueSource(final StoreReader reader) {
    this.reader = checkNotNull(reader);
  }

  @Nonnull
  static ImmutableKeyValueSource<Symbol, ByteSource> fromFile(final File dbFile)
      throws IOException {
    final StoreReader reader;
    try {
      reader = PalDB.createReader(dbFile);
    } catch (Exception e) {
      // The reader throws all underlying IOExceptions as unchecked exceptions, so we undo this,
      // providing checked exceptions from the cause if it is an IOException.
      Throwables.propagateIfInstanceOf(e.getCause(), IOException.class);
      // Just throw as-is if it wasn't IOException
      throw Throwables.propagate(e);
    }
    return new PalDBKeyValueSource(reader);
  }

  @Nonnull
  @Override
  public Iterable<Symbol> keys() throws IOException {
    final Iterable<String> keys = reader.keys();
    return FluentIterable.from(keys).transform(SymbolUtils.symbolizeFunction());
  }

  @Override
  @Nonnull
  public Optional<ByteSource> get(final Symbol key) throws IOException {
    final byte[] value;
    try {
      value = reader.getByteArray(key.asString());
    } catch (NotFoundException e) {
      return Optional.absent();
    }
    return Optional.of(ByteSource.wrap(value));
  }

  @Override
  public void close() throws IOException {
    try {
      reader.close();
    } catch (Exception e) {
      // The reader throws all underlying IOExceptions as unchecked exceptions, so we undo this,
      // providing checked exceptions from the cause if it is an IOException.
      Throwables.propagateIfInstanceOf(e.getCause(), IOException.class);
      // Just throw as-is if it wasn't IOException
      throw Throwables.propagate(e);
    }
  }
}
