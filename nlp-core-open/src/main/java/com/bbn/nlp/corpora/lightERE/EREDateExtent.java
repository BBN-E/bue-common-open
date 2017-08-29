package com.bbn.nlp.corpora.lightERE;

/**
 * @author Jay DeYoung
 */
public final class EREDateExtent extends ERELocatedString {

  protected EREDateExtent(final String source,
      final int offset,
      final int endOffset,
      final int length, final String date_content) {
    super(source, date_content, offset, endOffset, length);
  }

  public String getDateContent() {
    return trigger;
  }

  public static EREDateExtent from(final String source, final int offset, final int length,
      final String content) {
    final int endOffset = calculateEndOffset(offset, length);
    return new EREDateExtent(source, offset, endOffset, length, content);
  }

  public static EREDateExtent fromPreciseOffsets(final String source, final int start,
      final int end, final String date_content) {
    return new EREDateExtent(source, start, end, calculateLength(start, end), date_content);
  }
}

