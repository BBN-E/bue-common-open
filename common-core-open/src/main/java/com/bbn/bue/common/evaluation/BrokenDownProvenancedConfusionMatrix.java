package com.bbn.bue.common.evaluation;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

@Beta
public final class BrokenDownProvenancedConfusionMatrix<SignatureType, ProvenanceType> {

  private ImmutableMap<SignatureType, ProvenancedConfusionMatrix<ProvenanceType>> data;

  private BrokenDownProvenancedConfusionMatrix(
      Map<SignatureType, ProvenancedConfusionMatrix<ProvenanceType>> data) {
    this.data = ImmutableMap.copyOf(data);
  }

  public static <SignatureType, ProvenanceType> BrokenDownProvenancedConfusionMatrix fromMap(
      Map<SignatureType, ProvenancedConfusionMatrix<ProvenanceType>> data) {
    return new BrokenDownProvenancedConfusionMatrix<SignatureType, ProvenanceType>(data);
  }


  public Map<SignatureType, ProvenancedConfusionMatrix<ProvenanceType>> asMap() {
    return data;
  }

  public BrokenDownSummaryConfusionMatrix toSummary() {
    final ImmutableMap.Builder<SignatureType, SummaryConfusionMatrix> ret = ImmutableMap.builder();
    for (final Map.Entry<SignatureType, ProvenancedConfusionMatrix<ProvenanceType>> entry : data
        .entrySet()) {
      ret.put(entry.getKey(), entry.getValue().buildSummaryMatrix());
    }
    return BrokenDownSummaryConfusionMatrix.fromMap(ret.build());
  }
}
