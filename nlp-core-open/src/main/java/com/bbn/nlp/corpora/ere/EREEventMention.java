package com.bbn.nlp.corpora.ere;


import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;


public final class EREEventMention {
  private final String id;
  private final String type;
  private final String subtype;
  private final String realis;
  private final ERESpan trigger;
  private final ImmutableList<EREArgument> arguments;

  private EREEventMention(final String id, final String type, final String subtype, final String realis,
      final ERESpan trigger, final List<EREArgument> arguments) {
    this.id = checkNotNull(id);
    this.type = checkNotNull(type);
    this.subtype = checkNotNull(subtype);
    this.realis = checkNotNull(realis);
    this.trigger = checkNotNull(trigger);
    this.arguments = ImmutableList.copyOf(arguments);
  }

  public String getID() {
    return id;
  }

  public String getType() {
    return type;
  }

  public String getSubtype() {
    return subtype;
  }

  public String getRealis() {
    return realis;
  }

  public ERESpan getTrigger() {
    return trigger;
  }

  public ImmutableList<EREArgument> getArguments() {
    return arguments;
  }

  public static Builder builder(final String id, final String type, final String subtype, final String realis, final ERESpan trigger) {
    return new Builder(id, type, subtype, realis, trigger);
  }

  public static class Builder {
    private final String id;
    private final String type;
    private final String subtype;
    private final String realis;
    private final ERESpan trigger;
    private final List<EREArgument> arguments;

    private Builder(final String id, final String type, final String subtype, final String realis, final ERESpan trigger) {
      this.id = checkNotNull(id);
      this.type = checkNotNull(type);
      this.subtype = checkNotNull(subtype);
      this.realis = checkNotNull(realis);
      this.trigger = checkNotNull(trigger);
      this.arguments = Lists.newArrayList();
    }

    public EREEventMention build() {
      return new EREEventMention(id, type, subtype, realis, trigger, arguments);
    }

    public Builder withArgument(final EREArgument arg) {
      checkNotNull(arg);
      this.arguments.add(arg);
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
    final EREEventMention other = (EREEventMention) obj;
    return Objects.equal(id, other.id);
  }

  @Override
  public String toString() {
    return "EREEventMention{" +
        "id='" + id + '\'' +
        ", type='" + type + '\'' +
        ", subtype='" + subtype + '\'' +
        ", realis='" + realis + '\'' +
        ", trigger=" + trigger +
        ", arguments=" + arguments +
        '}';
  }
}

