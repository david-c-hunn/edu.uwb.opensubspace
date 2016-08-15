/*
 *    SEPC.java
 *    Copyright (C) 2013 Dave Hunn
 *
 */

package uwb.subspace.sepc;

import i9.data.core.DBStorage;
import i9.data.core.DataSet;
import i9.subspace.base.Cluster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.util.ArithmeticUtils;

/**
 *
 * @author Dave Hunn
 * @version 1.0
 */
public class SEPC {

    private boolean[] m_allocated;    // This array keeps track of which objects
                                      // have
                                      // been assigned to clusters
    private DataSet m_dataSet;        // The data to be analyzed
    private List<Cluster> m_clusters; // The discovered clusters
    private double m_alpha = 0.08;    // minimum cluster density
    private double m_beta = 0.25; // trade-off between the number of dimensions
                                  // and the number of instances
    private double m_epsilon =
        0.01; // allowable probability of failing to find a cluster that exists
    private double m_width = 200.0;   // max cluster width
    private int m_k = 2048;           // number trials per cluster
    private int m_s = 2;              // number of instances sampled per trial
    private double m_minQual = 100.0; // minimum cluster quality
    private int m_numClusters = 0;    // the number of clusters to look for
    private int m_numDims = 0;
    private Random m_RNG;
    private double m_maxOverlap = 0.75;
    private int m_maxUnmatchedSubspaces = 4;
    private boolean m_disjoint = false; // if this is true then each point may
                                        // belong to at most one cluster

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
    public SEPC(double alpha, double beta, double epsilon, double mu_0,
                int numClusters, double w, double maxOverlap,
                int maxUnmatchedSubspaces, double minSubspaceSize,
                boolean disjointMode, DBStorage dbStorage) {

        this.setAlpha(alpha);
        this.setBeta(beta);
        this.setEpsilon(epsilon);
        this.setMu_0(mu_0);
        this.setNumClusters(numClusters);
        this.setW(w);
        this.setMaxOverlap(maxOverlap);
        this.setMaxUnmatchedSubspaces(maxUnmatchedSubspaces);
        this.setData(dbStorage);
        m_disjoint = disjointMode;
        this.m_numDims = dbStorage.getDataSet().getNumDimensions();
        this.m_RNG = new Random();

        this.m_s = calcDiscrimSetSize(m_numDims, this.m_beta);
        this.m_k =
            calcNumTrials(this.m_alpha, this.m_beta, this.m_epsilon, this.m_s,
                          this.m_dataSet.numInstances(), m_numDims);
        double otherMu_0 = SepcCluster.quality(
            (int)Math.round(m_numDims * minSubspaceSize),
            (int)Math.round(m_dataSet.numInstances() * this.m_alpha),
            this.m_beta);
        if (this.m_minQual < otherMu_0)
            this.m_minQual = otherMu_0;
    }

    public void setData(DBStorage db) {
        m_dataSet = db.getDataSet();
        m_allocated = new boolean[m_dataSet.numInstances()];
    }

    public void setAlpha(double a) {
        if (a > 0.0 && a < 1.0) {
            m_alpha = a;
        } else {
            System.err.println("Attempted to set alpha to an invalid value.");
        }
    }
    public double getAlpha() { return m_alpha; }

    public void setBeta(double b) {
        if (b > 0.0 && b < 1.0) {
            this.m_beta = b;
        } else {
            System.err.println("Attempted to set beta to an invalid value.");
        }
    }
    public double getBeta() { return m_beta; }

    public void setW(double w) {
        if (w > 0.0) {
            this.m_width = w;
        } else {
            System.err.println("Attempted to set width to an invalid value.");
        }
    }
    public double getW() { return m_width; }

    public void setMu_0(double mu) {
        if (mu >= 0.0) {
            this.m_minQual = mu;
        } else {
            System.out.println("Attempted to set mu_0 to an invalid value.");
        }
    }

    public double getMu_0() { return m_minQual; }

    public int getNumClusters() { return m_numClusters; }

    public void setNumClusters(int numClusters) {
        this.m_numClusters = numClusters;
    }

    public double getEpsilon() { return m_epsilon; }

    public void setEpsilon(double epsilon) { this.m_epsilon = epsilon; }

    public double getMaxOverlap() { return m_maxOverlap; }

    /**
   * @return the k
   */
    public int getK() { return m_k; }

    /**
     * @param k the k to set
     */
    public void setK(int k) { this.m_k = k; }

    /**
     * @return the s
     */
    public int getS() { return m_s; }

    /**
     * @param s the s to set
     */
    public void setS(int s) { this.m_s = s; }

    public void setMaxOverlap(double maxOverlap) {
        if (maxOverlap >= 0.0 && maxOverlap < 1.0) {
            this.m_maxOverlap = maxOverlap;
        } else {
            System.out.println(
                "Attempted to set maxOverlap to an invalid value.");
        }
    }

    private int getMaxUnmatchedSubspaces() { return m_maxUnmatchedSubspaces; }

    private void setMaxUnmatchedSubspaces(int maxUnmatchedSubspaces) {
        this.m_maxUnmatchedSubspaces = maxUnmatchedSubspaces;
    }

    /**
     * findClusters
     *
     * @return A list of discovered clusters.
     */
    public List<Cluster> findClusters() {
        SepcCluster currCluster;
        SepcCluster bestCluster;
        boolean searching = true;
        boolean keepCluster;
        int numClustersFoundLastTry = 0;

        m_clusters = new ArrayList<Cluster>();
        while (searching) {
            bestCluster = new SepcCluster(new boolean[m_numDims],
                                          new ArrayList<Integer>());
            for (int trial = 0; trial < m_k; ++trial) {
                currCluster = buildCluster();
                keepCluster = isKeeper(currCluster, bestCluster.quality());
                if (keepCluster) {
                    if (!m_disjoint) {
                        removeSubClusters(currCluster);
                        m_clusters.add(currCluster);
                    }
                    if (bestCluster.quality() < currCluster.quality()) {
                        bestCluster = currCluster;
                    }
                }
            }
            if (m_disjoint) {
                // make sure we found a cluster other than the default best
                // cluster
                if (bestCluster.m_objects.size() > 0) {
                    allocatePoints(bestCluster);
                    m_clusters.add(bestCluster);
                }
            }
            if (m_clusters.size() <= numClustersFoundLastTry) {
                searching = false;
            } else {
                numClustersFoundLastTry = m_clusters.size();
                searching =
                    stillSearching(bestCluster.quality(), m_clusters.size());
            }
        }

        return m_clusters;
    }

    /**
     *
     * @param cluster
     */
    private void allocatePoints(SepcCluster cluster) {
        for (int i = 0; i < cluster.m_objects.size(); ++i) {
            int position = cluster.m_objects.get(i);
            m_allocated[position] = true;
        }
    }

    /**
     *
     * @param cluster
     */
    private void removeSubClusters(SepcCluster cluster) {
        for (ListIterator<Cluster> iter = m_clusters.listIterator();
             iter.hasNext();) {
            SepcCluster c = (SepcCluster)iter.next();
            if (c.overlap(cluster, m_maxUnmatchedSubspaces) > m_maxOverlap) {
                iter.remove();
            }
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
     *           with any existing cluster. Significant overlap is set by the
     * user
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

        if (cluster.quality() < m_minQual) {
            retVal = false; // min quality not met, try again
        } else if (m_disjoint) {
            if (cluster.quality() <= bestQual) {
                retVal = false;
            }
        } else if (!m_disjoint) {
            // check to see if the found cluster overlaps with existing clusters
            for (Cluster clust : m_clusters) {
                SepcCluster c = (SepcCluster)clust;
                if (cluster.overlap(c, m_maxUnmatchedSubspaces) >
                        m_maxOverlap &&
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
        List<Integer> samp = randomSample(m_s);
        SepcCluster c =
            new SepcCluster(new boolean[m_numDims], new ArrayList<Integer>());
        int position = 0;

        c.setBeta(m_beta);
        c.setWidth(m_width);
        if (c.calcBounds(samp, m_dataSet)) {
            for (int i = 0; i < m_dataSet.numInstances(); i++) {
                if (!m_allocated[i]) { // already added to a cluster, only used
                                       // in
                                       // disjoint mode
                    if (c.bounds(m_dataSet.instance(i))) {
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
        int minClusterSize = (int)(getAlpha() * m_dataSet.numInstances());

        if (bestQual < this.m_minQual) {
            // System.out.println("mu < mu_0 -> Done searching!");
            return false;
        } else if (m_numClusters > 0 && m_numClusters <= numClustersFound) {
            // System.out.println("Found the designated number of clusters ->
            // Done
            // searching!");
            return false;
        } else if (ptsRemaining < m_s) {
            // System.out.println("Fewer than s points left unclustered -> Done
            // searching!");
            return false;
        } else if (ptsRemaining < minClusterSize) {
            // System.out.println("Fewer than alpha of the data left unclustered
            // ->
            // Done searching!");
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

        for (boolean b : m_allocated) {
            retVal += b ? 0 : 1;
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
        int retVal =
            (int)Math.round(Math.log10((double)numDims / Math.log(4.0)) /
                            Math.log10(1 / aBeta));
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
                                     final int aNumInstances,
                                     final int aNumDims) {

        int m = (int)Math.ceil(anAlpha * aNumInstances);
        int l = (int)Math.floor(aBeta * m);
        double firstTerm =
            (double)choose(m, anS) / (double)choose(aNumInstances, anS);
        double secondTerm = (double)choose(l, anS) / (double)choose(m, anS);
        double Ptrial = firstTerm * (Math.pow(1.0 - secondTerm, aNumDims));
        int retVal =
            (int)Math.round(Math.log10(anEpsilon) / Math.log10(1.0 - Ptrial));

        return retVal;
    }

    /**
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
}
