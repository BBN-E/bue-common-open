package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.Finishable;
import com.bbn.bue.common.Inspector;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An {@link Inspector} which aligns the two sides of its input {@link EvalPair}s
 * and then runs other inspectors on the alignments.
 */
@Beta
public final class AligningObserver<LeftT, RightT>
    implements Inspector<EvalPair<Collection<LeftT>, Collection<RightT>>> {

  private final Aligner<LeftT, RightT> aligner;
  private final ImmutableList<Inspector<Alignment<LeftT, RightT>>> observers;

  private AligningObserver(
      final Aligner<LeftT, RightT> aligner,
      final Iterable<? extends Inspector<Alignment<LeftT, RightT>>> observers) {
    this.aligner = checkNotNull(aligner);
    this.observers = ImmutableList.copyOf(observers);
  }

  public static <LeftT, RightT> AligningObserver<LeftT, RightT> create(
      final Aligner<LeftT, RightT> aligner,
      final Iterable<? extends Inspector<Alignment<LeftT, RightT>>> observers) {
    return new AligningObserver<LeftT, RightT>(aligner, observers);
  }

  @Override
  public void inspect(EvalPair<Collection<LeftT>, Collection<RightT>> items) {
    final Alignment<LeftT, RightT> alignment = aligner.align(items.key(), items.test());
    for (final Inspector<Alignment<LeftT, RightT>> observer : observers) {
      observer.inspect(alignment);
    }
  }

  @Override
  public void finish() throws IOException {
    for (final Finishable observer : observers) {
      observer.finish();
    }
  }

  public static <LeftT, RightT> Builder<LeftT, RightT> forAlignerAndContextType(
      final Aligner<? super LeftT, ? super RightT> aligner) {
    return new Builder<LeftT, RightT>(aligner);
  }


  public static final class Builder<LeftT, RightT> {

    private final Aligner<LeftT, RightT> aligner;
    private final ImmutableList.Builder<Inspector<Alignment<LeftT, RightT>>>
        observers = ImmutableList.builder();

    // Aligner is contravariant in its arguments
    @SuppressWarnings("unchecked")
    private Builder(
        final Aligner<? super LeftT, ? super RightT> aligner) {
      this.aligner = checkNotNull((Aligner<LeftT, RightT>) aligner);
    }

    // cast is safe - see variance notes in Javadoc for these classes
    @SuppressWarnings("unchecked")
    public Builder<LeftT, RightT> withAlignmentObserver(
        Inspector<? extends Alignment<? super LeftT, ? super RightT>> observer) {
      observers.add((Inspector<Alignment<LeftT, RightT>>) observer);
      return this;
    }

    public AligningObserver<LeftT, RightT> build() {
      return new AligningObserver<LeftT, RightT>(aligner, observers.build());
    }
  }
}

