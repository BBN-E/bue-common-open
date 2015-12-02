package com.bbn.nlp.corpora.ere;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

public final class OffsetInfo {
  private final ImmutableList<OffsetSpan> spans;

  private OffsetInfo(final List<OffsetSpan> spans) {
    this.spans = ImmutableList.copyOf(spans);
  }


  public Optional<OffsetSpan> findSpan(final int offset) {
    for(final OffsetSpan span : spans) {
      if(span.isInSpan(offset)) {
        return Optional.of(span);
      }
    }
    return Optional.<OffsetSpan>absent();
  }

  // find the span the offset is in, then apply offsetAdjustor. If the offset is not within any span, return Optional.absent()
  public Optional<Integer> transformOffset(final int offset) {
    final Optional<OffsetSpan> span = findSpan(offset);
    if(span.isPresent()) {
      final Optional<Integer> adjustor = span.get().getOffsetAdjustor();
      if(adjustor.isPresent()) {
        return Optional.of(offset + adjustor.get());
      }
      else {
        return Optional.of(offset);
      }
    }
    else {
      return Optional.<Integer>absent();
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static OffsetInfo constantAdjustmentFor(final String sourceText, int constantAdjusment) {
    return OffsetInfo.builder().withSpan(OffsetSpan.builder(0, sourceText.length())
        .withOffset(Optional.of(constantAdjusment)).build()).build();
  }

  public static class Builder {
    private final List<OffsetSpan> spans;

    private Builder() {
      spans = Lists.newArrayList();
    }

    public OffsetInfo build() {
      return new OffsetInfo(spans);
    }

    public Builder withSpan(final OffsetSpan span) {
      spans.add(span);
      return this;
    }
  }

  public String toString() {
    StringBuffer s = new StringBuffer("");

    for(final OffsetSpan span : spans) {
      s.append(span.toString() + "\n");
    }

    return s.toString();
  }

  public static class OffsetSpan {
    private final int start;
    private final int end;
    private final Optional<Integer> offsetAdjustor;   // either positive or negative to offset the start,end

    private OffsetSpan(final int start, final int end, final Optional<Integer> offsetAdjustor) {
      this.start = start;
      this.end = end;
      this.offsetAdjustor = offsetAdjustor;
    }

    public int getStart() {
      return start;
    }

    public int getEnd() {
      return end;
    }

    public Optional<Integer> getOffsetAdjustor() {
      return offsetAdjustor;
    }

    public boolean isInSpan(final int offset) {
      return (start <= offset) && (offset <= end);
    }

    public String toString() {
      final String baseRet = "(" + start + ", " + end + ")";
      if (offsetAdjustor.isPresent()) {
        final int adjustedEnd = end + offsetAdjustor.get();
        final int adjustedStart = start + offsetAdjustor.get();
        return baseRet + " ---> " + "(" + adjustedStart + ", " + adjustedEnd + ")";
      } else {
        return baseRet;
      }
    }

    public static OffsetSpan.Builder builder(final int start, final int end) {
      return new OffsetSpan.Builder(start, end);
    }

    public static class Builder {
      private final int start;
      private final int end;
      private Optional<Integer> offsetAdjustor;

      private Builder(final int start, final int end) {
        this.start = start;
        this.end = end;
        this.offsetAdjustor = Optional.<Integer>absent();
      }

      public OffsetSpan build() {
        return new OffsetSpan(start, end, offsetAdjustor);
      }

      public Builder withOffset(final Optional<Integer> offsetAdjustor) {
        this.offsetAdjustor = offsetAdjustor;
        return this;
      }
    }
  }


}

