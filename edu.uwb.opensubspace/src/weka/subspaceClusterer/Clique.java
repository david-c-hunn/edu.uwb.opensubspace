package weka.subspaceClusterer;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;

import i9.subspace.base.ArffStorage;
import i9.subspace.base.Cluster;
import i9.subspace.clique.CLIQUE;
import i9.subspace.clique.Cover;

public class Clique extends SubspaceClusterer implements OptionHandler{

	private static final long serialVersionUID = 7923724410794833810L;

	private int xi = 10;
	private double tau = 1;
	
	@Override
	public void buildSubspaceClusterer(Instances data) throws Exception {

		ArffStorage arffstorage = new ArffStorage(data);
		List<Cluster> clusters = new ArrayList<Cluster>();
		
		int dimensions = data.numAttributes();
		/*CLIQUE s = new CLIQUE(dimensions, arffstorage, xi, (double) (tau
				/ arffstorage.getSize()));*/
		CLIQUE s = new CLIQUE(dimensions, arffstorage, xi, tau);

		List<Cover> covers = s.runClustering();
		
		for (Cover c : covers) {
			clusters.add(c);
		}

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
                new Option("\tXI (default = 10)",
                        "XI",
                        1,
                        "-XI <int>"));
        vector.addElement(
                new Option("\tTAU (default = 1)",
                        "TAU",
                        1,
                        "-TAU <double>"));
        return vector.elements();
    }

   
    public void setOptions(String[] options) throws Exception {
        String optionString = Utils.getOption("XI", options);
        if (optionString.length() != 0) {
            xi = Integer.parseInt(optionString);
        }

        optionString = Utils.getOption("TAU", options);
        if (optionString.length() != 0) {
            tau = Double.parseDouble(optionString);
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

        options[current++] = "-XI";
        options[current++] = "" + xi;
        options[current++] = "-TAU";
        options[current++] = "" + tau;

        return options;
    }

    public String globalInfo() {
        return "CLIQUE";
    }
    
    public void setXI(int xi) {
        this.xi = xi;
    }

    public int getXI() {
        return xi;
    }

    public void setTAU(double tau) {
        this.tau = tau;
    }

    public double getTAU() {
        return tau;
    }

	@Override
	public String getName() {
		return "CLIQUE";
	}

	@Override
	public String getParameterString() {
		return "XI="+xi+";TAU="+tau+";";
	}

	public static void main (String[] argv) {
		runSubspaceClusterer(new Clique(), argv);
	}	

    
}
