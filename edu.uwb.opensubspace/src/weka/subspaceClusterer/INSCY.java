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
import i9.subspace.base.GlobalCounters;
import i9.subspace.edsc.FPFactory;
import i9.subspace.edsc.Grid_1D;
import i9.subspace.experiments.Parameters;


public class INSCY extends SubspaceClusterer implements OptionHandler{

	private static final long serialVersionUID = 7923724410794833810L;

	private Parameters param;
	
	
	public INSCY(){
		super();
		param = new Parameters();
		param.overlapSize = 2; //wird abhngig von epsilon berechnet
//		//gesetzte Werte
		param.maximalClusterOnly = true; 
		param.pruningVariants = 2; // 1=NO,2=BORDER,3=CLUSTER, sonst = ELSE
		param.reclusteringVol = 0;
		param.minDimension = 2;

		//pendigits
		param.gridSize = 10;
		param.minSupport = 5;  //minCoreCounts
		param.epsilon = 4;
		param.tau = 8;		//minPoints
		param.density = 500;   //minDensity
		param.maximalClusterRate = 0;
		param.usingKernel = 1; //1; // 1=Rec,2=Epa,3=Squ
		
	}
	
	
	@Override
	public void buildSubspaceClusterer(Instances data) throws Exception {


		ArffStorage arffstorage = new ArffStorage(data);
		List<Cluster> clusters = null;

	// --------- SET GRID: ----------
		Grid_1D grid;
		param.dimensions = data.numAttributes();
		param.overlapSize = (int)Math.ceil(param.epsilon/2.0);
		grid = new Grid_1D(param.gridSize, param.overlapSize, param.dimensions);
		GlobalCounters counters = new GlobalCounters(param.dimensions);

		for (int i = 0; i < param.dimensions; i++){
			grid.setGrid(arffstorage, i);
		}
	// --------- SET GRID -----------

	// --------- INITFPTree: ----------
		FPFactory fpfactory = new FPFactory(arffstorage, grid, param, counters);
		clusters = fpfactory.runClustering();
		
		//map Linked ClusterList to a simple List so we can cast it to Arraylist later on
		
		List<Cluster> new_clusters = new ArrayList<Cluster>();

		for (Cluster c : clusters) {
			new_clusters.add(c);
		}
		
		setSubspaceClustering(new_clusters);
	}



    /**
     * Returns an enumeration of all the available options..
     *
     * @return Enumeration An enumeration of all available options.
     */
    public Enumeration listOptions() {
        Vector vector = new Vector();

        vector.addElement(new Option("\tgS","X",1,"-gS <int>"));
        vector.addElement(new Option("\tmS ","X",1,"-mS <int>"));
        vector.addElement(new Option("\tde ","X",1,"-de <double>"));
        vector.addElement(new Option("\tm ","X",1,"-m <double>"));
        vector.addElement(new Option("\te ","X",1,"-e <double>"));
        vector.addElement(new Option("\tR ","X",1,"-R <double>"));
        vector.addElement(new Option("\tK ","X",1,"-K <int>"));
        
        return vector.elements();
    }

   
    public void setOptions(String[] options) throws Exception {
    	String optionString;
    	optionString = Utils.getOption("gS", options);
        if (optionString.length() != 0) {
    		param.gridSize = Integer.parseInt(optionString);
        }
        optionString = Utils.getOption("mS", options);
        if (optionString.length() != 0) {
    		param.minSupport = Integer.parseInt(optionString);
        }
        optionString = Utils.getOption("de", options);
        if (optionString.length() != 0) {
            param.density = Integer.parseInt(optionString);
        }
        optionString = Utils.getOption("m", options);
        if (optionString.length() != 0) {
        	param.tau = Double.parseDouble(optionString);
        }
        optionString = Utils.getOption("e", options);
        if (optionString.length() != 0) {
        	param.epsilon = Double.parseDouble(optionString);
        }
        optionString = Utils.getOption("R", options);
        if (optionString.length() != 0) {
    		param.maximalClusterRate = Double.parseDouble(optionString);
        }
        optionString = Utils.getOption("K", options);
        if (optionString.length() != 0) {
    		param.usingKernel = Integer.parseInt(optionString);
        }
    }

    /**
     * Gets the current option settings for the OptionHandler.
     *
     * @return String[] The list of current option settings as an array of strings
     */
    public String[] getOptions() {
		
    	Vector result;

		result = new Vector();

		result.add("-gS");
        result.add("" + param.gridSize);
        result.add("-mS");
        result.add("" + param.minSupport);
        result.add("-de");
        result.add("" + param.density);
        result.add("-m");
        result.add("" + param.tau);
        result.add("-e");
        result.add("" + param.epsilon);
        result.add("-R");
        result.add("" + param.maximalClusterRate);
        result.add("-K");
        result.add("" + param.usingKernel);

		return (String[]) result.toArray(new String[result.size()]);
    }

    public String globalInfo() {
        return "INSCY";
    }


    /** Set Methodes **/
    public void setGridSize(int v) {
    	param.gridSize = v;
    }
    public void setminSize(int v) {
    	param.minSupport = v;
    }
    public void setUsingKernel(int v) {
    	param.usingKernel = v;
    }
    public void setDensity(double v) {
    	param.density = v;
    }
    public void setEpsilon(double v) {
    	param.epsilon = v;
    	param.overlapSize = (int) Math.ceil(v/2.0);
    }
    public void setminPoints(double v) {
    	param.tau = v;
    }
    public void setMaximalClusterRate(double v) {
    	param.maximalClusterRate = v;
    }

    /** Get Methodes **/
    public int getGridSize() {
    	return param.gridSize;
    }
    public int getminSize() {
    	return param.minSupport;
    }
    public int getUsingKernel() {
    	return param.usingKernel;
    }
    public double getDensity() {
    	return param.density;
    }
    public double getEpsilon() {
    	return param.epsilon;
    }
    public double getminPoints() {
    	return param.tau;
    }
    public double getMaximalClusterRate() {
    	return param.maximalClusterRate;
    }


	@Override
	public String getName() {
		return "INSCY";
	}

	@Override
	public String getParameterString() {
		return param.toString();
	}
	
	public static void main (String[] argv) {
		runSubspaceClusterer(new INSCY(), argv);
	}	


}
