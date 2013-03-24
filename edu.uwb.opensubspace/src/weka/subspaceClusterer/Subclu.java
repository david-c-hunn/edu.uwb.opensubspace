package weka.subspaceClusterer;


import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;

import i9.subspace.base.ArffStorage;
import i9.subspace.base.Cluster;
import i9.subspace.subclu.SUBCLU;

public class Subclu extends SubspaceClusterer implements OptionHandler{

	private static final long serialVersionUID = 7923724410794833810L;

	private double epsilon = 4;
	private int minSupport = 4;
	private int minOutputDim = 1;
	
	@Override
	public void buildSubspaceClusterer(Instances data) throws Exception {

		ArffStorage arffstorage = new ArffStorage(data);
		List<Cluster> clusters = null;
		

		int dimensions = data.numAttributes();
		SUBCLU subclu = new SUBCLU(dimensions, epsilon, minSupport, arffstorage,
				minOutputDim);
		clusters = subclu.runClustering();

		setSubspaceClustering(clusters);
		toString();
		
	}

    /**
     * Returns an enumeration of all the available options..
     *
     * @return Enumeration An enumeration of all available options.
     */
    public Enumeration listOptions() {
        Vector vector = new Vector();

        vector.addElement(
                new Option("\tepsilon (default = )",
                        "E",
                        1,
                        "-E <double>"));
        vector.addElement(
                new Option("\tminPoints (default = )",
                        "M",
                        1,
                        "-M <int>"));
        return vector.elements();
    }

   
    public void setOptions(String[] options) throws Exception {
        String optionString = Utils.getOption('M', options);
        if (optionString.length() != 0) {
            minSupport = Integer.parseInt(optionString);
        }

        optionString = Utils.getOption('E', options);
        if (optionString.length() != 0) {
        	epsilon = Double.parseDouble(optionString);
        }
    }

    /**
     * Gets the current option settings for the OptionHandler.
     *
     * @return String[] The list of current option settings as an array of strings
     */
    public String[] getOptions() {
        String[] options = new String[4];
        int current = 0;

        options[current++] = "-M";
        options[current++] = "" + minSupport;
        options[current++] = "-E";
        options[current++] = "" + epsilon;

        return options;
    }

    public String globalInfo() {
        return "SUBCLU";
    }

    public void setEpsilon(double eps) {
        this.epsilon = eps;
    }
    
    public double getEpsilon() {
        return epsilon;
    }

    public void setminPoints(int minpts) {
        this.minSupport = minpts;
        
    }

    public int getminPoints() {
        return minSupport;
    }

	@Override
	public String getName() {
		return "Subclu";
	}

	@Override
	public String getParameterString() {
		return "EPS="+epsilon+";minPoints="+minSupport;
	}

	public static void main (String[] argv) {
		runSubspaceClusterer(new Subclu(), argv);
	}	

    
}
