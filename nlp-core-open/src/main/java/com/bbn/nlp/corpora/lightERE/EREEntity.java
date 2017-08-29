package com.bbn.nlp.corpora.lightERE;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author Jay DeYoung
 */
public final class EREEntity {

  // xml entities
  private final List<EREEntityMention> mentions;

  // xml attributes
  private final TYPE type;
  private final String id;
  private final String name;

  public EREEntity(final List<EREEntityMention> mentions, final TYPE type, final String id,
      final String name) {
    this.mentions = mentions;
    this.type = type;
    this.id = id;
    this.name = name;
  }

  public List<EREEntityMention> getMentions() {
    return mentions;
  }

  public TYPE getType() {
    return type;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public enum TYPE {
    PER,
    ORG,
    LOC,
    GPE,
    TITLE
  }

  @Override
  public String toString() {
    return "EREEntity{" +
        "mentions=" + mentions +
        ", type=" + type +
        ", id='" + id + '\'' +
        ", name='" + name + '\'' +
        '}';
  }

  public static Builder builder(final String id, final String type) {
    return new Builder(id, TYPE.valueOf(type));
  }

  public static Builder builder(final String id, final TYPE type) {
    return new Builder(id, type);
  }

  public static class Builder {

    private List<EREEntityMention> mentions = Lists.newArrayList();
    private TYPE type;
    private String id;
    private String name;

    public Builder(final String id, final TYPE type) {
      this.id = id;
      this.type = type;
    }

    public Builder withMentions(final List<EREEntityMention> mentions) {
      this.mentions = mentions;
      return this;
    }

    public Builder setType(final TYPE type) {
      this.type = type;
      return this;
    }

    public Builder setId(final String id) {
      this.id = id;
      return this;
    }

    public Builder setName(final String name) {
      this.name = name;
      return this;
    }

    public EREEntity build() {
      return new EREEntity(this.mentions, this.type, this.id, this.name);
    }

    public Builder withMention(final EREEntityMention entityMention) {
      mentions.add(entityMention);
      return this;
    }
  }
}

