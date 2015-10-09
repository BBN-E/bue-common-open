package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.Finishable;
import com.bbn.bue.common.PairedContextObserver;
import com.bbn.bue.common.UnaryContextObserver;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;

import java.io.IOException;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link PairedContextObserver} which applies a {@link ProvenancedAligner} to its input and
 * applies further observers to the resulting alignment.
 */
@Beta
public final class ProvenancedAligningObserver<CtxT, LeftT, LeftProvT, RightT, RightProvT>
    implements PairedContextObserver<CtxT, Collection<LeftProvT>, Collection<RightProvT>> {

  private final ProvenancedAligner<LeftT, LeftProvT, RightT, RightProvT> provenancedAligner;
  private final ImmutableList<UnaryContextObserver<CtxT, ProvenancedAlignment<LeftT, LeftProvT, RightT, RightProvT>>>
      provenancedObservers;
  private final ImmutableList<UnaryContextObserver<CtxT, Alignment<LeftT, RightT>>>
      unprovenancedObservers;

  private ProvenancedAligningObserver(
      final ProvenancedAligner<LeftT, LeftProvT, RightT, RightProvT> provenancedAligner,
      final Iterable<? extends UnaryContextObserver<CtxT, ProvenancedAlignment<LeftT, LeftProvT, RightT, RightProvT>>> provenancedObservers,
      final Iterable<? extends UnaryContextObserver<CtxT, Alignment<LeftT, RightT>>> unprovenancedObservers) {
    this.provenancedAligner = checkNotNull(provenancedAligner);
    this.provenancedObservers = ImmutableList.copyOf(provenancedObservers);
    this.unprovenancedObservers = ImmutableList.copyOf(unprovenancedObservers);
  }

  public static <CtxT, LeftT, LeftProvT, RightT, RightProvT> ProvenancedAligningObserver<CtxT, LeftT, LeftProvT, RightT, RightProvT> create(
      final ProvenancedAligner<LeftT, LeftProvT, RightT, RightProvT> provenancedAligner,
      final Iterable<? extends UnaryContextObserver<CtxT, ProvenancedAlignment<LeftT, LeftProvT, RightT, RightProvT>>> provenancedObservers,
      final Iterable<? extends UnaryContextObserver<CtxT, Alignment<LeftT, RightT>>> unprovenancedObservers) {
    return new ProvenancedAligningObserver<CtxT, LeftT, LeftProvT, RightT, RightProvT>(
        provenancedAligner, provenancedObservers,
        unprovenancedObservers);
  }

  @Override
  public void observe(CtxT context, Collection<LeftProvT> keyItems,
      Collection<RightProvT> testItems) {
    final ProvenancedAlignment<LeftT, LeftProvT, RightT, RightProvT> alignment =
        provenancedAligner.align(keyItems, testItems);
    for (final UnaryContextObserver<CtxT, ProvenancedAlignment<LeftT, LeftProvT, RightT, RightProvT>> observer : provenancedObservers) {
      observer.observe(context, alignment);
    }
    for (final UnaryContextObserver<CtxT, Alignment<LeftT, RightT>> observer : unprovenancedObservers) {
      observer.observe(context, alignment);
    }
  }

  @Override
  public void finish() throws IOException {
    for (final Finishable observer : provenancedObservers) {
      observer.finish();
    }
    for (final Finishable observer : unprovenancedObservers) {
      observer.finish();
    }
  }

  public static <CtxT, LeftT, LeftProvT, RightT, RightProvT> Builder<CtxT, LeftT, LeftProvT, RightT, RightProvT> forAlignerAndContextType(
      final ProvenancedAligner<LeftT, LeftProvT, RightT, RightProvT> provenancedAligner,
      final TypeToken<CtxT> contextTypeToken) {
    return new Builder<CtxT, LeftT, LeftProvT, RightT, RightProvT>(provenancedAligner);
  }

  public static <CtxT, InT, EqClassT> Builder<CtxT, EqClassT, InT, EqClassT, InT> forEquivalenceFunctionAndContextType(
      final Function<? super InT, ? extends EqClassT> equivalenceFunction,
      final TypeToken<CtxT> contextTypeToken) {
    return new Builder<CtxT, EqClassT, InT, EqClassT, InT>(
        EquivalenceBasedProvenancedAligner.forEquivalenceFunction(equivalenceFunction));
  }

  public static final class Builder<CtxT, LeftT, LeftProvT, RightT, RightProvT> {

    final ProvenancedAligner<LeftT, LeftProvT, RightT, RightProvT> provenancedAligner;
    private final ImmutableList.Builder<UnaryContextObserver<CtxT, ProvenancedAlignment<LeftT, LeftProvT, RightT, RightProvT>>>
        provenancedObservers = ImmutableList.builder();
    private final ImmutableList.Builder<UnaryContextObserver<CtxT, Alignment<LeftT, RightT>>>
        unprovenancedObservers = ImmutableList.builder();

    private Builder(
        final ProvenancedAligner<LeftT, LeftProvT, RightT, RightProvT> provenancedAligner) {
      this.provenancedAligner = checkNotNull(provenancedAligner);
    }

    // cast is safe - see variance notes in Javadoc for these classes
    @SuppressWarnings("unchecked")
    public Builder<CtxT, LeftT, LeftProvT, RightT, RightProvT> withAlignmentObserver(
        UnaryContextObserver<? super CtxT, ? extends Alignment<? super LeftT, ? super RightT>> observer) {
      unprovenancedObservers.add((UnaryContextObserver<CtxT, Alignment<LeftT, RightT>>) observer);
      return this;
    }

    // cast is safe - see variance notes in Javadoc for these classes
    @SuppressWarnings("unchecked")
    public Builder<CtxT, LeftT, LeftProvT, RightT, RightProvT> withProvenancedObserver(
        UnaryContextObserver<? super CtxT, ? extends ProvenancedAlignment<LeftT, LeftProvT, RightT, RightProvT>> observer) {
      provenancedObservers.add(
          (UnaryContextObserver<CtxT, ProvenancedAlignment<LeftT, LeftProvT, RightT, RightProvT>>) observer);
      return this;
    }

    public ProvenancedAligningObserver<CtxT, LeftT, LeftProvT, RightT, RightProvT> build() {
      return new ProvenancedAligningObserver<CtxT, LeftT, LeftProvT, RightT, RightProvT>(
          provenancedAligner, provenancedObservers.build(),
          unprovenancedObservers.build());
    }
  }
}
