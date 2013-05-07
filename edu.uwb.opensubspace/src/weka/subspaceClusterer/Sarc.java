package weka.subspaceClusterer;

import i9.subspace.base.ArffStorage;
import i9.subspace.sarc.SARC;

import java.util.Enumeration;
import java.util.Vector;

import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;


public class Sarc extends SubspaceClusterer implements OptionHandler {
	private static final long serialVersionUID = 5624336775621682596L;
	private double m_alpha       = 0.01;  // min cluster density
	private double m_beta        = 0.25;  // trade-off between num dims and num instances
	private double m_epsilon     = 0.01;  // chance of failing to find a cluster
	private double m_minQual     = 1.00;
	private int    m_numClusters = 0;     // number of clusters to find, <= 0 leaves it up to SEPC
	
	
	@Override
	public void buildSubspaceClusterer(Instances data) throws Exception {
		ArffStorage arffstorage = new ArffStorage(data);
		SARC s = new SARC(m_alpha, m_beta, m_epsilon, m_minQual, m_numClusters, arffstorage);
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

		vector.addElement(new Option("\talpha (default = 0.01)", "alpha", 1,
				"-alpha <double>"));
		vector.addElement(new Option("\tbeta (default = 0.25)", "beta", 1,
				"-beta <double>"));
		vector.addElement(new Option("\tepsilon (default = 0.01)", "epsilon", 1,
				"-epsilon <double>"));
		vector.addElement(new Option("\tmu_0 (default = 1.0)", "minQual", 1,
				"-minQual <double>"));
		vector.addElement(new Option("\tnumClusters (default = 0)", "numClusters", 1,
				"-numClusters <int>"));
		
		return vector.elements();
	}

	public void setOptions(String[] options) throws Exception {
		String optionString = Utils.getOption("alpha", options);
		
		if (optionString.length() != 0) {
			setAlpha(Double.parseDouble(optionString));
		}
		
		optionString = Utils.getOption("beta", options);
		if (optionString.length() != 0) {
			setBeta(Double.parseDouble(optionString));
		}
		
		optionString = Utils.getOption("epsilon", options);
		if (optionString.length() != 0) {
			setEpsilon(Double.parseDouble(optionString));
		}
		
		optionString = Utils.getOption("minQual", options);
    if (optionString.length() != 0) {
      setMinQual(Double.parseDouble(optionString));
    }	
		
		optionString = Utils.getOption("numClusters", options);
		if (optionString.length() != 0) {
			setNumClusters(Integer.parseInt(optionString));
		}
		
//		optionString = Utils.getOption("distance", options);
//    if (optionString.length() != 0) {
//      
//    }
	}

	/**
	 * Gets the current option settings for the OptionHandler.
	 * 
	 * @return String[] The list of current option settings as an array of
	 *                  strings
	 */
	public String[] getOptions() {
		String[] options = new String[8]; // = 2 * the number of arguments
		int current = 0;

		options[current++] = "-alpha";
		options[current++] = "" + m_alpha;
		options[current++] = "-beta";
		options[current++] = "" + m_beta;
		options[current++] = "-epsilon";
		options[current++] = "" + m_epsilon;
		options[current++] = "-minQual";
    options[current++] = "" + m_minQual;
		options[current++] = "-numClusters";
		options[current++] = "" + m_numClusters;
		
		
		return options;
	}

	public String globalInfo() {
		return "Soft Assginment Randomized Clustering";
	}

	public double getAlpha() {
		return m_alpha;
	}

	public void setAlpha(double alpha) {
		if (alpha > 0.0 && alpha < 1.0)
			this.m_alpha = alpha;
	}

	public double getBeta() {
		return m_beta;
	}

	public void setBeta(double beta) {
		if (beta > 0.0 && beta < 1.0)
			this.m_beta = beta;
	}

	public double getEpsilon() {
		return m_epsilon;
	}

	public void setEpsilon(double epsilon) {
		if (epsilon > 0.0 && epsilon < 1.0)
			this.m_epsilon = epsilon;
	}

	public double getMinQual() {
    return m_minQual;
  }

  public void setMinQual(double minQual) {
    this.m_minQual = minQual;
  }

  public int getNumClusters() {
		return m_numClusters;
	}

	public void setNumClusters(int numClusters) {
		this.m_numClusters = numClusters;
	}

	@Override
	public String getName() {
		return "SARC";
	}

	@Override
	public String getParameterString() {
		return "alpha=" + m_alpha + "; beta=" + m_beta + "; epsilon=" + m_epsilon  + 
		       "; minQual=" + m_minQual + "; numClusters=" + m_numClusters;
	}

	public static void main (String[] argv) {
		runSubspaceClusterer(new Sarc(), argv);
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
