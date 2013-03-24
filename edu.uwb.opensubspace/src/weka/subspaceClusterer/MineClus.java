package weka.subspaceClusterer;

import i9.subspace.base.ArffStorage;
import i9.subspace.base.Cluster;
import i9.subspace.ken.MineCLUS;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;

public class MineClus extends SubspaceClusterer implements OptionHandler {

	private static final long serialVersionUID = 7923724410794833810L;

	private double alpha = 0.001;
	private double beta = 0.1;
	private double w = 50;
	private int maxout = -1;
	private int numbin = 1;
	private int k = 2;

	@Override
	public void buildSubspaceClusterer(Instances data) throws Exception {

		ArffStorage arffstorage = new ArffStorage(data);
		List<Cluster> clusters = null;

		int dimensions = data.numAttributes();
		MineCLUS s = new MineCLUS(alpha, beta, w, maxout, numbin, k,
				arffstorage);
		setSubspaceClustering(s.cluster_result);
		toString();

	}

	/**
	 * Returns an enumeration of all the available options..
	 * 
	 * @return Enumeration An enumeration of all available options.
	 */
	public Enumeration listOptions() {
		Vector vector = new Vector();

		vector.addElement(new Option("\tALPHA (default = 0.001)", "ALPHA", 1,
				"-a <double>"));
		vector.addElement(new Option("\tBETA (default = 0.1)", "BETA", 1,
				"-b <double>"));
		vector.addElement(new Option("\tw (default = 50)", "w", 1,
				"-w <double>"));
		vector.addElement(new Option("\tMAXOUT (default = -1)", "MAXOUT", 1,
				"-m <double>"));
		vector.addElement(new Option("\tnumBins (default = 1)", "n", 1,
				"-n <int>"));
		vector.addElement(new Option("\tk (default = 5)", "k", 1,
				"-k <double>"));
		return vector.elements();
	}

	public void setOptions(String[] options) throws Exception {
		String optionString = Utils.getOption("a", options);
		if (optionString.length() != 0) {
			alpha = Double.parseDouble(optionString);
		}

		optionString = Utils.getOption("b", options);
		if (optionString.length() != 0) {
			beta = Double.parseDouble(optionString);
		}

		optionString = Utils.getOption("w", options);
		if (optionString.length() != 0) {
			w = Double.parseDouble(optionString);
		}
		optionString = Utils.getOption("m", options);
		if (optionString.length() != 0) {
			maxout = Integer.parseInt(optionString);
		}
		optionString = Utils.getOption("n", options);
		if (optionString.length() != 0) {
			numbin = Integer.parseInt(optionString);
		}
		optionString = Utils.getOption("k", options);
		if (optionString.length() != 0) {
			k = Integer.parseInt(optionString);
		}
	}

	/**
	 * Gets the current option settings for the OptionHandler.
	 * 
	 * @return String[] The list of current option settings as an array of
	 *         strings
	 */
	public String[] getOptions() {
		String[] options = new String[12];
		int current = 0;

		options[current++] = "-a";
		options[current++] = "" + alpha;
		options[current++] = "-b";
		options[current++] = "" + beta;
		options[current++] = "-w";
		options[current++] = "" + w;
		options[current++] = "-m";
		options[current++] = "" + maxout;
		options[current++] = "-n";
		options[current++] = "" + numbin;
		options[current++] = "-k";
		options[current++] = "" + k;

		return options;
	}

	public String globalInfo() {
		return "MineClus";
	}

	public void setALPHA(double a) {
		this.alpha = a;
	}

	public double getALPHA() {
		return alpha;
	}

	public void setBETA(double b) {
		this.beta = b;
	}

	public double getBETA() {
		return beta;
	}

	public void setw(double w) {
		this.w = w;
	}

	public double getw() {
		return w;
	}

	public void setMAXOUT(int m) {
		this.maxout = m;
	}

	public int getMAXOUT() {
		return maxout;
	}

	public void setnumBins(int n) {
		this.numbin = n;
	}

	public int getnumBins() {
		return numbin;
	}

	public void setk(int k) {
		this.k = k;
	}

	public int getk() {
		return k;
	}

	@Override
	public String getName() {
		return "MineClus";
	}

	@Override
	public String getParameterString() {
		return "ALPHA=" + alpha + ";BETA=" + beta + ";w=" + w + ";MAXOUT="
				+ maxout + ";numBins=" + numbin + ";k=" + k;
	}
	
	public static void main (String[] argv) {
		runSubspaceClusterer(new MineClus(), argv);
	}	


}
