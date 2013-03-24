package weka.subspaceClusterer;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import i9.subspace.base.Cluster;
import i9.subspace.p3c.P3C;
import weka.clusterers.SimpleKMeans;
import weka.core.Capabilities;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.core.Capabilities.Capability;


/**
 * Implementation of P3C
 * "P3C: A robust projected clustering algorithm" by Moise, Sander, Ester (ICDM'06)
 */
public class P3c  extends SubspaceClusterer implements OptionHandler{

  private static final long serialVersionUID = 1L;
  private double m_ChiSquareAlpha  = 0.005;
  private int m_PoissonThreshold = 19;
  
  /**
   * Generates a clusterer. Has to initialize all fields of the clusterer
   * that are not being set via options.
   *
   * @param data set of instances serving as training data 
   * @throws Exception if the clusterer has not been 
   * generated successfully
   */
  public void buildSubspaceClusterer(Instances data) throws Exception {
	  
	  List<Cluster> clusters = null;
	  P3C p3c = new P3C(data,m_PoissonThreshold,m_ChiSquareAlpha);
	  clusters = p3c.runClustering();
 	  setSubspaceClustering(clusters);
 	  
  }
  
  
  /**
   * Returns a string describing this clusterer
   * @return a description of the evaluator suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
    return "Cluster data using the P3C algorithm";
  }

  /**
   * Returns default capabilities of the clusterer.
   *
   * @return      the capabilities of this clusterer
   */
  public Capabilities getCapabilities() {
    Capabilities result = super.getCapabilities();

    // attributes
    result.enable(Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capability.NUMERIC_CLASS);

    return result;
  }

  
  /**
	 * Returns an enumeration describing the available options.
	 *
	 * @return 		an enumeration of all the available options.
	 */
    public Enumeration listOptions() {
		Vector result = new Vector();

		result.addElement(
				new Option("\tPoissonThreshold\n" 
						 + "\t(default "
						 + m_PoissonThreshold 
						 + ")",
						 "P", 1, "-P <poission>"));
		result.addElement(
				new Option("\tChiSquareAlpha\n" 
						 + "\t(default "
						 + m_ChiSquareAlpha 
						 + ")",
						 "A", 1, "-A <alpha>"));


		return result.elements();
	}

	/**
	 * Parses a given list of options. Valid options are:<p>
	 *
	 * @param options 	the list of options as an array of strings
	 * @throws Exception 	if an option is not supported
	 */
	public void setOptions(String[] options) throws Exception {
		String tmpStr;
		
		tmpStr = Utils.getOption('P', options);
		if (tmpStr.length() != 0)
			m_PoissonThreshold = Integer.parseInt(tmpStr);
		
		tmpStr = Utils.getOption('A', options);
		if (tmpStr.length() != 0)
			m_ChiSquareAlpha = Double.parseDouble(tmpStr);
	}

	/**
	 * Gets the current settings of the classifier.
	 *
	 * @return an array of strings suitable for passing to setOptions
	 */
	public String[] getOptions() {
		Vector result;

		result = new Vector();

		result.add("-P");
		result.add("" + m_PoissonThreshold);
		result.add("-A");
		result.add("" + m_ChiSquareAlpha);
		
		return (String[]) result.toArray(new String[result.size()]);
	}

	  /**
	   * Returns the tip text for this property
	   * 
	   * @return 		tip text for this property suitable for
	   * 			displaying in the explorer/experimenter gui
	   */
	  public String clustererTipText() {
	    return "Poission...";
	  }

	  public int getPoission(){
		  return m_PoissonThreshold;
	  }
	  
	  public void setPoission(int PoissonThreshold){
		  m_PoissonThreshold = PoissonThreshold; 
	  }

	  public double getAlpha(){
		  return m_ChiSquareAlpha;
	  }
	  
	  public void setAlpha(double ChiSquareAlpha){
		  m_ChiSquareAlpha= ChiSquareAlpha; 
	  }

	@Override
	public String getName() {
		return "P3C";
	}


	@Override
	public String getParameterString() {
		return "POISSION="+m_PoissonThreshold+";ALPHA="+m_ChiSquareAlpha;
	}
	
	public static void main (String[] argv) {
		runSubspaceClusterer(new P3c(), argv);
	}	
	
}
