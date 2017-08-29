package com.bbn.bue.common.files;

import com.bbn.bue.common.parameters.Parameters;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Given a directory, writes tab-separated ID-to-file map for all files beneath that directory
 * (recursively), where the filename is the ID.  This will fail if duplicate IDs are generated.
 * This typically will not happen with LDC-sourced text documents, which is our primary use case.
 *
 * @author Ryan Gabbard
 */
public final class FileMapFromFilenames {

  private static final Logger log = LoggerFactory.getLogger(FileMapFromFilenames.class);

  private FileMapFromFilenames() {
    throw new UnsupportedOperationException();
  }

  public static void main(String[] argv) {
    // we wrap the main method in this way to
    // ensure a non-zero return value on failure
    try {
      trueMain(argv);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void trueMain(String[] argv) throws IOException {
    final Parameters params = Parameters.loadSerifStyle(new File(argv[0]));
    final File inputDirectory = params.getExistingDirectory("com.bbn.fileMapper.inputDirectory");
    final File outputMapFile = params.getCreatableFile("com.bbn.fileMapper.outputMapFile");

    final ImmutableMap.Builder<Symbol, File> mapB = ImmutableMap.builder();

    Files.walkFileTree(inputDirectory.toPath(), new FileMapFromFilenamesVisitor(mapB));

    final ImmutableMap<Symbol, File> ret = mapB.build();
    FileUtils.writeSymbolToFileMap(ret, com.google.common.io.Files.asCharSink(outputMapFile,
        Charsets.UTF_8));
    log.info("Mapped {} files to {}", ret.size(), outputMapFile);
  }

  public static class FileMapFromFilenamesVisitor implements FileVisitor<Path> {

    private final ImmutableMap.Builder<Symbol, File> mapB;

    public FileMapFromFilenamesVisitor(final ImmutableMap.Builder<Symbol, File> mapB) {
      this.mapB = checkNotNull(mapB);
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
        throws IOException {
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
        throws IOException {
      mapB.put(Symbol.from(file.toFile().getName()), file.toFile());
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file, final IOException exc)
        throws IOException {
      throw exc;
    }

    @Override
    public FileVisitResult postVisitDirectory(final Path dir, final IOException exc)
        throws IOException {
      return FileVisitResult.CONTINUE;
    }
  }
}
