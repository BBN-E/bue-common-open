package com.bbn.nlp.clusters;

import com.bbn.bue.common.symbols.Symbol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a single Brown cluster. Brown clusters are identified by binary strings (cluster IDs)
 * which always begin with 1 (e.g. 10011). 0 is not a valid cluster. Two {@code Cluster} objects are
 * equal iff they have the same cluster ID.
 *
 * @author rgabbard
 */
public final class Cluster {

  @JsonCreator
  private Cluster(@JsonProperty("clusterID") final Symbol clusterID) {
    this.cluster = clusterID;

  }


  /*
   * Create a cluster from a binary string.
   */
  public static Cluster fromString(final String clusterID) {
    checkNotNull(clusterID);
    checkArgument(!"0".equals(clusterID));
    for (int i = 0; i < clusterID.length(); ++i) {
      final char c = clusterID.charAt(i);
      checkArgument(c == '0' || c == '1');
    }
    return new Cluster(Symbol.from(clusterID));
  }

  /*
   * Returns a String representation of the cluster ID.
   */
  @JsonProperty("clusterID")
  public Symbol asSymbol() {
    return cluster;
  }

  public Symbol asSymbolTruncatedToNBits(final int numBits) {
    if (bits() >= numBits) {
      return Symbol.from(cluster.toString().substring(0, numBits));
    } else {
      return cluster;
    }


  }

  public List<Symbol> asSymbolTruncatedToNBits(final Iterable<Integer> bitLevels) {
    ImmutableList.Builder<Symbol> builder = new ImmutableList.Builder<Symbol>();
    for (Integer i : bitLevels) {
      builder.add(this.asSymbolTruncatedToNBits(i));
    }
    return builder.build();
  }

  public Symbol asSymbolTruncatedToNBitsSerifCompatible(final int numBits) {
    if (bits() >= numBits) {
      return Symbol.from(cluster.toString().substring(0, numBits));
    } else {
      return Symbol.from("0");

    }


  }

  /*
   * The number of bits in the cluster ID.
   */
  public int bits() {
    return cluster.toString().length();
  }

  private final Symbol cluster;

  @Override
  public int hashCode() {
    return cluster.hashCode();
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
    final Cluster other = (Cluster) obj;
    if (cluster != other.cluster) {
      return false;
    }
    return true;
  }
}
