package com.bbn.nlp.clusters;

import com.bbn.bue.common.converters.StringToOSFile;
import com.bbn.bue.common.parameters.Parameters;
import com.bbn.bue.common.serialization.jackson.ImmutableMapProxy;
import com.bbn.bue.common.symbols.Symbol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.inject.Qualifier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a Brown clustering, such as those used by Serif for name-finding, etc. Generally
 * your code should prefer to refer to {@link HierarchicalWordClustering} instead of to this
 * directly.
 */
public final class Clusters implements Serializable, HierarchicalWordClustering {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(Clusters.class);

  private final ImmutableMap<Symbol, Cluster> clustersByWord;
  private transient final Multimap<Symbol, Symbol> wordsByCluster = HashMultimap.create();
  private transient final Set<Integer> cachedBitLevels = Sets.newHashSet();

  // cached hashcode
  private int hashCode = 0;

  private Clusters(final ImmutableMap<Symbol, Cluster> clustersByWord) {
    this.clustersByWord = checkNotNull(clustersByWord);
  }

  public static Clusters from(final Parameters params) throws IOException {
    return loadClusters(params.getExistingFile("cluster_file"));
  }

  public static Clusters from(final String clusterFileString) throws IOException {
    final File clusterFile = new StringToOSFile().decode(clusterFileString);
    return loadClusters(clusterFile);
  }

  /*
   * Loads clusters from a file where each line has a word followed by a space
   * followed by its cluster ID. Behavior is undefined if a word appears
   * in multiple clusters.
   */
  public static Clusters from(final File clusterFile) throws IOException {
    return loadClusters(clusterFile);
  }

  public static Clusters createEmptyForTesting() {
    return new Clusters(ImmutableMap.<Symbol, Cluster>of());
  }

  public int size() {
    return clustersByWord.size();
  }

  private static Clusters loadClusters(final File clusterFile) throws IOException {
    final ImmutableMap.Builder<Symbol, Cluster> clustsByWord = ImmutableMap.builder();

    for (final String line : Files.readLines(clusterFile, Charsets.UTF_8)) {
      final String[] wordClustPair = line.trim().split(" ");
      if (wordClustPair.length < 2) {
        continue;
      }
      final Symbol word = Symbol.from(wordClustPair[0]);
      final String clust = wordClustPair[1];
      clustsByWord.put(word, Cluster.fromString(clust));
    }

    return new Clusters(clustsByWord.build());
  }

  /**
   * Returns a Cluster object for the cluster containing the word, otherwise absent.
   */
  @Override
  public Optional<Cluster> getClusterForWord(final Symbol word) {
    return Optional.fromNullable(clustersByWord.get(word));
  }

  /**
   * Gets all words in a cluster.
   */
  @Override
  public Collection<Symbol> getWords(final Cluster cluster) {
    return getWordCache(cluster.bits()).get(cluster.asSymbol());
  }

  /*
   * Takes the {@code nBits}-length prefix of {@code cluster}'s ID
   * and returns all words in all clusters with that prefix.
   */
  @Override
  public Collection<Symbol> getWords(final Cluster cluster, final int nBits) {
    checkArgument(nBits > 0);
    checkArgument(cluster.bits() >= nBits);
    return getWordCache(nBits).get(cluster.asSymbolTruncatedToNBits(nBits));
  }

  private Multimap<Symbol, Symbol> getWordCache(final int nBits) {
    if (!cachedBitLevels.contains(nBits)) {
      for (final Map.Entry<Symbol, Cluster> entry : clustersByWord.entrySet()) {
        final Cluster cluster = entry.getValue();
        final Symbol word = entry.getKey();

        if (cluster.bits() >= nBits) {
          wordsByCluster.put(cluster.asSymbolTruncatedToNBits(nBits), word);
        }
      }
    }
    return wordsByCluster;
  }

  // Jackson serialization ugliness
  // needed because Jackson can't serialize maps
  @JsonProperty("clustersByWord")
  private ImmutableMapProxy<Symbol, Cluster> clustersByWordForJackson() {
    return ImmutableMapProxy.forMap(clustersByWord);
  }

  @JsonCreator
  private static Clusters createFromJson(
      @JsonProperty("clustersByWord") ImmutableMapProxy<Symbol, Cluster> clustersByWord) {
    return new Clusters(clustersByWord.toImmutableMap());
  }

  @Override
  public int hashCode() {
    // this will keep recomputing if the hashcode really is zero, but this is very unlikely
    if (hashCode == 0) {
      // words by cluster is just a reversed view of this
      hashCode = Objects.hash(clustersByWord);
    }
    return hashCode;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final Clusters other = (Clusters) obj;
    // words by cluster is just a reversed view of clusters by word
    // other fields just for caching
    return Objects.equals(this.clustersByWord, other.clustersByWord);
  }

  @Qualifier
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface BrownClustersP {

  }

}

