package com.bbn.bue.common.evaluation;

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
}
