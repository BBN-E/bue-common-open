package com.bbn.nlp.coreference.measures;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.bbn.bue.common.CollectionUtilities;
import com.bbn.bue.common.EquivalenceUtils;
import com.bbn.bue.common.evaluation.FMeasureCounts;
import com.bbn.bue.common.evaluation.FMeasureInfo;
import com.bbn.bue.common.evaluation.PrecisionRecallPair;
import com.google.common.base.Equivalence;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Equivalence.Wrapper;

public final class B3Scorer<T> {
	public enum B3Method { ByCluster, ByElement };
	
    private B3Scorer(Equivalence<T> equivalence, B3Method method) {
    	this.equivalence = checkNotNull(equivalence);
    	this.method = checkNotNull(method);
    }
    
    public static <T> B3Scorer<T> create(B3Method method) {
    	return new B3Scorer<T>(null, method);
    }
    
    public static <T> B3Scorer<T> createWithEquivalence(
    	final Equivalence<T> equivalence, B3Method method) 
    {
    	return new B3Scorer<T>(new EquivalenceUtils.DefaultEquivalence<T>(), method);
    }
    
	public FMeasureInfo score(Iterable<? extends Iterable<T>> predicted, 
			Iterable<? extends Iterable<T>> gold) 
	{
		final Iterable<? extends Set<Wrapper<T>>> predictedAsSets = 
				toIterableOfSetsOfWrappers(predicted);
		final Iterable<? extends Set<Wrapper<T>>> goldAsSets = 
				toIterableOfSetsOfWrappers(gold);
		
		if (method == B3Method.ByElement) {
			return scoreWrappedByElement(predictedAsSets, goldAsSets);
		} else {
			throw new RuntimeException("B3Method.ByCluster not yet implemented");
		}
	}
	
	private <Q> FMeasureInfo scoreWrappedByElement(Iterable<? extends Set<Q>> predicted, 
			Iterable<? extends Set<Q>> gold) 
	{
		final Map<Q, ? extends Set<Q>> predictedItemToGroup =
			CollectionUtilities.makeElementsToContainersMap(predicted);
		final Map<Q, ? extends Set<Q>> goldItemToGroup =
			CollectionUtilities.makeElementsToContainersMap(gold);
				
		checkPartitionsOverSameElements(predictedItemToGroup.keySet(), 
				goldItemToGroup.keySet());
		
		// if this is empty, we know the other is too,
		// by the above
		if (predictedItemToGroup.isEmpty()) {
			return new PrecisionRecallPair(0.0f, 0.0f);
		}
		
		double precisionTotal = 0.0;
		double recallTotal = 0.0;
		for (final Q item : goldItemToGroup.keySet()) {
			final Set<Q> goldGroup = goldItemToGroup.get(item);
			final Set<Q> predictedGroup = predictedItemToGroup.get(item);
			final Set<Q> inBoth = Sets.intersection(goldGroup, predictedGroup);
			
			precisionTotal += inBoth.size() / ((double)predictedGroup.size());
			recallTotal += inBoth.size() / ((double)goldGroup.size());
		}
		
		return new PrecisionRecallPair(
				(float)(precisionTotal/goldItemToGroup.keySet().size()),
				(float)(recallTotal/goldItemToGroup.keySet().size()));
	}

	private <Q> void checkPartitionsOverSameElements(
			final Set<Q> predictedItems,
			final Set<Q> goldItems) {
		if (!predictedItems.equals(goldItems)) {
			final Set<Q> predictedButNotGold = Sets.difference(
				predictedItems, goldItems);
			final Set<Q> goldButNotPredicted = Sets.difference(
					goldItems, predictedItems);
			throw new RuntimeException(String.format(
				"Elements in partitions must match. In predicted but not gold: %s. In gold but not predicted: %s",
				predictedButNotGold, goldButNotPredicted));
		}
	}
	
	private Iterable<? extends Set<Wrapper<T>>> toIterableOfSetsOfWrappers(
			Iterable<? extends Iterable<T>> iterables) 
	{
		final ImmutableList.Builder<Set<Equivalence.Wrapper<T>>> ret = 
				ImmutableList.builder();
		
		for (final Iterable<T> iterable : iterables) {
			ret.add(FluentIterable.from(iterable)
					.transform(EquivalenceUtils.Wrap(equivalence))
					.toImmutableSet());
		}
		
		return ret.build();
	}
    
	private final Equivalence<T> equivalence;
	private final B3Method method;
}

