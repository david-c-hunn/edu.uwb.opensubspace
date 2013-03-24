package weka.subspaceClusterer;

import i9.subspace.base.ArffStorage;
import i9.subspace.base.Cluster;
import i9.subspace.ken.FastDOC;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;

public class Doc extends SubspaceClusterer implements OptionHandler {

	private static final long serialVersionUID = 7923724410794833810L;

	private double alpha = 0.001;
	private double beta = 0.1;
	private double w = 50; // wird absolut gemessen
	private int maxiter = 1024;	
	private int d0 = -1; // so wird DOC genutzt
	private int k = 2; // ungefähre Anzahl Cluster auch bei DOC

	@Override
	public void buildSubspaceClusterer(Instances data) throws Exception {

		ArffStorage arffstorage = new ArffStorage(data);
		List<Cluster> clusters = null;

		int dimensions = data.numAttributes();
		FastDOC s = new FastDOC(alpha, beta, w, d0, maxiter, k, arffstorage);
		setSubspaceClustering(s.cluster_result);
		toString();

	}

	/**
	 * Returns an enumeration of all the available options..
	 * 
	 * @return Enumeration An enumeration of all available options.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Enumeration listOptions() {
		Vector vector = new Vector();

		vector.addElement(new Option("\tALPHA (default = 0.08)", "ALPHA", 1,
				"-a <double>"));
		vector.addElement(new Option("\tBETA (default = 0.25)", "BETA", 1,
				"-b <int>"));
		vector.addElement(new Option("\tw (default = 0.2)", "w", 1,
				"-w <double>"));
		vector.addElement(new Option("\tMAXITER (default = 1024)", "MAXITER", 1,
		"-m <double>"));
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
			maxiter = Integer.parseInt(optionString);
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
		String[] options = new String[10];
		int current = 0;

		options[current++] = "-a";
		options[current++] = "" + alpha;
		options[current++] = "-b";
		options[current++] = "" + beta;
		options[current++] = "-w";
		options[current++] = "" + w;
		options[current++] = "-m";
		options[current++] = "" + maxiter;
		options[current++] = "-k";
		options[current++] = "" + k;

		return options;
	}

	public String globalInfo() {
		return "DOC";
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
	
	public void setMAXITER(int m) {
		this.maxiter = m;
	}

	public int getMAXITER() {
		return maxiter;
	}
	public void setk(int k) {
		this.k = k;
	}

	public int getk() {
		return k;
	}

	@Override
	public String getName() {
		return "DOC";
	}

	@Override
	public String getParameterString() {
		return "ALPHA=" + alpha + ";BETA=" + beta + ";w=" + w + ";MAXITER=" + maxiter + ";k=" + k;
	}
	
	public static void main (String[] argv) {
		runSubspaceClusterer(new Doc(), argv);
	}	


}
