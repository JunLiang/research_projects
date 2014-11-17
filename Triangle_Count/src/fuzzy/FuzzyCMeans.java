package fuzzy;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;



public class FuzzyCMeans {
	
	Integer numberOfPaths;
	Integer numberOfFeatures;
	Integer numberOfClusters;
	
	Double[][] pathMatrix;
	Double[][] clusterMembership;
	Double[][] clusterCenter;
	
	Double[][] distance;
	
	double minFeature = Double.MAX_VALUE;
	double maxFeature = Double.MIN_VALUE;
	
	double m;
	
	double epsilon;
	
	public static final int max_iterations = 1000;
	
	
	
	public FuzzyCMeans(int path, int feature, int cluster, int fuzzy, double e) {
		this.numberOfClusters = cluster;
		this.numberOfFeatures = feature;
		this.numberOfPaths = path;
		this.m = fuzzy;
		
		pathMatrix = new Double[this.numberOfPaths][];
		for ( int i = 0; i < this.numberOfPaths; i++) {			
			pathMatrix[i] = new Double[this.numberOfFeatures];
		}
		
		clusterMembership = new Double[this.numberOfClusters][];
		distance = new Double[this.numberOfClusters][];
		
		for (int i = 0; i < this.numberOfClusters; i++) {
			clusterMembership[i] = new Double[this.numberOfPaths];
			distance[i] = new Double[this.numberOfPaths];
		}
		
		clusterCenter = new Double[this.numberOfClusters][];
		for (int i = 0; i < this.numberOfClusters; i++) {
			clusterCenter[i] = new Double[this.numberOfFeatures];
		}
		
		epsilon = e;
	}
	
	public void loadMatrixFile(String matrixFileName) throws MatrixDataException {
		BufferedReader in = null;
		int rows = 0;
		String s=null;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(matrixFileName)));
			while ((s = in.readLine()) != null) {
				String[] values = s.split("\\s+");
				if (values.length != this.numberOfFeatures) {
					throw new MatrixDataException();
				} else {
					for (int j = 0; j < this.numberOfFeatures; j++) {
						Double value = Double.valueOf(values[j]);
						this.pathMatrix[rows][j] = value;
						
						if (value > this.maxFeature) {
							this.maxFeature = value;
						}
						
						if (value < this.minFeature) {
							this.minFeature = value;
						}
					}
				}
				rows++;
			}			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (in != null ) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		if (rows < this.numberOfPaths) {
			throw new MatrixDataException();
		}
	}
	
	private void initCalculation() {
		
		Random random = new Random();
		
		double[] maxValues = new double [this.numberOfFeatures];
		double[] minValues = new double [this.numberOfFeatures];
		
		for (int i = 0; i < this.numberOfFeatures; i++) {
			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			
			for (int j = 0; j < this.numberOfPaths; j++) {
				if (pathMatrix[j][i] > max) {
					max = pathMatrix[j][i];
				}
				
				if (pathMatrix[j][i] < min) {
					min = pathMatrix[j][i];
				}
			}
			
			maxValues[i] = max;
			minValues[i] = min;
		}
		
		for (int i = 0; i < this.numberOfClusters; i++) {
			/*Integer[] randArray = new Integer[this.numberOfPaths];
			Integer sum = 0;
			for (int j = 0; j < this.numberOfPaths ; j++) {
				
				randArray[j] = random.nextInt(100000);
				
				sum+= randArray[j];
			}
			for (int j = 0; j < this.numberOfPaths ; j++) {
				this.clusterMembership[i][j] = (double) randArray[j] / (double) sum;
			}*/
			for (int j = 0; j < this.numberOfFeatures; j++) {
				this.clusterCenter[i][j] = random.nextDouble() * (maxValues[j] - minValues[j]) + minValues[j];
			}
		}
		
		for (int i = 0; i < this.numberOfClusters; i++) {
			for (int j = 0; j < this.numberOfPaths; j++) {
				double value = 0.0;
				for (int k = 0; k < this.numberOfFeatures; k++) {
					value += Math.pow((this.pathMatrix[j][k] - this.clusterCenter[i][k]), 2.0);
				}
				
				this.distance[i][j] = Math.sqrt(value);
			}
		}
		
	}
	
	private double calculateOneStep() {
		
				
		//Step 1, update cluster memberships
		TreeSet<Integer> skipSet = new TreeSet<Integer>();
		for (int j = 0; j < this.numberOfPaths; j++) {
			skipSet.clear();
			for (int i = 0; i < this.numberOfClusters; i++){
				if ( distance[i][j] == 0.0) {
					skipSet.add(i);
				}
			}
			
			if (skipSet.size() == 0) {
				
				for (int i = 0; i < this.numberOfClusters; i++) {
					double sum = 0.0;
					for (int ii = 0; ii < this.numberOfClusters; ii++) {
						double value = distance[i][j] / distance [ii][j];						
						value = Math.pow(value, 2.0/(this.m -1 ));
						sum += value;
					}
					
					this.clusterMembership[i][j] = 1.0/sum;
				}
				
			} else {
				for (int i = 0; i < this.numberOfClusters; i++) {
					if (!skipSet.contains(i)) {
						this.clusterMembership[i][j] = 0.0;
					} else {
						this.clusterMembership[i][j] = 1.0/skipSet.size();
					}
				}
			}
		}
		
		// Step 2 update cluster center;
		for (int i = 0; i < this.numberOfClusters; i++) {
			double sum = 0;
			for (int j = 0; j < this.numberOfPaths; j++) {
				sum += Math.pow(this.clusterMembership[i][j], this.m);
			}
			for (int k = 0; k < this.numberOfFeatures; k++) {
				double sum1 = 0.0;
				for (int j = 0; j < this.numberOfPaths; j++) {
					sum1 += Math.pow(this.clusterMembership[i][j], this.m)
							* this.pathMatrix[j][k];
				}
				this.clusterCenter[i][k] = sum1 / sum;
			}
		}
		
		// Step 2 calculate distances of paths to new cluster centers
		for (int i = 0; i < this.numberOfClusters; i++) {
			for (int j = 0; j < this.numberOfPaths; j++) {
				double value = 0.0;
				for (int k = 0; k < this.numberOfFeatures; k++) {
					value += Math.pow((this.pathMatrix[j][k] - this.clusterCenter[i][k]), 2.0);
				}
				
				this.distance[i][j] = Math.sqrt(value);
			}
		}
		
		//Step 4 calculate objective function
		Double objective = 0.0;
		
		for (int i = 0; i < this.numberOfClusters; i++) {
			for (int j = 0; j < this.numberOfPaths; j++) {
				objective += Math.pow(this.clusterMembership[i][j], this.m) * Math.pow(this.distance[i][j], 2.0);
				//objective += Math.pow(this.clusterMembership[i][j], this.m) * Math.pow(this.distance[i][j], 1.0);
			}
		}

		return objective;

	}
	
	public void calcualteFCM() {
		this.initCalculation();
		
		double objective_old = 0.0;
		
		double objective = this.calculateOneStep();
		
		int iteration = 0;
		
		while ( Math.abs(objective - objective_old) > this.epsilon && iteration < max_iterations) {
			
			objective_old =  objective;
			objective = this.calculateOneStep();
			
			iteration ++;
		}
		DecimalFormat decf = new DecimalFormat("#.###");
		for (int i = 0; i < this.numberOfClusters; i++) {
			double maxValue = Double.MIN_VALUE;
			int maxIndex = -1;
			for (int j = 0; j < this.numberOfPaths; j++) {
				if (this.clusterMembership[i][j] > maxValue) {
					maxValue = this.clusterMembership[i][j];
					maxIndex = j;
				}
				System.out.print(decf.format(this.clusterMembership[i][j]) + " ");
				//System.out.print(this.clusterMembership[i][j] + " ");
			}
			System.out.print("   === " + (maxIndex+1) +" ");
			System.out.println();
		}
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FuzzyCMeans cmeans = new FuzzyCMeans(8, 9, 5, 2, 0.000001);
		
		try {
			cmeans.loadMatrixFile("C:\\temp\\matrix.txt");
			cmeans.calcualteFCM();
		}catch (MatrixDataException ex) {
			ex.printStackTrace();
		}

	}

}
