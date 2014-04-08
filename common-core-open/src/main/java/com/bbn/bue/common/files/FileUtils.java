package com.bbn.bue.common.files;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.bbn.bue.common.collections.MapUtils;
import com.bbn.bue.common.symbols.Symbol;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.primitives.Ints;

import static com.bbn.bue.common.collections.MapUtils.copyWithKeysTransformedByInjection;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class FileUtils {
	private FileUtils() { throw new UnsupportedOperationException(); }

	/**
	 * Takes a file with filenames listed one per line and returns a list of
	 * the corresponding File objects.  Ignores blank lines and lines with a
	 * "#" in the first column position.
	 *
	 * @param fileList
	 * @return
	 * @throws IOException
	 */
	public static List<File> loadFileList(final File fileList) throws IOException {
		final List<File> ret = Lists.newArrayList();

		for (final String filename : Files.readLines(fileList, Charsets.UTF_8)) {
			if (!filename.isEmpty() && !filename.startsWith("#")) {
				ret.add(new File(filename.trim()));
			}
		}

		return ret;
	}

	/**
	 * Returns another file just like the input but with a different extension.
	 * If the input file has an extension (a suffix beginning with "."),
	 * everything after the . is replaced with newExtension. Otherwise,
	 * a newExtension is appended to the filename and a new File is returned.
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

	public static void writeLines(final File f, final Iterable<String> data, final Charset charSet) throws IOException {
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
		final File f) throws IOException
	{
		return MapUtils.copyWithKeysTransformedByInjection(
                loadStringToFileMap(f), Symbol.FromString);
	}

	public static Map<String, File> loadStringToFileMap(
			final File f) throws IOException
	{
		final Splitter onTab = Splitter.on("\t").trimResults();
		final ImmutableMap.Builder<String,File> ret = ImmutableMap.builder();
        int lineNo = 0;
		for (final String line : Files.readLines(f, Charsets.UTF_8)) {
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
	 * Writes a single integer to the beginning of a file, overwriting what
	 * was there originally but leaving the rest of the file intact.  This is
	 * useful when you are writing a long binary file with a size header, but
	 * don't know how many elements are there until the end.
	 */
	public static  void writeIntegerToStart(final File f, final int num) throws FileNotFoundException, IOException {
		final RandomAccessFile fixupFile = new RandomAccessFile(f, "rw");
		fixupFile.writeInt(num);
		fixupFile.close();
	}

	public static int[] loadBinaryIntArray(final ByteSource inSup,
			final boolean compressed) throws IOException
	{
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
			for (int i=0; i<size; ++i) {
				ret[i] = dis.readInt();
			}
			return ret;
		} finally {
			dis.close();
		}
	}

	public static int[] loadTextIntArray(final File f) throws NumberFormatException, IOException
	{
		final List<Integer> ret = Lists.newArrayList();

		for (final String line : Files.readLines(f, Charsets.UTF_8)) {
			ret.add(Integer.parseInt(line));
		}

		return Ints.toArray(ret);
	}

	public static void writeBinaryIntArray(final int[] arr,
		final ByteSink outSup) throws IOException
	{
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
	 * @param f If f is the filesystem root, a runtime exeption will be thrown.
	 * @param siblingDirName The non-empty name of the sibling directory.
	 * @return
	 */
	public static File siblingDirectory(final File f, final String siblingDirName) {
		checkNotNull(f);
		checkNotNull(siblingDirName);
		checkArgument(!siblingDirName.isEmpty());

		final File parent = f.getParentFile();
		if (parent != null) {
			return new File(parent, siblingDirName);
		} else {
			throw new RuntimeException(String.format("Cannot create sibling directory %s of %s because the latter has no parent.",
				siblingDirName, f));
		}
	}

	public static BufferedReader optionallyCompressedBufferedReader(final File f,
			final boolean compressed) throws IOException
	{
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
		return FluentIterable.from(Files.readLines(symbolListFile, Charsets.UTF_8))
			.transform(Symbol.FromString)
			.toList();
	}

	public static final Function<File, String> ToName = new Function<File, String>() {
		@Override
		public String apply(final File f) { return f.getName(); }
	};


	public static boolean isEmptyDirectory(final File directory) {
		if (directory.exists() && directory.isDirectory()) {
			return directory.listFiles().length == 0;
		}
		return false;
	}

	/**
	 * Make a predicate to test files for ending with the specified suffix.
	 * @param suffix May not be null or empty.
	 * @return
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
}
