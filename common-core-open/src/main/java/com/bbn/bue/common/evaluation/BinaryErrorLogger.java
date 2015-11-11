package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.HasDocID;
import com.bbn.bue.common.Inspector;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.annotations.Beta;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An inspector which logs to a file the binary confusion matrix for an alignment. If an item is
 * aligned, it is counted as PRESENT/PRESENT. If it appears in the key alignment only, it is
 * PRESENT/ABSENT. If it appears in the test alignment onlt, it is ABSENT/PRESENT.  This will be
 * written to a file "DOCID/confusionMatrix.txt" in the specified output directory, where DOCID is
 * extracted from the aligned input items. All aligned items must share the same document ID. If the
 * input alignment is empty, no file is written.
 */
@Beta
public final class BinaryErrorLogger<KeyT extends HasDocID, TestT extends HasDocID> implements
    Inspector<Alignment<? extends KeyT, ? extends TestT>> {

  private final File outputDirectory;
  private final Function<? super KeyT, String> keyRenderer;
  private final Function<? super TestT, String> testRenderer;

  private static final Symbol PRESENT = Symbol.from("PRESENT");
  private static final Symbol ABSENT = Symbol.from("ABSENT");

  private BinaryErrorLogger(final File outputDirectory,
      final Function<? super KeyT, String> keyRenderer,
      final Function<? super TestT, String> testRenderer) {
    this.outputDirectory = checkNotNull(outputDirectory);
    this.keyRenderer = checkNotNull(keyRenderer);
    this.testRenderer = checkNotNull(testRenderer);
  }

  @Override
  public void inspect(final Alignment<? extends KeyT, ? extends TestT> alignment) {
    final ProvenancedConfusionMatrix.Builder<String> confusionMatrixBuilder =
        ProvenancedConfusionMatrix.builder();

    // true positives
    for (final TestT testItem : alignment.rightAligned()) {
      confusionMatrixBuilder.recordPredictedGold(PRESENT, PRESENT,
          keyRenderer.apply(Iterables.getFirst(alignment.alignedToRightItem(testItem), null)));
    }

    // false positives
    for (final TestT testItem : alignment.rightUnaligned()) {
      confusionMatrixBuilder.recordPredictedGold(PRESENT, ABSENT,
          testRenderer.apply(testItem));
    }

    // false negatives
    for (final KeyT keyItem : alignment.leftUnaligned()) {
      confusionMatrixBuilder.record(ABSENT, PRESENT, keyRenderer.apply(keyItem));
    }

    try {
      final Optional<Symbol> docID = searchForDocID(alignment);
      if (docID.isPresent()) {
        final File docDir =
            new File(outputDirectory, docID.get().asString());
        docDir.mkdir();
        Files.asCharSink(new File(docDir, "confusionMatrix.txt"), Charsets.UTF_8).write(
            confusionMatrixBuilder.build().prettyPrintWithFillerOrdering(Ordering.usingToString()));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Optional<Symbol> searchForDocID(
      final Alignment<? extends KeyT, ? extends TestT> alignment) {
    Symbol docID = null;
    for (final HasDocID hasDocID : Iterables
        .concat(alignment.leftAligned(), alignment.leftUnaligned(), alignment.rightAligned(),
            alignment.rightUnaligned())) {
      if (docID != null && !docID.equalTo(hasDocID.docID())) {
        throw new RuntimeException("Illegal mix of doc IDs in alignment");
      }
      docID = hasDocID.docID();
    }
    return Optional.fromNullable(docID);
  }

  @Override
  public void finish() throws IOException {
    // do nothing, this does not aggregate
  }

  /**
   * Creates a {@code BinaryErrorLogger}.  The {@code stringifier} is a means of rendering the
   * aligned items as strings.  If you don't care, choose {@link Functions#toStringFunction()}.
   */
  public static <ItemT extends HasDocID> BinaryErrorLogger<ItemT, ItemT> forStringifierAndOutputDir(
      Function<? super ItemT, String> stringifier, File outputDirectory) {
    outputDirectory.mkdirs();
    return new BinaryErrorLogger<ItemT, ItemT>(outputDirectory, stringifier, stringifier);
  }
}
