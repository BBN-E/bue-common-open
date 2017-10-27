package com.bbn.nlp.corpora.eventNugget;

import com.bbn.bue.common.TextGroupImmutable;

import com.google.common.collect.ImmutableList;

import org.immutables.func.Functional;
import org.immutables.value.Value;

@Value.Immutable(prehash = true)
@Functional
@TextGroupImmutable
public abstract class NuggetHopper {

  @Value.Parameter
  public abstract String id();

  @Value.Parameter
  public abstract ImmutableList<NuggetEventMention> eventMentions();

  public static class Builder extends ImmutableNuggetHopper.Builder {

  }
}
