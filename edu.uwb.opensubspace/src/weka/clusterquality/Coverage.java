package weka.clusterquality;

import i9.subspace.base.Cluster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import weka.core.Instances;

public class Coverage extends ClusterQualityMeasure{

	private double[] m_cluster_values = null;
	private double m_coverage;

	public void calculateQuality(ArrayList<Cluster> clusterList,
			Instances instances, ArrayList<Cluster> trueclusterList) {

		Set<Integer> coveredObj = new HashSet<Integer>();

		int dimensions = instances.numAttributes();
		// TODO: Coverage ohne Klassenlabel???
		if (instances.classIndex() >= 0)
			dimensions--;

		m_cluster_values = new double[clusterList.size()];
		for (int i = 0; i < clusterList.size(); i++) {
			Cluster cluster = clusterList.get(i);
			m_cluster_values[i] = cluster.m_objects.size() / (double)instances.numInstances();
			coveredObj.addAll(cluster.m_objects);
		}

		m_coverage = (coveredObj.size() / (double)instances.numInstances());


	}

	@Override
	public Double getValuePerCluster(int i) {
		return m_cluster_values[i];
	}

	@Override
	public String getName() {
		return "Coverage";
	}
	
	public Double getOverallValue(){
		return m_coverage;
	}


}
