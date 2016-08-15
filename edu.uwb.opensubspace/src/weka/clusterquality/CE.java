package weka.clusterquality;

import i9.subspace.base.Cluster;
import i9.subspace.base.assignment.AssignmentProblem;

import java.util.ArrayList;
import java.util.LinkedList;

import weka.core.Instances;

public class CE extends ClusterQualityMeasure {
	
	double distance = Double.NaN;
	
	double union = 0;
	double intersection = 0;
	double dmax = 0;
	
	int dims;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ArrayList<Cluster> trueClus = new ArrayList<Cluster>();
		ArrayList<Cluster> foundClus = new ArrayList<Cluster>();
		
		{ boolean[] sub = new boolean[8];
		sub[0] = true; sub[1] = true; sub[2] = true; sub[3] = true;
		sub[4] = false; sub[5] = false; sub[6] = false; sub[7] = false;
		LinkedList<Integer> obj = new LinkedList<Integer>();
		obj.add(2); obj.add(3);
		Cluster c = new Cluster(sub,obj);
		trueClus.add(c); }
		
		{ boolean[] sub = new boolean[8];
		sub[0] = false; sub[1] = false; sub[2] = false; sub[3] = false;
		sub[4] = false; sub[5] = true; sub[6] = true; sub[7] = false;
		LinkedList<Integer> obj = new LinkedList<Integer>();
		obj.add(3); obj.add(4);
		Cluster c = new Cluster(sub,obj);
		trueClus.add(c); }
		
		{ boolean[] sub = new boolean[8];
		sub[0] = false; sub[1] = false; sub[2] = false; sub[3] = true;
		sub[4] = true; sub[5] = true; sub[6] = false; sub[7] = false;
		LinkedList<Integer> obj = new LinkedList<Integer>();
		obj.add(6); obj.add(7); obj.add(8);
		Cluster c = new Cluster(sub,obj);
		trueClus.add(c); }
		
		{ boolean[] sub = new boolean[8];
		sub[0] = true; sub[1] = true; sub[2] = false; sub[3] = false;
		sub[4] = false; sub[5] = false; sub[6] = false; sub[7] = false;
		LinkedList<Integer> obj = new LinkedList<Integer>();
		obj.add(2); obj.add(3);
		Cluster c = new Cluster(sub,obj);
		foundClus.add(c); }
		
		{ boolean[] sub = new boolean[8];
		sub[0] = false; sub[1] = false; sub[2] = true; sub[3] = true;
		sub[4] = false; sub[5] = false; sub[6] = false; sub[7] = false;
		LinkedList<Integer> obj = new LinkedList<Integer>();
		obj.add(2); obj.add(3);
		Cluster c = new Cluster(sub,obj);
		foundClus.add(c); }
		
		{ boolean[] sub = new boolean[8];
		sub[0] = false; sub[1] = false; sub[2] = false; sub[3] = false;
		sub[4] = false; sub[5] = true; sub[6] = true; sub[7] = false;
		LinkedList<Integer> obj = new LinkedList<Integer>();
		obj.add(4); obj.add(5); obj.add(6); obj.add(7);
		Cluster c = new Cluster(sub,obj);
		foundClus.add(c); }
		
		ClusterQualityMeasure c = new CE();		
		c.calculateQuality(foundClus,null,trueClus);
		System.out.println("1.0-CE: " + c.getOverallValue());

	}
	
	void unionAndIntersection(ArrayList<Cluster> clusterList,
			Instances instances, ArrayList<Cluster> trueclusters) {
		
		union = 0;
		intersection = 0;
		
		
		int size = instances.numInstances();
		
		// speichert nach Meila Paper eine Zeile wie in
		// Figure  1; da wir overlapping haben, muss nicht nur
		// ja/nein, sondern genau bestimmt werden wie oft ein
		// Element abgedeckt wird
		int[] timesObjectFound = new int[size];
		int[] timesObjectTrue = new int[size];

		
		for(int d=0; d<dims; d++) {
			
			// diese Zeile ist erstmal -berhaupt nicht abgedeckt
			for(int i=0; i<size; i++) {
				timesObjectFound[i] = 0;
				timesObjectTrue[i] = 0;
			}
			
			// nur Cluster welche auch diese Dimension als relevant haben
			// erh-hen die Abdeckung (f-r diese "Dimensionszeile")
			for(Cluster c : clusterList) {
				if(c.m_subspace[d]) { // dimension ist relevant
					// f-r jedes Object in diesem Cluster nun Abdeckung erh-hen
					for(int obj : c.m_objects) {
						timesObjectFound[obj]++;
					}
				}
			}
			for(Cluster c : trueclusters) {
				if(c.m_subspace[d]) { // dimension ist relevant
					// f-r jedes Object in diesem Cluster nun Abdeckung erh-hen
					for(int obj : c.m_objects) {
						timesObjectTrue[obj]++;
					}
				}
			}
			
			// so, nun kann die union und intersection erstmal
			// f-r diese "Dimensionszeile" bestimmt werden
			
			// union war die summe der maxima
			// intersection die summe der minima
			for(int i=0; i<size; i++) {
				union += Math.max(timesObjectFound[i],timesObjectTrue[i]);
				intersection += Math.min(timesObjectFound[i],timesObjectTrue[i]);
			}
			
			// f-r die weiteren Dimensionen wird das einfach hochgez-hlt
			// -> wir sind fertig
		}
		
		//System.out.println(union);
		//System.out.println(intersection);
	}
	
	void dmax(ArrayList<Cluster> clusterList, ArrayList<Cluster> trueclusters) {
		
		// es muss einge quadrtische Matrix sein
		int max = Math.max(clusterList.size(),trueclusters.size());
		float[][] cost = new float[max][max];
		
		int count1 = 0;
		for(Cluster c1 : trueclusters) {
			int count2 = 0;
			for(Cluster c2 : clusterList) {
				// der Kosteneintrag ist der "Schnitt" der Cluster
				// (Achtung: nicht nur der Objekte!)
				// er ergibt sich einfach als Produkt der gemeinsamen Objekte
				// mal der gemeinsamen relevanten Dimensionen
				
				// z-hle erst die gemeinsamen relevanten dims
				int sharedDims = 0;
				for(int d=0; d<dims; d++) {
					if(c1.m_subspace[d] && c2.m_subspace[d]) {
						sharedDims++;
					}
				}
				
				// nun die Objekte
				LinkedList<Integer> obj = new LinkedList<Integer>(c1.m_objects);
				obj.retainAll(c2.m_objects);
				int sharedObj = obj.size();
				
				cost[count1][count2] = sharedDims*sharedObj;
				//System.out.println(cost[count1][count2]);
				count2++;
			}
			count1++;
		}
		
	    dmax = AssignmentProblem.maximize(cost);
	    //System.out.println(dmax);
	}
	

	@Override
	public void calculateQuality(ArrayList<Cluster> clusterList,
			Instances instances, ArrayList<Cluster> trueclusters) {
		
		distance = Double.NaN;
		union = 0;
		intersection = 0;
		dmax = 0;
		
		if(trueclusters == null) {
			// TODO Fehler, dass kein File geladen!!!
			return;
		}
		
		dims = -1;
		if(clusterList.size() != 0) {
			dims = clusterList.get(0).m_subspace.length;
		}
		if(trueclusters.size() != 0) {
			int dim2 = trueclusters.get(0).m_subspace.length;
			if(dims != -1) {
				// vergleiche ob Dimensionen identisch, falls nein,
				// gebe einen Fehler aus
				if(dim2 != dims) {
					// TODO Fehler melden
					return;
				}
			}
			dims = dim2;
		}

		unionAndIntersection(clusterList,instances,trueclusters);
		dmax(clusterList,trueclusters);
		distance = (union-dmax)/union;		
	}


	public String getName() {
		return "1.0-ClusErr";
	}

	@Override
	public Double getOverallValue(){
		return 1.0-distance;
	}


}
