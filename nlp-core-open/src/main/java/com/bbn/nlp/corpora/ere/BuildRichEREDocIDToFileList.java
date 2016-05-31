package com.bbn.nlp.corpora.ere;

import com.bbn.bue.common.files.FileUtils;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public final class BuildRichEREDocIDToFileList {

  private static final Logger log = LoggerFactory.getLogger(BuildRichEREDocIDToFileList.class);

  private BuildRichEREDocIDToFileList() {
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
    final ImmutableList<File> ereFiles = FileUtils.loadFileList(new File(argv[0]));
    final File outputFile = new File(argv[1]);
    outputFile.getParentFile().mkdirs();

    final ImmutableMap.Builder<Symbol, File> docIdToFileMapB = ImmutableMap.builder();
    final ERELoader loader = ERELoader.builder().prefixDocIDToAllIDs(false).build();
    for (final File ereFile : ereFiles) {
      docIdToFileMapB.put(Symbol.from(loader.loadFrom(ereFile).getDocId()), ereFile);
    }
    final ImmutableMap<Symbol, File> docIdToFileMap = docIdToFileMapB.build();
    FileUtils.writeSymbolToFileMap(docIdToFileMap, Files.asCharSink(outputFile, Charsets.UTF_8));
    log.info("Wrote {} mappings to {}", docIdToFileMap.size(), outputFile);
  }
}
