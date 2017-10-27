package com.bbn.nlp.corpora.lightERE;

/**
 * @author Jay DeYoung
 */
public final class ERETrigger extends ERELocatedString {


  protected ERETrigger(final String source, final String trigger, final int offset,
      final int endOffset,
      final int length) {
    super(source, trigger, offset, endOffset, length);
  }

  public static ERETrigger from(final String source, final String trigger, final int offset,
      final int length) {
    final int endOffset = calculateEndOffset(offset, length);
    return new ERETrigger(source, trigger, offset, endOffset, length);
  }

  public static ERETrigger fromPreciseOffsets(final String source, final String trigger,
      final int startOffset,
      final int endOffset) {
    return new ERETrigger(source, trigger, startOffset, endOffset,
        calculateLength(startOffset, endOffset));
  }
}
