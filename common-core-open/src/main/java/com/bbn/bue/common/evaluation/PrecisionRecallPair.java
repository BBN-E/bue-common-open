package com.bbn.bue.common.evaluation;

import static com.google.common.base.Preconditions.checkArgument;

public final class PrecisionRecallPair extends FMeasureInfo {
	public PrecisionRecallPair(float precision, float recall) {
		checkArgument(precision > 0.0);
		checkArgument(recall > 0.0);
		
		this.precision = precision;
		this.recall = recall;
	}
	
	public float precision() {
		return precision;
	}
	
	public float recall() {
		return recall;
	}
	
	private final float precision;
	private final float recall;
}
