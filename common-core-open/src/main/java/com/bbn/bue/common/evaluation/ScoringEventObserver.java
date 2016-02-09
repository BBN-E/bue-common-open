package com.bbn.bue.common.evaluation;

import java.io.File;
import java.io.IOException;

/**
 * An object which records scoring events of objects where the expected (gold) item is of type {@link KeyT} and the
 * predicted (system) item is of type {@link TestT}.
 *
 * {@link #finish(File)} is expected to be called when there are no more items to inspect. What happens if more items
 * are observed after finishing is not defined by the interface contract.
 *
 * An implementation is not required to take action upon every observation; for example, an F-score related observer
 * would take no action upon observation of a true negative. The author of an implementation may choose to raise an
 * exception if the implementation is asked to observe an event they cannot meaningfully observe.
 *
 * @author Constantine Lignos
 */
public interface ScoringEventObserver<KeyT, TestT> {

  /**
   * Records a true positive event.
   */
  void observeTruePositive(KeyT gold, TestT predicted, double score);

  /**
   * Records a true negative event.
   */
  void observeTrueNegative(KeyT gold, TestT predicted, double score);

  /**
   * Records a false positive event.
   */
  void observeFalsePositive(TestT predicted, double score);

  /**
   * Records a false negative event.
   */
  void observeFalseNegative(KeyT gold, double score);

  /**
   * Performs any final tasks, using the specified output directory as the base for output.
   */
  void finish(File outputDirectory) throws IOException;
}
