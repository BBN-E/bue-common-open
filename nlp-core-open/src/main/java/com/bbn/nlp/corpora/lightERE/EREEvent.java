package com.bbn.nlp.corpora.lightERE;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author Jay DeYoung
 */
public final class EREEvent {

  private final String id;
  private final String name;
  private final List<EREEventMention> events;

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public List<EREEventMention> getEvents() {
    return events;
  }

  public EREEvent(final String id, final String name, final List<EREEventMention> events) {
    this.id = id;
    this.name = name;
    this.events = events;
  }

  @Override
  public String toString() {
    return "EREEvent{" +
        "id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", \nevents=" + events +
        "\n}";
  }

  public static Builder builder(final String id, final String name) {
    return new Builder(id, name);
  }

  public static class Builder {

    private String id;
    private String name;
    private List<EREEventMention> events = Lists.newArrayList();

    public Builder(final String id,
        final String name) {
      this.id = id;
      this.name = name;
    }

    public Builder setId(final String id) {
      this.id = id;
      return this;
    }

    public Builder setName(final String name) {
      this.name = name;
      return this;
    }

    public Builder withEventMention(final EREEventMention eventMention) {
      this.events.add(eventMention);
      return this;
    }

    public EREEvent build() {
      return new EREEvent(this.id, this.name, this.events);
    }
  }
}

