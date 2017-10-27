package com.bbn.nlp.corpora.lightERE;

/**
 * @author Jay DeYoung
 */
public abstract class ERELocatedString {

  protected final String source;
  protected final String trigger;
  protected final int offset;
  protected final int endOffset;
  protected final int length;

  protected ERELocatedString(final String source, final String trigger, final int offset,
      final int endOffset,
      final int length) {
    this.source = source;
    this.trigger = trigger;
    this.offset = offset;
    this.endOffset = endOffset;
    this.length = length;
  }

  public String getSource() {
    return source;
  }

  public String getTrigger() {
    return trigger;
  }

  public int getOffset() {
    return offset;
  }

  public int getLength() {
    return length;
  }

  public int getEndOffset() {
    return endOffset;
  }

  protected static int calculateEndOffset(final int offset, final int length) {
    return offset + length - 1;
  }

  // inverse of calculate end offset
  protected static int calculateLength(final int start, final int end) {
    return end - start;
  }

  @Override
  public String toString() {
    return "ERELocatedString{" +
        "source='" + source + '\'' +
        ", trigger='" + trigger + '\'' +
        ", offset=" + offset +
        ", endOffset=" + endOffset +
        ", length=" + length +
        '}';
  }
}
