package com.bbn.bue.common.files;

import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the PalDB key-value source and sink.
 */
public final class PalDBKeyValueTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  /**
   * Tests basic source and sink operations.
   */
  @Test
  public void testPutAndGet() throws IOException {
    final Symbol key = Symbol.from("foo");
    final byte[] value = "bar".getBytes(Charsets.UTF_8);
    final File dbFile = createTestDB(key, value);
    final ImmutableKeyValueSource<Symbol, ByteSource> source =
        KeyValueSources.fromPalDB(dbFile);

    // Test get
    assertArrayEquals(value, source.getRequired(key).read());
    assertTrue(source.get(key).isPresent());
    assertFalse(source.get(Symbol.from("baz")).isPresent());

    // Test keys
    assertEquals(ImmutableSet.of(key), source.keySet());
    assertEquals(ImmutableSet.of(key), ImmutableSet.copyOf(source.keys()));
  }

  /**
   * Tests that we successfully are mapping the PalDB unchecked exceptions back to IOException when
   * appropriate.
   */
  @Test(expected = IOException.class)
  public void testIOException() throws IOException {
    final File dbFile = folder.newFile("test.db");
    KeyValueSources.fromPalDB(dbFile);
  }

  private File createTestDB(final Symbol key, final byte[] value) throws IOException {
    final File dbFile = folder.newFile("test.db");
    try (final KeyValueSink<Symbol, byte[]> sink =
             KeyValueSinks.forPalDB(dbFile, false)) {
      sink.put(key, value);
    }
    return dbFile;
  }
}
