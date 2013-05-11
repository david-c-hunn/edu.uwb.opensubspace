/*
 *    SARC.java
 *    Copyright (C) 2013 Dave Hunn
 *
 */

package i9.subspace.sarc;
import i9.data.core.DBStorage;
import i9.data.core.DataSet;
import i9.subspace.base.Cluster;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import weka.core.Instance;
import weka.core.Instances;
import weka.subspaceClusterer.SubspaceClusterer;



/**
 * An implementation of the S.oft A.ssignemt R.andomized Clustering 
 * algorithm (SARC). 
 *  
 * @author Dave Hunn
 * @version 0.5
 */
public class SARC {

  /** Stores the data set as an ArrayList to get random access for sampling. */
  private DataSet m_dataSet = null;                

  /**  A list of the discovered clusters. */
  private List<Cluster> m_clusters = null;

  private final Distance m_distance;

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
  private double m_minQual = 200;  

  /** 
   * An optional parameter that allows the user to specify the number of 
   * clusters to search for. If it is set to a value less than or equal to zero,
   * then this parameter is ignored. 
   */
  private int m_numClusters = 0; 

  /** A random number generator used for sampling the data set. */
  private Random m_RNG;

  /** An array of the global clustering score after each iteration. */
  private List<Double> m_globalScores;

  /** The current iteration. */
  private int m_iter = 0;

  /** 
   * This factor determines when the algorithm has converged. If the 
   * difference between the current global score and the previous
   * global score is less than delta, then the algorithm has convered to some
   * maximum global quality.   
   */
  private double m_delta = 2.0;

  /** Show debug messages. */
  private boolean m_verbose = true;

  private int m_numThreads;

  public double getGlobalScore() {
    if (m_iter >= m_globalScores.size()) { // we don't have a cached value for the current iteration
      double score  = 0.0;

      for (Cluster c : m_clusters) {
        SoftCluster sc = (SoftCluster) c;
        score += sc.conditionalQuality();
      }
      m_globalScores.add(score);
    }

    return m_globalScores.get(m_iter);
  }

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

  public double getDelta() {
    return m_delta;
  }

  public void setDelta(double d) {
    this.m_delta = d;
  }

  public void setVerbose(boolean v) {
    m_verbose = v;
  }
  public boolean getVerbose() {
    return m_verbose;
  }

  public String getGlobalScoresString() {
    StringBuilder retVal = new StringBuilder();
    int idx = 1;

    for (double score : m_globalScores) {
      retVal.append(idx++);
      retVal.append(",");
      retVal.append(score);
      retVal.append('\n');
    }

    return retVal.toString();
  }

  /**
   * Constructor.
   * @param alpha
   * @param beta
   * @param epsilon
   * @param numClusters
   * @param dbStorage
   */
  public SARC (double alpha, double beta, double epsilon, double minQual,
      int numClusters, DBStorage dbStorage) {
    int numDims = dbStorage.getDataSet().getNumDimensions();
    int numObjects = dbStorage.getDataSet().getInstanceCount();

    m_RNG = new Random();

    setAlpha(alpha);
    setBeta(beta);
    setEpsilon(epsilon);
    setMinQual(minQual);
    setNumClusters(numClusters);
    setDataSet(dbStorage);
    setSampleSize(calcDiscrimSetSize(numDims, m_beta));
    setNumTrials(calcNumTrials(m_alpha, m_beta, m_epsilon, m_sampleSize, 
        numObjects, numDims));
    //TODO: improve this 
    m_distance = new NormalPDFDistance();
    m_numThreads = Runtime.getRuntime().availableProcessors();
  }

  /** 
   * @return A list of discovered clusters.
   */
  public List<Cluster> findClustersInParallel() {
    SoftCluster     bestCluster    = null;
    SoftCluster     currCluster    = null;
    boolean         searching      = true;
    ExecutorService exec           = Executors.newCachedThreadPool();
    List<Future<SoftCluster>> candidateClusters = new ArrayList<Future<SoftCluster>>();
    
    m_clusters = new ArrayList<Cluster>();
    m_globalScores = new ArrayList<Double>();
    m_globalScores.add(0.0); // add zero as first element, so, we don't have to 
    m_iter = 1;              // check if we are on the first iteration in the 
    
    while (searching) {
      bestCluster = null;
      for (int trial = 0; trial < m_numTrials; trial += m_numThreads) {
        candidateClusters.clear();
        // submit m_numThreads jobs to the executor
        for (int task = 0; task < m_numThreads; task++) {
          Callable<SoftCluster> builder = new ClusterBuilder();
          Future<SoftCluster> submit = exec.submit(builder);
          candidateClusters.add(submit);
        }
        try {
          // pull out the first candidate cluster
          currCluster = candidateClusters.get(0).get();
          // find the best cluster built and cache it, discard all others
          for (Future<SoftCluster> future : candidateClusters) {
            SoftCluster sc = future.get();
            if (sc.conditionalQuality() > currCluster.conditionalQuality()) {
              currCluster = sc;
            }
          }
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (ExecutionException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        m_clusters.add(currCluster);
        assignObjectsToClusters();
        if (m_distance.compare(currCluster.conditionalQuality(), m_minQual) < 0 ) {
          m_clusters.remove(currCluster);
          continue; // cluster doesn't meet the minimum criteria
        }
        if (bestCluster == null) {
          bestCluster = currCluster;
        } else if (m_distance.compare(bestCluster.conditionalQuality(), 
            currCluster.conditionalQuality()) < 0 ) {          
          m_clusters.remove(bestCluster); 
          bestCluster = currCluster;
        } else {
          m_clusters.remove(currCluster);
        } 
      }
      assignObjectsToClusters();
      getGlobalScore();
      searching = stillSearching(bestCluster, m_clusters.size());
      ++m_iter;
    }

    System.out.println(getGlobalScoresString());
    
    return m_clusters;
  }

  
  /** 
   * @return A list of discovered clusters.
   */
  public List<Cluster> findClusters() {
    SoftCluster   bestCluster    = null;
    SoftCluster   currCluster    = null;
    boolean       searching      = true;

    m_clusters = new ArrayList<Cluster>();
    m_globalScores = new ArrayList<Double>();
    m_globalScores.add(0.0); // add zero as first element, so, we don't have to 
    m_iter = 1;              // check if we are on the first iteration in the 
    // while loop.
    if (m_verbose) {
      if (this.m_numClusters > 0) {
        System.out.println("Started clustering: Searching for " 
            + this.m_numClusters + " clusters.");
      }
    }
    while (searching) {
      bestCluster = null;
      for (int trial = 0; trial < m_numTrials; ++trial) {
        currCluster = buildCluster();
        m_clusters.add(currCluster);
        currCluster.quality(); // force quality calc on all objects
        assignObjectsToClusters();
        if (m_distance.compare(currCluster.conditionalQuality(), m_minQual) < 0 ) {
          m_clusters.remove(currCluster);
          continue; // cluster doesn't meet the minimum criteria
        }
        if (bestCluster == null) {
          bestCluster = currCluster;
        } else if (m_distance.compare(bestCluster.conditionalQuality(), 
            currCluster.conditionalQuality()) < 0 ) {          
          m_clusters.remove(bestCluster); 
          bestCluster = currCluster;
        } else {
          m_clusters.remove(currCluster);
        } 
      }
      assignObjectsToClusters();
      getGlobalScore();
      if (Math.abs(m_globalScores.get(m_iter - 1) - m_globalScores.get(m_iter)) <= m_delta) {
        System.out.println("We have reached the convergence zone!");
        System.out.println("Found " + m_clusters.size() + " clusters.");
        //searching = false;
      } //else {
      if (m_verbose) {
        if (bestCluster != null)
          System.out.println("Best Cluster Quality from this iteration: " + 
              bestCluster.conditionalQuality());
      }
      if (bestCluster == null) {
        searching = false;
      } else {
        searching = stillSearching(bestCluster, m_clusters.size());
      }
      ++m_iter;
    }

    System.out.println(getGlobalScoresString());
    
    return m_clusters;
  }

  private class ClusterBuilder implements Callable<SoftCluster> {
    SoftCluster cluster;
    
    public SoftCluster getCluster() {
      return cluster;
    }
    
    // Constructor
    ClusterBuilder() {
      cluster = new SoftCluster(new boolean[m_dataSet.numAttributes()],
          new ArrayList<Integer>(), m_dataSet, m_distance);
    }

    @Override
    public SoftCluster call() throws Exception {
      List<Integer> samp = randomSample(m_sampleSize, m_dataSet.numInstances());
      
      cluster.calc(samp);
      assignObjectsToCluster();
      
      return cluster;
    }
    
    private void assignObjectsToCluster() {
      boolean itsMine = true;
      
      for (int i = 0; i < m_dataSet.getInstanceCount(); i++) {
        itsMine = true;
        for (Cluster c : m_clusters) {
          SoftCluster sc = (SoftCluster)c;
          if (sc.m_objScore[i] > cluster.m_objScore[i]) {
            itsMine = false;
            break;
          } 
        }
        if (itsMine) {
          cluster.m_objects.add(i);
        }
      }
    }
  }

  
  /**
   * Assigns objects to the cluster they score highest with. Equivalently,
   * assigns each object to the closest cluster centroid.
   */
  private void assignObjectsToClusters() {
    for (Cluster c : m_clusters) {
      c.m_objects.clear();
    }

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
  }

  /**
   * Adds newCluster to the list of discovered clusters in quality order. So,
   * the highest quality cluster is first in the list and the lowest quality 
   * cluster is last.
   * @param newCluster -- A cluster to add to the list of discovered clusters.
   */
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
   * @return A SoftCluster.
   */
  private SoftCluster buildCluster() {
    List<Integer> samp = randomSample(m_sampleSize);
    SoftCluster c = new SoftCluster(new boolean[m_dataSet.getNumDimensions()], 
        new ArrayList<Integer>(), 
        m_dataSet, 
        m_distance);

    c.calc(samp);

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
   * 
   * @param bestCluster
   * @param numClustersFound
   * @return
   */
  private boolean stillSearching(SoftCluster bestCluster, int numClustersFound) {
    boolean retVal = true;
    
    if (bestCluster == null) {
      retVal = false;
    } else if (bestCluster.conditionalQuality() < m_minQual) {
      retVal = false;
    } else if (m_numClusters > 0 && m_numClusters <= numClustersFound) {
      retVal = false;
    }

    return retVal;
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
   * @return A randomly selected set of instances from the m_dataSet.
   */
  private List<Integer> randomSample(final int sampSize) {
    Set<Integer> sample = new HashSet<Integer>(sampSize);
    int numInstances = m_dataSet.numInstances();
    int position;

    while (sample.size() < sampSize) {
      position = m_RNG.nextInt(numInstances);
      sample.add(position);
    }

    return new ArrayList<Integer>(sample);
  }

  /**
   * randomSample
   * 
   * @param sampSize The number of instances to include in the sample.
   * 
   * @return A randomly selected set of instances from the m_dataSet.
   */
  private List<Integer> randomSample(final int sampSize, final int maxIdx) {
    Set<Integer> sample = new HashSet<Integer>(sampSize);
    int position;

    while (sample.size() < sampSize) {
      position = m_RNG.nextInt(maxIdx);
      sample.add(position);
    }

    return new ArrayList<Integer>(sample);
  }

  

}




