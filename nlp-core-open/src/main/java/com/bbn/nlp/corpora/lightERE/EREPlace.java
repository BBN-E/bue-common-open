package com.bbn.nlp.corpora.lightERE;

import com.google.common.base.Optional;

/**
 * @author Jay DeYoung
 */
public final class EREPlace {

  private final Optional<String> type;
  private final String entity_mention_id;

  private EREPlace(final Optional<String> type, final String entity_mention_id) {
    this.type = type;
    this.entity_mention_id = entity_mention_id;
  }

  public Optional<String> getType() {
    return type;
  }

  public String getEntity_mention_id() {
    return entity_mention_id;
  }

  @Override
  public String toString() {
    return "EREPlace{" +
        "type=" + type +
        ", entity_mention_id='" + entity_mention_id + '\'' +
        '}';
  }

  public static EREPlace from(final Optional<String> type, final String entity_mention_id) {
    return new EREPlace(type, entity_mention_id);
  }
}

