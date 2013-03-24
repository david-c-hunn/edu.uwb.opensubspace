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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


/**
 * 
 * @author Dave Hunn
 * @version 1.0
 */
public class SASC {	
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
  public SASC (double    alpha,           double  beta,    
               double    epsilon,         double  mu_0,
               int       numClusters,     double  w,
               double    maxOverlap,      int     maxUnmatchedSubspaces, 
               double    minSubspaceSize, boolean disjointMode, 
               DBStorage dbStorage) {

    this.setAlpha(alpha);
    this.setBeta(beta);
    this.setEpsilon(epsilon);
    //this.setEpsilon(0.001);
    //this.setMu_0(mu_0);
    this.setMu_0(0);
    //this.setNumClusters(numClusters);
    this.setNumClusters(10);
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
    System.out.println("k=" + k);
    System.out.println("s=" + s);
//    double otherMu_0 = SepcCluster.quality((int)Math.round(numDims*minSubspaceSize), 
//        (int)Math.round(data.size()*this.alpha), 
//        this.beta);
//    if (this.mu_0 < otherMu_0)
//      this.mu_0 = otherMu_0; 
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
      System.err.println("Attempted to set alpha to an invalid value.");
    }
  }
  public double getAlpha() {
    return alpha;
  }

  public void setBeta(double b) {
    if (b > 0.0 && b < 1.0) {
      this.beta = b;
    } else {
      System.err.println("Attempted to set beta to an invalid value.");
    }
  }
  public double getBeta() {
    return beta;
  }

  public void setW(double w) {
    if (w > 0.0) {
      this.width = w;
    } else {
      System.err.println("Attempted to set width to an invalid value.");
    }
  }
  public double getW() {
    return width;
  }

  public void setMu_0(double mu) {
    if (mu >= 0.0 ) {
      this.mu_0 = mu;
    } else {
      System.err.println("Attempted to set mu_0 to an invalid value.");
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
    SoftCluster   bestCluster    = null;
    SoftCluster   currCluster    = null;
    boolean       searching      = true;
    int           numClustersFoundLastTry = 0;
    double        last_total_qual = 0.0;
    double        this_total_qual = 0.0;
    
    clusters = new ArrayList<Cluster>();
    if (this.numClusters > 0)
      System.out.println("Started clustering: Searching for " + this.numClusters + " clusters.");
    while (searching) {
      bestCluster = null;
      for (int trial = 0; trial < k; ++trial) {
        currCluster = buildCluster();
        //System.out.println(currCluster.quality());
        if (! isRedundant(currCluster, 0.0)) {
          //System.out.println("Current found cluster is redundant, discard and continue.");
          addCluster(currCluster);
          //System.out.println("Found new cluster! That's " + clusters.size() + " so far.");
          
          this_total_qual = 0.0;
          for (Cluster c : clusters) {
            SoftCluster sc = (SoftCluster) c;
            this_total_qual += sc.quality();
          }
          System.out.print(this_total_qual + ",");
          //System.out.println("Total quality = " + this_total_qual);
        }
        if (bestCluster == null) {
          bestCluster = currCluster;
        } else if (bestCluster.quality() > currCluster.quality()) {          
          bestCluster = currCluster;
        }
      }
      if (last_total_qual < this_total_qual) {
        //TODO: create an object to store the clustering from each iteration
        searching = false;
        System.out.println("Quality decreased from the previuos iteration.");
      } else if (clusters.size() <= numClustersFoundLastTry) {
        searching = false;
      } else {
        numClustersFoundLastTry = clusters.size();
        searching = stillSearching(bestCluster.quality(), clusters.size());
      }
      last_total_qual = this_total_qual;
    }
    
    // assign points to the cluster they score highest with
    for (int i = 0; i < data.size(); i++) {
      SoftCluster best = (SoftCluster) clusters.get(0);
      
      for (Cluster c : clusters) {
        SoftCluster sc = (SoftCluster)c;
        if (sc.m_objScore[i] > best.m_objScore[i]) {
          best = sc;
        } 
      }
      best.m_objects.add(i);
    }
    
    int min_size = 0;// (int) (this.alpha * data.size());
    List<Cluster> remove = new ArrayList<Cluster>();
    
    for (Cluster c : clusters) {
      if (c.m_objects.size() < min_size) {
        remove.add(c);
      } else {
        SoftCluster sc = (SoftCluster) c;
        sc.setSubspace(0.03);
        System.out.println(sc.toString());
        System.out.print(sc.toString3());
      }
        
    }
    for (Cluster c : remove) {
      clusters.remove(c);
    }
    return clusters;
  }

  private void addCluster(SoftCluster newCluster) {
    int idx = 0;
    
    if (clusters.size() == 0) {
      clusters.add(newCluster);
    } else {
      for (Cluster c : clusters) {
        SoftCluster sc = (SoftCluster) c;

        if (newCluster.quality() > sc.quality()) {
          clusters.add(idx, newCluster);
          break;
        }
        idx++;
      }
      if (idx == clusters.size()) {
        clusters.add(newCluster);
      }
    } 
    if (numClusters > 0 && clusters.size() > numClusters) {
      clusters.remove(numClusters - 1);
    }
  }

/**
 * 
 * @return
 */
private SoftCluster buildCluster() {
  List<Integer> samp = randomSample(s);
  SoftCluster c = new SoftCluster(new boolean[numDims], new ArrayList<Integer>());
 
  c.calc(samp, data);

  return c;
}

/**
 *  
 * @param cluster   -- A SoftCluster to check for redundancy against existing
 *                     found clusters.
 * @param threshold -- If cluster's center scores at least this amount against 
 *                     another
 * @return True if cluster is redundant.
 */
private boolean isRedundant(SoftCluster cluster, double threshold) {
  List<Cluster> remove = new ArrayList<Cluster>();
  
  for (Cluster clust : clusters) {
    SoftCluster c = (SoftCluster)clust;
    double o1 = cluster.overlap(c);
    double o2 = c.overlap(cluster);
    if (o1 < threshold || o2 < threshold) {
      // cluster and c are too close, one of them will have to go
      if (cluster.quality() < c.quality()) {
        // cluster is better than the existing cluster, mark the existing cluster for deletion
        remove.add(c);
      } else {
        // cluster is not as good as the existing cluster
        return true;           
      }
    }
  } 
  // remove clusters found 
  //assert(remove.size() == 0);
  for (Cluster c : remove) {
    clusters.remove(c);
  }
  
  return false;
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
  if (bestQual < mu_0) {
    System.out.println("mu < mu_0 -> Done searching!");
    return false;
  } else if (numClusters > 0 && numClusters <= numClustersFound) {
    System.out.println("Found the designated number of clusters -> Done searching!");
    return false;
  }
  
  return true;
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
 * @return
 */
private static int calcNumTrials(final double anAlpha, final double aBeta, 
    final double anEpsilon, final int anS, 
    final int aNumInstances, final int aNumDims) {
  int m = (int)Math.ceil(anAlpha * aNumInstances);
  int l = (int)Math.floor(aBeta * m);
  double firstTerm = choose(m, anS).doubleValue() 
      / choose(aNumInstances, anS).doubleValue(); 
  double secondTerm = choose(l, anS).doubleValue()
      / choose(m, anS).doubleValue();
  double Ptrial =  firstTerm * (Math.pow(1.0 - secondTerm, aNumDims));
  int retVal = (int)Math.round(Math.log10(anEpsilon) 
      / Math.log10(1 - Ptrial));

  return retVal;
}

/**
 * Code obtained from: 
 * http://stackoverflow.com/questions/2201113/combinatoric-n-choose-r-in-java-math
 * 
 * @param N
 * @param K
 * @return
 */
private static BigInteger choose(final int N, final int K) {
  BigInteger ret = BigInteger.ONE;

  for (int k = 0; k < K; k++) {
    ret = ret.multiply(BigInteger.valueOf(N-k))
        .divide(BigInteger.valueOf(k+1));
  }
  return ret;
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




