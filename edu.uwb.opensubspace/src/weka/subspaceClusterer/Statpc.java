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

public class Statpc extends SubspaceClusterer implements OptionHandler{

	private static final long serialVersionUID = 7923724410794833810L;

	double alpha_0 = 1.0E-10;
	double alpha_k = 0.001;
	double alpha_h = 0.001;
	
	@Override
	public void buildSubspaceClusterer(Instances data) throws Exception {

		ArffStorage arffstorage = new ArffStorage(data);
		List<Cluster> clusters = null;

		i9.subspace.statpc.Statpc statpc = new i9.subspace.statpc.Statpc(arffstorage,
				alpha_0, alpha_k, alpha_h);
		clusters = statpc.getClusters();

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
                new Option("\tALPHA_0 (default = )",
                        "ALPHA_0",
                        1,
                        "-ALPHA_0 <double>"));
        vector.addElement(
                new Option("\tALPHA_k (default = )",
                        "ALPHA_k",
                        1,
                        "-ALPHA_k <double>"));
        vector.addElement(
                new Option("\tALPHA_h (default = )",
                        "ALPHA_h",
                        1,
                        "-ALPHA_h <double>"));
        return vector.elements();
    }

   
    public void setOptions(String[] options) throws Exception {
        String optionString = Utils.getOption("ALPHA_0", options);
        if (optionString.length() != 0) {
            alpha_0 = Double.parseDouble(optionString);
        }

        optionString = Utils.getOption("ALPHA_k", options);
        if (optionString.length() != 0) {
            alpha_k= Double.parseDouble(optionString);
        }

        optionString = Utils.getOption("ALPHA_h", options);
        if (optionString.length() != 0) {
        	alpha_h = Double.parseDouble(optionString);
        }
    }

    /**
     * Gets the current option settings for the OptionHandler.
     *
     * @return String[] The list of current option settings as an array of strings
     */
    public String[] getOptions() {
        String[] options = new String[6];
        int current = 0;

        options[current++] = "-ALPHA_0";
        options[current++] = "" + alpha_0;
        options[current++] = "-ALPHA_k";
        options[current++] = "" + alpha_k ;
        options[current++] = "-ALPHA_h";
        options[current++] = "" + alpha_h;

        return options;
    }

    public String globalInfo() {
        return "STATPC";
    }


	@Override
	public String getName() {
		return "STATPC";
	}

	@Override
	public String getParameterString() {
		return "ALPHA_0="+alpha_0+";ALPHA_k="+alpha_k+";ALPHA_h="+alpha_h;
	}

	public double getALPHA_0() {
		return alpha_0;
	}

	public void setALPHA_0(double alpha_0) {
		this.alpha_0 = alpha_0;
	}

	public double getALPHA_k() {
		return alpha_k;
	}

	public void setALPHA_k(double alpha_k) {
		this.alpha_k = alpha_k;
	}

	public double getALPHA_h() {
		return alpha_h;
	}

	public void setALPHA_h(double alpha_h) {
		this.alpha_h = alpha_h;
	}

	public static void main (String[] argv) {
		runSubspaceClusterer(new Statpc(), argv);
	}	

    
}
