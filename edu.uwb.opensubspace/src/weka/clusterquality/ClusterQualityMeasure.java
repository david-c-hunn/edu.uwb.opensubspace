package weka.clusterquality;

/**
 * @author jansen
 *  
 * The class implements the abstract class for weka evaluation measures.
 * calculateQuality() does all the required calculations. Either getValuePerCluster(), 
 * getOverallValue(), getCustomOutput(), or all of them need to be overridden, otherwise 
 * nothing will be printed to the weka console. 
 *
 */

import i9.subspace.base.Cluster;
import java.util.ArrayList;
import weka.core.Instances;

public abstract class ClusterQualityMeasure {

	

	/**
 	 * This method is called once from within SubspaceClusterEvaluation and 
	 * within the method all needed output values should be calculated
	 * @param clusterList the list of clusters resulting form the clustering
	 * @param instances the Instances of the selected dataset
	 * @param trueclusters list of true clusters read from an external file, added through the EvaluationGUI 
	 */
	public abstract void calculateQuality(ArrayList<Cluster> clusterList, Instances instances, ArrayList<Cluster> trueclusters);

	
	/**
	 * Override this method if the measure supports per cluster values
	 * @param i nr of cluster
	 * @return quality measure for cluster i of the clustering
	 */
	public Double getValuePerCluster(int i){
		return null;
	}

	/** Override this method if the measure supports an overall value (e.g. sum or mean)
	 * @return overall quality measure for the clustering
	 */
	public Double getOverallValue(){
		return null;
	}

	/** Override this method if the measure supports (additional) data that can not be 
	 * displayed in the overview cluster table. The data is being displayed in its own 
	 * block per measure below the overview cluster table.
	 * @return String with non-table data
	 */
	public String getCustomOutput(){
		return null;
	}


	/** Name of the measure 
	 * @return Name of the measure
	 */
	public abstract String getName();	
	
	
}
