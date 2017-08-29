package com.bbn.nlp.corpora.lightERE;

/**
 * @author Jay DeYoung
 */
public final class EREArg {

  private final String entity_mention_id;
  private final String entity_id;
  private final String type;
  private final String text;

  private EREArg(final String entity_mention_id, final String entity_id, final String type,
      final String text) {
    this.entity_mention_id = entity_mention_id;
    this.entity_id = entity_id;
    this.type = type;
    this.text = text;
  }

  public String getEntityMentionId() {
    return entity_mention_id;
  }

  public String getEntityId() {
    return entity_id;
  }

  public String getType() {
    return type;
  }

  public String getText() {
    return text;
  }

  public static EREArg from(final String entity_mention_id, final String entity_id,
      final String type, final String text) {
    return new EREArg(entity_mention_id, entity_id, type, text);
  }

  @Override
  public String toString() {
    return "EREArg{" +
        "entity_mention_id='" + entity_mention_id + '\'' +
        ", entity_id='" + entity_id + '\'' +
        ", type='" + type + '\'' +
        '}';
  }
}

