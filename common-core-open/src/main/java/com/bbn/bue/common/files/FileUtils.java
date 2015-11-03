package com.bbn.bue.common.files;

import com.bbn.bue.common.StringUtils;
import com.bbn.bue.common.collections.MapUtils;
import com.bbn.bue.common.io.GZIPByteSink;
import com.bbn.bue.common.io.GZIPByteSource;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.primitives.Ints;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.skip;

public final class FileUtils {

  private FileUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * Takes a file with filenames listed one per line and returns a list of the corresponding File
   * objects.  Ignores blank lines and lines with a "#" in the first column position. Treats the
   * file as UTF-8 encoded.
   */
  public static ImmutableList<File> loadFileList(final File fileList) throws IOException {
    return loadFileList(Files.asCharSource(fileList, Charsets.UTF_8));
  }

  /**
   * Takes a {@link com.google.common.io.CharSource} with filenames listed one per line and returns
   * a list of the corresponding File objects.  Ignores blank lines and lines with a "#" in the
   * first column position.
   */
  public static ImmutableList<File> loadFileList(final CharSource source) throws IOException {
    final ImmutableList.Builder<File> ret = ImmutableList.builder();

    for (final String filename : source.readLines()) {
      if (!filename.isEmpty() && !filename.startsWith("#")) {
        ret.add(new File(filename.trim()));
      }
    }

    return ret.build();
  }

  /**
   * takes a List of fileNames and returns a list of files, ignoring any empty entries white space
   * at the end of a name
   */
  public static ImmutableList<File> loadFileList(final Iterable<String> fileNames)
      throws IOException {
    final ImmutableList.Builder<File> ret = ImmutableList.builder();

    for (String filename : fileNames) {
      if (!filename.isEmpty()) {
        ret.add(new File(filename.trim()));
      }
    }

    return ret.build();
  }

  /**
   * Writes the absolutes paths of the given files in iteration order, one-per-line. The currently
   * implementation is just a quick one which forms the string in memory and writes it. If your file
   * list is very large, you should modify it to work line-by-line, being very careful with all the
   * trickiness of file I/O exception handling in Java 6.
   */
  public static void writeFileList(Iterable<File> files, CharSink sink) throws IOException {
    // proper exception handling in JDK 6 is ugly, so we'll let Guava handle it for us
    // at the expense of memory.  If you are writing huge file lists, go ahead and implement
    // the line-by-line version, but it is out-of-scope for the moment
    sink.write(FluentIterable.from(files)
        .transform(ToAbsolutePath)
        .join(StringUtils.NewlineJoiner)
        .concat("\n"));
  }

  /**
   * Like {@link #loadFileList(java.io.File)}, except if the file name ends in ".gz" or ".tgz" it is
   * treated as GZIP compressed. This is often convenient for loading e.g. document lists which
   * benefit from being compressed for large corpora.
   */
  public static ImmutableList<File> loadPossiblyCompressedFileList(File fileList)
      throws IOException {
    final CharSource source;
    if (fileList.getName().endsWith(".gz") || fileList.getName().endsWith(".tgz")) {
      source = GZIPByteSource.fromCompressed(fileList).asCharSource(Charsets.UTF_8);
    } else {
      source = Files.asCharSource(fileList, Charsets.UTF_8);
    }
    return loadFileList(source);
  }

  /**
   * Takes a file with relative pathnames listed one per line and returns a list of the
   * corresponding {@link java.io.File} objects, resolved against the provided base path using the
   * {@link java.io.File#File(java.io.File, String)} constructor. Ignores blank lines and lines with
   * a "#" in the first column position.
   */
  public static ImmutableList<File> loadFileListRelativeTo(File fileList, File basePath)
      throws IOException {
    checkNotNull(basePath);
    final ImmutableList.Builder<File> ret = ImmutableList.builder();

    for (final String filename : Files.readLines(fileList, Charsets.UTF_8)) {
      if (!filename.isEmpty() && !filename.startsWith("#")) {
        ret.add(new File(basePath, filename.trim()));
      }
    }

    return ret.build();
  }

  /**
   * Returns another file just like the input but with a different extension. If the input file has
   * an extension (a suffix beginning with "."), everything after the . is replaced with
   * newExtension. Otherwise, a newExtension is appended to the filename and a new File is returned.
   * Note that unless you want double .s, newExtension should not begin with a .
   */
  public static File swapExtension(final File f, final String newExtension) {
    Preconditions.checkNotNull(f);
    Preconditions.checkNotNull(newExtension);
    Preconditions.checkArgument(f.isFile());

    final String absolutePath = f.getAbsolutePath();
    final int dotIndex = absolutePath.lastIndexOf(".");
    String basePath;

    if (dotIndex >= 0) {
      basePath = absolutePath.substring(0, dotIndex);
    } else {
      basePath = absolutePath;
    }

    return new File(String.format("%s.%s", basePath, newExtension));
  }

  public static File addExtension(final File f, final String extension) {
    Preconditions.checkNotNull(f);
    Preconditions.checkNotNull(extension);
    Preconditions.checkArgument(!extension.isEmpty());
    Preconditions.checkArgument(!f.isDirectory());

    final String absolutePath = f.getAbsolutePath();
    return new File(absolutePath + "." + extension);
  }

  public static void writeLines(final File f, final Iterable<String> data, final Charset charSet)
      throws IOException {
    final FileOutputStream fin = new FileOutputStream(f);
    final BufferedOutputStream bout = new BufferedOutputStream(fin);
    final PrintStream out = new PrintStream(bout);

    boolean threw = true;
    try {
      for (final String s : data) {
        out.println(s);
      }
      threw = false;
    } finally {
      Closeables.close(out, threw);
    }
  }

  public static ImmutableMap<Symbol, File> loadSymbolToFileMap(
      final File f) throws IOException {
    return loadSymbolToFileMap(Files.asCharSource(f, Charsets.UTF_8));
  }

  public static ImmutableMap<Symbol, File> loadSymbolToFileMap(
      final CharSource source) throws IOException {
    return MapUtils.copyWithKeysTransformedByInjection(
        loadStringToFileMap(source), Symbol.FromString);
  }

  public static Map<Symbol, CharSource> loadSymbolToFileCharSourceMap(CharSource source)
      throws IOException {

    return Maps.transformValues(loadSymbolToFileMap(source),
        FileUtils.AsCharSource);
  }

  public static Map<String, File> loadStringToFileMap(final File f) throws IOException {
    return loadStringToFileMap(Files.asCharSource(f, Charsets.UTF_8));
  }


  public static Map<String, File> loadStringToFileMap(
      final CharSource source) throws IOException {
    final Splitter onTab = Splitter.on("\t").trimResults();
    final ImmutableMap.Builder<String, File> ret = ImmutableMap.builder();
    int lineNo = 0;
    for (final String line : source.readLines()) {
      final Iterator<String> parts = onTab.split(line).iterator();

      final String key;
      final File value;
      boolean good = true;

      if (parts.hasNext()) {
        key = parts.next();
      } else {
        key = null;
        good = false;
      }

      if (parts.hasNext()) {
        value = new File(parts.next());
      } else {
        value = null;
        good = false;
      }

      if (!good || parts.hasNext()) {
        throw new RuntimeException(String.format("Corrupt line: %s", line));
      }

      try {
        ret.put(key, value);
      } catch (IllegalArgumentException iae) {
        throw new IOException(String.format("Error processing line %d of file map: %s",
            lineNo, line), iae);
      }
      ++lineNo;
    }
    return ret.build();
  }

  /**
   * Writes a single integer to the beginning of a file, overwriting what was there originally but
   * leaving the rest of the file intact.  This is useful when you are writing a long binary file
   * with a size header, but don't know how many elements are there until the end.
   */
  public static void writeIntegerToStart(final File f, final int num) throws IOException {
    final RandomAccessFile fixupFile = new RandomAccessFile(f, "rw");
    fixupFile.writeInt(num);
    fixupFile.close();
  }

  public static int[] loadBinaryIntArray(final ByteSource inSup,
      final boolean compressed) throws IOException {
    InputStream in = inSup.openStream();
    if (compressed) {
      try {
        in = new GZIPInputStream(in);
      } catch (final IOException e) {
        in.close();
        throw e;
      }
    }
    final DataInputStream dis = new DataInputStream(in);

    try {
      final int size = dis.readInt();
      final int[] ret = new int[size];
      for (int i = 0; i < size; ++i) {
        ret[i] = dis.readInt();
      }
      return ret;
    } finally {
      dis.close();
    }
  }

  public static int[] loadTextIntArray(final File f) throws NumberFormatException, IOException {
    final List<Integer> ret = Lists.newArrayList();

    for (final String line : Files.readLines(f, Charsets.UTF_8)) {
      ret.add(Integer.parseInt(line));
    }

    return Ints.toArray(ret);
  }

  public static void writeBinaryIntArray(final int[] arr,
      final ByteSink outSup) throws IOException {
    final OutputStream out = outSup.openBufferedStream();
    final DataOutputStream dos = new DataOutputStream(out);

    try {
      dos.writeInt(arr.length);
      for (final int x : arr) {
        dos.writeInt(x);
      }
    } finally {
      dos.close();
    }
  }

  public static void backup(final File f) throws IOException {
    backup(f, ".bak");
  }

  public static void backup(final File f, final String extension) throws IOException {
    Files.copy(f, addExtension(f, extension));
  }

  /**
   * Given a file, returns a File representing a sibling directory with the specified name.
   *
   * @param f              If f is the filesystem root, a runtime exeption will be thrown.
   * @param siblingDirName The non-empty name of the sibling directory.
   */
  public static File siblingDirectory(final File f, final String siblingDirName) {
    checkNotNull(f);
    checkNotNull(siblingDirName);
    checkArgument(!siblingDirName.isEmpty());

    final File parent = f.getParentFile();
    if (parent != null) {
      return new File(parent, siblingDirName);
    } else {
      throw new RuntimeException(String
          .format("Cannot create sibling directory %s of %s because the latter has no parent.",
              siblingDirName, f));
    }
  }

  public static BufferedReader optionallyCompressedBufferedReader(final File f,
      final boolean compressed) throws IOException {
    InputStream stream = new BufferedInputStream(new FileInputStream(f));
    if (compressed) {
      try {
        stream = new GZIPInputStream(stream);
      } catch (final IOException e) {
        stream.close();
        throw e;
      }
    }
    return new BufferedReader(new InputStreamReader(stream));
  }

  public static ImmutableList<Symbol> loadSymbolList(final File symbolListFile) throws IOException {
    return loadSymbolList(Files.asCharSource(symbolListFile, Charsets.UTF_8));
  }

  public static ImmutableList<Symbol> loadSymbolList(final CharSource source) throws IOException {
    return FluentIterable.from(source.readLines())
        .transform(Symbol.FromString)
        .toList();
  }

  public static final Function<File, String> ToName = new Function<File, String>() {
    @Override
    public String apply(final File f) {
      return f.getName();
    }
  };

  public static final Function<File, String> ToAbsolutePath = new Function<File, String>() {
    @Override
    public String apply(final File f) {
      return f.getAbsolutePath();
    }
  };

  public static boolean isEmptyDirectory(final File directory) {
    if (directory.exists() && directory.isDirectory()) {
      return directory.listFiles().length == 0;
    }
    return false;
  }

  /**
   * Make a predicate to test files for ending with the specified suffix.
   *
   * @param suffix May not be null or empty.
   */
  public static Predicate<File> EndsWith(final String suffix) {
    checkArgument(!suffix.isEmpty());

    return new Predicate<File>() {
      @Override
      public boolean apply(final File f) {
        return f.getName().endsWith(suffix);
      }
    };
  }

  /**
   * Loads a file in the format {@code key value1 value2 value3} (tab-separated) into a {@link
   * com.google.common.collect.Multimap} of {@code String} to {@code String}. Each key should only
   * appear on one line, and there should be no duplicate values. Each key and value has whitespace
   * trimmed off. Skips empty lines and allows comment-lines with {@code #} in the first position.
   * If a key has no values, it will not show up in the keySet of the returned multimap.
   */
  public static ImmutableMultimap<String, String> loadStringMultimap(CharSource multimapSource)
      throws IOException {
    final ImmutableMultimap.Builder<String, String> ret = ImmutableMultimap.builder();

    int count = 0;
    for (final String line : multimapSource.readLines()) {
      ++count;
      if (line.startsWith("#")) {
        continue;
      }
      final List<String> parts = multimapSplitter.splitToList(line);
      if (parts.isEmpty()) {
        continue;
      }
      ret.putAll(parts.get(0), skip(parts, 1));
    }

    return ret.build();
  }

  /**
   * Deprecated in favor of version with {@link com.google.common.io.CharSource} argument.
   *
   * @deprecated
   */
  @Deprecated
  public static ImmutableMultimap<String, String> loadStringMultimap(File multimapFile)
      throws IOException {
    return loadStringMultimap(Files.asCharSource(multimapFile, Charsets.UTF_8));
  }

  /**
   * Deprecated in favor of the CharSource version to force the user to define their encoding. If
   * you call this, it will use UTF_8 encoding.
   *
   * @deprecated
   */
  @Deprecated
  public static ImmutableMultimap<Symbol, Symbol> loadSymbolMultimap(File multimapFile)
      throws IOException {
    return loadSymbolMultimap(Files.asCharSource(multimapFile, Charsets.UTF_8));
  }

  /**
   * Loads a file in the format {@code key value1 value2 value3} (tab-separated) into a {@link
   * com.google.common.collect.Multimap} of {@link com.bbn.bue.common.symbols.Symbol} to Symbol.
   * Each key should only appear on one line, and there should be no duplicate values. Each key and
   * value has whitespace trimmed off. Skips empty lines and allows comment-lines with {@code #} in
   * the first position. If a key has no values, it will not show up in the keySet of the returned
   * multimap.
   */
  public static ImmutableMultimap<Symbol, Symbol> loadSymbolMultimap(CharSource multimapSource)
      throws IOException {
    final ImmutableMultimap<String, String> stringMM = loadStringMultimap(multimapSource);
    final ImmutableMultimap.Builder<Symbol, Symbol> ret = ImmutableMultimap.builder();

    for (final Map.Entry<String, Collection<String>> entry : stringMM.asMap().entrySet()) {
      ret.putAll(Symbol.from(entry.getKey()),
          Collections2.transform(entry.getValue(), Symbol.FromString));
    }

    return ret.build();
  }

  private static final Splitter MAP_SPLITTER =
      Splitter.on("\t").trimResults().omitEmptyStrings().limit(2);

  /**
   * Loads a file in the format {@code key value1} (tab-separated) into a {@link
   * com.google.common.collect.ImmutableMap} of {@link String}s. Each key should only appear on one
   * line, and there should be no duplicate values. Each key and value has whitespace trimmed off.
   * Skips empty lines and allows comment-lines with {@code #} in the first position.
   */
  public static ImmutableMap<String, String> loadStringMap(CharSource source) throws IOException {
    final ImmutableMap.Builder<String, String> ret = ImmutableMap.builder();

    int count = 0;
    for (final String line : source.readLines()) {
      ++count;
      if (line.startsWith("#")) {
        continue;
      }
      final List<String> parts = MAP_SPLITTER.splitToList(line);
      if (parts.isEmpty()) {
        continue;
      }
      if (parts.size() == 2) {
        ret.put(parts.get(0), parts.get(1));
      } else {
        throw new RuntimeException(
            "When reading a map from " + source + ", line " + count + " is invalid: "
                + line);
      }
    }

    return ret.build();
  }


  public static void writeSymbolMultimap(Multimap<Symbol, Symbol> mm, CharSink charSink)
      throws IOException {
    final Joiner tabJoiner = Joiner.on('\t');
    charSink.writeLines(Iterables.transform(mm.asMap().entrySet(),
        new Function<Map.Entry<Symbol, Collection<Symbol>>, String>() {
          @Override
          public String apply(Map.Entry<Symbol, Collection<Symbol>> input) {
            return input.getKey() + "\t" + tabJoiner.join(input.getValue());
          }
        }));
  }


  public static ImmutableTable<Symbol, Symbol, Symbol> loadSymbolTable(CharSource input)
      throws IOException {
    final ImmutableTable.Builder<Symbol, Symbol, Symbol> ret = ImmutableTable.builder();

    int lineNo = 0;
    for (final String line : input.readLines()) {
      final List<String> parts = StringUtils.OnTabs.splitToList(line);
      if (parts.size() != 3) {
        throw new IOException(String.format("Invalid line %d when reading symbol table: %s",
            lineNo, line));
      }
      ret.put(Symbol.from(parts.get(0)), Symbol.from(parts.get(1)), Symbol.from(parts.get(2)));
      ++lineNo;
    }

    return ret.build();
  }

  private static final Splitter multimapSplitter =
      Splitter.on("\t").trimResults().omitEmptyStrings();

  /**
   * Transforms a file to a {@link com.google.common.io.CharSource} with UTF-8 encoding.
   */
  private static final Function<File, CharSource> AsCharSource = new Function<File, CharSource>() {
    @Override
    public CharSource apply(File f) {
      return Files.asCharSource(f, Charsets.UTF_8);
    }
  };

  /**
   * Throws an {@link java.io.IOException} if the supplied directory either does not exist or is not
   * a directory.
   */
  public static void assertDirectoryExists(File directory) throws IOException {
    if (!directory.isDirectory()) {
      throw new IOException(directory + " does not exist or is not a directory");
    }
  }

  /**
   * Just like {@link Files#asByteSource(java.io.File)}, but decompresses the incoming data using
   * GZIP.
   */
  public static ByteSource asCompressedByteSource(File f) throws IOException {
    return GZIPByteSource.fromCompressed(Files.asByteSource(f));
  }

  /**
   * Just like {@link Files#asByteSink(java.io.File, com.google.common.io.FileWriteMode...)}, but
   * decompresses the incoming data using GZIP.
   */
  public static ByteSink asCompressedByteSink(File f) throws IOException {
    return GZIPByteSink.gzipCompress(Files.asByteSink(f));
  }

  /**
   * Just like {@link Files#asCharSource(java.io.File, java.nio.charset.Charset)}, but decompresses
   * the incoming data using GZIP.
   */
  public static CharSource asCompressedCharSource(File f, Charset charSet) throws IOException {
    return asCompressedByteSource(f).asCharSource(charSet);
  }

  /**
   * Just like {@link Files#asCharSink(java.io.File, java.nio.charset.Charset,
   * com.google.common.io.FileWriteMode...)}, but decompresses the incoming data using GZIP.
   */
  public static CharSink asCompressedCharSink(File f, Charset charSet) throws IOException {
    return asCompressedByteSink(f).asCharSink(charSet);
  }

  // Guava predicates and functions
  public static Predicate<File> isDirectoryPredicate() {
    return new Predicate<File>() {
      @Override
      public boolean apply(final File input) {
        return input.isDirectory();
      }
    };
  }

  /**
   * wraps any IOException and throws a RuntimeException
   */
  public static Function<File, Iterable<String>> toLinesFunction(final Charset charset) {
    return new Function<File, Iterable<String>>() {
      @Override
      public Iterable<String> apply(final File input) {
        try {
          return Files.readLines(input, charset);
        } catch (IOException e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
      }
    };
  }

  /**
   * Returns a set consisting of the lines of the provided {@link CharSource}.
   */
  public static ImmutableSet<Symbol> loadSymbolSet(final CharSource source) throws IOException {
    return ImmutableSet.copyOf(loadSymbolList(source));
  }
}
