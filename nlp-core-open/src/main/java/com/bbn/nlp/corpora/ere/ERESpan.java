package com.bbn.nlp.corpora.ere;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ERESpan {
  public final int start;
  public final int end;
  public final String text;

  private ERESpan(final int start, final int end, final String text) {
    this.start = start;
    this.end = end;
    this.text = checkNotNull(text);
  }

  public static ERESpan from(final int start, final int end, final String text) {
    return new ERESpan(start, end, text);
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  public String getText() {
    return text;
  }

  @Override
  public String toString() {
    return "ERESpan{" +
        "start=" + start +
        ", end=" + end +
        ", text='" + text + '\'' +
        '}';
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(start, end, text);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }

    ERESpan other = (ERESpan) obj;
    return (start==other.start) && (end==other.end) && text.equals(other.text);
  }
}
