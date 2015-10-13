package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.Finishable;
import com.bbn.bue.common.Inspector;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A strategy for scoring things for some task.  Input items are received in pairs (typically a gold
 * standard and a system output). These are each translated into collections of scorable items (e.g.
 * event mentions).  These scoreable items are then inspected by provided {@link
 * Inspector}s.
 */
@Beta
public final class ScoringRegimeObserver<LeftT, LeftScorableT, RightT, RightScorableT>
    implements Inspector<EvalPair<LeftT, RightT>> {

  private final Function<LeftT, Collection<LeftScorableT>> leftScoreableExtractor;
  private final Function<RightT, Collection<RightScorableT>> rightScoreableExtractor;
  private final ImmutableList<Inspector<EvalPair<Collection<LeftScorableT>, Collection<RightScorableT>>>>
      observers;

  private ScoringRegimeObserver(
      final Function<LeftT, Collection<LeftScorableT>> leftScorableExtractor,
      final Function<RightT, Collection<RightScorableT>> rightScorableExtractor,
      final Iterable<? extends Inspector<EvalPair<Collection<LeftScorableT>, Collection<RightScorableT>>>> observers) {
    this.leftScoreableExtractor = checkNotNull(leftScorableExtractor);
    this.rightScoreableExtractor = checkNotNull(rightScorableExtractor);
    this.observers = ImmutableList.copyOf(observers);
  }

  // this is actually very slightly unsafe. If you attempted to insert an item into the collection
  // returned by the scorable item functions, you could potentially get a failure.  But why
  // would you do this?
  @SuppressWarnings("unchecked")
  public static <KeyInput, TestInput, KeyScoreableT, TestScoreableT> ScoringRegimeObserver<KeyInput, KeyScoreableT, TestInput, TestScoreableT>
  create(
      final Function<? super KeyInput, ? extends Collection<? extends KeyScoreableT>> keyScoreableObjectExtractor,
      final Function<? super TestInput, ? extends Collection<? extends TestScoreableT>> testScoreableObjectExtractor,
      final Iterable<? extends Inspector<EvalPair<Collection<KeyScoreableT>, Collection<TestScoreableT>>>> observers) {
    return new ScoringRegimeObserver<KeyInput, KeyScoreableT, TestInput, TestScoreableT>(
        (Function<KeyInput, Collection<KeyScoreableT>>) keyScoreableObjectExtractor,
        (Function<TestInput, Collection<TestScoreableT>>) testScoreableObjectExtractor,
        observers);
  }

  @Override
  public void inspect(EvalPair<LeftT, RightT> item) {
    final Collection<LeftScorableT> keyItems = leftScoreableExtractor.apply(item.key());
    final Collection<RightScorableT> testItems = rightScoreableExtractor.apply(item.test());

    for (final Inspector<EvalPair<Collection<LeftScorableT>, Collection<RightScorableT>>> observer : observers) {
      observer.inspect(EvalPair.of(keyItems, testItems));
    }
  }

  public void finish() throws IOException {
    for (final Finishable observer : observers) {
      observer.finish();
    }
  }
}
