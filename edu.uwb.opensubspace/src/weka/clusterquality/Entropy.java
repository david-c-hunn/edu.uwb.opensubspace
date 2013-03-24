package weka.clusterquality;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import weka.core.Instance;
import weka.core.Instances;
import i9.subspace.base.Cluster;

public class Entropy extends ClusterQualityMeasure{
	
	private int m_k = 200;
	private double[] m_cluster_values = null;
	private double m_entropy;
	
	@Override
	public void calculateQuality(ArrayList<Cluster> clusterList, Instances instances, ArrayList<Cluster> trueclusterList){
		
		int max_Objects_in_Clusters = 0;
		for (Cluster cluster : clusterList) {
			max_Objects_in_Clusters += cluster.m_objects.size();
		}
		

		double sumEntropie = 0;
		m_cluster_values = new double[clusterList.size()];
		Cluster cluster;
		double d;
		for (int i = 0; i < clusterList.size(); i++) {
			cluster = clusterList.get(i);
			d = entropie(cluster,instances);
			if(d==0.0) d=0.0;
			m_cluster_values[i]=d;
			sumEntropie += (d * cluster.m_objects.size());

		}
		m_entropy= sumEntropie / max_Objects_in_Clusters;

	}

	@Override
	public Double getValuePerCluster(int i) {
		return 1.0-m_cluster_values[i];
	}	

	@Override
	public Double getOverallValue(){
		return 1.0-m_entropy;
	}	
	
	
		
	private double entropie(Cluster cluster, Instances instances) {	
		double[][] histogram = getFirstKMostFrequent(cluster,instances);
		double entropie = 0;
		for (int i = 0; i < histogram[0].length; i++) {
			entropie += histogram[1][i] * Math.log(histogram[1][i]);
		}
		
	
		// normierung durch maximale entropie
		entropie = -entropie/Math.log(instances.numClasses());
				
		return entropie;
	}
	
	public double[][] getFirstKMostFrequent(Cluster cluster, Instances instances) {
		int countAll = cluster.m_objects.size();

		HashMap<Integer, HistogramItem> m_map = new HashMap<Integer, HistogramItem>();

		for (int obj : cluster.m_objects) {
			Instance ins = instances.instance(obj);
			int className = (int) ins.classValue();
			HistogramItem item = m_map.get(className);
			if (item != null)
				item.count++;
			else {
				item = new HistogramItem();
				item.name = className;
				item.count = 1;
				m_map.put(className, item);
			}
		}

		ArrayList<HistogramItem> sortList = new ArrayList<HistogramItem>(m_map.size());
		for (Integer key : m_map.keySet())
			sortList.add(m_map.get(key));
		Collections.sort(sortList, new HistogramItemComparator());

		double[][] result = new double[2][Math.min(m_k, sortList.size())];
		// result[0][i] = name
		// result[1][i] = frequency
		for (int i = 0; i < m_k && i < sortList.size(); i++) {
			result[0][i] = sortList.get(i).name;
			result[1][i] = sortList.get(i).count * 1.0 / countAll;
		}

		return result;
	}

	@Override
	public String getName() {
		return "1.0-Entropy";
	}

}

class HistogramItem {
	int name;
	int count;
}

class HistogramItemComparator implements Comparator<HistogramItem> {

	public int compare(HistogramItem h1, HistogramItem h2) {
		return h2.count - h1.count;
	}

}
	

