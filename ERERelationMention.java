package com.bbn.nlp.corpora.ere;


import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public final class ERERelationMention {
  private final String id;
  private final String realis;
  private final Optional<ERESpan> trigger;
  private final ImmutableMap<String, EREArgument> arguments;
  
  private ERERelationMention(final String id, final String realis, final Optional<ERESpan> trigger, 
      final Map<String, EREArgument> arguments) {
    this.id = checkNotNull(id);
    this.realis = checkNotNull(realis);
    this.trigger = trigger;
    this.arguments = ImmutableMap.copyOf(arguments);
  }
  
  public String getID() {
    return id;
  }
 
  public String getRealis() {
    return realis;
  }

  public Optional<ERESpan> getTrigger() {
    return trigger;
  }

  public Optional<EREArgument> getArgument(final String role) {
    if(arguments.containsKey(role)) {
      return Optional.of(arguments.get(role));
    }
    else {
      return Optional.absent();
    }
  }
  
  public ImmutableMap<String, EREArgument> getArguments() {
    return arguments;
  }
   
  public static Builder builder(final String id, final String realis, final Optional<ERESpan> trigger) {
    return new Builder(id, realis, trigger);
  }
  
  public static class Builder {
    private final String id;
    private final String realis;
    private final Optional<ERESpan> trigger;
    private final Map<String, EREArgument> arguments;

    private Builder(final String id, final String realis, final Optional<ERESpan> trigger) {
      this.id = checkNotNull(id);
      this.realis = checkNotNull(realis);
      this.trigger = trigger;
      this.arguments = Maps.newHashMap();
    }

    public ERERelationMention build() {
      return new ERERelationMention(id, realis, trigger, arguments);
    }

    public Builder withArgument(final String role, final EREArgument arg) {
      checkNotNull(role);
      checkNotNull(arg);
      arguments.put(role, arg);
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
    final ERERelationMention other = (ERERelationMention) obj;
    return Objects.equal(id, other.id);
  }
  
}

