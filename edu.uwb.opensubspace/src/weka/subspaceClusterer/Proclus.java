package weka.subspaceClusterer;


import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import i9.subspace.base.Cluster;
import weka.core.Capabilities;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.core.Capabilities.Capability;


public class Proclus  extends SubspaceClusterer implements OptionHandler{


  static final long serialVersionUID = -3235809600124455376L;

  private int k  = 4;
  private int d = 3;
  
  /**
   * Generates a clusterer. Has to initialize all fields of the clusterer
   * that are not being set via options.
   *
   * @param data set of instances serving as training data 
   * @throws Exception if the clusterer has not been 
   * generated successfully
   */
  public void buildSubspaceClusterer(Instances data) throws Exception {
	  
	  i9.subspace.proclus.Proclus proclus = new i9.subspace.proclus.Proclus(data,k,d);
	  proclus.runClustering();
	  List<Cluster> clusters = proclus.getClustering();
 	  setSubspaceClustering(clusters);
  }
  
  
  /**
   * Returns a string describing this clusterer
   * @return a description of the evaluator suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
    return "Cluster data using the Proclus algorithm";
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
				new Option("\tNumberOfClusters\n" 
						 + "\t(default "
						 + k 
						 + ")",
						 "K", 1, "-K <NumberOfClusters>"));
		result.addElement(
				new Option("\tAverageDimensions\n" 
						 + "\t(default "
						 + d 
						 + ")",
						 "D", 1, "-D <AverageDimensions>"));


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
		
		tmpStr = Utils.getOption('K', options);
		if (tmpStr.length() != 0)
			k = Integer.parseInt(tmpStr);
		
		tmpStr = Utils.getOption('D', options);
		if (tmpStr.length() != 0)
			d = Integer.parseInt(tmpStr);
	}

	/**
	 * Gets the current settings of the classifier.
	 *
	 * @return an array of strings suitable for passing to setOptions
	 */
	public String[] getOptions() {
		Vector result;

		result = new Vector();

		result.add("-K");
		result.add("" + k);
		result.add("-D");
		result.add("" + d);
		
		return (String[]) result.toArray(new String[result.size()]);
	}

	  /**
	   * Returns the tip text for this property
	   * 
	   * @return 		tip text for this property suitable for
	   * 			displaying in the explorer/experimenter gui
	   */
	  public String clustererTipText() {
	    return "...";
	  }

	  public int getNumberOfClusters(){
		  return k;
	  }
	  
	  public void setNumberOfClusters(int numerOfCluster){
		 k = numerOfCluster; 
	  }

	  public int getAverageDimensions(){
		  return d;
	  }
	  
	  public void setAverageDimensions(int averageDimension){
		  d= averageDimension; 
	  }

	  
	@Override
	public String getName() {
		return "PROCLUS";
	}


	@Override
	public String getParameterString() {
		return "K="+k+";D="+d;
	}

	public static void main (String[] argv) {
		runSubspaceClusterer(new Proclus(), argv);
	}	
}
