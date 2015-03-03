package com.bbn.nlp.coreference.measures;

import java.util.Collections;
import java.util.Set;

import org.jgrapht.alg.KuhnMunkresMinimalWeightBipartitePerfectMatching;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.bbn.bue.common.evaluation.FMeasureInfo;
import com.bbn.bue.common.evaluation.PrecisionRecallPair;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class MentionCEAFScorer {
  
	public FMeasureInfo score(final Iterable<? extends Iterable<?>> predicted, final Iterable<? extends Iterable<?>> gold) {
		final int partitionSize = Math.max(Iterables.size(predicted), Iterables.size(gold));
		
		final ImmutableList<Node> goldAsSets = toNodesWithPadding(gold, partitionSize, "gold");
		final ImmutableList<Node> predictedAsSets = toNodesWithPadding(predicted, partitionSize, "predicted");
		
		final ImmutableMap<String, Node> idToNodeMap = getIdToNodeMap(Iterables.concat(goldAsSets, predictedAsSets));
		
		final SimpleWeightedGraph<Node, DefaultWeightedEdge> graph = constructWeightedGraph(goldAsSets, predictedAsSets);
		
		final KuhnMunkresMinimalWeightBipartitePerfectMatching<Node, DefaultWeightedEdge> biGraph =
				constructMaxWeightBipartiteGraph(graph, goldAsSets, predictedAsSets);
		
		
		final int goldMentionCount = mentionCount(goldAsSets);
		final int predictedMentionCount = mentionCount(predictedAsSets);
		
		final double biGraphWeight = biGraph.getMatchingWeight();
		final Set<DefaultWeightedEdge> maxEdges = biGraph.getMatching();
		//System.out.println("** BIPARTITE GRAPH **");
		//System.out.println("biGraphWeight="+biGraphWeight);
		//System.out.println("maxEdges:");
		float graphSimilarity = 0;
		for(final DefaultWeightedEdge edge : maxEdges) {
			final String edgeString = edge.toString();
			final String sourceId = getSourceNodeId(edgeString);
			final String targetId = getTargetNodeId(edgeString);
			//System.out.println(sourceId+"-"+targetId+" " + edgeString);
			
			final Node sourceNode = idToNodeMap.get(sourceId);
			final Node targetNode = idToNodeMap.get(targetId);
			graphSimilarity += Sets.intersection(sourceNode.getMembers(), targetNode.getMembers()).size();
		}
		//System.out.println("******");
		
		final Optional<Float> recall = ((Optional<Float>) ((graphSimilarity>0 && goldMentionCount!=0) ? 
				Optional.of(graphSimilarity/(float)goldMentionCount) : Optional.absent()));
		final Optional<Float> precision = ((Optional<Float>) ((graphSimilarity>0 && predictedMentionCount!=0) ? 
				Optional.of(graphSimilarity/(float)predictedMentionCount) : Optional.absent()));
		
		if(recall.isPresent() && precision.isPresent()) {
			return new PrecisionRecallPair(precision.get(), recall.get());
			//return (2*precision.get()*recall.get())/(precision.get()+recall.get());
		}
		else {
			return new PrecisionRecallPair(0, 0);
		}
		
		
	}
  
	private String getSourceNodeId(final String edgeString) {
		return edgeString.substring(1, edgeString.indexOf(":"));
	}
	
	private String getTargetNodeId(final String edgeString) {
		return edgeString.substring(edgeString.indexOf(" : ")+3, edgeString.lastIndexOf(":"));
	}
	
	private ImmutableMap<String, Node> getIdToNodeMap(final Iterable<Node> nodes) {
		final ImmutableMap.Builder<String, Node> ret = ImmutableMap.builder();
		
		for(final Node node : nodes) {
			ret.put(node.getPartitionId(), node);
		}
		
		return ret.build();
	}
	
	private int mentionCount(ImmutableList<Node> sets) {
		int c = 0;
		for(final Node set : sets) {
			c += set.getMembers().size();
		}
		
		return c;
	}
	
	private KuhnMunkresMinimalWeightBipartitePerfectMatching<Node, DefaultWeightedEdge> constructMaxWeightBipartiteGraph(
			final SimpleWeightedGraph<Node, DefaultWeightedEdge> graph,
			final ImmutableList<Node> goldAsSets,
			final ImmutableList<Node> predictedAsSets) {
		return new KuhnMunkresMinimalWeightBipartitePerfectMatching<Node, DefaultWeightedEdge>(graph, goldAsSets, predictedAsSets);
	}
	
	private SimpleWeightedGraph<Node, DefaultWeightedEdge> constructWeightedGraph(
			final ImmutableList<Node> goldAsSets, 
			final ImmutableList<Node> predictedAsSets) {
		SimpleWeightedGraph<Node, DefaultWeightedEdge> graph = new SimpleWeightedGraph<Node, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		// add vertices
		for(final Node set : goldAsSets) {
			graph.addVertex(set);
		}
		for(final Node set : predictedAsSets) {
			graph.addVertex(set);
		}
		
		// first, let's find the maximum similarity
		int maxSimilarity = 0;
		for(final Node goldSet : goldAsSets) {
			for(final Node predictedSet : predictedAsSets) {
				final int similarity = Sets.intersection(goldSet.getMembers(), predictedSet.getMembers()).size();
				if(similarity > maxSimilarity) {
					maxSimilarity = similarity;
				}
			}
		}
		
		// add edges
		for(final Node goldSet : goldAsSets) {
			for(final Node predictedSet : predictedAsSets) {
				//System.out.println("goldSet:"+goldSet);
				//System.out.println("predictedSet:"+predictedSet);
				DefaultWeightedEdge edge = graph.addEdge(goldSet, predictedSet);
				final double edgeWeight = maxSimilarity - Sets.intersection(goldSet.getMembers(), predictedSet.getMembers()).size();
				//System.out.println("edge:"+edge.toString() + " weight="+edgeWeight);
				graph.setEdgeWeight(edge, edgeWeight);
			}
		}
		
		return graph;
	}
	
	
	static ImmutableList<Node> toNodesWithPadding(final Iterable<? extends Iterable<?>> iterables, 
			final int desiredSize, final String partitionId) {
		final ImmutableList.Builder<Node> ret = ImmutableList.builder();
		
		int nodeCounter = 1;
		for(final Iterable<?> iterable : iterables) {
			ret.add(Node.from(partitionId+nodeCounter, ImmutableSet.copyOf(iterable)));
			nodeCounter += 1;
		}
		
		for(int i=Iterables.size(iterables); i<desiredSize; i++) {
			ret.add(Node.from(partitionId+nodeCounter, ImmutableSet.of()));
			nodeCounter += 1;
		}
		
		return ret.build();
	  }

	private static class Node {
		private final String partitionId;
		private final Set<Object> members;
		
		public static Node from(final String id, final Set<Object> members) {
			return new Node(id, members);
		}
		
		private Node(final String partitionId, final Set<Object> members) {
			this.partitionId = partitionId;
			this.members = members;
		}
		
		public String getPartitionId() {
			return partitionId;
		}
		
		public Set<Object> getMembers() {
			return members;
		}
		
		public String toString() {
			return partitionId+":"+members.toString();
		}
		
		public int hashCode() {
			return Objects.hashCode(partitionId, members);
		}
		
		public boolean equals(Object obj) {
			if(obj == null) {
				return false;
			}
			if(this == obj) {
				return true;
			}
			if(getClass() != obj.getClass()) {
				return false;
			}
			Node other = (Node)obj;
			if(this.partitionId==other.getPartitionId() && this.members.equals(other.getMembers())) {
				return true;
			}
			else {
				return false;
			}
		}
		
	}
}
