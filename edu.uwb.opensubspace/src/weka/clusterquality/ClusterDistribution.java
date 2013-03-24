package weka.clusterquality;

import i9.subspace.base.Cluster;

import java.util.ArrayList;

import weka.core.Instances;

public class ClusterDistribution extends ClusterQualityMeasure{
	
	private int[] clustersInSubspace = null;
	int numberOfClusters = 0;
	 
	
	@Override
	public void calculateQuality(ArrayList<Cluster> clusterList, Instances instances, ArrayList<Cluster> trueclusters) {
		
		if(clusterList.isEmpty()) {
			clustersInSubspace = null;
			numberOfClusters = 0;
			return;
		}
		
		clustersInSubspace = new int[clusterList.get(0).m_subspace.length];
		
		for(int i=0;i<clusterList.size();i++){
			Cluster cluster = clusterList.get(i);
			int clusterDim = 0;
			for (boolean b : cluster.m_subspace)
				if (b)
					clusterDim++;
			clustersInSubspace[clusterDim - 1]++;
		}
		
		numberOfClusters = clusterList.size();
	}
	
	private String toString2(int[] clustersInSubspace){
		String result = "";
		for(int i=0;i<clustersInSubspace.length;i++){
			result +=clustersInSubspace[i] + " "; 
		}
		return result;
	}
	
	public double getValue(int i){
		return numberOfClusters;
	}
	
	@Override
	public String getCustomOutput() {
		
		if(clustersInSubspace == null) {
			return "ClusterDist=\tna\t\nNumOfCluster=\t0\t\nAverage Num Dims=\tna";
		}
		
		return "ClusterDist=\t" + toString2(clustersInSubspace) + "\nNumOfCluster=\t" + numberOfClusters + "\nAverage Num Dims=\t" + averageDim();
	}

	@Override
	public String getName() {
		return "Cluster Distribution";
	}

	/*
	 * Author: Dave Hunn
	 * Date:   8/25/2012
	 * 
	 * This method returns the average dimensionality of all found clusters.
	 */
	private double averageDim() {
		double retVal = 0.0;
		
		for (int i = 1; i < clustersInSubspace.length; ++i) {
			retVal += (i)*clustersInSubspace[i];
		}
		return retVal / numberOfClusters;
	}

}
