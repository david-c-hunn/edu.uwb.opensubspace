package weka.subspaceClusterer;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.subspaceClusterer.SubspaceClusterer;

import i9.subspace.base.ArffStorage;
import i9.subspace.base.Cluster;
import i9.subspace.fires.presentation.Presenter;
import i9.subspace.fires.project.Properties;

public class Fires extends SubspaceClusterer implements OptionHandler{

	private static final long serialVersionUID = 7923724410794833810L;

	private Properties param;
	
	
	public Fires(){
		super();
		param = new Properties();
	}
	
	
	@Override
	public void buildSubspaceClusterer(Instances data) throws Exception {


		ArffStorage arffstorage = new ArffStorage(data);
		
		param.dimensions = data.numAttributes();

		// CLustering starten und Cluster ausgeben
		//PresentationManager p = new PresentationManager(param);
		Presenter presenter = new Presenter(param,arffstorage);
		List<Cluster> result = presenter.createClusters();

		//map Linked ClusterList to a simple List so we can cast it to Arraylist later on
		
		List<Cluster> new_clusters = new ArrayList<Cluster>();

		for (Cluster c : result) {
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
        
        vector.addElement(new Option("\tBASE_DBSCAN_EPSILON","X",1,"-BASE_DBSCAN_EPSILON <double>"));
        vector.addElement(new Option("\tBASE_DBSCAN_MINPTS","X",1,"-BASE_DBSCAN_MINPTS <int>"));
        vector.addElement(new Option("\tPRE_MINIMUMPERCENT","X",1,"-PRE_MINIMUMPERCENT <double>"));
        vector.addElement(new Option("\tGRAPH_K","X",1,"-GRAPH_K <int>"));
        vector.addElement(new Option("\tGRAPH_MU","X",1,"-GRAPH_MU <int>"));
        vector.addElement(new Option("\tGRAPH_MINCLU","X",1,"-GRAPH_MINCLU <int>"));
        vector.addElement(new Option("\tGRAPH_SPLIT","X",1,"-GRAPH_SPLIT <double>"));
        vector.addElement(new Option("\tPOST_DBSCAN_EPSILON","X",1,"-POST_DBSCAN_EPSILON <double>"));
        vector.addElement(new Option("\tPOST_DBSCAN_MINPTS","X",1,"-POST_DBSCAN_MINPTS <int>"));
        
        return vector.elements();
    }

   
    public void setOptions(String[] options) throws Exception {
    	String optionString;
        optionString = Utils.getOption("BASE_DBSCAN_EPSILON", options);
        if (optionString.length() != 0) {
    		param.BASE_DBSCAN_EPSILON = Double.parseDouble(optionString);
        }
        optionString = Utils.getOption("BASE_DBSCAN_MINPTS", options);
        if (optionString.length() != 0) {
            param.BASE_DBSCAN_MINPTS = Integer.parseInt(optionString);
        }
        optionString = Utils.getOption("PRE_MINIMUMPERCENT", options);
        if (optionString.length() != 0) {
        	param.PRE_MINIMUMPERCENT = Double.parseDouble(optionString);
        }
        optionString = Utils.getOption("GRAPH_K", options);
        if (optionString.length() != 0) {
        	param.GRAPH_K = Integer.parseInt(optionString);
        }
        optionString = Utils.getOption("GRAPH_MU", options);
        if (optionString.length() != 0) {
    		param.GRAPH_EPSILON = Integer.parseInt(optionString);
        }
        optionString = Utils.getOption("GRAPH_MINCLU", options);
        if (optionString.length() != 0) {
    		param.GRAPH_MINPTS = Integer.parseInt(optionString);
        }
        optionString = Utils.getOption("GRAPH_SPLIT", options);
        if (optionString.length() != 0) {
    		param.GRAPH_SPLIT = Double.parseDouble(optionString);
        }
        optionString = Utils.getOption("POST_DBSCAN_EPSILON", options);
        if (optionString.length() != 0) {
    		param.POST_DBSCAN_EPSILON = Double.parseDouble(optionString);
        }
        optionString = Utils.getOption("POST_DBSCAN_MINPTS", options);
        if (optionString.length() != 0) {
    		param.POST_DBSCAN_MINPTS = Integer.parseInt(optionString);
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

        result.add("-BASE_DBSCAN_EPSILON");
        result.add("" + param.BASE_DBSCAN_EPSILON);
        result.add("-BASE_DBSCAN_MINPTS");
        result.add("" + param.BASE_DBSCAN_MINPTS);
        result.add("-PRE_MINIMUMPERCENT");
        result.add("" + param.PRE_MINIMUMPERCENT);
        result.add("-GRAPH_K");
        result.add("" + param.GRAPH_K);
        result.add("-GRAPH_MU");
        result.add("" + param.GRAPH_EPSILON);
        result.add("-GRAPH_MINCLU");
        result.add("" + param.GRAPH_MINPTS);
        result.add("-GRAPH_SPLIT");
        result.add("" + param.GRAPH_SPLIT);
        result.add("-POST_DBSCAN_EPSILON");
        result.add("" + param.POST_DBSCAN_EPSILON);
        result.add("-POST_DBSCAN_MINPTS");
        result.add("" + param.POST_DBSCAN_MINPTS);

		return (String[]) result.toArray(new String[result.size()]);
    }

    public String globalInfo() {
        return "FIRES";
    }
    

    /** Set Methodes **/
    public void setBASE_DBSCAN_EPSILON(double v) {
    	param.BASE_DBSCAN_EPSILON = v;
    }
    public void setBASE_DBSCAN_MINPTS(int v) {
    	param.BASE_DBSCAN_MINPTS = v;
    }
    public void setPRE_MINIMUMPERCENT(double v) {
    	param.PRE_MINIMUMPERCENT = v;
    }
    public void setGRAPH_K(int v) {
    	param.GRAPH_K = v;
    }
    public void setGRAPH_MU(int v) {
    	param.GRAPH_EPSILON = v;
    }
    public void setGRAPH_MINCLU(int v) {
    	param.GRAPH_MINPTS = v;
    }
    public void setGRAPH_SPLIT(double v) {
    	param.GRAPH_SPLIT = v;
    }
    public void setPOST_DBSCAN_EPSILON(double v) {
    	param.POST_DBSCAN_EPSILON = v;
    }
    public void setPOST_DBSCAN_MINPTS(int v) {
    	param.POST_DBSCAN_MINPTS = v;
    }


    /** Get Methodes **/
    public double getBASE_DBSCAN_EPSILON() {
    	return param.BASE_DBSCAN_EPSILON;
    }
    public int getBASE_DBSCAN_MINPTS() {
    	return param.BASE_DBSCAN_MINPTS ;
    }
    public double getPRE_MINIMUMPERCENT() {
    	return param.PRE_MINIMUMPERCENT;
    }
    public int getGRAPH_K() {
    	return param.GRAPH_K ;
    }
    public int getGRAPH_MU() {
    	return param.GRAPH_EPSILON ;
    }
    public int getGRAPH_MINCLU() {
    	return param.GRAPH_MINPTS ;
    }
    public double getGRAPH_SPLIT() {
    	return param.GRAPH_SPLIT ;
    }
    public double getPOST_DBSCAN_EPSILON() {
    	return param.POST_DBSCAN_EPSILON ;
    }
    public int getPOST_DBSCAN_MINPTS() {
    	return param.POST_DBSCAN_MINPTS;
    }

    
	@Override
	public String getName() {
		return "FIRES";
	}


	@Override
	public String getParameterString() {
		// TODO hier noch anpassen?
		return param.toString();
	}
	
	public static void main (String[] argv) {
		runSubspaceClusterer(new Fires(), argv);
	}	


}
