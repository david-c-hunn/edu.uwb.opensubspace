package uwb.subspace.sepc;

import i9.data.core.Instance;
import i9.subspace.base.Cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import uwb.subspace.sepc.SEPC.DataPoint;

public class SepcCluster extends Cluster {
	
	private static final long serialVersionUID = -4939641414180123491L;
	private double           m_width;  // The m_width of this cluster in any dimension
	private double           m_beta;   // The trade-off factor between subspaces and objects
	private double[]         m_lowBounds;
	private double[]         m_uppBounds;
	
	/**
	 * Constructor
	 * @param subspace A boolean array indicating the congregating dimensions
	 *                 of the cluster.
	 * @param objects  A List of of indices of objects contained in the cluster.
	 */
	public SepcCluster(boolean[] subspace, List<Integer> objects) {
		super(subspace, objects);
		m_lowBounds = new double[m_subspace.length];
		m_uppBounds = new double[m_subspace.length];
		Arrays.fill(m_lowBounds, Double.MIN_VALUE);
		Arrays.fill(m_uppBounds, Double.MAX_VALUE);
	}

	/**
	 * Copy Constructor.
	 * @param other A SepcCluster to copy.
	 */
	public SepcCluster(SepcCluster other) {
		super(other.m_subspace, other.m_objects);
		m_lowBounds = Arrays.copyOf(other.m_lowBounds, other.m_lowBounds.length);
		m_uppBounds = Arrays.copyOf(other.m_uppBounds, other.m_uppBounds.length);
		m_width = other.m_width;
		m_beta = other.m_beta;
	}

	public double getWidth() {
		return m_width;
	}
	public void setWidth(double width) {
		this.m_width = width;
	}

	public double getBeta() {
		return m_beta;
	}
	public void setBeta(double beta) {
		this.m_beta = beta;
	}

	/**
	 * calcBounds
	 * @param sample A set of instances to use in determining the
	 *                          parameters of a cluster.                        
	 * @param data The list of possible data points to be clustered.
	 * @return True if a subspace cluster is found that meets the m_width 
	 *         requirement.
	 */
	public boolean calcBounds(List<Integer> sample, List<DataPoint> data) {
		ArrayList<ArrayList<Double>> discSet = transpose(sample, data);

		// find the congregating dimensions and the associated ranges
		for (int c = 0; c < discSet.size(); ++c) {
			double min, max;

			Collections.sort(discSet.get(c));
			min = discSet.get(c).get(0);
			max = discSet.get(c).get(discSet.get(c).size() - 1);
			if (Math.abs(max - min) <= m_width) {
				m_subspace[c] = true;
				m_lowBounds[c] = max - m_width;
				m_uppBounds[c] = min + m_width;
			} else {
				m_subspace[c] = false;
			}
		}
		// if at least one subspace dimension meets the requirements
		// return true
		for (int i = 0; i < m_subspace.length; ++i) {
			if (m_subspace[i]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * transpose: This helper method puts the data in the sample in a form
	 *            that facilitates finding the min and max in each dimension.
	 * @param sample The indexes of instances within data to transpose.
	 * @param data   The data to pull instances from.
	 * @return A transposed matrix.
	 */
	private ArrayList<ArrayList<Double>> transpose(List<Integer> sample, List<DataPoint> data) {
		ArrayList<ArrayList<Double>> retVal = new ArrayList<ArrayList<Double>>();
		int cols = data.get(0).instance.getDataSet().getNumDimensions();
		int rows = sample.size();

		for (int c = 0; c < cols; ++c) {
			retVal.add(new ArrayList<Double>());
		}

		for (int r = 0; r < rows; ++r) {
			Instance inst = data.get(sample.get(r)).instance;
			double values[] = inst.getFeatureArray();
			for (int c = 0; c < cols; ++c) {
				retVal.get(c).add(values[c]);
			}
		}
		return retVal;
	}

	/**
	 * bounds: An instance is bounded by a cluster if its points fall within the 
	 *         ranges defined for the cluster in each dimension. Note, in non-
	 *         congregating dimensions, a cluster has an infinite range.
	 * 
	 * @param inst An instance to check for inclusion in the cluster of the 
	 *             calling SepcCluster.
	 *             
	 * @return Returns true if inst is bounded by the cluster defined by the
	 *         calling ClusterBuilder.
	 */
	public boolean bounds(Instance inst) {
		boolean retVal = true;

		for (int i = 0; i < inst.getArray().length - 1; ++i) {
			if (inst.getElement(i) < m_lowBounds[i] || 
          inst.getElement(i) > m_uppBounds[i]) {
        retVal = false;
        break;
      }
		}
		return retVal;
	}

	/**
	 * A cluster's quality is related to the number of dimensions and points it 
	 * includes using the equation
	 * 
	 *   quality = n * (1/m_beta)^d
	 *   
	 * where n is the number of points in the cluster and d is the number of 
	 * congregating dimensions of the cluster.
	 * 
	 * TODO: For lage dimensional data sets quality needs to be a big double
	 * 
	 * @return The quality of the calling SepcCluster.
	 */
	public double quality () {
		int numDims = 0;
		double retVal;
		
		for (int i = 0; i < m_subspace.length; ++i) {
			numDims += m_subspace[i] ? 1 : 0;
		}
		retVal = m_objects.size() * Math.pow(1.0/m_beta, numDims);

		return retVal;
	}

	/**
	 * A static version of the quality function.
	 * @param numDims
	 * @param numPoints
	 * @param m_beta
	 * @return
	 */
	public static double quality(int numDims, int numPoints, double beta) {
		return numPoints * Math.pow((1.0/beta), numDims);
	}
	
	/**
	 * overlap: A measure of the overlap between two clusters. Returns a number
	 *          between 0 and 1 (inclusive). Overlap is measured as 
	 *          follows: 
	 *            (1) If two clusters do not span the same subspace within
	 *                the tolerance specified by maxUnmatchedSubspaces then 
	 *                they have zero overlap.
	 *            (2) Otherwise overlap is the number of instances in the calling
	 *                Cluster that occur in the other Cluster divided by the total
	 *                number of clusters in the calling Cluster.    
	 * @param other Another Cluster.
	 * @return How much the calling Cluster overlaps with the other Cluster.
	 */
	public double overlap(Cluster other, int maxUnmatchedSubspaces) {
		int count = 0;

		if (unMatchedSubspaces(other) > maxUnmatchedSubspaces) {
			return 0.0;
		}
		Collections.sort(this.m_objects);
		Collections.sort(other.m_objects);
		for (int inst : this.m_objects) {
			if (other.m_objects.contains(inst)) {
				count++;
			}	
		}
		return (double)count / (double)this.m_objects.size();
	}

	/**
	 * 
	 * @return The number of congregating dimensions of the calling cluster.
	 */
	public int subspaceSize() {
		int count = 0;
		
		for(int i = 0; i < m_subspace.length; ++i) {
			count += (m_subspace[i] ? 1 : 0);
		}
		return count;
	}
	
	/**
	 * 
	 * @param other Another cluster.
	 * @return The number of congregating dimensions that are different
	 *         between the calling cluster and the other cluster.
	 */
	public int unMatchedSubspaces(Cluster other) {
		int retVal = 0;

		for (int i = 0; i < this.m_subspace.length; ++i) {
			if (this.m_subspace[i] != other.m_subspace[i]) {
				retVal++;
			}
		}
		return retVal;
	}

	/**
	 * clear: Clears the SepcCluster.
	 */
	public void clear() {
		for (int i = 0; i < m_subspace.length; ++i) {
			m_subspace[i] = false;
		}
		Arrays.fill(m_lowBounds, Double.MIN_VALUE);
    Arrays.fill(m_uppBounds, Double.MAX_VALUE);
		m_objects.clear();
	}
}





