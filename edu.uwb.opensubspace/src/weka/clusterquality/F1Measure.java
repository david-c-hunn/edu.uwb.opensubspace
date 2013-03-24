package weka.clusterquality;


import i9.subspace.base.Cluster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;
import weka.core.Instance;
import weka.core.Instances;

public class F1Measure extends ClusterQualityMeasure{
	
	double m_F1_values[] = null;
	double m_F1 = 0.0;
	double m_precision[] = null;
	double m_recall[] = null;
	
	
	@Override
	public void calculateQuality(ArrayList<Cluster> clusters, Instances instances, ArrayList<Cluster> trueclusterList) {
		//distinct classes
		int size = instances.numDistinctValues(instances.classIndex());
		
		ArrayList<Integer>[] inClasses = new ArrayList[size];
		Set<Integer>[] outClasses = new HashSet[size];
		for (int i = 0; i<size; i++){
			inClasses[i] = new ArrayList<Integer>();
		}
		for (int i = 0; i<size; i++){
			outClasses[i] = new HashSet<Integer>();
		}

		//Assign objects to the respective input classes
		//Objekte den jeweiligen Eingangsklassen zuordnen 
		for (int i = 0; i < instances.numInstances(); i++) {
			Instance obj = instances.instance(i);
			inClasses[(int)obj.classValue()].add(i);
		}
		
		//Die häufigkeiten der Klassen in den Ausgabeclustern bestimmen und F_value bzgl. dieser Klasse berechnen.
		for (int i = 0; i < clusters.size(); i++) {
			int[] classFrequency = new int[size]; 
			for (int j = 0; j < size; j++){ 
				classFrequency[j]=0;
			}
			ArrayList<Integer> thisCluster = new ArrayList<Integer>();
			Cluster cluster = clusters.get(i);
			ListIterator<Integer> objects;
			objects = cluster.m_objects.listIterator();
			while (objects.hasNext()){
				int objID = objects.next();
				Instance instance = instances.instance(objID);
				classFrequency[(int)instance.classValue()]++;
				thisCluster.add(objID);
			}
			double maxValue = 0;
			int maxIndex = 0;
			for (int k = 0; k < size; k++){
				// Variante 1
				//if (classFrequency[k]>maxValue) {
				//	maxValue = classFrequency[k];
				//	maxIndex = k;
				//}
				// Variante 2
				double val = classFrequency[k]*classFrequency[k]/(double)inClasses[k].size();
				if(val > maxValue) {
					maxValue = val;
					maxIndex = k;
				}
				// Ende Varianten
			}
			outClasses[maxIndex].addAll(thisCluster);
		}

		//F-value berechnen pro AusgabeCluster
		m_F1_values = new double[size];
		m_precision = new double[size];
		m_recall = new double[size];
		m_F1 = 0.0;
		
		for (int i = 0; i < size; i++){
			int intersect = 0;
			for (int j = 0; j < inClasses[i].size(); j++){
				int in = (int) inClasses[i].get(j);
				if (outClasses[i].contains(in)) {
					intersect++;
				}
			}
			m_precision[i] = 0;
			m_recall[i] = 0;
			if ((outClasses[i].size()==0)&&(inClasses[i].size()==0)){
				m_F1_values[i] = 0;
			} else {
				m_F1_values[i] = 2*(double)intersect/(outClasses[i].size()+inClasses[i].size());
				if (outClasses[i].size()!=0) m_precision[i] = (double)intersect/outClasses[i].size();
				if (inClasses[i].size()!=0) m_recall[i] = (double)intersect/inClasses[i].size();
			}

			m_F1+= m_F1_values[i];
			
		}
		m_F1 = m_F1/size;
		
	}

	@Override
	public Double getValuePerCluster(int i) {
		int index = i;
		if (i < m_F1_values.length)
			return m_F1_values[index];
		else
			return -1.0;
	}
	
	@Override
	public Double getOverallValue(){
		return m_F1;
	}
	
	@Override
	public String getName() {
		return "F1Measure";
	}


}
