package i9.subspace.clique;

import i9.data.core.DBStorage;
import i9.data.core.Instance;
import i9.subspace.base.Cluster;
import i9.subspace.base.Log;
import i9.subspace.base.Timer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;


/* berarbeitete version von sundermeyer */

public class CLIQUE {

    private final int __maxDomain = 1000;
    private int m_dimensions;
    private DBStorage m_dbstorage;
    private int m_N; // size of the DB
    private int m_xi; // number of grids per dimension

    /**
     * A bigger tau results in more cells passing the threshold
     * 
     * tau is the threshold-propability for a cell having
     */
    private int minPoints; // part of the threshold

    /**
     * list of maximal interesting subspaces as result of the clustering
     */
    private List<Set<Integer>> m_mis;
    
    private HashMap<Integer, Set<Integer>> m_verticalData;
    private double[] m_gridSize;
    private double[] m_minValues;
    public List<Cluster> m_ClusterList = new ArrayList<Cluster>();
    
    /**
     * @author pishchulin
     */
    
    public List<Cover> m_CoverList;
    

    public CLIQUE(int dimensions, DBStorage dbstorage,
            int xi, double tau) {
        m_dimensions = dimensions;
        m_dbstorage = dbstorage;

        m_N = m_dbstorage.getSize();
        m_xi = xi;
        minPoints = (int) Math.ceil(tau * m_N);

        m_mis = new ArrayList<Set<Integer>>();
        m_verticalData = new HashMap<Integer, Set<Integer>>();
    }
    
    /**
     * @author pishchulin
     * 22.10.08
     */

    public List<Cover> runClustering(){
    	
//    	Timer timer = new Timer();
//    	timer.start();
    	discretize(); // and create vertical data representation
//    	long time = timer.stop();
//    	System.out.println("discretize: " + time);
//    	timer.start();
    	mineSubspaces();
//    	time = timer.stop();
//    	System.out.println("mineSubsp: " + time);
    	return covering(mergeClusters());
    }
   
    
    
    /**
     * Diskretisierung rückgängig machen.
     * 
     * @param clusters Cluster in Zellenform
     * @return Cluster als Punkte
     */
    private List<Cluster> assignPoints(List<List<Set<Integer>>> clusters) {
        List<Cluster> result = new ArrayList<Cluster>();
        
        for (List<Set<Integer>> mergedCells : clusters) {
            HashSet<Integer> elements = new HashSet<Integer>();
            
            boolean[] constraints = null;
            for (Set<Integer> cell : mergedCells) {
                if (constraints == null) {
                    constraints = new boolean[m_dimensions];
                    for (int dimension : cell) {
                        int constraint = dimension / 1000;
                        constraints[constraint] = true;
                    }
                }
                
                int position = 0;
                for (Instance dataInstance : m_dbstorage) {
                    boolean inCluster = true;
                    double[] values = dataInstance.getFeatureArray();
                    for (int i = 0; i < m_dimensions; i++) {
                        if (!constraints[i])
                            continue;
                        
                        int gridID = gridID(values[i], i);

                        if (!cell.contains(gridID)) {
                            inCluster = false;
                            break;
                        }
                    }
                    if (inCluster)
                        elements.add(position);
                    position++;
                }
            }
            
            result.add(new Cluster(constraints, new ArrayList<Integer>(
                    elements)));
        }

//        print(result.toArray(new Cluster[0]));
        return result;
    }
    
    private static void print(Cluster[] result) { // type erasure :-(
        System.out.println("merged clusters:");
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].m_subspace.length; j++)
                if (result[i].m_subspace[j])
                    System.out.print(j + " ");
            System.out.printf("(%d)\n", result[i].m_objects.size());
        }
    }

    /**
     * GridID berechnen.
     * 
     * @param value Wert in der Dimension
     * @param dimension Dimension
     * @return GridID
     */
    private int gridID(double value, int dimension) {
        int gridID = (int) Math.floor((value - m_minValues[dimension])
                / m_gridSize[dimension]);
        gridID = Math.min(gridID, m_xi - 1); // (MS)
        gridID = dimension * __maxDomain + gridID;
        return gridID;
    }
    
    /**
     * Angrenzende Cluster zusammenfassen.
     * 
     * @return zusammengefasste Cluster
     */
    private List<List<Set<Integer>>> mergeClusters() {
        Set<Set<Integer>> unmerged = new HashSet<Set<Integer>>(m_mis);
        List<List<Set<Integer>>> result = new ArrayList<List<Set<Integer>>>();
//        Timer t = new Timer();
//    	t.start();
        while (!unmerged.isEmpty()) {
            Set<Integer> element = unmerged.iterator().next();
            List<Set<Integer>> merged = new ArrayList<Set<Integer>>();
            merged.add(element);
            Queue<Set<Integer>> added = new LinkedList<Set<Integer>>();
            added.add(element);

            while (!added.isEmpty()) {
                unmerged.removeAll(added);
                Set<Integer> a = added.poll();
                for (Set<Integer> u : unmerged) {
                    if (adjacent(u, a)) {
                        merged.add(u);
                        added.add(u);
                    }
                }
            }
            result.add(merged);
        }
//        long time = t.stop();
//        System.out.println("mergeClus: " + time);
        
//        print(result);
        return result;
    }
    
    /**
     * Ergebnis des Mergings ausgeben.
     * 
     * @param result zusammengefasste Zellen
     */
    private static void print(List<List<Set<Integer>>> result) {
        System.out.println("merged cells:");
        for (List<Set<Integer>> cluster : result) {
            for (Set<Integer> set : cluster) {
                for (Integer item : set) {
                    System.out.print(item + " ");
                }
                System.out.print(";  ");
            }
            System.out.println();
        }
    }
    
    /**
     * Entscheidet, ob zwei Zellen adjazent sind.
     * 
     * @param cell1 Zelle
     * @param cell2 Zelle
     * @return true, falls Zellen adjazent, false sonst
     */
    private boolean adjacent(Set<Integer> cell1, Set<Integer> cell2) {
        if (cell1.size() != cell2.size())
            return false;
        
        final Set<Integer> intersection = new HashSet<Integer>(cell1);
        intersection.retainAll(cell2);
        
        if (intersection.size() != cell1.size() - 1)
            return false;
        
        final Set<Integer> c1 = new HashSet<Integer>(cell1),
                           c2 = new HashSet<Integer>(cell2);
        c1.removeAll(intersection);
        c2.removeAll(intersection);
        
        int x = c1.iterator().next(),
            y = c2.iterator().next();
           
        // Dimensionen gleich und Zellen angrenzend?
        if (x / __maxDomain == y / __maxDomain && Math.abs(
                x % __maxDomain - y % __maxDomain) == 1)
            return true;
        
        return false;
    }

    protected void discretize() {
        m_minValues = new double[m_dimensions];
        double[] maxValues = new double[m_dimensions];
        for (int i = 0; i < m_dimensions; i++) {
            m_minValues[i] = Double.MAX_VALUE;
            maxValues[i] = Double.MIN_VALUE;
        }

        for (Instance dataInstance : m_dbstorage) {
            double[] values = dataInstance.getFeatureArray();
            for (int i = 0; i < m_dimensions; i++) {
                if (m_minValues[i] > values[i])
                    m_minValues[i] = values[i];
                if (maxValues[i] < values[i])
                    maxValues[i] = values[i];
            }
        }

        m_gridSize = new double[m_dimensions];
        for (int i = 0; i < m_dimensions; i++)
            m_gridSize[i] = (maxValues[i] - m_minValues[i]) / m_xi;
        
        int position = 0;
        for (Instance dataInstance : m_dbstorage) {
            double[] values = dataInstance.getFeatureArray();
            for (int i = 0; i < m_dimensions; i++) {
                if (m_gridSize[i] == 0.) // solche Dimensionen ignorieren! (MS)
                    continue;
                
                int gridID = gridID(values[i], i);

                if (m_verticalData.containsKey(gridID)) {
                    m_verticalData.get(gridID).add(position);
                } else {
                    Set<Integer> newList = new HashSet<Integer>();
                    newList.add(position);
                    m_verticalData.put(gridID, newList);
                }
            }

            position++;
        }
    }

    protected void mineSubspaces() {
        // System.out.print("IS1: ");
//        Timer t = new Timer();
//        t.start();
    	SortedSet<Integer> IS1 = new TreeSet<Integer>();
        for (Integer gridID : m_verticalData.keySet()) {
            if (isInteresting(m_verticalData.get(gridID).size(), 1)) {
                IS1.add(gridID);
                // System.out.print(gridID + " ");
            }
        }
        for(Integer i:IS1){
        	Set<Integer> s = new HashSet<Integer>();
        	s.add(i);
        	m_mis.add(s);
        }
//        long time = t.stop();
//        System.out.println("IS1: " + time);
                
        MIS(); // Apriory algorithm
    }

    /**
     * Identification of subspaces that contain clusters, Apriory style algorithm
     *  
     * @author pishchulin
     */
    
    private void MIS() {
    	
    	if(m_mis.size() > 1){
    		
    		List<Set<Integer>> is = new ArrayList<Set<Integer>>();
    		is.addAll(m_mis);
    		
    		
    		for(int i = 1; i < m_dimensions+1; i++){
    			
    			if (is.size()>0){
//    				Timer t = new Timer();
//                    t.start();
    				HashSet<SortedSet<Integer>> possibleCandidates = generateCandidates(is, i); //join step
//    				long time = t.stop();
    				is.clear();
//    				System.out.println("IS"+i+": " + time);
    				for (SortedSet<Integer> c: possibleCandidates)
    					if (allSubsetsFrequent(c)){ // prune step
    						Set<Integer> objects = getObjects(c);
    						if (isInteresting(objects.size(), i+1)){// check density  
    							m_mis.add((Set<Integer>)c);
    							is.add((Set<Integer>)c);
    						}
    					}
    			}
    		}
    		
    	    	    	
      }//else System.out.println("Only 1-d clusters");
    
    }
    	
    
    /**
     * join step in the Apriori
     *  
     * @param fromItems List<Set<Integer>> as m_mis
     * @return candidates HashSet<SortedSet<Integer>> as merging of items in m_mis
     * @author pishchulin
     */
    
    private HashSet<SortedSet<Integer>> generateCandidates(List<Set<Integer>> fromItems, int level){
    	
    	HashSet<SortedSet<Integer>> candidates = new HashSet<SortedSet<Integer>>();
    	SortedSet<Integer> ss1 = new TreeSet<Integer>();
    	SortedSet<Integer> ss2 = new TreeSet<Integer>();
    	
    	
    	for(Set<Integer> s1: fromItems.subList(0, fromItems.size()-1)){
    		
    		if (s1.size() != level) //join only subspaces of the current level
   				continue;
    		
        	int ind = fromItems.indexOf(s1);
    		
			for(Set<Integer> s2: fromItems.subList(ind+1, fromItems.size())){
				if (s2.size()==level){
					
					ss1.clear();
					ss1.addAll(s1);
					ss2.clear();
					ss2.addAll(s2);
					
					if (isToJoin(ss1,ss2)){
						//union of the sets
						SortedSet<Integer> joinSet = new TreeSet<Integer>();
						joinSet.addAll(ss1);
						joinSet.addAll(ss2);
						candidates.add(joinSet);
					}
    			}
					
    		}
    		
    	}
    	
    	return candidates;
    }
    /**
     * Decides whether both sets can be joined
     * @param ss1   sorted set of interval ids
     * @param ss2	sorted set of interval ids
     * @return boolean
     */
    
    private boolean isToJoin(SortedSet<Integer> ss1, SortedSet<Integer> ss2){
    	
    	if (ss1.size() != ss2.size())
			return false;
				
		int pos1 = 0;
		int pos2;
		for(int i:ss1){
			pos2 = 0;
			for(int j:ss2){
				if (pos1==pos2){
					if ((i!=j)&&(pos1!=ss1.size()-1))return false;
				}
				pos2++;
			}
			pos1++;
		}
    	return true;
    }
    
    
    /**
     * prune step in Apriori
     * check whether all k-1 subsets are dense
     *  
     * @param possibleCandidate SortedSet<Integer>
     * @return candidates HashSet<SortedSet<Integer>> as merging of items in m_mis
     * @author pishchulin
     */
    
    private boolean allSubsetsFrequent(SortedSet<Integer> possibleCandidate){ 
    	
    	SortedSet<Integer> lowerSS = new TreeSet<Integer>();
    	for(Integer i:possibleCandidate){
    		lowerSS.clear();
    		lowerSS.addAll(possibleCandidate);
    		lowerSS.remove(i);
    		if (!m_mis.contains(lowerSS))return false;
    	}
    	
    	return true;
    }

    private Set<Integer> getObjects(Set<Integer> subspace) {
        Set<Integer> result = null;
        for (Integer i : subspace) {
            if (result == null) {
                result = new HashSet<Integer>();
                result.addAll(getObjects(i));
            } else {
                result = intersection(result, getObjects(i));
            }
        }
        return result;
    }

    private Set<Integer> getObjects(int subspace) {
        return m_verticalData.get(subspace);
    }

    private Set<Integer> intersection(Set<Integer> s1, Set<Integer> s2) {
        // System.out.println(s1.size() + " " + s2.size());
        Set<Integer> result = new HashSet<Integer>();
        for (Integer i : s1) {
            if (s2.contains(i)) {
                result.add(i);

            // System.out.println(result.size());
            }
        }
        return result;
    }

    private boolean isInteresting(int n_p, int p) {
        return n_p >= minPoints;
    }

    private void printMIS() {
        System.out.println("MIS:");
        for (Set<Integer> set : m_mis) {
            for (Integer item : set) {
                System.out.print(item + " ");
            }
            System.out.println();
        }

    }
    
    
    private List<Set<Integer>> testCluster(){
    	
    	List<Set<Integer>> cl = new ArrayList<Set<Integer>>(); 
    	Set<Integer> s = new HashSet<Integer>();
		
		s.clear();
		s.add(0);
		s.add(1000);
		s.add(2000);
		cl.add(new HashSet<Integer>(s));
		s.clear();
		s.add(1);
		s.add(1000);
		s.add(2000);
		cl.add(new HashSet<Integer>(s));
		s.clear();
		s.add(2);
		s.add(1000);
		s.add(2000);
		cl.add(new HashSet<Integer>(s));
		s.clear();
		s.add(0);
		s.add(1001);
		s.add(2000);
		cl.add(new HashSet<Integer>(s));
		s.clear();
		s.add(1);
		s.add(1001);
		s.add(2000);
		cl.add(new HashSet<Integer>(s));
		s.clear();
		s.add(2);
		s.add(1001);
		s.add(2000);
		cl.add(new HashSet<Integer>(s));
		s.clear();
		s.add(0);
		s.add(1000);
		s.add(2001);
		cl.add(new HashSet<Integer>(s));
		s.clear();
		s.add(1);
		s.add(1000);
		s.add(2001);
		cl.add(new HashSet<Integer>(s));
		s.clear();
		s.add(2);
		s.add(1000);
		s.add(2001);
		cl.add(new HashSet<Integer>(s));
		s.clear();
		s.add(0);
		s.add(1001);
		s.add(2001);
		cl.add(new HashSet<Integer>(s));
		s.clear();
		s.add(1);
		s.add(1001);
		s.add(2001);
		cl.add(new HashSet<Integer>(s));
		s.clear();
		s.add(2);
		s.add(1001);
		s.add(2001);
		cl.add(new HashSet<Integer>(s));
		
		s.clear();
		s.add(3);
		s.add(1001);
		s.add(2000);
		cl.add(new HashSet<Integer>(s));
		s.clear();
		s.add(3);
		s.add(1001);
		s.add(2001);
		cl.add(new HashSet<Integer>(s));
		
		return cl;
    	
    }
    
    
    /**
     * generating minimal cluster descriptions by MBRs
     * 
     * @author pishchulin
     * 23.10.08.
     * @param  List<List<Set<Integer>>> clusters as the list of clusters being connected dense units     
     * @return List<Cover> list of minimal covers w.r.t. list of clusters
     */
    
    private List<Cover> covering(List<List<Set<Integer>>> clusters){
    
    	List<Cover> result = new ArrayList<Cover>();

//		all units which were found after increasing or decreasing of interval id to 1;
		List<Set<Integer>> regionDelta = new ArrayList<Set<Integer>>();
		
//		all units which were found during the growing of the region in some particular dimention
		List<Set<Integer>> regionMax = new ArrayList<Set<Integer>>();
		
//		region from which the growing is started
		List<Set<Integer>> regionToGrow = new ArrayList<Set<Integer>>();
		
		List<Set<Integer>> unCovered = new ArrayList<Set<Integer>>();
		
		//MBRs
		HashSet<Rectangle> rectangles = new HashSet<Rectangle>();
		
    	int id = -1;
    	for(List<Set<Integer>> cl:clusters){
    		
    		//covering with max regions
    		//providing at the same time minimal covering,
    		//not like in CLIQUE, do not allow the rectangles to contain
    		//already covered units, regions can not grow by means of already covered units
    		
    		//MBRs
//    		HashSet<Rectangle> rectangles = new HashSet<Rectangle>();
    		
    		rectangles.clear();
    		
//    		cl = testCluster();
    		    		    		
//    		List<Set<Integer>> unCovered = new ArrayList<Set<Integer>>(cl);
    		unCovered.clear();
    		unCovered.addAll(cl);
    		
    		
    		for(Set<Integer> unit:cl){
    			
    			if (!unCovered.contains(unit))continue;
    			
    			//MBR
    			Rectangle r = new Rectangle();
    			
//        		region from which the growing is started
//    			List<Set<Integer>> regionToGrow = new ArrayList<Set<Integer>>();
    			regionToGrow.clear();
    			
    			regionToGrow.add(new HashSet<Integer>(unit));
				unCovered.remove(unit);
				Set<Integer> tmp = new HashSet<Integer>();

//				all units which were found after increasing or decreasing of interval id to 1;
//				List<Set<Integer>> regionDelta = new ArrayList<Set<Integer>>();
    			
				regionDelta.clear();
				
				for(int gId:unit){
    				int maxValue = gId;
					int minValue = gId;
					int maxValueLocal = gId;
					int minValueLocal = gId;

//		    		all units which were found during the growing of the region in some particular dimention
//		    		List<Set<Integer>> regionMax = new ArrayList<Set<Integer>>();
		    		regionMax.clear();
					
    				for(int step=-1;step<2;step+=2){
    					id = gId;
   						boolean hasGrown = true;
    					do{
    						if (!isInDomain(step, id)){//check whether id+step is still within domain (not like 2000-1 or 1999+1)
    							hasGrown=false;
    							break;
    						}
    						id+=step;
    						
    						//looking for all units, witch are different only in dimension
    						//of gId to get a rectangular region
    						hasGrown = true;
    						for(Set<Integer> u:regionToGrow){
    							tmp.clear();
    							tmp.addAll(u);
    							tmp.remove(gId);
    							tmp.add(id);
    							//escape the 3.2 step of the original CLIQUE
    							//allowing only grow by units which are not yet covered
    							if(unCovered.contains(tmp)){
    								regionDelta.add(new HashSet<Integer>(tmp));
    								//bounds of the MBR
    								if(maxValueLocal<id)
    									maxValueLocal=id;
    		    					else if(minValueLocal>id)
    		    						minValueLocal=id;
    							}else{
    								regionDelta.clear();
    								hasGrown = false;
    							}
    						}
    						if(hasGrown){
    							regionMax.addAll(new ArrayList<Set<Integer>>(regionDelta));
    							unCovered.removeAll(regionDelta);
    							maxValue=maxValueLocal;
    							minValue=minValueLocal;
    							regionDelta.clear();
    						}
    							
    					}while(hasGrown);
    				}
    				regionToGrow.addAll(new ArrayList<Set<Integer>>(regionMax));
    				r.addValues(minValue, maxValue);
    			}
    			r.addUnit(new ArrayList<Set<Integer>>(regionToGrow));
    			rectangles.add(r);
    		}
    		Cover cover = assignPoints(new HashSet<Rectangle>(rectangles));
    		result.add(cover);
    	}
    	return result;
    
    }
    
    protected Cover assignPoints(HashSet<Rectangle> rectangles) {
        
//    	Timer t = new Timer();
//		t.start();
    	ArrayList<Integer> maxValues = new ArrayList<Integer>();
    	maxValues = rectangles.iterator().next().getMaxValues();
    	boolean[] constraints = new boolean[m_dimensions];
    	
    	//to determine the dimensions use the max values of the MBR
		//could also use the min values
    	for(Integer id:maxValues){
			int constraint = id / __maxDomain;
            constraints[constraint] = true;
		}
    	
    	List<Integer> objects = new ArrayList<Integer>();
        for(Rectangle r:rectangles){
        	int position = 0;
            for (Instance dataInstance : m_dbstorage) {
            	boolean inCluster = true;
                double[] values = dataInstance.getFeatureArray();
                int ind = 0;
                for (int i = 0; i < m_dimensions; i++) {
                    if (!constraints[i])
                        continue;

                    int gridID = gridID(values[i], i);
//                  id must be between max and min values
                    if (!((gridID<=r.getMaxValues().get(ind)&&
                    		gridID>=r.getMinValues().get(ind)))) {
                        inCluster = false;
                        break;
                    }
                    ind++;
                }
                if (inCluster) {
                	objects.add(position);
                }
                position++;
            }
    	}
//        long time = t.stop();
//		System.out.println("assign points: " + time);
        return new Cover(constraints, new ArrayList<Integer>(
        		objects), rectangles);
    }
    
    
    private boolean isInDomain(int step, int id){
    	return !(((step>0)&&(remainder(id+step, __maxDomain) ==0))||
				   ((step<0)&&(remainder(id+step, __maxDomain) ==__maxDomain+step))||
				   (id+step<0));
    }
    
    
    private double remainder( double a, int b ) 
    { 
      return Math.signum(a) * 
             (Math.abs(a) - Math.abs(b) * Math.floor(Math.abs(a)/Math.abs(b))); 
    }
    
    public static void main(String args[]) {
    	
    	String path = "D:\\DA\\";
    	String params="";
    	
    	int xi;double tau;
//    	String name = "diabetes";int dimNr = 8;int xiStart = 2;int minFractionStart = 7;int iterNr = 15; 
//    	String name = "glass";int dimNr = 9;int xiStart = 2;int minFractionStart = 8;int iterNr = 15;
//    	String name = "shape";int dimNr = 17;int xiStart = 7;int minFractionStart = 9;int iterNrXi = 15; int iterNrFraction = 7;
//    	String name = "CliqueTest";int dimNr = 3;int xiStart = 10;int minFractionStart = 3;int iterNr = 1;
//    	String name = "liver-disorders";int dimNr = 6;int xiStart = 2;int minFractionStart = 2;int iterNr = 10;
//    	String name = "signs20";int dimNr = 20;int xiStart = 7;int minFractionStart = 10;int iterNrXi = 15; int iterNrFraction = 7;
//    	String name = "pendigits16";int dimNr = 16;int xiStart = 4;int minFractionStart = 8;int iterNrXi = 1; int iterNrFraction = 7;
//    	String name = "vowel";int dimNr = 10;int xiStart = 10;int minFractionStart = 11;int iterNr = 1;
//    	String name = "BreastCancerPrognostic";int dimNr = 33;int xiStart = 2;int minFractionStart = 2;int iterNr = 15;
//    	String name = "faceall25";int dimNr = 25;int xiStart = 10;int minFractionStart = 10;int iterNrXi = 15; int iterNrFraction = 7;
//    	String name = "pendigits48";int dimNr = 48;int xiStart = 10;int minFractionStart = 15;int iterNrXi = 1; int iterNrFraction = 2;
//    	String name = "pendigits32";int dimNr = 32;int xiStart = 9;int minFractionStart = 15;int iterNrXi = 10; int iterNrFraction = 4;
//    	String name = "faceall50";int dimNr = 50;int xiStart = 9;int minFractionStart = 12;int iterNrXi = 10; int iterNrFraction = 4;
//    	String name = "SwedishLeaf50";int dimNr = 50;int xiStart = 10;int minFractionStart = 13;int iterNrXi = 10; int iterNrFraction = 4;
    	//String name = "db_C10D5";int dimNr = 5;int xiStart = 10;int minFractionStart = 11;int iterNrXi = 1; int iterNrFraction = 1;
    	//String name = "db_C10D10";int dimNr = 10;int xiStart = 5;int minFractionStart = 12;int iterNrXi = 1; int iterNrFraction = 1;
    	//String name = "db_C10D15";int dimNr = 15;int xiStart = 6;int minFractionStart = 12;int iterNrXi = 1; int iterNrFraction = 1;
    	String name = "db_C10D15B";int dimNr = 15;int xiStart = 6;int minFractionStart = 12;int iterNrXi = 1; int iterNrFraction = 1;
    	for(int i=0;i<iterNrXi;i++){
    		xi = xiStart+i;
    		for(int j=0;j<iterNrFraction;j++){
        		tau=Math.pow(minFractionStart+j, -1);
        		params = ";xi="+xi+";tau="+tau;

        		Timer t = new Timer();
            	t.start();
            	File file = new File(path+name+".bin");		
            	DBStorage dbstorage = new DBStorage(file);
        		dbstorage.setDataSet(dbstorage.loadStructure());
        		
        		CLIQUE clique = new CLIQUE(dimNr, dbstorage, xi, tau);
        		List<Cover> cl = clique.runClustering();	
        		
        		long time = t.stop();
        		
        		new File(path+name+params+".log").delete();
        		Log log = new Log(path+name+params+".log", true, false); 
        		log.log("CLIQUE;DIM="+dimNr+";FILE="+file.getPath()+params+"\n");
        		for (Cover c :cl) {
//        			System.out.println(c);
        			log.log(c.toString());
        		}
        		log.log("time="+time+"\n");
        	}
    	}
    	
    	
		
	}
   
}
