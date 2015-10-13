package com.bbn.bue.common.evaluation;

import com.google.common.base.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public final class EvalPair<KeyT, TestT> {

  private final KeyT keyItem;
  private final TestT testItem;

  private EvalPair(final KeyT keyItem, final TestT testItem) {
    this.keyItem = checkNotNull(keyItem);
    this.testItem = checkNotNull(testItem);
  }

  public KeyT key() {
    return keyItem;
  }

  public TestT test() {
    return testItem;
  }

  public static <KeyT, TestT> EvalPair<KeyT, TestT> of(
      KeyT key, TestT test) {
    return new EvalPair<KeyT, TestT>(key, test);
  }

  public static <F, T, KeyT extends F, TestT extends F>
  Function<EvalPair<? extends KeyT, ? extends TestT>, EvalPair<T, T>> functionOnBoth(
      final Function<F, T> func) {
    return new Function<EvalPair<? extends KeyT, ? extends TestT>, EvalPair<T, T>>() {
      @Override
      public EvalPair<T, T> apply(final EvalPair<? extends KeyT, ? extends TestT> input) {
        return EvalPair.of(func.apply(input.key()), func.apply(input.test()));
      }
    };
  }
}
