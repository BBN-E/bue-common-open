package com.bbn.nlp.clusters;

import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Optional;

import java.util.Collection;

/**
 * Represents a hierarchical clustering, such as the Brown clustering used by Serif for
 * name-finding, etc. For greater flexibility, prefer to use this for an interface in your code
 * instead of referring to {@link Clusters} directly.
 **/
public interface HierarchicalWordClustering {

  /**
   * Gets the {@link Cluster} a word belongs to, if any.
   */
  Optional<Cluster> getClusterForWord(final Symbol word);

  /**
   * Gets what words occur in a {@link Cluster}.  Note that if this {@code
   * HierarchicalWordClustering} applies any sort of normalization during lookup, it probably will
   * not be (but may be) taken into account when getting the words for a cluster.
   */
  Collection<Symbol> getWords(final Cluster cluster);

  /**
   * Gets what words occur in in the clustering at the specified bit depth of {@link Cluster}. Note
   * that if this {@code HierarchicalWordClustering} applies any sort of normalization during
   * lookup, it probably will not be (but may be) taken into account when getting the words for a
   * cluster.
   */
  Collection<Symbol> getWords(final Cluster cluster, final int nBits);

}
