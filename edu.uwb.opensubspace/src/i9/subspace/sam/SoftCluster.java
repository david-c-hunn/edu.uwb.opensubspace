

package i9.subspace.sam;
import i9.data.core.DataSet;
import i9.subspace.base.Cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

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
	private double[] m_center;
	
	/** The standard deviation of the cluster along each dimension */
	private double[] m_spread;
	
	/** 
	 * The weights associated with each attribute (or dimension). The sum of the
	 * elements of this vector is one.
	 *  
	 */
	private double[] m_weights;
	
	/** Calculating quality is computationally expensive, so, it is cached. */
	private double m_score;
	
	/** 
	 * This constant is used in tuning attribute weights. Setting lambda to values
	 * greater than one will tend to force the weights to be more equal. Setting
	 * lambda to values less than one (but greater than zero) will cause the 
	 * weights associated with low spread attributes to dominate.
	 */
	private double m_lambda = 50;
	
	/** 
	 *  Also cache the scores for each object, since, only later are outliers
	 *  discarded.
	 */
	public double[] m_objScore;
	
	/** Used to calculate the mean along a given dimension. */
	private static final Mean m_meanCalc = new Mean();
	
	/** Used to calculate sample standard deviation (uses n-1) along a dimension. */
	private static final StandardDeviation m_stdDevCalc = new StandardDeviation();
	
	/** The distance function the cluster will use. */
	private Distance m_distance = null;
	
	public double[] center() {
    return m_center;
  }
  
  public double[] spread() {
    return m_spread;
  }
	
	/** 
	 * Copy Constructor.
	 * @param other Another SoftCluster to deep copy.
	 */
	public SoftCluster(SoftCluster other) {
	  super(other.m_subspace.clone(),new ArrayList<Integer>(other.m_objects)); 
	  this.m_center = Arrays.copyOf(other.m_center, other.m_center.length);
	  this.m_spread = Arrays.copyOf(other.m_spread, other.m_spread.length);
	  this.m_weights = Arrays.copyOf(other.m_weights, other.m_weights.length);
	  this.m_objScore = Arrays.copyOf(other.m_objScore, other.m_objScore.length);
	  this.m_dataSet = other.m_dataSet;
	  this.m_lambda = other.m_lambda;
	  this.m_distance = other.m_distance;
	}
	
	/**
	 * Constructor.
	 * @param subspace
	 * @param objects
	 */
	public SoftCluster(boolean[] subspace, List<Integer> objects, DataSet data) {
		super(subspace, objects);
		m_center = new double[subspace.length];
		m_spread = new double[subspace.length];
		m_weights = new double[subspace.length];
		m_dataSet = data;
		
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
	  ArrayList<double[]> discSet = transpose(sample);
		
	  m_objScore = new double[m_dataSet.getInstanceCount()]; // allocate storage to cache each object score
		m_score = -1;                                          // Make sure quality is calc'd with next request
		
		// find the mean and standard deviation along each dimension
		for (int c = 0; c < discSet.size(); ++c) {
		  m_center[c] = m_meanCalc.evaluate(discSet.get(c));
		  m_spread[c] = m_stdDevCalc.evaluate(discSet.get(c), m_center[c])
		              / Math.sqrt((double)discSet.get(c).length);
		  
		}
		// calc the weights for each attribute
		double total = 0.0;
		for (int i = 0; i < discSet.size(); i++) {
		  m_weights[i] = Math.exp(-m_spread[i] / m_lambda);
		  total += m_weights[i];
		}
		for (int i = 0; i < discSet.size(); i++) {
      m_weights[i] /= total;
      //System.out.println(m_weights[i]);
    }
		
		return true;
	}

	/**
	 * Returns a transposed matrix.
	 * 
	 * @param sample -- The indexes of instances within data to transpose.
	 * @return A transposed matrix.
	 */
	private ArrayList<double[]> transpose(List<Integer> sample) {
		ArrayList<double[]> retVal = new ArrayList<double[]>();
		int cols = m_dataSet.getNumDimensions();
		int rows = sample.size();

		for (int c = 0; c < cols; ++c) {
			retVal.add(new double[rows]);
		}

		for (int r = 0; r < rows; ++r) {
		  Instance inst = m_dataSet.instance(1);
			double values[] = inst.toDoubleArray();
			
			for (int c = 0; c < cols; ++c) {
				retVal.get(c)[r] = values[c];
			}
		}
		return retVal;
	}
	
	/**
   * @return The quality of the calling SoftCluster.
   */
  public double quality() {
    double sumProbs = 0.0;   // sum of the probabilities over all pts
    double prodStdevs = 1.0; // the product of the std dev in each dimension
    int count = 0;      
    
    if (m_score > 0) {
      return m_score; // return the cached value
    }
    
    for (Instance i : m_dataSet) {
      m_objScore[count] = score(i); 
      sumProbs += m_objScore[count];
      count++;
    }
    m_score = sumProbs / prodStdevs;
    
    return m_score;
  }
  
  /**
   * 
   * @param i
   * @return 
   */
  public double score(Instance i) {
    double exponent = 0.0;
    double retVal = 0.0;
    
    for (int j = 0; j < m_center.length; j++) {
      exponent +=  score(i.value(j), j);
    }
    retVal = Math.exp(exponent);
    
    return retVal;
  }
  
  /**
   * 
   * @param attribute
   * @param dim
   * @return
   */
  public double score(double attribute, int dim) {
    double retVal = - Math.pow(attribute - m_center[dim], 2) 
                  / (2 * Math.pow(m_spread[dim], 2)); 
    retVal *= m_weights[dim];
    
    return retVal;
  }
  
  public double overlap(SoftCluster other) {
    double exponent = 0.0;
    double retVal = 0.0;
    
    assert(other.m_center.length == this.m_center.length);
    for (int j = 0; j < other.m_center.length; j++) {
      exponent +=  score(other.m_center[j], j);
    }
    retVal = Math.exp(exponent);
    assert(retVal > 0.0);
    
    return retVal;
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
    double min = Double.MAX_VALUE;
    double max = Double.MIN_VALUE;
    double total = 0.0;
    double avg = 0.0;
    
    retVal.append("[ ");
    for (int i = 0; i < m_weights.length; i++) {
      retVal.append(String.format("%.2f", m_weights[i]));
      retVal.append(" ");
    }
    retVal.append(" ] #: " + m_objects.size() + " / ");
    
    for (Integer i : m_objects) {
      retVal.append(m_objScore[i] + " ");
    }
    
    return retVal.toString();
  }
}



