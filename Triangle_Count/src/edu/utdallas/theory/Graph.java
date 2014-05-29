package edu.utdallas.theory;

import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;

public class Graph {
	
	private TreeMap<Integer, TreeSet<Integer>> edges;
	private ConcurrentSkipListMap<Integer, Integer> triangles;
	
	
	public Graph() {
		edges = new TreeMap<Integer, TreeSet<Integer>> ();
		triangles = new ConcurrentSkipListMap<Integer, Integer>();
	}

	public TreeMap<Integer, TreeSet<Integer>> getEdges() {
		return edges;
	}

	public void setEdges(TreeMap<Integer, TreeSet<Integer>> edges) {
		this.edges = edges;
	}

	public void setTriangles(ConcurrentSkipListMap<Integer, Integer> triangles) {
		this.triangles = triangles;
	}
	
	public ConcurrentSkipListMap<Integer, Integer> getTriangles() {
		return triangles;
	}


}
