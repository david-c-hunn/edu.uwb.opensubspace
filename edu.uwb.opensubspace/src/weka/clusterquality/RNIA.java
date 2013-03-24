package weka.clusterquality;

import i9.subspace.base.Cluster;

import java.util.ArrayList;
import java.util.LinkedList;

import weka.core.Instances;

public class RNIA extends ClusterQualityMeasure {
	
	double distance = Double.NaN;
	
	double union = 0;
	double intersection = 0;

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
		
		ClusterQualityMeasure c = new RNIA();		
		c.calculateQuality(foundClus,null,trueClus);
		System.out.println("1.0-RNIA: " + c.getOverallValue());

	}
	
	void unionAndIntersection(ArrayList<Cluster> clusterList,
			Instances instances, ArrayList<Cluster> trueclusters) {
		
		union = 0;
		intersection = 0;
		
		int dims = -1;
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
		
		int size = instances.numInstances();
		
		// speichert nach Meila Paper eine Zeile wie in
		// Figure  1; da wir overlapping haben, muss nicht nur
		// ja/nein, sondern genau bestimmt werden wie oft ein
		// Element abgedeckt wird
		int[] timesObjectFound = new int[size];
		int[] timesObjectTrue = new int[size];

		
		for(int d=0; d<dims; d++) {
			
			// diese Zeile ist erstmal überhaupt nicht abgedeckt
			for(int i=0; i<size; i++) {
				timesObjectFound[i] = 0;
				timesObjectTrue[i] = 0;
			}
			
			// nur Cluster welche auch diese Dimension als relevant haben
			// erhöhen die Abdeckung (für diese "Dimensionszeile")
			for(Cluster c : clusterList) {
				if(c.m_subspace[d]) { // dimension ist relevant
					// für jedes Object in diesem Cluster nun Abdeckung erhöhen
					for(int obj : c.m_objects) {
						timesObjectFound[obj]++;
					}
				}
			}
			for(Cluster c : trueclusters) {
				if(c.m_subspace[d]) { // dimension ist relevant
					// für jedes Object in diesem Cluster nun Abdeckung erhöhen
					for(int obj : c.m_objects) {
						timesObjectTrue[obj]++;
					}
				}
			}
			
			// so, nun kann die union und intersection erstmal
			// für diese "Dimensionszeile" bestimmt werden
			
			// union war die summe der maxima
			// intersection die summe der minima
			for(int i=0; i<size; i++) {
				union += Math.max(timesObjectFound[i],timesObjectTrue[i]);
				intersection += Math.min(timesObjectFound[i],timesObjectTrue[i]);
			}
			
			// für die weiteren Dimensionen wird das einfach hochgezählt
			// -> wir sind fertig
		}
		
		//System.out.println(union);
		//System.out.println(intersection);
	}

	@Override
	public void calculateQuality(ArrayList<Cluster> clusterList,
			Instances instances, ArrayList<Cluster> trueclusters) {
		
		distance = Double.NaN;
		union = 0;
		intersection = 0;
		
		if(trueclusters == null) {
			// TODO Fehler, dass kein File geladen!!!
			return;
		}


		unionAndIntersection(clusterList,instances,trueclusters);
		distance = (union-intersection)/union;		
	}

	@Override
	public String getName() {
		return "1.0-RNIA";
	}

	@Override
	public Double getOverallValue() {
		return 1.0-distance;
	}


}
