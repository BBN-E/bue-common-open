package com.bbn.nlp.clusters;

import com.bbn.bue.common.AbstractParameterizedModule;
import com.bbn.bue.common.ModuleFromParameter;
import com.bbn.bue.common.StringNormalizer;
import com.bbn.bue.common.parameters.Parameters;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import com.google.inject.Provides;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.inject.Singleton;

/**
 * Provides Brown {@link Clusters}. For backward compatability, {@link Clusters} itself is
 * bound to the Brown clusters specified by the {@code cluster_file} parameter.
 *
 * For newer code, we expose a map of Brown cluster keys to {@link Cluster} by looking for
 * namespaces of the form {@code com.bbn.serif.brownClusters.<key>}. Under that namespace we look
 * for a {@code cluster_file} parameter.
 * Loads Brown clusters from the "cluster file" parameter.
 */
@Beta
public final class ClustersFromParamsM extends AbstractParameterizedModule {

  private static final Logger log = LoggerFactory.getLogger(ClustersFromParamsM.class);

  static final String BASE_NAMESPACE =
      Parameters.joinNamespace("com", "bbn", "serif", "brownClusters");
  static final String OLD_CLUSTERS_PARAM = "cluster_file";

  public ClustersFromParamsM(final Parameters parameters) {
    super(parameters);
  }

  /**
   * Old way of binding Brown clusters, provided for backwards compatibility
   */
  @Provides
  @Singleton
  public Clusters getClusters(Parameters params) throws IOException {
    if (params.isPresent(OLD_CLUSTERS_PARAM)) {
      return Clusters.from(params.getExistingFile(OLD_CLUSTERS_PARAM));
    } else {
      // = absent
      return null;
    }
  }

  @Override
  public void configure() {
  }

  @Provides
  @Singleton
  @Clusters.BrownClustersP
  public Map<String, HierarchicalWordClustering> getClusterings(Injector injector) {
    final ImmutableMap.Builder<String, HierarchicalWordClustering> ret = ImmutableMap.builder();
    final Parameters brownClustersNamespace = params().copyNamespace(BASE_NAMESPACE);
    for (final String key : brownClustersNamespace.getStringList("activeKeys")) {
      try {
        final File clusterFile =
            params().getExistingFile(Parameters.joinNamespace(BASE_NAMESPACE, key,
                "clusterFile"));
        log.info("Loading Brown clusters {} from {}", key, clusterFile);

        final Clusters baseClusters = Clusters.from(clusterFile);
        final String normalizerParam = Parameters.joinNamespace(BASE_NAMESPACE, key, "normalizer");
        if (params().isPresent(normalizerParam)) {
          log.info("Normalizing Brown clusters {} with normalizer {}", key,
              params().getString(normalizerParam));
          final StringNormalizer normalizer =
              injector.createChildInjector(ModuleFromParameter.forParameter(
                  normalizerParam).extractFrom(params())).getInstance(StringNormalizer.class);
          ret.put(key, HierarchicalWordClusterings.normalizeClustering(baseClusters, normalizer));
        } else {
          ret.put(key, baseClusters);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return ret.build();
  }
}


