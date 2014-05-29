package edu.utdallas.theory;

import java.util.TreeSet;

public class TriangleCounter implements Runnable{
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		this.countingTriangles();
		
	}
	
	Graph aGraph;
	Integer sourceNode;
	
	public TriangleCounter (Graph aGraph, Integer sourceNode) {
		this.aGraph = aGraph;
		this.sourceNode = sourceNode;
	}
	
	public void countingTriangles() {
					
		Integer triangleCount = 0;
		
		TreeSet<Integer> firstNeighbour = aGraph.getEdges().get(sourceNode);
		
		if (firstNeighbour== null) return;
		
		for (Integer neighbour : firstNeighbour) {
			//remove self loop			
			if (neighbour == sourceNode) continue;
			
			TreeSet<Integer> secondNeighbour = aGraph.getEdges().get(neighbour);
			
			if (secondNeighbour == null) continue;
			
			for (Integer child : secondNeighbour ) {
				
				if (child == sourceNode || child == neighbour) continue;
				
				if (aGraph.getEdges().get(child) != null && aGraph.getEdges().get(child).contains(sourceNode)) {
					triangleCount++;
				}
			}
		}
		synchronized(aGraph.getTriangles()) {
			aGraph.getTriangles().put(sourceNode, triangleCount);
		}
	}

}