package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.Finishable;
import com.bbn.bue.common.Inspector;
import com.bbn.bue.common.collections.BootstrapIterator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An inspector which can use a supplied strategy to do some sort of scoring or inspection of a
 * corpus using bootstrapping.  A strategy must supply two things: a way of mapping every observed
 * item to some sort of summary (an {@link com.bbn.bue.common.evaluation.BootstrapInspector.ObservationSummarizer}
 * and one or more ways of aggregating a collection of collections of summaries into a final output
 * (a {@link com.bbn.bue.common.evaluation.BootstrapInspector.SummaryAggregator}.
 *
 * The bootstrap inspector will apply the {@code ObservationSummarizer} to each item it observes and
 * remember the result.  When it is {@link #finish()}ed, it will then take a specified number of
 * samples with replacement from the collection of summaries. Each of these samples will be
 * presented to the {@code SummaryAggregators}. Finally, each {@code SummaryAggregator}'s {@code
 * finish()} method will be called to produce final output.
 *
 * To make an example concrete, your {@code ObservationSummarizer} could produce the true positive,
 * false positive, and false negative counts for some classification task on a document.  The
 * aggregator could sum these of the corpus and create a corpus-wide F-measure and then output the
 * mean and standard deviation of these F-measures. This would give you some idea how robust your
 * F-measure is with respect to the corpus composition.
 */
public final class BootstrapInspector<ObsT, SummaryT> implements Inspector<ObsT> {
  private final int numSamples;
  private final Random rng;
  private final ObservationSummarizer<ObsT, SummaryT> observationSummarizer;
  private final ImmutableList<SummaryAggregator<SummaryT>> summaryAggregators;
  private final List<SummaryT> observationSummaries = Lists.newArrayList();

  private BootstrapInspector(
      final ObservationSummarizer<ObsT, SummaryT> observationSummarizer,
      final Iterable<? extends SummaryAggregator<SummaryT>> summaryAggregators,
      final int numSamples, final Random rng) {
    checkArgument(numSamples > 0, "Number of bootstrap samples must be positive");
    this.numSamples = numSamples;
    this.rng = checkNotNull(rng);
    this.observationSummarizer = checkNotNull(observationSummarizer);
    this.summaryAggregators = ImmutableList.copyOf(summaryAggregators);
  }

  @Override
  public void inspect(ObsT item) {
    observationSummaries.add(observationSummarizer.summarizeObservation(item));
  }

  @Override
  public void finish() throws IOException {
    final Iterator<Collection<SummaryT>> bootstrapSamples =
        Iterators.limit(BootstrapIterator.forData(observationSummaries, rng), numSamples);
    while (bootstrapSamples.hasNext()) {
      final Collection<SummaryT> sample = bootstrapSamples.next();
      for (final SummaryAggregator<SummaryT> summaryAggregator : summaryAggregators) {
        summaryAggregator.observeSample(sample);
      }
    }
    for (final SummaryAggregator<SummaryT> summaryAggregator : summaryAggregators) {
      summaryAggregator.finish();
    }
  }

  /**
   * A strategy for reducing a pair observation to a some sort of summary we can later bootstrap
   * sample over. For example, we bootstrap sampling F-measure over a corpus, if each pair
   * observation is for one document, a good summary would be the number of true positives, false
   * positives, and false negatives.
   *
   * @param <ObsT>     The type of thing to be observed. Contravariant.
   * @param <SummaryT> The type of summary produced. Covariant.
   */
  public interface ObservationSummarizer<ObsT, SummaryT> {

    SummaryT summarizeObservation(final ObsT item);
  }

  /**
   * A strategy of aggregating bootstrap samples into some sort of final output.  This will be shown
   * some number of sampled collections of summaries and will then take some action when {@link
   * #finish()} is called.
   *
   * @param <SummaryT> The type of summary to expect. Contravariant.
   */
  public interface SummaryAggregator<SummaryT> extends Finishable {

    void observeSample(Collection<SummaryT> observationSummaries);
  }

  public interface BootstrapStrategy<ObsT, SummaryT> {

    ObservationSummarizer<ObsT, SummaryT> createObservationSummarizer();

    Collection<SummaryAggregator<SummaryT>> createSummaryAggregators();
  }

  // cast is safe - see covariance and contravariance notes on ObservationSummarizer
  @SuppressWarnings("unchecked")
  public static <ObsT, SummaryT> Builder<ObsT, SummaryT> forSummarizer(
      final ObservationSummarizer<? super ObsT, ? extends SummaryT> observationSummarizer,
      final Random rng) {
    return new Builder<ObsT, SummaryT>(
        (ObservationSummarizer<ObsT, SummaryT>) observationSummarizer, rng);
  }

  public static <ObsT, SummaryT> Builder<ObsT, SummaryT> forStrategy(
      final BootstrapStrategy<ObsT, SummaryT> strategy, final Random rng) {
    final Builder<ObsT, SummaryT> ret = forSummarizer(strategy.createObservationSummarizer(), rng);
    for (final SummaryAggregator<SummaryT> aggregator : strategy.createSummaryAggregators()) {
      ret.withSummaryAggregator(aggregator);
    }
    return ret;
  }

  public static final class Builder<ObsT, SummaryT> {

    private int numSamples = 1000;
    private final Random rng;
    private final ObservationSummarizer<ObsT, SummaryT> observationSummarizer;
    private final ImmutableList.Builder<SummaryAggregator<SummaryT>> summaryAggregators =
        ImmutableList.builder();

    private Builder(
        final ObservationSummarizer<ObsT, SummaryT> observationSummarizer,
        final Random rng) {
      this.rng = checkNotNull(rng);
      this.observationSummarizer = checkNotNull(observationSummarizer);
    }

    /**
     * Specify the number of boostrap samples to use. Defaults to 1000.
     */
    public Builder withNumSamples(int numSamples) {
      this.numSamples = numSamples;
      return this;
    }

    // OK because SummaryAggregator is contravariant in SummaryT
    @SuppressWarnings("unchecked")
    public Builder withSummaryAggregator(SummaryAggregator<? super SummaryT> summaryAggregator) {
      summaryAggregators.add((SummaryAggregator<SummaryT>) summaryAggregator);
      return this;
    }

    public BootstrapInspector<ObsT, SummaryT> build() {
      return new BootstrapInspector<ObsT, SummaryT>(observationSummarizer,
          summaryAggregators.build(), numSamples, rng);
    }
  }
}

