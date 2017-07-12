package com.bbn.bue.common.files;

import com.bbn.bue.common.TextGroupImmutable;
import com.bbn.bue.common.annotations.MoveToBUECommon;
import com.bbn.bue.common.collections.ImmutableListMultitable;
import com.bbn.bue.common.collections.ImmutableMultitable;
import com.bbn.bue.common.collections.ImmutableSetMultitable;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.io.CharSource;
import com.google.common.io.LineProcessor;

import java.io.IOException;
import java.util.List;

/**
 * Loads a {@link com.bbn.bue.common.collections.Multitable} from a {@link CharSource}.
 *
 * The input is required to fall into three fields when split by {@link #fieldSplitter()}
 * (defaults to tab-separated).  The first of these is expected to be the row key, the second
 * the column key, and the third the value.  Multiple values may appear in one value field
 * if the optional {@link #valueListSplitter()} is specified to split them with.
 *
 *
 */
@MoveToBUECommon
@org.immutables.value.Value.Immutable
@TextGroupImmutable
public abstract class MultitableLoader<R, C, V> {
  public abstract Optional<Splitter> valueListSplitter();
  @org.immutables.value.Value.Default
  public Splitter fieldSplitter() {
    return Splitter.on("\t");
  }
  public abstract Function<String, ? extends R> rowInterpreter();
  public abstract Function<String, ? extends C> columnInterpreter();
  public abstract Function<String, ? extends V> valueInterpreter();

  public final ImmutableListMultitable<R, C, V> loadToListMultitable(CharSource source)
      throws IOException {
    final ImmutableListMultitable.Builder<R, C, V> ret =
        ImmutableListMultitable.builder();

    loadToMultitable(source, ret);

    return ret.build();
  }

  public final ImmutableSetMultitable<R, C, V> loadToSetMultitable(CharSource source)
      throws IOException {
    final ImmutableSetMultitable.Builder<R, C, V> ret =
        ImmutableSetMultitable.builder();

    loadToMultitable(source, ret);

    return ret.build();
  }

  public static <R,C,V> MultitableLoader.Builder<R,C,V> builder() {
    return new MultitableLoader.Builder<>();
  }

  public static MultitableLoader.Builder<String, String, String> builderForStrings() {
    return new MultitableLoader.Builder<String,String,String>()
        .rowInterpreter(Functions.<String>identity())
        .columnInterpreter(Functions.<String>identity())
        .valueInterpreter(Functions.<String>identity());
  }

  private void loadToMultitable(final CharSource source,
      final ImmutableMultitable.Builder<R, C, V> ret) throws IOException {
    source.readLines(new LineProcessor<Void>() {
      @Override
      public boolean processLine(final String line) throws IOException {
        final List<String> fields = fieldSplitter().splitToList(line);
        if (fields.size() == 3) {
          final R rowKey = interpret(fields.get(0), rowInterpreter(), "row key", line);
          final C columnKey = interpret(fields.get(1), columnInterpreter(), "column key", line);

          if (valueListSplitter().isPresent()) {
            for (final String value : valueListSplitter().get().split(fields.get(2))) {
              ret.put(rowKey, columnKey, interpret(value, valueInterpreter(), "value", line));
            }
          } else {
            ret.put(rowKey, columnKey, interpret(fields.get(2), valueInterpreter(), "value", line));
          }
        } else {
          throw new IOException("Cannot parse lines as multitable entries:\n" + line);
        }
        // we never stop procesisng lines early
        return true;
      }

      @Override
      public Void getResult() {
        return null;
      }
    });
  }

  private <T> T interpret(String field, Function<String, T> interpreter, String fieldName, String line)
      throws IOException {
    try {
      return interpreter.apply(field);
    } catch (Exception e) {
      throw new IOException("While parsing multitable line\n" + line + "\n failed to interpret " +
          fieldName + " " + field + " using " + interpreter);
    }
  }

  public static class Builder<R,C,V> extends ImmutableMultitableLoader.Builder<R,C,V> {}
}
