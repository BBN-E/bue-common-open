package com.bbn.nlp.corpora.eventNugget;

import com.bbn.bue.common.TextGroupImmutable;
import com.bbn.bue.common.symbols.Symbol;
import com.bbn.nlp.corpora.ere.ERESpan;

import org.immutables.func.Functional;
import org.immutables.value.Value;

/**
 * @author Yee Seng Chan
 */
@Value.Immutable(prehash = true)
@Functional
@TextGroupImmutable
public abstract class NuggetEventMention {

  @Value.Parameter
  public abstract String id();

  @Value.Parameter
  public abstract Symbol type();

  @Value.Parameter
  public abstract Symbol subtype();

  @Value.Parameter
  public abstract Symbol realis();

  @Value.Parameter
  public abstract ERESpan trigger();

  public static class Builder extends ImmutableNuggetEventMention.Builder {

  }
}
