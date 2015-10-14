package com.bbn.bue.common.evaluation;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Map;

/**
 * Utility methods for dealing with {@link Alignment}s.
 */
public final class Alignments {

  private Alignments() {
    throw new UnsupportedOperationException();
  }

  /**
   * Splits an alignment into many alignments based on a function mapping aligned items to some set
   * of keys.  Returns a map where the keys are all observed outputs of the key function on items in
   * the alignment and the values are new alignments containing only elements which yield that
   * output when the key function is applied.  For example, if you had an alignment of
   * EventMentions, you could use an "event type" key function to produce one alignment per event
   * type.
   */
  @SuppressWarnings("unchecked")
  public static <T, V> ImmutableMap<V, Alignment<T, T>> splitAlignmentByKeyFunction(
      Alignment<? extends T, ? extends T> alignment,
      Function<? super T, ? extends V> keyFunction) {
    // we first determine all keys we could ever encounter to ensure we can construct our map
    // deterministically
    // Java will complain about this cast but it is safe because ImmutableSet if covariant in its
    // type parameter
    final ImmutableSet<? extends V> allKeys =
        FluentIterable.from((ImmutableSet<T>) alignment.allLeftItems())
            .append(alignment.allRightItems())
            .transform(keyFunction).toSet();

    final ImmutableMap.Builder<V, MultimapAlignment.Builder<T, T>> keysToAlignmentsB =
        ImmutableMap.builder();
    for (final V key : allKeys) {
      keysToAlignmentsB.put(key, MultimapAlignment.<T, T>builder());
    }

    final ImmutableMap<V, MultimapAlignment.Builder<T, T>> keysToAlignments =
        keysToAlignmentsB.build();

    for (final T leftItem : alignment.allLeftItems()) {
      final V keyVal = keyFunction.apply(leftItem);
      final MultimapAlignment.Builder<T, T> alignmentForKey = keysToAlignments.get(keyVal);
      alignmentForKey.addLeftItem(leftItem);
      for (T rightItem : alignment.alignedToLeftItem(leftItem)) {
        if (keyFunction.apply(rightItem).equals(keyVal)) {
          alignmentForKey.align(leftItem, rightItem);
        }
      }
    }
    for (final T rightItem : alignment.allRightItems()) {
      final V keyVal = keyFunction.apply(rightItem);
      final MultimapAlignment.Builder<T, T> alignmentForKey = keysToAlignments.get(keyVal);
      alignmentForKey.addRightItem(rightItem);
      for (final T leftItem : alignment.alignedToRightItem(rightItem)) {
        if (keyVal.equals(keyFunction.apply(leftItem))) {
          alignmentForKey.align(leftItem, rightItem);
        }
      }
    }

    final ImmutableMap.Builder<V, Alignment<T, T>> ret = ImmutableMap.builder();
    for (final Map.Entry<V, MultimapAlignment.Builder<T, T>> entry : keysToAlignments.entrySet()) {
      ret.put(entry.getKey(), entry.getValue().build());
    }

    return ret.build();
  }
}
