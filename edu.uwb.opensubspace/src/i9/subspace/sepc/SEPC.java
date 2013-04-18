/*
 *    SEPC.java
 *    Copyright (C) 2013 Dave Hunn
 *
 */

package i9.subspace.sepc;
import i9.data.core.DBStorage;
import i9.data.core.Instance;
import i9.subspace.base.Cluster;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.util.ArithmeticUtils;


/**
 * 
 * @author Dave Hunn
 * @version 1.0
 */
public class SEPC {	
	/**
	 * A simple data structure to hold a reference to an instance and keep track
	 * of whether it has been used in a cluster.
	 * @author Dave Hunn
	 * @version 1.0
	 */
	public class DataPoint {
		public Instance instance;
		public boolean allocated = false;
		public DataPoint(Instance i) {
			instance = i;
		}
	}

	private List<DataPoint> data;                // The data to be analyzed
	private List<Cluster>   clusters;            // The discovered clusters
	private double          alpha       = 0.08;  // minimum cluster density
	private double          beta        = 0.25;  // trade-off between the number of dimensions and the number of instances 
	private double          epsilon     = 0.01;  // allowable probability of failing to find a cluster that exists 
	private double          width       = 200.0; // max cluster width 
	private int             k           = 2048;  // number trials per cluster 
	private int             s           = 2;     // number of instances sampled per trial
	private double          mu_0        = 100.0; // minimum cluster quality
	private int             numClusters = 0;     // the number of clusters to look for
	private int             numDims     = 0;
	private Random          gen;
	private double          maxOverlap  = 0.75;  
	private int             maxUnmatchedSubspaces = 4;
	private boolean         disjoint    = false;  // if this is true then each point may belong to at most one cluster

	/**
	 * 
	 * @param alpha
	 * @param beta
	 * @param epsilon
	 * @param mu_0
	 * @param numClusters
	 * @param w
	 * @param maxOverlap
	 * @param maxUnmatchedSubspaces
	 * @param minSubspaceSize
	 * @param disjointMode
	 * @param dbStorage
	 */
	public SEPC (double    alpha,           double  beta,    
	             double    epsilon,         double  mu_0,
	             int       numClusters,     double  w,
			         double    maxOverlap,      int     maxUnmatchedSubspaces, 
			         double    minSubspaceSize, boolean disjointMode, 
			         DBStorage dbStorage) {
	  
		this.setAlpha(alpha);
		this.setBeta(beta);
		this.setEpsilon(epsilon);
		this.setMu_0(mu_0);
		this.setNumClusters(numClusters);
		this.setW(w);
		this.setMaxOverlap(maxOverlap);
		this.setMaxUnmatchedSubspaces(maxUnmatchedSubspaces);
		this.setData(dbStorage);
		disjoint = disjointMode;
		this.numDims = dbStorage.getDataSet().getNumDimensions();
		this.gen = new Random();

		this.s = calcDiscrimSetSize(numDims, this.beta);
		this.k = calcNumTrials(this.alpha, this.beta, this.epsilon, this.s, 
				this.data.size(), numDims);
		double otherMu_0 = SepcCluster.quality((int)Math.round(numDims*minSubspaceSize), 
				                               (int)Math.round(data.size()*this.alpha), 
				                               this.beta);
		if (this.mu_0 < otherMu_0)
			this.mu_0 = otherMu_0; 
	}

	public void setData(DBStorage db) {
		data = new ArrayList<DataPoint>(db.getSize());
		for (Instance inst : db) {
			data.add(new DataPoint(inst));
		}
	}

	public void setAlpha(double a) {
		if (a > 0.0 && a < 1.0) {
			alpha = a;
		} else {
			System.out.println("Attempted to set alpha to an invalid value.");
		}
	}
	public double getAlpha() {
		return alpha;
	}

	public void setBeta(double b) {
		if (b > 0.0 && b < 1.0) {
			this.beta = b;
		} else {
			System.out.println("Attempted to set beta to an invalid value.");
		}
	}
	public double getBeta() {
		return beta;
	}

	public void setW(double w) {
		if (w > 0.0) {
			this.width = w;
		} else {
			System.out.println("Attempted to set width to an invalid value.");
		}
	}
	public double getW() {
		return width;
	}

	public void setMu_0(double mu) {
		if (mu >= 0.0 ) {
			this.mu_0 = mu;
		} else {
			System.out.println("Attempted to set mu_0 to an invalid value.");
		}
	}

	public double getMu_0() {
		return mu_0;
	}
	
	public int getNumClusters() {
		return numClusters;
	}

	public void setNumClusters(int numClusters) {
		this.numClusters = numClusters;
	}

	public double getEpsilon() {
		return epsilon;
	}

	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}

	public double getMaxOverlap() {
		return maxOverlap;
	}

	/**
   * @return the k
   */
  public int getK() {
    return k;
  }

  /**
   * @param k the k to set
   */
  public void setK(int k) {
    this.k = k;
  }

  /**
   * @return the s
   */
  public int getS() {
    return s;
  }

  /**
   * @param s the s to set
   */
  public void setS(int s) {
    this.s = s;
  }
  
  public void setMaxOverlap(double maxOverlap) {
		if (maxOverlap >= 0.0 && maxOverlap < 1.0) {
			this.maxOverlap = maxOverlap;
		} else {
			System.out.println("Attempted to set maxOverlap to an invalid value.");
		}
	}

	private int getMaxUnmatchedSubspaces() {
		return maxUnmatchedSubspaces;
	}

	private void setMaxUnmatchedSubspaces(int maxUnmatchedSubspaces) {
		this.maxUnmatchedSubspaces = maxUnmatchedSubspaces;
	}

	/**
	 * findClusters
	 * 
	 * @return A list of discovered clusters.
	 */
	public List<Cluster> findClusters() {
		SepcCluster   currCluster;
		double    bestQual;
		boolean       searching = true;
		boolean       keepCluster;
		int           numClustersFoundLastTry = 0;

		clusters = new ArrayList<Cluster>();
		while (searching) {
			bestQual = 0.0;

			for (int trial = 0; trial < k; ++trial) {
				currCluster = buildCluster();
				keepCluster = isKeeper(currCluster, bestQual);
				if (keepCluster) {
					if (disjoint) {
						allocatePoints(currCluster);
					} else {
						removeSubClusters(currCluster);
					}
					if (bestQual < currCluster.quality()) {
						bestQual = currCluster.quality();
					}
					clusters.add(new SepcCluster(currCluster));
				}
			}

			if (clusters.size() <= numClustersFoundLastTry) {
				searching = false;
			} else {
				numClustersFoundLastTry = clusters.size();
				searching = stillSearching(bestQual, clusters.size());
			}
		}

		return clusters;
	}

	/**
	 * 
	 * @param currCluster
	 */
	private void allocatePoints(SepcCluster currCluster) {
		for (int i = 0; i < currCluster.m_objects.size(); ++i) {
			int position = currCluster.m_objects.get(i);
			data.get(position).allocated = true;
		}
	}

	/**
	 * 
	 * @param cluster
	 */
	private void removeSubClusters(SepcCluster cluster) {
		List<Cluster> toRemove = new ArrayList<Cluster>();

		for (Cluster clust : clusters) {
			SepcCluster c = (SepcCluster)clust;
			if (c.overlap(cluster, maxUnmatchedSubspaces) > maxOverlap) {
				toRemove.add(c);
			}
		}
		for (Cluster clust : toRemove) {
			clusters.remove(clust);
		}
	}

	/**
	 * isKeeper: This function evaluates cluster against the quality criteria.
	 *           If the quality of cluster is lower than mu_0, then false is
	 *           returned. If disjoint mode has been enabled, then the method
	 *           will check cluster's quality against bestQual. If cluster's 
	 *           quality is less than bestQual then false is returned. If SEPC 
	 *           is running in non-disjoint mode, then
	 *           this method will check to see if cluster overlaps significantly
	 *           with any existing cluster. Significant overlap is set by the user
	 *           using the maxUnmatchedSubspaces and maxOverlap class variables.
	 *           If cluster overlaps significantly with an existing cluster and
	 *           it's quality is less than that existing cluster, then false is
	 *           returned.
	 * @param cluster
	 * @param bestQual
	 * @return Returns true if cluster is a keeper.
	 */
	private boolean isKeeper(SepcCluster cluster, double bestQual) {
		boolean retVal = true;
		
		if (cluster.quality() < mu_0) {
			retVal = false; // min quality not met, try again
		} else if (disjoint) {
			if (cluster.quality() <= bestQual) {
				retVal = false;
			}
		} else if (!disjoint) {
			// check to see if the found cluster overlaps with existing clusters
			for (Cluster clust : clusters) {
				SepcCluster c = (SepcCluster)clust;
				if (cluster.overlap(c, maxUnmatchedSubspaces) > maxOverlap && 
						cluster.quality() < c.quality()) {
					retVal = false; // currCluster is mostly in c
					break;
				}
			}
		} 
		return retVal;
	}

	/**
	 * 
	 * @return
	 */
	private SepcCluster buildCluster() {
		List<Integer> samp = randomSample(s);
		SepcCluster c = new SepcCluster(new boolean[numDims], new ArrayList<Integer>());
		int position = 0;

		c.setBeta(beta);
		c.setWidth(width);
		if (c.calcBounds(samp, data)) {
			for (DataPoint d : data) {
				if (!d.allocated) { // already added to a cluster, only used in disjoint mode
					if (c.bounds(d.instance)) {
						c.m_objects.add(position);
					}
				}
				position++;
			}
		}
		return c;
	}

	/**
	 * stillSearching
	 * 
	 * @param bestQual
	 * 
	 * @param numClustersFound
	 * 
	 * @return
	 */
	private boolean stillSearching(double bestQual, int numClustersFound) {
		int ptsRemaining = numPointsRemaining();
		
		if (bestQual < this.mu_0) {
			//System.out.println("mu < mu_0 -> Done searching!");
			return false;
		} else if (numClusters > 0 && numClusters <= numClustersFound) {
			//System.out.println("Found the designated number of clusters -> Done searching!");
			return false;
		} else if (ptsRemaining < s) {
			//System.out.println("Fewer than s points left unclustered -> Done searching!");
			return false;
		} else if (ptsRemaining < (int)(getAlpha() * data.size())) {
			//System.out.println("Fewer than alpha of the data left unclustered -> Done searching!");
			return false;
		} 
		return true;
	}

	/**
	 * numPointsRemaining
	 * 
	 * @return The number of points that have not been allocated to a cluster.
	 */
	private int numPointsRemaining() {
		int retVal = 0;

		for (DataPoint pt : data) {
			retVal += pt.allocated ? 0 : 1;  
		}
		return retVal;
	}

	/** 
	 * @param numDims
	 * 
	 * @param aBeta 
	 * 
	 * @return The optimal cardinality of a discriminating set.
	 */
	private static int calcDiscrimSetSize(final int numDims, 
			final double aBeta) {
		int retVal = (int)Math.round(Math.log10((double)numDims / Math.log(4.0)) 
				/ Math.log10(1 / aBeta));
		if (retVal < 2) // min set size is 2
			retVal = 2;
		return retVal;
	}

	/**
	 * 
	 * @param anAlpha
	 * @param aBeta
	 * @param anEpsilon
	 * @param anS
	 * @param aNumInstances
	 * @param aNumDims
	 * @return The optimal number of trials given the passed data.
	 */
	private static int calcNumTrials(final double anAlpha, final double aBeta, 
			final double anEpsilon, final int anS, 
			final int aNumInstances, final int aNumDims) {
		
	  int m = (int)Math.ceil(anAlpha * aNumInstances);
		int l = (int)Math.floor(aBeta * m);
		double firstTerm = (double)choose(m, anS) / (double)choose(aNumInstances, anS); 
		double secondTerm = (double)choose(l, anS) / (double)choose(m, anS);
		double Ptrial =  firstTerm * (Math.pow(1.0 - secondTerm, aNumDims));
		int retVal = (int)Math.round(Math.log10(anEpsilon) / Math.log10(1.0 - Ptrial));

		return retVal;
	}

	/**
	 * 
	 * @param N
	 * @param K
	 * @return N choose K.
	 */
	private static long choose(final int N, final int K) {
		long answer = ArithmeticUtils.binomialCoefficient(N, K);
	  
		return answer;
	}

	/**
	 * randomSample
	 * 
	 * @param sampSize The number of instances to include in the sample.
	 * 
	 * @return A randomly selected set of instances from the data set.
	 */
	private List<Integer> randomSample(final int sampSize) {
		Set<Integer> sample = new HashSet<Integer>(sampSize);
		int numInstances = data.size();
		int position;

		while (sample.size() < sampSize) {
			position = gen.nextInt(numInstances);
			if (data.get(position).allocated == false) {
				sample.add(position);
			}
		}

		return new ArrayList<Integer>(sample);
	}
}




