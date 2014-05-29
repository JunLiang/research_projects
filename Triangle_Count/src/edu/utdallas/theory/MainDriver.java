package edu.utdallas.theory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainDriver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			File textFile = new File("/home/junliang/Projects/SocNet/soc-LiveJournal1.txt");
			BufferedReader reader = new BufferedReader( new FileReader(textFile));
			
			Graph aGraph = new Graph();
			
			String line = null;
			
			Long edgeCount = 0L;
			
			long startTime = System.currentTimeMillis();
			
			while ((line = reader.readLine()) != null) {
				String[] values = line.split("\t");
				
				if (values.length != 2) continue;
				
				Integer source = Integer.valueOf(values[0]);
				Integer sink = Integer.valueOf(values[1]);
				TreeSet<Integer> edge = aGraph.getEdges().get(source);
				
				if (edge == null) {
					edge = new TreeSet<Integer>();
					aGraph.getEdges().put(source, edge);					
				}
				edge.add(sink);
				edgeCount++;
			}
			reader.close();
			long endTime = System.currentTimeMillis() - startTime;
			System.out.println(edgeCount+" edges and " +aGraph.getEdges().size() + " vertices read in " + endTime  + " milliseconds");
			
			startTime = System.currentTimeMillis();
			
			//aGraph.countingTriangles();
			ExecutorService exectuorSvc = Executors.newFixedThreadPool(8);
			for (Integer source : aGraph.getEdges().keySet()) {
				Runnable worker = new TriangleCounter(aGraph, source);
				exectuorSvc.execute(worker);
			}
			exectuorSvc.shutdown();
			exectuorSvc.awaitTermination(1, TimeUnit.HOURS);
			endTime = System.currentTimeMillis() - startTime;
			
			System.out.println("Counted all triangles in " + endTime  + " milliseconds");
			
			

			for (Integer source : aGraph.getTriangles().keySet()) {
				System.out.println("Node " + source + " is in " + aGraph.getTriangles().get(source) +" triangles");
			}
			System.out.println("Again Counted all triangles in " + endTime  + " milliseconds");
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

}