package com.bbn.nlp.corpora.ere;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class EREEvent implements Iterable<EREEventMention> {
  private final String id;
  private ImmutableList<EREEventMention> eventMentions;

  private EREEvent(final String id, final List<EREEventMention> eventMentions) {
    this.id = checkNotNull(id);
    this.eventMentions = ImmutableList.copyOf(eventMentions);
  }

  @Override
  public Iterator<EREEventMention> iterator() {
    return eventMentions.iterator();
  }

  public String getID() {
    return id;
  }

  public ImmutableList<EREEventMention> getEventMentions() {
    return eventMentions;
  }

  public static Builder builder(final String id) {
    return new Builder(id);
  }

  public static class Builder {
    private final String id;
    private final List<EREEventMention> eventMentions;

    private Builder(final String id) {
      this.id = checkNotNull(id);
      this.eventMentions = Lists.newArrayList();
    }

    public EREEvent build() {
      return new EREEvent(id, eventMentions);
    }

    public Builder withEventMention(final EREEventMention em) {
      checkNotNull(em);
      this.eventMentions.add(em);
      return this;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final EREEvent other = (EREEvent) obj;
    return Objects.equal(id, other.id);
  }

  @Override
  public String toString() {
    return "EREEvent{" +
        "id='" + id + '\'' +
        ", eventMentions=" + eventMentions +
        '}';
  }
}

