package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.Finishable;
import com.bbn.bue.common.PairedContextObserver;
import com.bbn.bue.common.UnaryContextObserver;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;

import java.io.IOException;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link PairedContextObserver} which aligns its two inputs and then runs other observes on the
 * alignments.
 */
@Beta
public final class AligningObserver<CtxT, LeftT, RightT>
    implements PairedContextObserver<CtxT, Collection<LeftT>, Collection<RightT>> {

  private final Aligner<LeftT, RightT> aligner;
  private final ImmutableList<UnaryContextObserver<CtxT, Alignment<LeftT, RightT>>>
      observers;

  private AligningObserver(
      final Aligner<LeftT, RightT> aligner,
      final Iterable<? extends UnaryContextObserver<CtxT, Alignment<LeftT, RightT>>> observers) {
    this.aligner = checkNotNull(aligner);
    this.observers = ImmutableList.copyOf(observers);
  }

  public static <CtxT, LeftT, RightT> AligningObserver<CtxT, LeftT, RightT> create(
      final Aligner<LeftT, RightT> aligner,
      final Iterable<? extends UnaryContextObserver<CtxT, Alignment<LeftT, RightT>>> observers) {
    return new AligningObserver<CtxT, LeftT, RightT>(aligner, observers);
  }

  @Override
  public void observe(CtxT context, Collection<LeftT> keyItems,
      Collection<RightT> testItems) {
    final Alignment<LeftT, RightT> alignment = aligner.align(keyItems, testItems);
    for (final UnaryContextObserver<CtxT, Alignment<LeftT, RightT>> observer : observers) {
      observer.observe(context, alignment);
    }
  }

  @Override
  public void finish() throws IOException {
    for (final Finishable observer : observers) {
      observer.finish();
    }
  }

  public static <CtxT, LeftT, RightT> Builder<CtxT, LeftT, RightT> forAlignerAndContextType(
      final Aligner<LeftT, RightT> aligner, final TypeToken<CtxT> contextTypeToken) {
    return new Builder<CtxT, LeftT, RightT>(aligner);
  }


  public static final class Builder<CtxT, LeftT, RightT> {

    private final Aligner<LeftT, RightT> aligner;
    private final ImmutableList.Builder<UnaryContextObserver<CtxT, Alignment<LeftT, RightT>>>
        observers = ImmutableList.builder();

    private Builder(
        final Aligner<LeftT, RightT> aligner) {
      this.aligner = checkNotNull(aligner);
    }

    // cast is safe - see variance notes in Javadoc for these classes
    @SuppressWarnings("unchecked")
    public Builder<CtxT, LeftT, RightT> withAlignmentObserver(
        UnaryContextObserver<? super CtxT, ? extends Alignment<? super LeftT, ? super RightT>> observer) {
      observers.add((UnaryContextObserver<CtxT, Alignment<LeftT, RightT>>) observer);
      return this;
    }

    public AligningObserver<CtxT, LeftT, RightT> build() {
      return new AligningObserver<CtxT, LeftT, RightT>(aligner, observers.build());
    }
  }
}
