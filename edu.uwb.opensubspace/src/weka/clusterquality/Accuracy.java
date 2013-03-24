package weka.clusterquality;

import i9.subspace.base.Cluster;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class Accuracy extends ClusterQualityMeasure{
	
	private double m_accuracy = 0.0;
	
	public void calculateQuality(ArrayList<Cluster> clusterList,
			Instances instances, ArrayList<Cluster> trueclusterList) {
		
		m_accuracy = 0.0;
		
		StringBuffer result = new StringBuffer();

		// für jedes Objekt eine Liste anlegen, in der gleich die Clusternummern
		// reinkommen
		ArrayList<HashSet<Integer>> objIDnachCluster = new ArrayList<HashSet<Integer>>();
		for (int i = 0; i < instances.numInstances(); i++) {
			objIDnachCluster.add(new HashSet<Integer>(clusterList.size()));
		}

		// bestimme, in welche Cluster ein Objekt alles fällt
		for (int i = 0; i < clusterList.size(); i++) {
			Cluster c = clusterList.get(i);
			for (Integer s : c.m_objects) {
				objIDnachCluster.get(s).add(i);
			}
		}

		//erzeuge neues instance set für klassifikator
		FastVector atts = new FastVector();
		for (int i = 0; i < clusterList.size(); i++) {
			atts.addElement(new Attribute("c"+i));
		}
		
		//Class Attribute
		FastVector attVals = new FastVector();
		Enumeration en = instances.classAttribute().enumerateValues();
		while (en.hasMoreElements()) {
			String v = (String) en.nextElement();
			attVals.addElement(v);
		}	
		atts.addElement(new Attribute("class", attVals));

		Instances accSet = new Instances("Accuracy", atts, 0);
		//TODO: direkt über attribut
		accSet.setClassIndex(accSet.numAttributes()-1);
		for (int i = 0; i < instances.numInstances(); i++) {
			double[] vals = new double[accSet.numAttributes()];
			for (int j = 0; j < clusterList.size(); j++) {
				if (objIDnachCluster.get(i).contains(j)) {
					vals[j]=1;
				}
				else {
					vals[j]=0;
				}
			}
			//set class
			double index = instances.instance(i).classValue();
			vals[vals.length-1] = index;
			accSet.add(new Instance(1.0, vals));	
		}
		//System.out.println(accSet);
		
		
		//starte klassfifikator
		
		
		try {
			
			int numFolds = 10; 
			
			// neu
			Random random = new Random(1);
			accSet.randomize(random);
			if (true) {
				accSet.stratify(numFolds);
			}
			// neu Ende
			
			Evaluation eval = new Evaluation(accSet);
			//classifier.buildClassifier(accSet);
			for (int fold = 0; fold < numFolds; fold++) {
				
				// neu
				Instances train  = accSet.trainCV(10, fold,random);
				// alt
				//Instances train  = accSet.trainCV(10, fold);
				
				eval.setPriors(train);
				Classifier classifier = new J48();
				classifier.buildClassifier(train);
				
				Instances test = accSet.testCV(numFolds, fold);
				for (int i = 0; i < test.numInstances(); i++) {
					eval.evaluateModelOnceAndRecordPrediction(classifier, test.instance(i));
				}
			}
			m_accuracy= eval.pctCorrect();;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public static void main(String[] args) throws Exception {
		FastVector atts;
		FastVector attVals;
		Instances data;
		double[] vals;
		int i;

		atts = new FastVector();
		atts.addElement(new Attribute("att1"));
		attVals = new FastVector();
		for (i = 0; i < 5; i++)
			attVals.addElement("val" + (i + 1));
		atts.addElement(new Attribute("att2", attVals));

		data = new Instances("MyRelation", atts, 0);

		vals = new double[data.numAttributes()];
		vals[0] = Math.PI;
		vals[1] = attVals.indexOf("val3");

	}

	
	@Override
	public Double getOverallValue(){
		return m_accuracy/100;
	}

	public String getName() {
		return "Accuracy";
	}



}
