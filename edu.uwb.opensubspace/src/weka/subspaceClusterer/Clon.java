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
import uantwerp.clon.ClonImpl;

public class Clon extends SubspaceClusterer implements OptionHandler {
	private static final long serialVersionUID = 17L;

	private int k = 170;
	private int minLen = 90;
	
	@Override
	public void buildSubspaceClusterer(Instances data) throws Exception {
		ArffStorage arffstorage = new ArffStorage(data);
		ClonImpl clon = new ClonImpl(arffstorage, k, minLen);
		List<Cluster> clusters = clon.runClustering();

		this.setSubspaceClustering(clusters);
		toString();
	}

    /**
     * Returns an enumeration of all the available options.
     *
     * @return Enumeration An enumeration of all available options.
     */
    public Enumeration listOptions() {
        Vector vector = new Vector();

        vector.addElement(
                new Option("\tk (default = 170)",
                        "k",
                        1,
                        "-k <int>"));
        vector.addElement(
                new Option("\tminLen (default = 90)",
                        "minLen",
                        1,
                        "-minLen <int>"));
        return vector.elements();
    }

   
    public void setOptions(String[] options) throws Exception {
        String optionString = Utils.getOption('k', options);
        if (optionString.length() != 0) {
            k = Integer.parseInt(optionString);
        }

        optionString = Utils.getOption("minLen", options);
        if (optionString.length() != 0) {
        	minLen = Integer.parseInt(optionString);
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

        options[current++] = "-k";
        options[current++] = "" + k;
        options[current++] = "-minLen";
        options[current++] = "" + minLen;

        return options;
    }

    public String globalInfo() {
        return "Carti-Clon";
    }

    public void setK(int val) {
        this.k = val;
    }
    
    public int getK() {
        return this.k;
    }

    public void setMinLen(int val) {
        this.minLen = val;
        
    }

    public int getMinMoints() {
        return this.minLen;
    }

	@Override
	public String getName() {
		return "Carti-Clon";
	}

	@Override
	public String getParameterString() {
		return "k=" + k + "; minlen=" + minLen;
	}

	public static void main (String[] argv) {
		runSubspaceClusterer(new Clon(), argv);
	}	

}
