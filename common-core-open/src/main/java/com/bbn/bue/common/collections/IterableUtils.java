package com.bbn.bue.common.collections;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;

import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class IterableUtils {

  private IterableUtils() {
  }

  ;

  /**
   * Promotes a predicate over T to a predicate over Iterable<T> that returns true if an only if any
   * the predicate is true for any of the iterable's contents.
   */
  public static <T> Predicate<Iterable<T>> anyPredicate(final Predicate<T> pred) {
    return new Predicate<Iterable<T>>() {
      @Override
      public boolean apply(final Iterable<T> iter) {
        return com.google.common.collect.Iterables.any(iter, pred);
      }
    };
  }

  /**
   * Promotes a predicate over T to a predicate over Iterable<T> that returns true if an only if any
   * the predicate is true for any of the iterable's contents.
   */
  public static <T> Predicate<Iterable<T>> allPredicate(final Predicate<T> pred) {
    return new Predicate<Iterable<T>>() {
      @Override
      public boolean apply(final Iterable<T> iter) {
        return com.google.common.collect.Iterables.all(iter, pred);
      }
    };
  }

  /**
   * Applys a one-to-many transform to each element of an {@code Iterable} and concatenates all the
   * results into one {@code Iterable}. This is done lazily.
   */
  public static <InType, OutType> Iterable<OutType> applyOneToManyTransform(
      final Iterable<InType> input,
      final Function<? super InType, ? extends Iterable<OutType>> function) {
    return Iterables.concat(Iterables.transform(input, function));
  }

  /**
   * An implementation of Python's zip.
   */
  public static <X, Y> Iterable<ZipPair<X, Y>> zip(final Iterable<X> iter1,
      final Iterable<Y> iter2) {
    return new ZipIterable<X, Y>(iter1, iter2);
  }

  public static <T> Iterable<List<T>> zip(
      final Iterable<? extends Iterable<? extends T>> iterables) {
    return new MultiZipIterable<T>(iterables);
  }

  /**
   * An iterable for iteration of two Iterables paired up with zip
   *
   * @author rgabbard
   */
  public static final class ZipIterable<X, Y> implements Iterable<ZipPair<X, Y>> {

    private ZipIterable(final Iterable<X> iter1, final Iterable<Y> iter2) {
      this.iterable1 = checkNotNull(iter1);
      this.iterable2 = checkNotNull(iter2);
    }

    private final Iterable<X> iterable1;
    private final Iterable<Y> iterable2;

    @Override
    public Iterator<ZipPair<X, Y>> iterator() {
      final Iterator<X> iter1 = iterable1.iterator();
      final Iterator<Y> iter2 = iterable2.iterator();

      return new UnmodifiableIterator<ZipPair<X, Y>>() {
        int elementsRead = 0;

        @Override
        public boolean hasNext() {
          if (iter1.hasNext() != iter2.hasNext()) {
            final String shorterOne = iter1.hasNext() ? "right" : "left";
            throw new RuntimeException(
                String.format(
                    "Iterable length mismatch in zip: %s has %d elements, but the other has more.",
                    shorterOne, elementsRead));
          }

          return iter1.hasNext();
        }

        @Override
        public ZipPair<X, Y> next() {
          ++elementsRead;
          return new ZipPair<X, Y>(iter1.next(), iter2.next());
        }
      };
    }

    public static <X, Y> ZipIterable<X, Y> zip(final Iterable<X> iter1, final Iterable<Y> iter2) {
      return new ZipIterable<X, Y>(iter1, iter2);
    }
  }

  /**
   * Represents a matching pair of items from two zipped iterators.
   *
   * @author rgabbard
   */
  public static final class ZipPair<X, Y> {

    public ZipPair(final X first, final Y second) {
      this.first = first;
      this.second = second;
    }

    public X first() {
      return first;
    }

    public Y second() {
      return second;
    }

    @Override
    public String toString() {
      return String.format("(%s,%s)", first, second);
    }

    ;

    @Override
    public int hashCode() {
      return Objects.hashCode(first, second);
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof ZipPair)) {
        return false;
      }
      final ZipPair<?, ?> other = (ZipPair<?, ?>) obj;
      return Objects.equal(first, other.first)
          && Objects.equal(second, other.second);
    }

    public static <X, Y> ZipPair<X, Y> from(final X x, final Y y) {
      return new ZipPair<X, Y>(x, y);
    }

    private final X first;
    private final Y second;
  }

  /**
   * An iterable for iteration of many Iterables paired up with zip.
   *
   * @author rgabbard
   */
  public static final class MultiZipIterable<T> implements Iterable<List<T>> {

    private MultiZipIterable(final Iterable<? extends Iterable<? extends T>> iterables) {
      this.iterables = ImmutableList.copyOf(iterables);
      checkArgument(!this.iterables.isEmpty());
    }

    private final List<Iterable<? extends T>> iterables;

    @Override
    public Iterator<List<T>> iterator() {
      final ImmutableList.Builder<Iterator<? extends T>> iteratorsB = ImmutableList.builder();

      for (final Iterable<? extends T> iterable : iterables) {
        iteratorsB.add(iterable.iterator());
      }

      final ImmutableList<Iterator<? extends T>> iterators = iteratorsB.build();

      return new UnmodifiableIterator<List<T>>() {
        @Override
        public boolean hasNext() {
          if (!allTheSame(Lists.transform(iterators, HasNext))) {
            throw new RuntimeException("Iterable length mismatch in zip: %s is longer.");
          }

          return iterators.get(0).hasNext();
        }

        @Override
        public List<T> next() {
          final ImmutableList.Builder<T> ret = ImmutableList.builder();

          for (final Iterator<? extends T> it : iterators) {
            ret.add(it.next());
          }

          return ret.build();
        }
      };
    }
  }


  /**
   * Two argument function to be used for reduction
   *
   * @author mshafir
   */
  public interface Function2<A, B> {

    B apply(B b, A a);
  }

  /**
   * reduces an iterable to a single value starting from an initial value and applying down the
   * iterable.
   */
  public static <A, B> B reduce(final Iterable<A> iterable,
      final B initial, final Function2<A, B> func) {
    B b = initial;
    for (final A item : iterable) {
      b = func.apply(b, item);
    }
    return b;
  }

  public static final Function2<Number, Number> SumReducer = new Function2<Number, Number>() {
    @Override
    public Number apply(final Number a, final Number b) {
      return a.doubleValue() + b.doubleValue();
    }
  };

  public static final Function2<Integer, Integer> SumReducerInt =
      new Function2<Integer, Integer>() {
        @Override
        public Integer apply(final Integer a, final Integer b) {
          return a + b;
        }
      };

  public static final Function<Iterable<Number>, Number> Sum =
      new Function<Iterable<Number>, Number>() {
        @Override
        public Number apply(final Iterable<Number> it) {
          return reduce(it, 0, SumReducer);
        }
      };

  public static final Function<Iterable<Integer>, Integer> IntSum =
      new Function<Iterable<Integer>, Integer>() {
        @Override
        public Integer apply(final Iterable<Integer> it) {
          return reduce(it, 0, SumReducerInt);
        }
      };

  /**
   * Transforms an Iterable<T> to a Map<T, Integer> where each item is mapped to its zero-indexed
   * position in the Iterable's sequence.  If an item occurs twice, an IllegalArgumentException will
   * be thrown.
   */
  public static <T> ImmutableMap<T, Integer> itemToIndexMap(final Iterable<T> sequence) {
    final ImmutableMap.Builder<T, Integer> ret = ImmutableMap.builder();

    int idx = 0;
    for (final T item : sequence) {
      ret.put(item, idx);
      ++idx;
    }

    return ret.build();
  }

  /**
   * Transforms an Iterable<T> to a Map<T, Integer> where each item is mapped to its zero-indexed
   * position(s) in the Iterable's sequence.
   */
  public static <T> ImmutableMultimap<T, Integer> itemToIndexMultimap(
      Iterable<T> iterable) {
    final ImmutableMultimap.Builder ret = ImmutableMultimap.builder();
    int idx = 0;
    for (final T x : iterable) {
      ret.put(x, idx++);
    }
    return ret.build();
  }

  /**
   * Given a paired sequence of Iterables, produce a map with keys from the first and values from
   * the second. An exception will be raised if the Iterables have different numbers of elements, or
   * if there are multiple mappings for the same key.
   */
  public static <K, V> ImmutableMap<K, V> mapFromPairedKeyValueSequences(
      final Iterable<K> keys, final Iterable<V> values) {
    final ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
    for (final ZipPair<K, V> pair : zip(keys, values)) {
      builder.put(pair.first(), pair.second());
    }
    return builder.build();
  }

  public static final Function<Iterator<?>, Boolean> HasNext =
      new Function<Iterator<?>, Boolean>() {
        @Override
        public Boolean apply(final Iterator<?> x) {
          return x.hasNext();
        }
      };

  /**
   * Prefer allEqual, whose name is less ambiguous between equality and identity
   */
  @Deprecated
  public static <T> boolean allTheSame(final Iterable<T> iterable) {
    if (Iterables.isEmpty(iterable)) {
      return true;
    }

    final T reference = Iterables.getFirst(iterable, null);

    for (final T x : iterable) {
      if (!x.equals(reference)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns whether all the items in the iterable are equal to one another by {@code .equals()}.
   * Returns true for an empty iterable.
   */
  public static <T> boolean allEqual(final Iterable<T> iterable) {
    if (Iterables.isEmpty(iterable)) {
      return true;
    }

    final T reference = Iterables.getFirst(iterable, null);

    for (final T x : iterable) {
      if (!x.equals(reference)) {
        return false;
      }
    }
    return true;
  }
}
