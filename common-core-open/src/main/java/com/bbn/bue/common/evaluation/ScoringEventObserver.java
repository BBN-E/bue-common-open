package com.bbn.bue.common.evaluation;

import java.io.File;
import java.io.IOException;

/**
 * An object which can record scoring events of objects where the expected (gold) item is of type {@link KeyT} and the
 * predicted (system) item is of type {@link TestT}.
 *
 * {@link #finish(File)} is expected to be called when there are no more items to inspect. What happens if more items
 * are observed after finishing is not defined by the interface contract.
 *
 * An implementation is not required to take action upon every observation; for example, an F-score related observer
 * would take no action upon observation of a true negative.
 */
public interface ScoringEventObserver<KeyT, TestT> {

  void observeTruePositive(KeyT gold, TestT predicted, double score);

  void observeTrueNegative(KeyT gold, TestT predicted, double score);

  void observeFalsePositive(TestT predicted, double score);

  void observeFalseNegative(KeyT gold, double score);

  void finish(File outputDirectory) throws IOException;
}
