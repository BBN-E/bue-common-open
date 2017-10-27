package com.bbn.nlp.corpora.lightERE;

/**
 * @author Jay DeYoung
 */
public final class EREEntityMention extends ERELocatedString {

  private final String id;
  private final NOUNTYPES noun_type;
  private final String text;

  private EREEntityMention(final String id,
      final NOUNTYPES noun_type, final String source,
      final int offset, final int endOffset, final int length, final String text) {
    super(source, text, offset, endOffset, length);
    this.id = id;
    this.noun_type = noun_type;
    this.text = text;
  }

  public String getId() {
    return id;
  }

  public NOUNTYPES getNounType() {
    return noun_type;
  }

  public String getText() {
    return text;
  }

  public static EREEntityMention from(final String id, final String type, final String source,
      final int offset,
      final int length,
      final String text) {
    final int endOffset = calculateEndOffset(offset, length);
    return new EREEntityMention(id, NOUNTYPES.valueOf(type), source, offset, endOffset, length,
        text);
  }

  public static EREEntityMention fromPreciseOffsets(final String id, final NOUNTYPES type,
      final String source, final int start, final int end, final String text) {
    final int length = calculateLength(start, end);
    return new EREEntityMention(id, type, source, start, end, length, text);
  }

  public enum NOUNTYPES {
    NOM,
    NAM,
    PRO,
    NA,
  }

  @Override
  public String toString() {
    return "EREEntityMention{" +
        "id='" + id + '\'' +
        ", noun_type=" + noun_type +
        ", text='" + text + '\'' +
        ", located='" + super.toString() + "'" +
        '}';
  }
}

