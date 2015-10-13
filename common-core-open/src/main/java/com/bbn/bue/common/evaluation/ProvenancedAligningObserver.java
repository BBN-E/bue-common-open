package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.Finishable;
import com.bbn.bue.common.Inspector;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;

import java.io.IOException;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An {@link Inspector} which applies a {@link ProvenancedAligner} to its input and
 * applies further inspectors to the resulting alignment.
 */
@Beta
public final class ProvenancedAligningObserver<LeftT, LeftProvT, RightT, RightProvT>
    implements Inspector<EvalPair<Collection<LeftProvT>, Collection<RightProvT>>> {

  private final ProvenancedAligner<LeftT, LeftProvT, RightT, RightProvT> provenancedAligner;
  private final ImmutableList<Inspector<ProvenancedAlignment<LeftT, LeftProvT, RightT, RightProvT>>>
      provenancedObservers;
  private final ImmutableList<Inspector<Alignment<LeftT, RightT>>>
      unprovenancedObservers;

  private ProvenancedAligningObserver(
      final ProvenancedAligner<LeftT, LeftProvT, RightT, RightProvT> provenancedAligner,
      final Iterable<? extends Inspector<ProvenancedAlignment<LeftT, LeftProvT, RightT, RightProvT>>> provenancedObservers,
      final Iterable<? extends Inspector<Alignment<LeftT, RightT>>> unprovenancedObservers) {
    this.provenancedAligner = checkNotNull(provenancedAligner);
    this.provenancedObservers = ImmutableList.copyOf(provenancedObservers);
    this.unprovenancedObservers = ImmutableList.copyOf(unprovenancedObservers);
  }

  public static <LeftT, LeftProvT, RightT, RightProvT> ProvenancedAligningObserver<LeftT, LeftProvT, RightT, RightProvT> create(
      final ProvenancedAligner<LeftT, LeftProvT, RightT, RightProvT> provenancedAligner,
      final Iterable<? extends Inspector<ProvenancedAlignment<LeftT, LeftProvT, RightT, RightProvT>>> provenancedObservers,
      final Iterable<? extends Inspector<Alignment<LeftT, RightT>>> unprovenancedObservers) {
    return new ProvenancedAligningObserver<LeftT, LeftProvT, RightT, RightProvT>(
        provenancedAligner, provenancedObservers,
        unprovenancedObservers);
  }

  @Override
  public void inspect(EvalPair<Collection<LeftProvT>, Collection<RightProvT>> items) {
    final ProvenancedAlignment<LeftT, LeftProvT, RightT, RightProvT> alignment =
        provenancedAligner.align(items.key(), items.test());
    for (final Inspector<ProvenancedAlignment<LeftT, LeftProvT, RightT, RightProvT>> observer : provenancedObservers) {
      observer.inspect(alignment);
    }
    for (final Inspector<Alignment<LeftT, RightT>> observer : unprovenancedObservers) {
      observer.inspect(alignment);
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

  public static <CtxT, LeftT, LeftProvT, RightT, RightProvT> Builder<LeftT, LeftProvT, RightT, RightProvT> forAlignerAndContextType(
      final ProvenancedAligner<LeftT, LeftProvT, RightT, RightProvT> provenancedAligner) {
    return new Builder<LeftT, LeftProvT, RightT, RightProvT>(provenancedAligner);
  }

  public static <CtxT, InT, EqClassT> Builder<EqClassT, InT, EqClassT, InT> forEquivalenceFunctionAndContextType(
      final Function<? super InT, ? extends EqClassT> equivalenceFunction,
      final TypeToken<CtxT> contextTypeToken) {
    return new Builder<EqClassT, InT, EqClassT, InT>(
        EquivalenceBasedProvenancedAligner.forEquivalenceFunction(equivalenceFunction));
  }

  public static final class Builder<LeftT, LeftProvT, RightT, RightProvT> {

    final ProvenancedAligner<LeftT, LeftProvT, RightT, RightProvT> provenancedAligner;
    private final ImmutableList.Builder<Inspector<ProvenancedAlignment<LeftT, LeftProvT, RightT, RightProvT>>>
        provenancedObservers = ImmutableList.builder();
    private final ImmutableList.Builder<Inspector<Alignment<LeftT, RightT>>>
        unprovenancedObservers = ImmutableList.builder();

    private Builder(
        final ProvenancedAligner<LeftT, LeftProvT, RightT, RightProvT> provenancedAligner) {
      this.provenancedAligner = checkNotNull(provenancedAligner);
    }

    // cast is safe - see variance notes in Javadoc for these classes
    @SuppressWarnings("unchecked")
    public Builder<LeftT, LeftProvT, RightT, RightProvT> withAlignmentObserver(
        Inspector<? extends Alignment<? super LeftT, ? super RightT>> observer) {
      unprovenancedObservers.add((Inspector<Alignment<LeftT, RightT>>) observer);
      return this;
    }

    // cast is safe - see variance notes in Javadoc for these classes
    @SuppressWarnings("unchecked")
    public Builder<LeftT, LeftProvT, RightT, RightProvT> withProvenancedObserver(
        Inspector<? extends ProvenancedAlignment<LeftT, LeftProvT, RightT, RightProvT>> observer) {
      provenancedObservers.add(
          (Inspector<ProvenancedAlignment<LeftT, LeftProvT, RightT, RightProvT>>) observer);
      return this;
    }

    public ProvenancedAligningObserver<LeftT, LeftProvT, RightT, RightProvT> build() {
      return new ProvenancedAligningObserver<LeftT, LeftProvT, RightT, RightProvT>(
          provenancedAligner, provenancedObservers.build(),
          unprovenancedObservers.build());
    }
  }
}
