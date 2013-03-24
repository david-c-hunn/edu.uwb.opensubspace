package weka.gui.visualize.subspace;

import i9.subspace.base.ArffStorage;
import i9.subspace.base.Cluster;
import i9.subspace.visa.densities.ExpDens;
import i9.subspace.visa.densities.ObjectInformation;
import i9.subspace.visa.densities.TotalClusterStatistics;
import i9.subspace.visa.ssClusterDistance.DistanceInfo;


import java.util.ArrayList;

import javax.swing.JPanel;


import weka.core.Instances;
import weka.core.SerializedObject;
import weka.gui.visualize.subspace.distance2d.CirclePanel;
import weka.gui.visualize.subspace.distance2d.ScalingObject;
import weka.gui.visualize.subspace.distance3d.Kugel;
import weka.gui.visualize.subspace.mds.ConfigurationMatrix;
import weka.gui.visualize.subspace.mds.DisimilarityMatrix;
import weka.gui.visualize.subspace.mds.EuclideanDistanceFunction;
import weka.gui.visualize.subspace.mds.MultidimensionalScaling;
import weka.gui.visualize.subspace.mds.WeightMatrix;
import weka.subspaceClusterer.SubspaceClusterer;

public class SubspaceVisualData extends JPanel {

	private static final long serialVersionUID = 1L;

	private TotalClusterStatistics data_boxplot;
	private ArrayList<ObjectInformation> data_indepth;

	private double alpha = 0.5;
	private double epsilon = 0.5;
	private double densityFactor = 0.5;

	private CirclePanel[] data_MDS2d = null;
	private Kugel [] data_MDS3d = null;

	private boolean hasVisual = false;
	
	private String m_historyName = "";
	
	public String getHistoryName() {
		return m_historyName;
	}

	public void setHistoryName(String historyName) {
		m_historyName = historyName;
	}

	public SubspaceVisualData() {}
	
	/**
	 * Set up a new SupspaceVisualData instances. This is empty at first, use calculateVisual
	 * 
	 * @param alpha
	 * @param epsilon "area of influence" needed for Epanechnikov-Density
	 * @param densityFactor Faktor F aus der Formel phi^S(o) >= F * alpha(|S|).
	 */
	public SubspaceVisualData(double alpha, double epsilon, double densityFactor) {
		this.epsilon = epsilon;
		this.densityFactor = densityFactor;
		this.alpha = alpha;
	}
	
	
  	/**
  	 * Calculates the visual data for the 
  	 * @param clusterer the clusterer
  	 * @param instances weka instances
  	 */
  	public void calculateVisual(ArrayList<Cluster> clustering, Instances instances){

  		if(clustering.size()>1){
			ExpDens ed = new ExpDens(clustering, new ArffStorage(instances), epsilon, densityFactor, alpha);
	
			//inDepth stuff
			data_indepth = ed.createPointsDensColor();
	
			//Boxplot stuff (not supported anymore for now)
			data_boxplot = ed.extractClusterMeansAndVariances();
	
			//MDS stuff
			DistanceInfo distanceInfo = ed.clusterDistances();
			data_MDS2d = calculateMDS2D(distanceInfo);
			data_MDS3d = calculateMDS3D(distanceInfo);

			hasVisual = true;
		}
  	}
  	
  	
  	
  	private CirclePanel[] calculateMDS2D(DistanceInfo distanceInfo) {
  		DisimilarityMatrix dis = new DisimilarityMatrix(distanceInfo.getDistmatrix());
  		ConfigurationMatrix mds2D = MultidimensionalScaling.smacofWithRandomRestart(5,dis,3,0.00001,500.0, new EuclideanDistanceFunction(),new WeightMatrix(dis.getRowDimension()));
  		
  		CirclePanel[] data = new CirclePanel[mds2D.getRowDimension()];
  		ScalingObject scale = new ScalingObject(); 

  		for (int i = 0; i < mds2D.getRowDimension(); i++) {

  			float x = (float)mds2D.get(i, 0);
  			float y = (float)mds2D.get(i, 1);
  			float radius = distanceInfo.getNbObjectsInCluster(i);
  			int color = distanceInfo.getClusterDimensionality(i);

  			data[i] = new CirclePanel(i,x,y,radius,color,scale);
		}
  		return data;
	}

  	private Kugel[] calculateMDS3D(DistanceInfo distanceInfo) {
  		DisimilarityMatrix dis = new DisimilarityMatrix(distanceInfo.getDistmatrix());
  		ConfigurationMatrix mds3D = MultidimensionalScaling.smacofWithRandomRestart(5,dis,3,0.00001,500.0, new EuclideanDistanceFunction(),new WeightMatrix(dis.getRowDimension()));
  		
  		Kugel[] data = new Kugel[mds3D.getRowDimension()];

  		for (int i = 0; i < mds3D.getRowDimension(); i++) {

  			float x = (float)mds3D.get(i, 0);
  			float y = (float)mds3D.get(i, 1);
  			float z = (float)mds3D.get(i, 2);
  			float radius = distanceInfo.getNbObjectsInCluster(i);
  			int color = distanceInfo.getClusterDimensionality(i);

  			data[i] = new Kugel(i,x,y,z,radius,color);

		}
  		return data;
	}
	
	public boolean hasVisual(){
		return hasVisual;
	}

	public ArrayList<ObjectInformation> getInDepth() {
		return data_indepth;
	}

	public CirclePanel[] getMDS2D() {
		SerializedObject so;
		try {
			so = new SerializedObject(data_MDS2d);
			return (CirclePanel[]) so.getObject();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}

	public Kugel[] getMDS3D() {
		return data_MDS3d;
	}

}
