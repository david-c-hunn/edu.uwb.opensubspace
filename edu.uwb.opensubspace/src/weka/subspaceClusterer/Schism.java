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
import i9.subspace.schism.SCHISM;

public class Schism extends SubspaceClusterer implements OptionHandler{

	private static final long serialVersionUID = 7923724410794833810L;
	private static final double rho = 0.8;
	
	private int xi = 10;
	private double tau = 0.0045;
	private double u = 0.0025;
	
	
	@Override
	public void buildSubspaceClusterer(Instances data) throws Exception {

		ArffStorage arffstorage = new ArffStorage(data);
		List<Cluster> clusters = null;
		
		int dimensions = data.numAttributes();
		SCHISM s = new SCHISM(dimensions, arffstorage, xi, tau, u, rho);

		clusters = s.runClustering();
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
                new Option("\tXI (default = 5)",
                        "XI",
                        1,
                        "-Xi <double>"));
        vector.addElement(
                new Option("\tTAU (default = 0.005)",
                        "TAU",
                        1,
                        "-T <int>"));
        vector.addElement(
                new Option("\tU (default = 0.05)",
                        "U",
                        1,
                        "-U <double>"));
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

        optionString = Utils.getOption("U", options);
        if (optionString.length() != 0) {
        	u = Double.parseDouble(optionString);
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

        options[current++] = "-XI";
        options[current++] = "" + xi;
        options[current++] = "-TAU";
        options[current++] = "" + tau;
        options[current++] = "-U";
        options[current++] = "" + u;

        return options;
    }

    public String globalInfo() {
        return "SCHISM";
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

    public void setU(double u) {
        this.u = u;
    }

    public double getU() {
        return u;
    }


	@Override
	public String getName() {
		return "Schism";
	}

	@Override
	public String getParameterString() {
		return "XI="+xi+";TAU="+tau+";U="+u; //+";RHO="+rho;
	}

	public static void main (String[] argv) {
		runSubspaceClusterer(new Schism(), argv);
	}	

    
}
