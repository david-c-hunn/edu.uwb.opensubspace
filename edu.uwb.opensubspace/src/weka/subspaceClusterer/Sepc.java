package weka.subspaceClusterer;

import i9.subspace.base.ArffStorage;

import java.util.Enumeration;
import java.util.Vector;

import uwb.subspace.sepc.SEPC;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;


public class Sepc extends SubspaceClusterer implements OptionHandler {
	private static final long serialVersionUID = 5624336775621682596L;
	private double alpha       = 0.08;  // min cluster density
	private double beta        = 0.25;  // trade-off between num dims and num instances
	private double epsilon     = 0.05;  // chance of failing to find a cluster
	private double mu_0        = 1;     // minimum cluster quality
	private int    numClusters = 0;     // number of clusters to find, <= 0 leaves it up to SEPC
	private double w           = 100.0; // minimum cluster width
	private double maxOverlap  = 0.50;  // the maximum number of a cluster's points that may overlap another cluster in the same subspace
	private double dimOverlap  = 0.20; 
	private double minSubspaceSize = 0.5;
	private boolean disjointMode = true;
	
	@Override
	public void buildSubspaceClusterer(Instances data) throws Exception {
		ArffStorage arffstorage = new ArffStorage(data);
		int maxUnmatchedSubspaces = (int)Math.round((data.numAttributes() - 1) * dimOverlap);
		SEPC s = new SEPC(alpha, beta, epsilon, mu_0, numClusters, w, maxOverlap, 
				          maxUnmatchedSubspaces, minSubspaceSize, disjointMode, arffstorage);
		setSubspaceClustering(s.findClusters());
		toString();
	}

	/**
	 * Returns an enumeration of all the available options.
	 * 
	 * @return Enumeration An enumeration of all available options.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Enumeration listOptions() {
		Vector vector = new Vector();

		vector.addElement(new Option("\talpha (default = 0.08)", "alpha", 1,
				"-a <double>"));
		vector.addElement(new Option("\tbeta (default = 0.35)", "beta", 1,
				"-b <double>"));
		vector.addElement(new Option("\tepsilon (default = 0.05)", "epsilon", 1,
				"-e <double>"));
		vector.addElement(new Option("\tmu_0 (default = 1,000,000)", "mu_0", 1,
				"-m <double>"));
		vector.addElement(new Option("\tnumClusters (default = 0)", "numClusters", 1,
				"-n <int>"));
		vector.addElement(new Option("\twidth (default = 100.0)", "width", 1,
				"-w <double>"));
		vector.addElement(new Option("\toverlap (default = 0.50)", "overlap", 1,
				"-o <double>"));
		vector.addElement(new Option("\tdimOverlap (default = 0.20)", "dimOverlap", 1,
				"-d <double>"));
		vector.addElement(new Option("\tminSubspaceSize (default = 0.50)", "minSubspaceSize", 1,
				"-s <double>"));
		vector.addElement(new Option("\tdisjointMode (default = true)", "disjointMode", 1,
				"-x <boolean>"));
		
		return vector.elements();
	}

	public void setOptions(String[] options) throws Exception {
		String optionString = Utils.getOption("a", options);
		if (optionString.length() != 0) {
			setAlpha(Double.parseDouble(optionString));
		}
		
		optionString = Utils.getOption("b", options);
		if (optionString.length() != 0) {
			setBeta(Double.parseDouble(optionString));
		}
		
		optionString = Utils.getOption("e", options);
		if (optionString.length() != 0) {
			setEpsilon(Double.parseDouble(optionString));
		}
		
		optionString = Utils.getOption("m", options);
		if (optionString.length() != 0) {
			setMu_0(Double.parseDouble(optionString));
		}
		
		optionString = Utils.getOption("n", options);
		if (optionString.length() != 0) {
			setNumClusters(Integer.parseInt(optionString));
		}
		
		optionString = Utils.getOption("w", options);
		if (optionString.length() != 0) {
			setW(Double.parseDouble(optionString));
		}
		
		optionString = Utils.getOption("o", options);
		if (optionString.length() != 0) {
			setMaxOverlap(Double.parseDouble(optionString));
		}
		
		optionString = Utils.getOption("d", options);
		if (optionString.length() != 0) {
			setDimOverlap(Double.parseDouble(optionString));
		}
		
		optionString = Utils.getOption("s", options);
		if (optionString.length() != 0) {
			setMinSubspaceSize(Double.parseDouble(optionString));
		}
		
		optionString = Utils.getOption("x", options);
		if (optionString.length() != 0) {
			setDisjointMode(Boolean.parseBoolean(optionString));
		}
	}

	/**
	 * Gets the current option settings for the OptionHandler.
	 * 
	 * @return String[] The list of current option settings as an array of
	 *                  strings
	 */
	public String[] getOptions() {
		String[] options = new String[20]; // = 2 * the number of arguments
		int current = 0;

		options[current++] = "-a";
		options[current++] = "" + alpha;
		options[current++] = "-b";
		options[current++] = "" + beta;
		options[current++] = "-e";
		options[current++] = "" + epsilon;
		options[current++] = "-m";
		options[current++] = "" + mu_0;
		options[current++] = "-n";
		options[current++] = "" + numClusters;
		options[current++] = "-w";
		options[current++] = "" + w;
		options[current++] = "-o";
		options[current++] = "" + maxOverlap;
		options[current++] = "-d";
		options[current++] = "" + dimOverlap;
		options[current++] = "-s";
		options[current++] = "" + minSubspaceSize;
		options[current++] = "-x";
		options[current++] = "" + disjointMode;
		
		return options;
	}

	public String globalInfo() {
		return "Simple and Efficient Projective Clustering (SEPC): A Monte "
			 + "Carlo algorithm that performs trials that sample a small "
			 + "subset of the data points to determine the dimensions in which "
			 + "the points are sufficiently close to form a cluster and then "
			 + "searches the rest of the data for data points that are part of "
			 + "the cluster.";
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		if (alpha > 0.0 && alpha < 1.0)
			this.alpha = alpha;
	}

	public double getBeta() {
		return beta;
	}

	public void setBeta(double beta) {
		if (beta > 0.0 && beta < 1.0)
			this.beta = beta;
	}

	public double getEpsilon() {
		return epsilon;
	}

	public void setEpsilon(double epsilon) {
		if (epsilon > 0.0 && epsilon < 1.0)
			this.epsilon = epsilon;
	}

	public double getMu_0() {
		return mu_0;
	}

	public void setMu_0(double mu_0) {
		this.mu_0 = mu_0;
	}

	public int getNumClusters() {
		return numClusters;
	}

	public void setNumClusters(int numClusters) {
		this.numClusters = numClusters;
	}

	public double getW() {
		return w;
	}

	public void setW(double w) {
		if (w > 0.0)
			this.w = w;
	}

	public double getMaxOverlap() {
		return maxOverlap;
	}

	public void setMaxOverlap(double maxOverlap) {
		this.maxOverlap = maxOverlap;
	}

	public double getDimOverlap() {
		return dimOverlap;
	}

	public void setDimOverlap(double dimOverlap) {
		if (dimOverlap > 0.0)
			this.dimOverlap = dimOverlap;
	}

	public double getMinSubspaceSize() {
		return minSubspaceSize;
	}

	public void setMinSubspaceSize(double minSubspaceSize) {
		this.minSubspaceSize = minSubspaceSize;
	}

	public boolean isDisjointMode() {
		return disjointMode;
	}

	public void setDisjointMode(boolean disjointMode) {
		this.disjointMode = disjointMode;
	}

	@Override
	public String getName() {
		return "SEPC";
	}

	@Override
	public String getParameterString() {
		return "alpha=" + alpha + 
		       "; beta=" + beta + 
		       "; epsilon=" + epsilon + 
		       "; mu_0=" + mu_0 + 
		       "; numClusters=" + numClusters +
		       "; w=" + w +
		       "; maxOverlap=" + maxOverlap + 
		       "; dimOverlap=" + dimOverlap +
		       "; minSubspaceSize=" + minSubspaceSize +
		       "; disjointMode=" + disjointMode;
	}

	public static void main (String[] argv) {
		runSubspaceClusterer(new Sepc(), argv);
	}

// TODO: Figure out how to use this feature	
//	@Override
//	public TechnicalInformation getTechnicalInformation() {
//		TechnicalInformation info = new TechnicalInformation(Type.ARTICLE);
//		
//		info.
//		
//		// TODO Auto-generated method stub
//		return null;
//	}
}
