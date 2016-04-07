package com.bbn.bue.common.evaluation;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates {@link ProvenancedAlignment}s based on grouping items into equivalence classes. The items
 * aligned are the equivalence classes and the provenances are the original items.
 */
@Beta
public final class EquivalenceBasedProvenancedAligner<LeftT, RightT, EqClassT>
    implements ProvenancedAligner<EqClassT, LeftT, EqClassT, RightT> {

  private final Function<LeftT, EqClassT> leftEqClassFunction;
  private final Function<RightT, EqClassT> rightEqClassFunction;

  private EquivalenceBasedProvenancedAligner(
      final Function<LeftT, EqClassT> leftEqClassFunction,
      final Function<RightT, EqClassT> rightEqClassFunction) {
    this.leftEqClassFunction = checkNotNull(leftEqClassFunction);
    this.rightEqClassFunction = checkNotNull(rightEqClassFunction);
  }

  // the generic cast is always safe because it is just discarding extra bounds information
  // we don't need
  @SuppressWarnings("unchecked")
  /**
   * Creates an aligner given a single function from items to equivalence classes (requires left
   * and right items to both be of types compatible with this function).
   */
  public static <InT, EqClass> EquivalenceBasedProvenancedAligner<InT, InT, EqClass> forEquivalenceFunction(
      Function<? super InT, ? extends EqClass> equivalenceFunction) {
    return new EquivalenceBasedProvenancedAligner<InT, InT, EqClass>(
        (Function<InT, EqClass>) equivalenceFunction,
        (Function<InT, EqClass>) equivalenceFunction);
  }

  // the generic cast is always safe because it is just discarding extra bounds information
  // we don't need
  @SuppressWarnings("unchecked")
  /**
   * Creates an aligner given two separate functions for mapping the left and right items to
   * (presumably the same set of) equivalence classes.
   */
  public static <LeftT, RightT, EqClass> EquivalenceBasedProvenancedAligner<LeftT, RightT, EqClass> forKeyAndTestEquivalenceFunctions(
      Function<? super LeftT, ? extends EqClass> leftEquivalenceFunction,
      Function<? super RightT, ? extends EqClass> rightEquivalenceFunction) {
    return new EquivalenceBasedProvenancedAligner<LeftT, RightT, EqClass>(
        (Function<LeftT, EqClass>) leftEquivalenceFunction,
        (Function<RightT, EqClass>) rightEquivalenceFunction);
  }

  public EquivalenceBasedProvenancedAlignment<EqClassT, LeftT, RightT> align(
      Iterable<? extends LeftT> left,
      Iterable<? extends RightT> right) {
    final ImmutableListMultimap<? extends EqClassT, ? extends LeftT>
        leftEquivalenceClassToProvenance =
        Multimaps.index(left, leftEqClassFunction);
    final ImmutableListMultimap<? extends EqClassT, ? extends RightT>
        rightEquivalenceClassToProvenance =
        Multimaps.index(right, rightEqClassFunction);

    return EquivalenceBasedProvenancedAlignment.fromEquivalenceClassMaps(
        leftEquivalenceClassToProvenance, rightEquivalenceClassToProvenance);
  }

  @Override
  public Function<EvalPair<? extends Iterable<? extends LeftT>, ? extends Iterable<? extends RightT>>, ProvenancedAlignment<EqClassT, LeftT, EqClassT, RightT>> asFunction() {
    return new Function<EvalPair<? extends Iterable<? extends LeftT>, ? extends Iterable<? extends RightT>>, ProvenancedAlignment<EqClassT, LeftT, EqClassT, RightT>>() {
      @Override
      public ProvenancedAlignment<EqClassT, LeftT, EqClassT, RightT> apply(final
      EvalPair<? extends Iterable<? extends LeftT>, ? extends Iterable<? extends RightT>> input) {
        return align(input.key(), input.test());
      }
    };
  }

}
