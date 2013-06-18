

package uwb.subspace.sarc;
import i9.data.core.DataSet;
import i9.subspace.base.Cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import weka.core.Instance;

/**
 * 
 * @author dave
 *
 */
public class SoftCluster extends Cluster {
	
	/** This is needed for serialization. */
	private static final long serialVersionUID = -8725673217556828433L;
	
	/** 
	 * The whole data set. This reference is maintained for quality calculations.
	 */
	private DataSet m_dataSet;
	
	/** The center the of the cluster */
	public double[] m_center;
	
	/** The sample variance of the cluster along each dimension */
	public double[] m_spread;
	
	/** 
	 * The weights associated with each attribute (or dimension). The sum of the
	 * elements of this vector is one.
	 */
	public double[] m_weights;
	
	/** Calculating quality is computationally expensive, so, it is cached. */
	private double m_score;
	
	/** 
	 * This constant is used in tuning attribute weights. Setting lambda to values
	 * greater than one will tend to force the weights to be more equal. Setting
	 * lambda to values less than one (but greater than zero) will cause the 
	 * weights associated with low spread attributes to dominate.
	 */
	private double m_lambda = 0.1;
	
	/** Cache the scores for each object. */
	public double[] m_objScore;
	
	/** Cache the distance to each object. */
	public double[] m_objDistances;
	
	/** Used to calculate the mean along a given dimension. */
	private static final Mean m_meanCalc = new Mean();
	
	/** Used to calculate sample variance (uses n-1) along a dimension. */
	private static final Variance m_varCalc = new Variance();
	
	/** The distance function the cluster will use. */
	private final Distance m_distance;
  
  public double getLambda() {
    return m_lambda;
  }

  public void setLambda(double lambda) {
    this.m_lambda = lambda;
  }
	
	/**
	 * Constructor.
	 * @param subspace
	 * @param objects
	 */
	public SoftCluster(boolean[] subspace, List<Integer> objects, DataSet data, 
	    Distance d) {
		super(subspace, objects);
		m_center = new double[subspace.length];
		m_spread = new double[subspace.length];
		m_weights = new double[subspace.length];
		m_dataSet = data;
		m_distance = (d != null) ? d : new NormalPDFDistance();
		m_varCalc.setBiasCorrected(true);
	}
	
	/**
	 * Uses sample to generate a cluster model.
	 * 
	 * @param sample -- A set of instances to use in determining the
	 *                  parameters of a cluster.                        
	 * @param data   -- The list of possible data points to be clustered.
	 * @return True all the time. Pretty useful, huh.
	 */
	public boolean calc(List<Integer> sample) {
	  List<double[]> discSet = transpose(sample);
		
	  m_objDistances = new double[m_dataSet.getInstanceCount()];
	  m_objScore = new double[m_dataSet.getInstanceCount()]; // allocate storage to cache each object score
		m_score = -1;                                          // Make sure quality is calc'd with next request
		
		// find the mean and variance along each dimension
		for (int c = 0; c < discSet.size(); ++c) {
		  // some code to test a heuristic to speed up the algorithm
	    // check to make sure that at least one of the dimensions is congregating
//		  double min, max;
//
//		  Arrays.sort(discSet.get(c));
//      min = discSet.get(c)[0];
//      max = discSet.get(c)[discSet.get(c).length - 1];
//      m_subspace[c] = (Math.abs(max - min) > 0.1) ? false : true;
		  
      // end heuristic insertion
      m_subspace[c] = true;
      m_center[c] = m_meanCalc.evaluate(discSet.get(c));
		  m_spread[c] = m_varCalc.evaluate(discSet.get(c), m_center[c]) + 0.00001; // add a small constant to prevent zeros 
		}
		calcWeights();
		
		// if at least one subspace dimension meets the requirements return true
    for (int i = 0; i < m_subspace.length; ++i) {
      if (m_subspace[i]) {
        return true;
      }
    }
    m_score = 0.0; // save time on quality calc, we want to reject this cluster
		return false;
	}

	/**
	 * Calculates the weights on each dimension using spread and lambda.
	 */
	public void calcWeights() {
	  double total = 0.0;
    
	  for (int i = 0; i < m_weights.length; i++) {
      m_weights[i] = Math.exp(-m_spread[i] / getLambda());
      total += m_weights[i];
    }
    for (int i = 0; i < m_weights.length; i++) {
      m_weights[i] /= total;
    }
	}
	
	/**
	 * Returns a transposed matrix.
	 * 
	 * @param sample -- The indexes of instances within data to transpose.
	 * @return A transposed matrix.
	 */
	private List<double[]> transpose(List<Integer> sample) {
		List<double[]> retVal = new ArrayList<double[]>();
		int cols = m_dataSet.getNumDimensions();
		int rows = sample.size();

		for (int c = 0; c < cols; ++c) {
			retVal.add(new double[rows]);
		}

		for (int r = 0; r < rows; ++r) {
		  Instance inst = m_dataSet.instance(sample.get(r));
			double values[] = inst.toDoubleArray();
			
			for (int c = 0; c < cols; ++c) {
				retVal.get(c)[r] = values[c];
			}
		}
		return retVal;
	}
	
	/**
	 * 
	 * @param other
	 * @return True if the calling cluster is higher quality than the other
	 *         cluster.
	 */
	public boolean higherQualityThan(SoftCluster other) {
	  boolean comp = (m_distance.compareLikelihood(this.conditionalQuality(), 
	      other.conditionalQuality()) > 0);
	  
	  return comp;
	}
	
	/**
   * @return The quality of the calling SoftCluster.
   */
  public double quality() {
    double sumDist = 0.0;   // sum of the probabilities over all pts
    double prodStdevs = 1.0; // the product of the std dev in each dimension
    int count = 0;      
    
    if (m_score >= 0) {
      return m_score; // return the cached value
    }
    
//    for (int dim = 0; dim < m_spread.length; dim++) {
//      prodStdevs *= m_spread[dim];
//    }
    
    for (Instance i : m_dataSet) {
      m_objDistances[count] = m_distance.calcDistance(i.toDoubleArray(), 
          m_center, m_spread, m_weights);
      m_objScore[count] = m_distance.calcLikelihood(i.toDoubleArray(), m_center, 
          m_spread, m_weights, getLambda()); 
      sumDist += m_objScore[count];
       
      count++;
    }
    m_score = sumDist / prodStdevs;
    
    return m_score;
  }
  
  /**
   * 
   * @return The quality of a cluster, calculated using only the points it owns.
   */
  public double conditionalQuality() {
    double ret_val = 0.0;
    
    this.quality(); // make sure quality has been calc'd on each obj
    
    for (int i : m_objects) {
      ret_val += m_objScore[i];
    }
    
    return ret_val;
  }
  
  public double overlap(SoftCluster other) {    
    throw new UnsupportedOperationException("Method not implemented!");
  }
  
  public void discardOutliers(double minScore) {
    int position = 0;
    
    for (int i = 0; i < m_objScore.length; i++) {
      if (m_objScore[i] > minScore) {
        m_objects.add(position);
      }
      position++;
    }
  }
  
  /**
   * 
   * @param threshold -- Dimensions with weights less than threshold are 
   *                     discarded.
   */
  public void setSubspace(double threshold) {
    for (int i = 0; i < m_subspace.length; i++) {
      m_subspace[i] = (m_weights[i] > threshold) ? true : false;
    }
  }
  
  /**
   * 
   */
  public String toString () {
    StringBuilder retVal = new StringBuilder();
    
    retVal.append("lambda= " + m_lambda + " ");
    retVal.append("center: [ ");
    for (int i = 0; i < m_center.length; i++) {
      retVal.append(String.format("%.2f", m_center[i]));
      retVal.append(" ");
    }
    retVal.append("] ");
    retVal.append("spread: [ ");
    for (int i = 0; i < m_spread.length; i++) {
      retVal.append(String.format("%.2f", m_spread[i]));
      retVal.append(" ");
    }
    retVal.append("] ");
    retVal.append("weights: [ ");
    for (int i = 0; i < m_weights.length; i++) {
      retVal.append(String.format("%.2f", m_weights[i]));
      retVal.append(" ");
    }
    retVal.append("] #: " + m_objects.size() + " / ");
    
    for (Integer i : m_objects) {
      retVal.append(String.format("%.2f", m_objScore[i]));
      retVal.append(" ");
    }
    
    return retVal.toString();
  }

 }



