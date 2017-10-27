package com.bbn.nlp.corpora.eventNugget;

import com.bbn.bue.common.TextGroupImmutable;

import com.google.common.collect.ImmutableList;

import org.immutables.func.Functional;
import org.immutables.value.Value;

/**
 * @author Yee Seng Chan
 */
@Value.Immutable(prehash = true)
@Functional
@TextGroupImmutable
public abstract class NuggetDocument {

  @Value.Parameter
  public abstract String kitId();

  @Value.Parameter
  public abstract String docId();

  @Value.Parameter
  public abstract SourceType sourceType();

  @Value.Parameter
  public abstract ImmutableList<NuggetHopper> hoppers();

  public enum SourceType {
    newswire,
    multi_post
  }

  public static class Builder extends ImmutableNuggetDocument.Builder {

  }
}
