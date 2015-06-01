package com.bbn.nlp.coreference.measures;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;

/**
 * A BLANC score. This is not represented using {@link com.bbn.bue.common.evaluation.FMeasureInfo}
 * because, although the terms precision, recall, and F-measure are used, each results from
 * averaging other P, R, and Fs, so they don't have the same relationship between themselves you
 * would normally expect. The BLANC score itself it always defined, but various sub-scores may be
 * {@code NaN}.
 */
@Beta
public final class BLANCResult {
  // precision of coref links
  private final double P_C;
  // precision of non-coref links
  private final double P_N;
  // recall of coref links
  private final double R_C;
  // recall of non-coref links
  private final double R_N;

  // F1 of coref links
  private final double F_C;
  // F1 of non-coref links
  private final double F_N;

  private final double blancScore;

  /**
   * cached computations derived from the data above
   */
  private final double blancP;
  private final double blancR;

  private BLANCResult(final double p_C, final double p_N, final double r_C, final double r_N,
      final double F_C, final double F_N, final double blancScore) {
    this.P_C = p_C;
    this.P_N = p_N;
    this.R_C = r_C;
    this.R_N = r_N;

    this.F_N = F_N;
    this.F_C = F_C;

    this.blancP = 0.5 * P_C + 0.5 * P_N;
    this.blancR = 0.5 * R_C + 0.5 * R_N;
    this.blancScore = blancScore;
  }

  /* package-private */
  static BLANCResult fromSetCounts(boolean itemSetsMatch, double corefLinksInBoth,
      double corefLinksInKey, double corefLinksInResponse, double nonCorefLinksInBoth,
      double nonCorefLinksInKey, double nonCorefLinksInResponse)
  {
    final double R_C = corefLinksInBoth / ((double) corefLinksInKey);
    final double P_C = corefLinksInBoth / ((double) corefLinksInResponse);
    final double R_N = nonCorefLinksInBoth / ((double) nonCorefLinksInKey);
    final double P_N = nonCorefLinksInBoth / ((double) nonCorefLinksInResponse);

    double blancScore;

    // this way of computing handles certain edge cases the P and R way does not
    final double F_C = 2*corefLinksInBoth/(corefLinksInKey+corefLinksInResponse);
    final double F_N = 2*nonCorefLinksInBoth/(nonCorefLinksInKey+nonCorefLinksInResponse);

    // handle special cases - section 4.1 of Luo et al.
    final boolean noCorefLinksOnEitherSide = corefLinksInKey == 0.0 && corefLinksInResponse == 0.0;
    final boolean noNonCorefLinksOnEitherSide =
        nonCorefLinksInKey == 0.0 && nonCorefLinksInResponse == 0.0;
    if (noCorefLinksOnEitherSide && noNonCorefLinksOnEitherSide) {
      // only a single item on both sides
      // is it the same?
      if (itemSetsMatch) {
        blancScore = 1.0;
      } else {
        blancScore = 0.0;
      }
    } else if (noCorefLinksOnEitherSide) {
      // only singletons on both sides
      blancScore = F_N;
    } else if (noNonCorefLinksOnEitherSide) {
      // both sides have one big cluster
      blancScore = F_C;
    } else {
      blancScore = 0.5*(F_N+F_C);
    }

    return new BLANCResult(P_C, P_N, R_C, R_N, F_C, F_N, blancScore);
  }

  public Optional<Double> corefLinkPrecision() {
    return ifDefined(P_C);
  }

  public Optional<Double> nonCorefLinkPrecision() {
    return ifDefined(P_N);
  }

  public Optional<Double> corefLinkRecall() {
    return ifDefined(R_C);
  }

  public Optional<Double> nonCorefLinkRecall() {
    return ifDefined(R_N);
  }

  public Optional<Double> corefLinkF1() {
    return ifDefined(F_C);
  }

  public Optional<Double> nonCorefLinkF1() {
    return ifDefined(F_N);
  }

  public Optional<Double> blancPrecision() {
    return ifDefined(blancP);
  }

  public Optional<Double> blancRecall() {
    return ifDefined(blancR);
  }

  /**
   * The final BLANC score. This is always defined (see Luo et. al, ACL 2014, sec 4.1 for edge cases).
   * If both documents are empty, we return 1.0.
   * @return
   */
  public double blancScore() {
    return blancScore;
  }

  private static Optional<Double> ifDefined(double d) {
    if (Double.isNaN(d)) {
      return Optional.absent();
    } else {
      return Optional.of(d);
    }
  }
}
