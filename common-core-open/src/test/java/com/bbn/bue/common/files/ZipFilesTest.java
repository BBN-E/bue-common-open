package com.bbn.bue.common.files;

import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Tests {@link ZipFiles}.
 */
public class ZipFilesTest {

  private static final byte[] testDocument = "Hello world!".getBytes(Charsets.UTF_8);
  private static final Symbol docId = Symbol.from("TEST_DOCUMENT");

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  private ZipFile zipFile;
  private ZipEntry entry;

  @Before
  public void setUp() throws IOException {
    zipFile =
        new ZipFile(Resources.getResource(ZipFilesTest.class, "test_zip_source.zip").getFile());
    entry = zipFile.getEntry("docs/2016-09-20-01.txt");
  }

  @Test
  public void entryAsByteSource() throws IOException {
    assertEquals("Hello world.\n",
        ZipFiles.entryAsByteSource(zipFile, entry).asCharSource(Charsets.UTF_8).read());
  }

  @Test
  public void entryAsString() throws IOException {
    assertEquals("Hello world.\n",
        ZipFiles.entryAsString(zipFile, entry, Charsets.UTF_8));
  }

  @Test
  public void testKeyValueSource() throws IOException {
    final Function<String, Symbol> docIdExtractor = new Function<String, Symbol>() {
      @Override
      public Symbol apply(final String input) {
        return Symbol.from(input.substring(input.lastIndexOf('/') + 1).replaceAll("\\.txt$", ""));
      }
    };
    final ImmutableKeyValueSource<Symbol, ByteSource> source =
        KeyValueSources.fromZip(zipFile, docIdExtractor);
    assertEquals(ImmutableSet.of(Symbol.from("2016-09-20-01")), source.keySet());
  }

  @Test
  public void testKeyValueSinkDefault() throws IOException {
    final File zipFile = folder.newFile("test.zip");
    final KeyValueSink<Symbol, byte[]> sink = KeyValueSinks.forZip(zipFile);

    // Write document
    sink.put(docId, testDocument);
    sink.close();

    // Read document
    final ImmutableKeyValueSource<Symbol, ByteSource> source =
        KeyValueSources.fromZip(new ZipFile(zipFile));
    final byte[] value = source.getRequired(docId).read();
    assertArrayEquals(testDocument, value);
    assertEquals(ImmutableSet.of(docId), source.keySet());
  }

  @Test
  public void testKeyValueSinkFunction() throws IOException {
    final Function<Symbol, String> keyToEntryFunction = new Function<Symbol, String>() {
      @Nonnull
      @Override
      public String apply(final Symbol input) {
        checkNotNull(input);
        return "files/" + input.asString() + ".txt";
      }
    };
    final Function<String, Symbol> entryToKeyFunction = new Function<String, Symbol>() {
      @Nonnull
      @Override
      public Symbol apply(final String input) {
        final String filename = input.substring(input.lastIndexOf('/') + 1);
        return Symbol.from(filename.replaceAll(".txt$", ""));
      }
    };

    final File zipFile = folder.newFile("test.zip");
    final KeyValueSink<Symbol, byte[]> sink = KeyValueSinks.forZip(zipFile, keyToEntryFunction);

    // Write document
    sink.put(docId, testDocument);
    sink.close();

    // Read document
    final ImmutableKeyValueSource<Symbol, ByteSource> source =
        KeyValueSources.fromZip(new ZipFile(zipFile), entryToKeyFunction);
    final byte[] value = source.getRequired(docId).read();
    assertArrayEquals(testDocument, value);
    assertEquals(ImmutableSet.of(docId), source.keySet());
  }
}
