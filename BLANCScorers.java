package com.bbn.nlp.coreference.measures;

/**
 * Utility and creation methods associated with BLANC scorers.
 */
public final class BLANCScorers {

  private BLANCScorers() {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a BLANC scorer which requires exactly the same items on both sides and which does not
   * allow items to be members of multiple clusters.
   */
  public static BLANCScorer getStandardBLANCScorer() {
    return new StandardBLANCScorer(false);
  }

  public static BLANCScorer getStandardBLANCScorerWithSelfEdges() {
    return new StandardBLANCScorer(true);
  }

  /**
   * Gets a BLANC scorer which handles item mismatches between the predicted and gold clusterings
   * and which allows items to be members of multiple clusters.
   */
  public static BLANCScorer getMultiBLANCScorer() {
    return new MultiBLANCScorer(false);
  }

  /**
   * Gets a BLANC scorer which handles item mismatches between the predicted and gold clusterings
   * and which allows items to be members of multiple clusters.  Treats each item as having a coref
   * edge to itself.
   */
  public static BLANCScorer getMultiBLANCScorerWithSelfEdges() {
    return new MultiBLANCScorer(true);
  }

  /* package-private */
  static double nanToZero(double x) {
    return Double.isNaN(x) ? 0.0 : x;
  }
}
