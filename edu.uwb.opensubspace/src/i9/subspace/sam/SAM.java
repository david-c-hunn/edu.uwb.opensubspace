/*
 *    SAM.java
 *    Copyright (C) 2013 Dave Hunn
 *
 */

package i9.subspace.sam;
import i9.data.core.DBStorage;
import i9.data.core.DataSet;
import i9.data.core.Instance;
import i9.subspace.base.Cluster;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


/**
 * An implementation of the S.oft A.ssignemt M.onte Carlo subspace clustering
 * algorithm (SAM). 
 *  
 * @author Dave Hunn
 * @version 0.5
 */
public class SAM {
  
  /** Stores the data set as an ArrayList to get random access for sampling. */
  private DataSet m_dataSet = null;                

  /**  A list of the discovered clusters. */
  private List<Cluster> m_clusters = null;

  /** 
   * The minimum cluster density as a fraction of the total number of objects. 
   */
  private double m_alpha = 0.01;  

  /* 
   * Determines the trade-off between the number of dimensions and the number 
   * of instances in a cluster. 
   */
  private double m_beta = 0.25;  

  /** The allowable probability of failing to find a cluster that exists. */
  private double m_epsilon = 0.01;  

  /** 
   * The number of trials performed in the main loop of the algorithm. This
   * is sometimes called k. This parameter is determined probabilistically 
   * using alpha, beta, epsilon, the size of the discriminating set, the 
   * number of objects in the data set, and the number of dimensions in the
   * data set.
   */
  private int m_numTrials = 4096;  

  /** 
   * The number of instances sampled per trial or equivalently, the size
   * of the discriminating set. This is also called s.
   */
  private int m_sampleSize = 2; 

  /** The minimum cluster quality. Also called mu 0. */
  private double m_minQual = 1048576;  

  /** 
   * An optional parameter that allows the user to specify the number of 
   * clusters to search for. If it is set to a value less than or equal to zero,
   * then this parameter is ignored. 
   */
  private int m_numClusters = 0; 

  /** A random number generator used for sampling the data set. */
  private Random m_RNG;


  public void setDataSet(DBStorage db) {
    m_dataSet = db.getDataSet();
  }

  public void setAlpha(double a) {
    if (a > 0.0 && a < 1.0) {
      m_alpha = a;
    } else {
      System.err.println("Attempted to set m_alpha to an invalid value.");
    }
  }
  public double getAlpha() {
    return m_alpha;
  }

  public void setBeta(double b) {
    if (b > 0.0 && b < 1.0) {
      this.m_beta = b;
    } else {
      System.err.println("Attempted to set m_beta to an invalid value.");
    }
  }
  public double getBeta() {
    return m_beta;
  }

  public void setMinQual(double m) {
    if (m >= 0.0 ) {
      this.m_minQual = m;
    } else {
      System.err.println("Attempted to set minQual to an invalid value.");
    }
  }
  public double getMinQual() {
    return m_minQual;
  }

  public void setNumClusters(int n) {
    this.m_numClusters = n;
  }
  public int getNumClusters() {
    return m_numClusters;
  }
  
  public void setEpsilon(double e) {
    if (e >= 0.0 ) {
      this.m_epsilon = e;
    } else {
      System.err.println("Attempted to set epsilon to an invalid value.");
    }
  }
  public double getEpsilon() {
    return m_epsilon;
  }

  public void setSampleSize(int s) {
    if (s > 1) { // min sample size = 2
      this.m_sampleSize = s;
    }
  }
  public int getSampleSize() {
    return m_sampleSize;
  }
  
  public void setNumTrials(int n) {
    if (n > 0) { // can't have a negative number of trials.
      this.m_numTrials = n;
    }
  }
  public int getNumTrials() {
    return m_numTrials;
  }
  
  
  /**
   * Constructor.
   * @param alpha
   * @param beta
   * @param epsilon
   * @param numClusters
   * @param dbStorage
   */
  public SAM (double alpha, double beta, double epsilon, int numClusters,     
              DBStorage dbStorage) {
    int numDims = dbStorage.getDataSet().getNumDimensions();
    int numObjects = dbStorage.getDataSet().getInstanceCount();
    
    m_RNG = new Random();
    
    setAlpha(alpha);
    setBeta(beta);
    setEpsilon(epsilon);
    setNumClusters(numClusters);
    setDataSet(dbStorage);
    setSampleSize(calcDiscrimSetSize(numDims, m_beta));
    setNumTrials(calcNumTrials(m_alpha, m_beta, m_epsilon, m_sampleSize, 
                                     numObjects, numDims));
  }

  /**
   * findClusters
   * 
   * @return A list of discovered m_clusters.
   */
  public List<Cluster> findClusters() {
    SoftCluster   bestCluster    = null;
    SoftCluster   currCluster    = null;
    boolean       searching      = true;
    int           numClustersFoundLastTry = 0;
    double        last_total_qual = 0.0;
    double        this_total_qual = 0.0;

    m_clusters = new ArrayList<Cluster>();
    if (this.m_numClusters > 0)
      System.out.println("Started clustering: Searching for " + this.m_numClusters + " m_clusters.");
    while (searching) {
      bestCluster = null;
      for (int trial = 0; trial < getNumTrials(); ++trial) {
        currCluster = buildCluster();
        //System.out.println(currCluster.quality());
        if (! isRedundant(currCluster, 0.0)) {
          //System.out.println("Current found cluster is redundant, discard and continue.");
          addCluster(currCluster);
          //System.out.println("Found new cluster! That'm_sampleSize " + m_clusters.size() + " so far.");

          this_total_qual = 0.0;
          for (Cluster c : m_clusters) {
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
      } else if (m_clusters.size() <= numClustersFoundLastTry) {
        searching = false;
      } else {
        numClustersFoundLastTry = m_clusters.size();
        searching = stillSearching(bestCluster.quality(), m_clusters.size());
      }
      last_total_qual = this_total_qual;
    }

    // assign points to the cluster they score highest with
    for (int i = 0; i < m_dataSet.getInstanceCount(); i++) {
      SoftCluster best = (SoftCluster) m_clusters.get(0);

      for (Cluster c : m_clusters) {
        SoftCluster sc = (SoftCluster)c;
        if (sc.m_objScore[i] > best.m_objScore[i]) {
          best = sc;
        } 
      }
      best.m_objects.add(i);
    }

    int min_size = 0;// (int) (this.alpha * m_data.size());
    List<Cluster> remove = new ArrayList<Cluster>();

    for (Cluster c : m_clusters) {
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
      m_clusters.remove(c);
    }
    return m_clusters;
  }

  private void addCluster(SoftCluster newCluster) {
    int idx = 0;

    if (m_clusters.size() == 0) {
      m_clusters.add(newCluster);
    } else {
      for (Cluster c : m_clusters) {
        SoftCluster sc = (SoftCluster) c;

        if (newCluster.quality() > sc.quality()) {
          m_clusters.add(idx, newCluster);
          break;
        }
        idx++;
      }
      if (idx == m_clusters.size()) {
        m_clusters.add(newCluster);
      }
    } 
    if (m_numClusters > 0 && m_clusters.size() > m_numClusters) {
      m_clusters.remove(m_numClusters - 1);
    }
  }

  /**
   * 
   * @return
   */
  private SoftCluster buildCluster() {
    List<Integer> samp = randomSample(m_sampleSize);
    SoftCluster c = new SoftCluster(new boolean[m_dataSet.getNumDimensions()], 
                                    new ArrayList<Integer>());

    c.calc(samp, m_dataSet);

    return c;
  }

  /**
   *  
   * @param cluster   -- A SoftCluster to check for redundancy against existing
   *                     found m_clusters.
   * @param threshold -- If cluster'm_sampleSize center scores at least this amount against 
   *                     another
   * @return True if cluster is redundant.
   */
  private boolean isRedundant(SoftCluster cluster, double threshold) {
    List<Cluster> remove = new ArrayList<Cluster>();

    for (Cluster clust : m_clusters) {
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
    // remove m_clusters found 
    //assert(remove.size() == 0);
    for (Cluster c : remove) {
      m_clusters.remove(c);
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
    if (bestQual < m_minQual) {
      System.out.println("mu < m_minQual -> Done searching!");
      return false;
    } else if (m_numClusters > 0 && m_numClusters <= numClustersFound) {
      System.out.println("Found the designated number of m_clusters -> Done searching!");
      return false;
    }

    return true;
  }

  /** 
   * @param m_numDims
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
   * @return A randomly selected set of instances from the m_data set.
   */
  private List<Integer> randomSample(final int sampSize) {
    Set<Integer> sample = new HashSet<Integer>(sampSize);
    int numInstances = m_data.numInstances();
    int position;

    while (sample.size() < sampSize) {
      position = m_RNG.nextInt(numInstances);
      sample.add(position);
    }

    return new ArrayList<Integer>(sample);
  }


}




