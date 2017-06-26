package com.bbn.bue.common.files;

import com.bbn.bue.common.StringUtils;
import com.bbn.bue.common.TextGroupImmutable;
import com.bbn.bue.common.collections.KeyValueSink;
import com.bbn.bue.common.collections.MapUtils;
import com.bbn.bue.common.io.GZIPByteSink;
import com.bbn.bue.common.io.GZIPByteSource;
import com.bbn.bue.common.symbols.Symbol;
import com.bbn.bue.common.symbols.SymbolUtils;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.common.primitives.Ints;

import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static com.bbn.bue.common.StringUtils.startsWith;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.skip;
import static com.google.common.collect.Iterables.transform;
import static java.nio.file.Files.walkFileTree;

@Value.Enclosing
public final class FileUtils {
  private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

  private FileUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * Create the parent directories of the given file, if needed.
   */
  public static void ensureParentDirectoryExists(File f) throws IOException {
    final File parent = f.getParentFile();
    if (parent != null) {
      if (!parent.isDirectory() && !parent.mkdirs()) {
        throw new IOException("Could not create parent directories for " + f.getAbsolutePath());
      }
    }
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
   * Writes the absolutes paths of the given files in iteration order, one-per-line. Each line
   * will end with a Unix newline.
   */
  public static void writeFileList(Iterable<File> files, CharSink sink) throws IOException {
    writeUnixLines(FluentIterable.from(files)
        .transform(toAbsolutePathFunction()), sink);
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
    checkNotNull(f);
    checkNotNull(newExtension);
    Preconditions.checkArgument(!f.isDirectory());

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

  /**
   * Derives one {@link File} from another by adding the provided extension.
   * The extension will be separated from the base file name by a ".".
   */
  public static File addExtension(final File f, final String extension) {
    checkNotNull(f);
    checkNotNull(extension);
    Preconditions.checkArgument(!extension.isEmpty());
    Preconditions.checkArgument(!f.isDirectory());

    final String absolutePath = f.getAbsolutePath();
    return new File(absolutePath + "." + extension);
  }

  /**
   * @deprecated Prefer {@link CharSink#writeLines(Iterable, String)} or {@link
   * FileUtils#writeUnixLines(Iterable, CharSink)}.
   */
  @Deprecated
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
    return loadMap(source, SymbolUtils.symbolizeFunction(), FileFunction.INSTANCE);
  }

  public static ImmutableListMultimap<Symbol, File> loadSymbolToFileListMultimap(
      final CharSource source) throws IOException {
    return loadMultimap(source, SymbolUtils.symbolizeFunction(), FileFunction.INSTANCE);
  }

  /**
   * Writes a map from symbols to file absolute paths to a file. Each line has a mapping with the key and value
   * separated by a single tab.  The file will have a trailing newline.
   */
  public static void writeSymbolToFileMap(Map<Symbol, File> symbolToFileMap, CharSink sink) throws IOException {
    writeSymbolToFileEntries(symbolToFileMap.entrySet(), sink);
  }

  private static final Function<Map.Entry<Symbol, String>, String>
      TO_TAB_SEPARATED_ENTRY = MapUtils.toStringWithKeyValueSeparator("\t");

  /**
   * Writes map entries from symbols to file absolute paths to a file. Each line has a mapping with
   * the key and value separated by a single tab.  The file will have a trailing newline.  Note that
   * the same "key" may appear in the file with multiple mappings.
   */
  public static void writeSymbolToFileEntries(final Iterable<Map.Entry<Symbol, File>> entries,
      final CharSink sink) throws IOException {

    writeUnixLines(
        transform(
            MapUtils.transformValues(entries, toAbsolutePathFunction()),
            TO_TAB_SEPARATED_ENTRY),
        sink);
  }

  public static Map<Symbol, CharSource> loadSymbolToFileCharSourceMap(CharSource source)
      throws IOException {
    return Maps.transformValues(loadSymbolToFileMap(source),
        FileUtils.asUTF8CharSourceFunction());
  }

  /**
   * Deserializes an {@link Map} from a {@link File}, where each line is a key, a tab character
   * ("\t"), and a value. Lines beginning with "#" are ignored.
   */
  public static Map<String, File> loadStringToFileMap(final File f) throws IOException {
    return loadStringToFileMap(Files.asCharSource(f, Charsets.UTF_8));
  }

  /**
   * Deserializes a {@link Map} from a {@link CharSource}, where each line is a key, a tab character ("\t"), and a value. Lines beginning with "#" are ignored.
   */
  public static Map<String, File> loadStringToFileMap(final CharSource source) throws IOException {
    return loadMap(source, Functions.<String>identity(), FileFunction.INSTANCE);
  }

  /**
   * Deserializes an {@link ImmutableListMultimap} from a {@link CharSource}, where each line is a key, a tab character ("\t"), and a value. Lines beginning with "#" are ignored.
   */
  public static ImmutableListMultimap<String, File> loadStringToFileListMultimap(
      final CharSource source) throws IOException {
    return loadMultimap(source, Functions.<String>identity(), FileFunction.INSTANCE);
  }

  /**
   * Deserializes an {@link ImmutableMap} from a {@link CharSource}, where each line is a key, a tab character ("\t"), and a value. Lines beginning with "#" are ignored.
   */
  public static <K, V> ImmutableMap<K, V> loadMap(final CharSource source,
      final Function<String, K> keyFunction, final Function<String, V> valueFunction)
      throws IOException {
    final ImmutableMap.Builder<K, V> ret = ImmutableMap.builder();
    loadMapToSink(source, MapUtils.asMapSink(ret), keyFunction, valueFunction);
    return ret.build();
  }

  /**
   * Deserializes an {@link ImmutableMap} from a {@link File}, where each line is a key, a tab character ("\t"), and a value. Lines beginning with "#" are ignored.
   */
  public static <K, V> ImmutableMap<K, V> loadMap(final File file,
      final Function<String, K> keyFunction, final Function<String, V> valueFunction)
      throws IOException {
    return loadMap(Files.asCharSource(file, Charsets.UTF_8), keyFunction, valueFunction);
  }

  /**
   * Deserializes a map from a {@link CharSource}, where each line is a key, a tab character ("\t"),
   * and a value. Lines beginning with "#" are ignored.
   */
  public static <K, V> ImmutableListMultimap<K, V> loadMultimap(final CharSource source,
      final Function<String, K> keyFunction, final Function<String, V> valueFunction)
      throws IOException {
    final ImmutableListMultimap.Builder<K, V> ret = ImmutableListMultimap.builder();
    loadMapToSink(source, MapUtils.asMapSink(ret), keyFunction, valueFunction);
    return ret.build();
  }

  /**
   * Deserializes an {@link ImmutableListMultimap} from a {@link File}, where each line is a key, a tab character ("\t"), and a value. Lines beginning with "#" are ignored.
   */
  public static <K, V> ImmutableListMultimap<K, V> loadMultimap(final File file,
      final Function<String, K> keyFunction, final Function<String, V> valueFunction)
      throws IOException {
    return loadMultimap(Files.asCharSource(file, Charsets.UTF_8), keyFunction, valueFunction);
  }

  private static <K, V> void loadMapToSink(final CharSource source,
      final KeyValueSink<K, V> mapSink, final Function<String, K> keyFunction,
      final Function<String, V> valueFunction)
      throws IOException {
    // Using a LineProcessor saves memory by not loading the whole file into memory. This can matter
    // for multi-gigabyte Gigaword-scale maps.
    // see issue #69 for modifying the input lines on the fly.
    final MapLineProcessor<K, V> processor =
        new MapLineProcessor<>(mapSink, keyFunction, valueFunction, Splitter.on("\t").trimResults());
    source.readLines(processor);
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
    new BackupRequest.Builder()
        .fileToBackup(f)
        .build().doBackup();
  }

  public static void backup(final File f, final String extension) throws IOException {
    new BackupRequest.Builder()
        .fileToBackup(f)
        .extension(extension)
        .build().doBackup();;
  }

  /**
   * A request to backup a file. This request is executed by calling {@link #doBackup()}.
   */
  @TextGroupImmutable
  @Value.Immutable
  public static abstract class BackupRequest {
    public abstract File fileToBackup();

    /**
     * The name of the type of object being backed up (e.g. "geonames database").  If this is
     * provided, a message is logged.
     */
    public abstract Optional<String> nameOfThingToBackup();

    /**
     * The logger to write a log message to. If not specified, defaults to the logger of
     * {@link FileUtils}
     */
    @Value.Default
    public Logger logger() {
      return FileUtils.log;
    }

    /**
     * The extension to append to the backup file.  A "." is automatically inserted.  Defaults to "bak"
     */
    @Value.Default
    public String extension() {
      return "bak";
    }

    /**
     * Whether to delete the file being backed up.
     */
    @Value.Default
    public boolean deleteOriginal() {
      return false;
    }

    @Value.Check
    protected void check() {
      checkArgument(!extension().isEmpty(), "Backup extension may not be empty");
    }

    /**
     * Execute the backup request.
     */
    public final void doBackup() throws IOException {
      if (fileToBackup().isFile()) {
        final File backupFile = addExtension(fileToBackup(), extension());
        final String operationMessage;
        if (deleteOriginal()) {
          operationMessage = "Moved";
          java.nio.file.Files.move(fileToBackup().toPath(),
                  backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else {
          operationMessage = "Copied";
          java.nio.file.Files.copy(fileToBackup().toPath(),
              backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        if (nameOfThingToBackup().isPresent()) {
          logger().info("{} existing {} from {} to {}", operationMessage, nameOfThingToBackup().get(),
              fileToBackup().getAbsolutePath(), backupFile.getAbsolutePath());
        }
      }
    }

    public static class Builder extends ImmutableFileUtils.BackupRequest.Builder {}
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
    return new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8));
  }

  public static ImmutableList<Symbol> loadSymbolList(final File symbolListFile) throws IOException {
    return loadSymbolList(Files.asCharSource(symbolListFile, Charsets.UTF_8));
  }

  /**
   * Loads a list of {@link Symbol}s from a file, one-per-line, skipping lines starting with "#" as
   * comments.
   */
  public static ImmutableList<Symbol> loadSymbolList(final CharSource source) throws IOException {
    return SymbolUtils.listFrom(loadStringList(source));
  }

  /**
   * @deprecated Prefer {@link #toNameFunction()}
   */
  @Deprecated
  public static final Function<File, String> ToName = ToNameEnum.INSTANCE;

  public static Function<File, String> toNameFunction() {
    return ToNameEnum.INSTANCE;
  }

  private enum ToNameEnum implements Function<File, String> {
    INSTANCE;

    @Override
    public String apply(final File f) {
      return f.getName();
    }
  }


  public static final Function<File, String> toAbsolutePathFunction() {
    return ToAbsolutePathFunction.INSTANCE;
  }

  private enum ToAbsolutePathFunction implements Function<File, String> {
    INSTANCE;

    @Override
    public String apply(final File input) {
      return input.getAbsolutePath();
    }
  }

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
    return loadStringMap(source, false);
  }

  /**
   * Like {@link #loadStringMap(CharSource)}, but differs in that lines can contain only
   * a key and no value and will be treated as having a value of empty string in the resulting
   * map. This is useful for specifying absent values for a specific key.
   *
   * @see FileUtils#loadStringMap(CharSource)
   */
  public static ImmutableMap<String, String> loadStringMapAllowingEmptyValues(CharSource source)
      throws IOException {
    return loadStringMap(source, true);
  }

  private static ImmutableMap<String, String> loadStringMap(CharSource source,
      final boolean allowEmptyValues) throws IOException {
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
      } else if (allowEmptyValues && parts.size() == 1) {
        ret.put(parts.get(0), "");
      } else {
        throw new RuntimeException(
            "When reading a map from " + source + ", line " + count + " is invalid: "
                + line);
      }
    }

    return ret.build();
  }

  /**
   * Loads a file in the format {@code key value1} (tab-separated) into a {@link
   * com.google.common.collect.ImmutableMap} of {@link String}s. Each key should only appear on one
   * line, and there should be no duplicate values. Each key and value has whitespace trimmed off.
   * Skips empty lines and allows comment-lines with {@code #} in the first position.
   */
  public static ImmutableMap<Symbol, Symbol> loadSymbolMap(CharSource source) throws IOException {
    final ImmutableMap.Builder<Symbol, Symbol> ret = ImmutableMap.builder();

    for (ImmutableMap.Entry<String, String> row : loadStringMap(source).entrySet()) {
      ret.put(Symbol.from(row.getKey()), Symbol.from(row.getValue()));
    }

    return ret.build();
  }

  public static void writeSymbolMultimap(Multimap<Symbol, Symbol> mm, CharSink charSink)
      throws IOException {
    final Joiner tabJoiner = Joiner.on('\t');
    writeUnixLines(transform(mm.asMap().entrySet(),
        new Function<Map.Entry<Symbol, Collection<Symbol>>, String>() {
          @Override
          public String apply(Map.Entry<Symbol, Collection<Symbol>> input) {
            return input.getKey() + "\t" + tabJoiner.join(input.getValue());
          }
        }), charSink);
  }


  public static ImmutableTable<Symbol, Symbol, Symbol> loadSymbolTable(CharSource input)
      throws IOException {
    final ImmutableTable.Builder<Symbol, Symbol, Symbol> ret = ImmutableTable.builder();

    int lineNo = 0;
    for (final String line : input.readLines()) {
      final List<String> parts = StringUtils.onTabs().splitToList(line);
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

  private enum AsUTF8CharSource implements Function<File, CharSource> {
    INSTANCE;

    @Override
    public CharSource apply(File f) {
      return Files.asCharSource(f, Charsets.UTF_8);
    }
  }

  /**
   * Transforms a file to a {@link com.google.common.io.CharSource} with UTF-8 encoding.
   */
  public static Function<File, CharSource> asUTF8CharSourceFunction() {
    return AsUTF8CharSource.INSTANCE;
  }

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
   * Loads a list of {@link Symbol}s from a file, one-per-line, skipping lines starting with "#"
   * as comments.
   */
  public static ImmutableSet<Symbol> loadSymbolSet(final CharSource source) throws IOException {
    return ImmutableSet.copyOf(loadSymbolList(source));
  }

  /**
   * Returns a {@link List} consisting of the lines of the provided {@link CharSource} in the
   * order given.
   */
  public static ImmutableList<String> loadStringList(final CharSource source) throws IOException {
    return FluentIterable.from(source.readLines())
        .filter(not(startsWith("#")))
        .toList();
  }

  /**
   * Loads a list of {@link String}s from a file, one-per-line, skipping lines starting with "#"
   * as comments.
   */
  public static ImmutableSet<String> loadStringSet(final CharSource source) throws IOException {
    return ImmutableSet.copyOf(loadStringList(source));
  }

  /**
   * Recursively delete this directory and all its contents.
   */
  public static void recursivelyDeleteDirectory(File directory) throws IOException {
    if (!directory.exists()) {
      return;
    }
    checkArgument(directory.isDirectory(), "Cannot recursively delete a non-directory");
    walkFileTree(directory.toPath(), new DeletionFileVisitor());
  }

  private static class DeletionFileVisitor implements FileVisitor<Path> {

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
        throws IOException {
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
        throws IOException {
      java.nio.file.Files.delete(file);
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file, final IOException exc)
        throws IOException {
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(final Path dir, final IOException exc)
        throws IOException {
      java.nio.file.Files.delete(dir);
      return FileVisitResult.CONTINUE;
    }
  }

  /**
   * Calls {@link #recursivelyDeleteDirectory(File)} on JVM exit.
   */
  public static void recursivelyDeleteDirectoryOnExit(final File directory) {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        try {
          recursivelyDeleteDirectory(directory);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  /**
   * Recursively copies a directory.
   *
   * @param sourceDir  the source directory
   * @param destDir    the destination directory, which does not need to already exist
   * @param copyOption options to be used for copying files
   */
  public static void recursivelyCopyDirectory(final File sourceDir, final File destDir,
      final StandardCopyOption copyOption)
      throws IOException {
    checkNotNull(sourceDir);
    checkNotNull(destDir);
    checkArgument(sourceDir.isDirectory(), "Source directory does not exist");
    destDir.mkdirs();
    walkFileTree(sourceDir.toPath(), new CopyFileVisitor(sourceDir.toPath(), destDir.toPath(),
        copyOption));
  }

  private static class CopyFileVisitor implements FileVisitor<Path> {

    private final Path sourcePath;
    private final Path destPath;
    private final StandardCopyOption copyOption;

    private CopyFileVisitor(Path sourcePath, Path destPath, final StandardCopyOption copyOption) {
      this.sourcePath = checkNotNull(sourcePath);
      this.destPath = checkNotNull(destPath);
      this.copyOption = checkNotNull(copyOption);
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
        throws IOException {
      final Path newPath = destPath.resolve(sourcePath.relativize(dir));
      if (!java.nio.file.Files.exists(newPath)) {
        java.nio.file.Files.createDirectory(newPath);
      }
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
        throws IOException {
      final Path newPath = destPath.resolve(sourcePath.relativize(file));
      java.nio.file.Files.copy(file, newPath, copyOption);
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file, final IOException exc)
        throws IOException {
      return FileVisitResult.TERMINATE;
    }

    @Override
    public FileVisitResult postVisitDirectory(final Path dir, final IOException exc)
        throws IOException {
      return FileVisitResult.CONTINUE;
    }
  }

  /**
   * @deprecated See {@link #toAbsolutePathFunction()}.
   */
  @Deprecated
  public static final Function<File, String> ToAbsolutePath = new Function<File, String>() {
    @Override
    public String apply(final File f) {
      return f.getAbsolutePath();
    }
  };

  /**
   * Generally we want to avoid {@link CharSink#writeLines(Iterable)} because it uses the OS default
   * line separator, but our code always works with Unix line endings regardless of platform. This
   * is just like {@link CharSink#writeLines(Iterable)}, but always uses Unix endings.
   */
  public static void writeUnixLines(Iterable<? extends CharSequence> lines, CharSink sink)
      throws IOException {
    sink.writeLines(lines, "\n");
  }

  /**
   * Creates a {@link File} from a {@link String} using the {@link File} constructor.
   */
  public Function<String, File> asFileFunction() {
    return FileFunction.INSTANCE;
  }

  private enum FileFunction implements Function<String, File> {
    INSTANCE;

    @Override
    public File apply(final String input) {
      return new File(checkNotNull(input));
    }
  }

  private static class MapLineProcessor<K, V> implements LineProcessor<Void> {

    private int lineNo;
    private final KeyValueSink<K, V> mapSink;
    private final Function<String, K> keyFunction;
    private final Function<String, V> valueFunction;
    private final Splitter splitter;

    private MapLineProcessor(final KeyValueSink<K, V> mapSink,
        final Function<String, K> keyFunction, final Function<String, V> valueFunction,
        final Splitter splitter) {
      this.mapSink = checkNotNull(mapSink);
      this.keyFunction = checkNotNull(keyFunction);
      this.valueFunction = checkNotNull(valueFunction);
      this.splitter = checkNotNull(splitter);
    }

    @Override
    public boolean processLine(final String line) throws IOException {
      ++lineNo;
      // see issue #69 for modifying the skipped lines.
      if (line.isEmpty() || line.startsWith("#")) {
        // Skip this line and go to the next one
        return true;
      }

      final Iterator<String> parts = splitter.split(line).iterator();

      final String key;
      final String value;
      boolean good = true;

      if (parts.hasNext()) {
        key = parts.next();
      } else {
        key = null;
        good = false;
      }

      if (parts.hasNext()) {
        value = parts.next();
      } else {
        value = null;
        good = false;
      }

      if (!good || parts.hasNext()) {
        throw new RuntimeException(String.format("Corrupt line #%d: %s", lineNo, line));
      }

      try {
        mapSink.put(keyFunction.apply(key), valueFunction.apply(value));
      } catch (IllegalArgumentException iae) {
        throw new IOException(String.format("Error processing line %d of file map: %s",
            lineNo, line), iae);
      }
      // all lines should be processed
      return true;
    }

    @Override
    public Void getResult() {
      // We don't produce a result; we just write to mapSink as a side-effect
      return null;
    }
  }
}
