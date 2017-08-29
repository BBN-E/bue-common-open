package com.bbn.nlp.clusters;

import com.bbn.bue.common.StringNormalizer;
import com.bbn.bue.common.TextGroupImmutable;
import com.bbn.bue.common.symbols.Symbol;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;

import org.immutables.value.Value;

import java.util.Collection;

/**
 * Utilities for working with {@link HierarchicalWordClusterings}
 */
public final class HierarchicalWordClusterings {

  private HierarchicalWordClusterings() {
    throw new UnsupportedOperationException();
  }

  /**
   * Creates another {@link HierarchicalWordClustering} where {@code normalizer} is applied before
   * lookup.
   */
  public static HierarchicalWordClustering normalizeClustering(
      HierarchicalWordClustering clustering,
      StringNormalizer normalizer) {
    return new NormalizedWordClustering.Builder().innerClustering(clustering)
        .stringNormalizer(normalizer).build();
  }
}

@Value.Immutable
@TextGroupImmutable
@JsonSerialize(as = ImmutableNormalizedWordClustering.class)
@JsonDeserialize(as = ImmutableNormalizedWordClustering.class)
abstract class NormalizedWordClustering implements HierarchicalWordClustering {

  public abstract HierarchicalWordClustering innerClustering();

  public abstract StringNormalizer stringNormalizer();

  @Override
  public Optional<Cluster> getClusterForWord(final Symbol word) {
    return innerClustering()
        .getClusterForWord(Symbol.from(stringNormalizer().normalize(word.asString())));
  }

  @Override
  public Collection<Symbol> getWords(final Cluster cluster) {
    return innerClustering().getWords(cluster);
  }

  @Override
  public Collection<Symbol> getWords(final Cluster cluster, final int nBits) {
    return innerClustering().getWords(cluster, nBits);
  }

  public static class Builder extends ImmutableNormalizedWordClustering.Builder {

  }
}
