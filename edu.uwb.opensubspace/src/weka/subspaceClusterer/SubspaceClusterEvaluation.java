/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    ClusterEvaluation.java
 *    Copyright (C) 1999 Mark Hall
 *
 */

package  weka.subspaceClusterer;

import i9.subspace.base.Cluster;

import java.io.File;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;

import weka.clusterquality.ClusterQualityMeasure;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * Class for evaluating clustering models.<p/>
 *
 * -t name of the training file <br/>
 * Specify the training file. <p/>
 *
 * -c class <br/>
 * Set the class attribute. If set, then class based evaluation of clustering
 * is performed. <p/>
 * 
 * -M cluster quality measures <br/>
 * subspace cluster quality measures in package weka.clusterquality. <br/>
 * separate measures with ';' e.g. -M F1Measure;Entropy;CE<br/>
 * 
 * 
 * @author   Mark Hall (mhall@cs.waikato.ac.nz)
 * @version  $Revision: 1.35 $
 * @see	     weka.core.Drawable
 */
public class SubspaceClusterEvaluation 
implements Serializable {

	/** for serialization */
	static final long serialVersionUID = -830188327319128005L;

	/** the clusterer */
	private SubspaceClusterer m_Clusterer;

	/** holds a string describing the results of clustering the training data */
	private StringBuffer m_clusteringResults;

	/** holds the number of clusters found by the clusterer */
	private int m_numClusters;

	/** holds the assigments of instances to clusters for a particular testing
      dataset */
	private double[] m_clusterAssignments;

	/** holds the average log likelihood for a particular testing dataset
     if the clusterer is a DensityBasedClusterer */
	private double m_logL;

	/** will hold the mapping of classes to clusters (for class based 
      evaluation) */
	private int[] m_classToCluster = null;

	/**
	 * set the clusterer
	 * @param clusterer the clusterer to use
	 */
	public void setClusterer(SubspaceClusterer clusterer) {
		m_Clusterer = clusterer;
	}

	/**
	 * return the results of clustering.
	 * @return a string detailing the results of clustering a data set
	 */
	public String clusterResultsToString() {
		return m_clusteringResults.toString();
	}

	/**
	 * Return the number of clusters found for the most recent call to
	 * evaluateClusterer
	 * @return the number of clusters found
	 */
	public int getNumClusters() {
		return m_numClusters;
	}

	/**
	 * Return an array of cluster assignments corresponding to the most
	 * recent set of instances clustered.
	 * @return an array of cluster assignments
	 */
	public double[] getClusterAssignments() {
		return m_clusterAssignments;
	}



	/**
	 * Constructor. Sets defaults for each member variable. Default Clusterer
	 * is EM.
	 */
	public SubspaceClusterEvaluation () {
		setClusterer(new P3c());
		m_clusteringResults = new StringBuffer();
		m_clusterAssignments = null;
	}

	/**
	 * Evaluate the clusterer on a set of instances. Calculates clustering
	 * statistics and stores cluster assigments for the instances in
	 * m_clusterAssignments
	 * 
	 * @param test the set of instances to cluster
	 * @throws Exception if something goes wrong
	 */
	//  public void evaluateClusterer(Instances test, ArrayList<ClusterQualityMeasure> measures) throws Exception {
	//    evaluateClusterer(test, measures, "");
	//  }

	/**
	 * Do we need this functionality anymore? Should be done by evaluateClustersQuality?
	 * @throws Exception if something goes wrong
	 */

	/*
  public void evaluateClusterer(Instances test, ArrayList<ClusterQualityMeasure> measures, String testFileName)
			throws Exception {

		m_numClusters = m_Clusterer.numberOfClusters();

		List<Integer> [] clusterAssignments;

		int unclusteredInstances = 0;

		m_Clusterer.calculateClusterAssignments(test.numInstances());
		clusterAssignments=m_Clusterer.getClusterAssignments();
		for (int i = 0; i < test.numInstances(); i++) {
			if(clusterAssignments[i] == null){
				unclusteredInstances++;
			}
		}

		m_clusteringResults.append("Result: [relevant dimensions] [amount] {clustered objects} \n");
		m_clusteringResults.append(m_Clusterer.toString());
		if (unclusteredInstances > 0)
			m_clusteringResults.append("\nUnclustered instances : "
					+ unclusteredInstances+"\n");
	}

  /**
	 * Called from SubspaceClusterPanel to run evaluation of measures on the clusterer  
	 *     
	 * @param clusterer
	 * @param inst
	 * @param measures
	 * @param trueClusters
	 * @param trueClusterFile
	 * @return String with the clusterquality results
	 * @throws Exception
	 */

	public static StringBuffer evaluateClustersQuality(SubspaceClusterer clusterer, Instances inst, ArrayList<ClusterQualityMeasure> measures, ArrayList<Cluster> trueClusters, File trueClusterFile)
			throws Exception{
		DataSource source = null;
		Instances instances = null;
		ArrayList<Cluster> clusterList;		

		source = new DataSource(inst);
		instances = source.getDataSet();

		StringBuffer sb = new StringBuffer(); 		

		if(measures!=null && measures.size() != 0 && clusterer!=null && clusterer.getSubspaceClustering()!=null){
			clusterList = (ArrayList<Cluster>)clusterer.getSubspaceClustering();

			//calculate Quality
			for(ClusterQualityMeasure m : measures) {
				m.calculateQuality(clusterList, instances, trueClusters);
			}

			//print trueclusterfile
			String filename = "none";
			if(trueClusterFile!=null)
				filename = trueClusterFile.getAbsolutePath(); 
			sb.append("\nTrue Cluster File: "+filename+"\n");

			sb.append("\nEvaluation measurements:\n");
			// output (measure x clusterID)
			sb.append("\t\t all \t\t");
			//check if we should print cluster ID's
			boolean printClusterID = false;
			for(ClusterQualityMeasure m : measures) {
				try {
					Class[] para = {int.class};
					m.getClass().getDeclaredMethod("getValuePerCluster",para);
					printClusterID = true;
					break;
				} catch (NoSuchMethodException e) {}
			}
			//print Cluster Id's
			if(printClusterID){
				for (int i = 0; i < clusterList.size(); i++) {
					sb.append("SC_"+i+"\t\t");
				}
			}
			sb.append("\n");

			//print table with values
			DecimalFormat format = new DecimalFormat();
			format.setMaximumFractionDigits(2);

			StringBuffer customOutputBlocks = new StringBuffer(); 		

			//print values 
			for(ClusterQualityMeasure m : measures) {
				//overall value
				String row = "";
				if(m.getOverallValue()!=null){
					if(m.getOverallValue().equals(Double.NaN)) {
						row+= "undef\t\t";
					}
					else {
						row+= format.format(m.getOverallValue())+"\t\t";
					}
				}

				try {
					//test if method is implemented
					Class[] para = {int.class};
					m.getClass().getDeclaredMethod("getValuePerCluster",para);
					//print cluster values
					for (int i = 0; i < clusterList.size(); i++) {
						if(m.getValuePerCluster(i)!=null){
							if(m.getValuePerCluster(i).equals(Double.NaN)){
								row+= "NaN\t\t";
							} else {
								row+= format.format(m.getValuePerCluster(i))+"\t\t";
							}
						}
					}
				} catch (NoSuchMethodException e) {}

				if(row!=""){
					sb.append(m.getName()+" \t");
					sb.append(row);
					sb.append("\n");
				}

				if(m.getCustomOutput()!=null){
					customOutputBlocks.append(" \n\n"+m.getName()+": \n");
					customOutputBlocks.append(m.getCustomOutput());
				}

			}
			sb.append("\n");
			sb.append(customOutputBlocks);
			sb.append("\n");
		}

		return sb;
	}

	/**
	 * Evaluates a clusterer with the options given in an array of
	 * strings. It takes the string indicated by "-t" as training file, the
	 * string indicated by "-T" as test file.
	 * If the test file is missing, a stratified ten-fold
	 * cross-validation is performed (distribution clusterers only).
	 * Using "-x" you can change the number of
	 * folds to be used, and using "-s" the random seed.
	 * If the "-p" option is present it outputs the classification for
	 * each test instance. If you provide the name of an object file using
	 * "-l", a clusterer will be loaded from the given file. If you provide the
	 * name of an object file using "-d", the clusterer built from the
	 * training data will be saved to the given file.
	 *
	 * @param clusterer machine learning clusterer
	 * @param options the array of string containing the options
	 * @throws Exception if model could not be evaluated successfully
	 * @return a string describing the results 
	 */
	public static String evaluateClusterer(SubspaceClusterer clusterer, String[] options)
			throws Exception {

		Instances train = null;
		String trainFileName;
		String measureOptionString = null;
		String[] savedOptions = null;
		StringBuffer text = new StringBuffer();
		int theClass = -1; // class based evaluation of clustering
		DataSource source = null;
		String trueFileName = null;
		
		try {
			if (Utils.getFlag('h', options)) {
				throw new Exception("Help requested.");
			}

			// Get basic options (options the same for all clusterers
			trainFileName = Utils.getOption('t', options);
			if(trainFileName.length()==0) throw new Exception("No input file, use -t");
			measureOptionString = Utils.getOption('M', options);
			trueFileName = Utils.getOption('T', options);

		} catch (Exception e) {
			throw new Exception('\n' + e.getMessage()
					+ makeOptionString(clusterer));
		}

		try {
			if (trainFileName.length() != 0) {
				source = new DataSource(trainFileName);
				train = source.getStructure();

				String classString = Utils.getOption('c', options);
				if (classString.length() != 0) {
					if (classString.compareTo("last") == 0)
						theClass = train.numAttributes();
					else if (classString.compareTo("first") == 0)
						theClass = 1;
					else
						theClass = Integer.parseInt(classString);

				} else {
					// if the dataset defines a class attribute, use it
					if (train.classIndex() != -1) {
						theClass = train.classIndex() + 1;
						System.err
						.println("Note: using class attribute from dataset, i.e., attribute #"
								+ theClass);
					}
				}

				if (theClass != -1) {
					if (theClass < 1 || theClass > train.numAttributes())
						throw new Exception("Class is out of range!");

					if (!train.attribute(theClass - 1).isNominal())
						throw new Exception("Class must be nominal!");

					train.setClassIndex(theClass - 1);
				}
			}

		} catch (Exception e) {
			throw new Exception("ClusterEvaluation: " + e.getMessage() + '.');
		}

		// Save options
		if (options != null) {
			savedOptions = new String[options.length];
			System.arraycopy(options, 0, savedOptions, 0, options.length);
		}

		// Set options for clusterer
		if (clusterer instanceof OptionHandler)
			((OptionHandler) clusterer).setOptions(options);

		Utils.checkForRemainingOptions(options);

		Instances inst = source.getDataSet();

		text.append("Scheme: "+clusterer.getName()+" ");
		text.append(clusterer.getParameterString());
		text.append("\n");
		text.append("Relation: "+inst.relationName()+"\n");

		// Build the clusterer
		if (theClass == -1) {
			if(measureOptionString != ""){
				System.out.println("You need to set the class attribute (-c) for evaluation measures to be calculated! ");
			}
			clusterer.buildSubspaceClusterer(inst);
			text.append(clusterer.toString());
		} else {
			//remove class
			Remove removeClass = new Remove();
			removeClass.setAttributeIndices("" + theClass);
			removeClass.setInvertSelection(false);
			removeClass.setInputFormat(train);
			Instances clusterTrain = Filter.useFilter(inst, removeClass);


			//cluster
			clusterer.buildSubspaceClusterer(clusterTrain);
			text.append(clusterer.toString());

			//evaluation
			SubspaceClusterEvaluation ce = new SubspaceClusterEvaluation();
			ce.setClusterer(clusterer);
			Instances evalInst = source.getDataSet();
			evalInst.setClassIndex(theClass-1);

			//TODO set measures somehow
			ArrayList<ClusterQualityMeasure> measures = null;

			if(measureOptionString != ""){
				measures = getMeasuresByOptions(measureOptionString);
			}

			ArrayList<Cluster> trueClusters = null;
			File trueClusterFile = null;

			// The following code was added by Dave on 12/12/12 
			// This allows you to run the cluster evaluation from the command
			// line and pass in a true cluster file
			if (trueFileName.length() > 0)
				trueClusterFile = new File(trueFileName);
			ArrayList<Cluster> tmpTrueClusters = 
					SubspaceClusterTools.getClusterList(trueClusterFile, -1);
			if (tmpTrueClusters != null) {
				trueClusters = tmpTrueClusters;
			}
			// End new code
			
			StringBuffer evalOutput = evaluateClustersQuality(clusterer, evalInst, measures, trueClusters, trueClusterFile);

			text.append("\n\n=== Eval stats for training data ===\n\n"
			+ ce.clusterResultsToString());
			text.append(evalOutput);

			//clusterer.save(evalInst,ce);

		}

		/*
		 * Output cluster predictions only (for the test data if specified,
		 * otherwise for the training data
		 */
		//TODO
		//		if (printClusterAssignments) {
		//			return printClusterings(clusterer, trainFileName, testFileName,
		//					attributesToOutput);
		//		}
		//
		//		text.append(clusterer.toString());
		//		text.append("\n\n=== Clustering stats for training data ===\n\n"
		//				+ printClusterStats(clusterer, trainFileName));
		//
		//		if (testFileName.length() != 0)
		//			text.append("\n\n=== Clustering stats for testing data ===\n\n"
		//					+ printClusterStats(clusterer, testFileName));

		//		if ((clusterer instanceof DensityBasedClusterer) && (doXval == true)
		//				&& (testFileName.length() == 0)
		//				&& (objectInputFileName.length() == 0)) {
		//			// cross validate the log likelihood on the training data
		//			random = new Random(seed);
		//			random.setSeed(seed);
		//			train = source.getDataSet();
		//			train.randomize(random);
		//			text.append(crossValidateModel(clusterer.getClass().getName(),
		//					train, folds, savedOptions, random));
		//		}

		// Save the clusterer if an object output file is provided
		//		if (objectOutputFileName.length() != 0) {
		//			objectOutputStream.writeObject(clusterer);
		//			objectOutputStream.flush();
		//			objectOutputStream.close();
		//		}

		// If classifier is drawable output string describing graph
		//		if ((clusterer instanceof Drawable) && (graphFileName.length() != 0)) {
		//			BufferedWriter writer = new BufferedWriter(new FileWriter(
		//					graphFileName));
		//			writer.write(((Drawable) clusterer).graph());
		//			writer.newLine();
		//			writer.flush();
		//			writer.close();
		//		}

		return text.toString();
	}


	private static int getDimsFromOptions(String[] options) {
		try {
			return Integer.parseInt(Utils.getOption('D', options));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	public static ArrayList<ClusterQualityMeasure> getMeasuresByOptions(String class_string){ 
		ArrayList<ClusterQualityMeasure> measures = new ArrayList<ClusterQualityMeasure>();

		Class classtype = ClusterQualityMeasure.class;
		String[] classes_string = class_string.split(":");
		//String class_string = GenericObjectEditor.EDITOR_PROPERTIES.getProperty(classtype.getName());

		for (int i = 0; i < classes_string.length; i++) {
			try {
				Class c = Class.forName("weka.clusterquality."+classes_string[i]);
				measures.add((ClusterQualityMeasure)c.newInstance());
			} catch (InstantiationException e1) {
				System.out.println("Not a valid subspace measure class: weka.clusterquality."+classes_string[i]);
				//e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				System.out.println("Not a valid subspace measure class: weka.clusterquality."+classes_string[i]);
				//e1.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.out.println("Not a valid subspace measure class: weka.clusterquality."+classes_string[i]);
				//e.printStackTrace();
			}
		}

		return measures;
	}


	/**
	 * Perform a cross-validation for DensityBasedClusterer on a set of
	 * instances.
	 * 
	 * @param clusterer
	 *            the clusterer to use
	 * @param data
	 *            the training data
	 * @param numFolds
	 *            number of folds of cross validation to perform
	 * @param random
	 *            random number seed for cross-validation
	 * @return the cross-validated log-likelihood
	 * @throws Exception
	 *             if an error occurs
	 */
	//  public static double crossValidateModel(DensityBasedClusterer clusterer,
	//					  Instances data,
	//					  int numFolds,
	//					  Random random) throws Exception {
	//    Instances train, test;
	//    double foldAv = 0;;
	//    data = new Instances(data);
	//    data.randomize(random);
	//    //    double sumOW = 0;
	//    for (int i = 0; i < numFolds; i++) {
	//      // Build and test clusterer
	//      train = data.trainCV(numFolds, i, random);
	//
	//      clusterer.buildClusterer(train);
	//
	//      test = data.testCV(numFolds, i);
	//      
	//      for (int j = 0; j < test.numInstances(); j++) {
	//	try {
	//	  foldAv += ((DensityBasedClusterer)clusterer).
	//	    logDensityForInstance(test.instance(j));
	//	  //	  sumOW += test.instance(j).weight();
	//	  //	double temp = Utils.sum(tempDist);
	//	} catch (Exception ex) {
	//	  // unclustered instances
	//	}
	//      }
	//    }
	//   
	//    //    return foldAv / sumOW;
	//    return foldAv / data.numInstances();
	//  }

	/**
	 * Performs a cross-validation 
	 * for a DensityBasedClusterer clusterer on a set of instances.
	 *
	 * @param clustererString a string naming the class of the clusterer
	 * @param data the data on which the cross-validation is to be 
	 * performed 
	 * @param numFolds the number of folds for the cross-validation
	 * @param options the options to the clusterer
	 * @param random a random number generator
	 * @return a string containing the cross validated log likelihood
	 * @throws Exception if a clusterer could not be generated 
	 */
	//  public static String crossValidateModel (String clustererString, 
	//					   Instances data, 
	//					   int numFolds, 
	//					   String[] options,
	//					   Random random)
	//    throws Exception {
	//    Clusterer clusterer = null;
	//    String[] savedOptions = null;
	//    double CvAv = 0.0;
	//    StringBuffer CvString = new StringBuffer();
	//
	//    if (options != null) {
	//      savedOptions = new String[options.length];
	//    }
	//
	//    data = new Instances(data);
	//
	//    // create clusterer
	//    try {
	//      clusterer = (Clusterer)Class.forName(clustererString).newInstance();
	//    }
	//    catch (Exception e) {
	//      throw  new Exception("Can't find class with name " 
	//			   + clustererString + '.');
	//    }
	//
	//    if (!(clusterer instanceof DensityBasedClusterer)) {
	//      throw  new Exception(clustererString 
	//			   + " must be a distrinbution " 
	//			   + "clusterer.");
	//    }
	//
	//    // Save options
	//    if (options != null) {
	//      System.arraycopy(options, 0, savedOptions, 0, options.length);
	//    }
	//
	//    // Parse options
	//    if (clusterer instanceof OptionHandler) {
	//      try {
	//	((OptionHandler)clusterer).setOptions(savedOptions);
	//	Utils.checkForRemainingOptions(savedOptions);
	//      }
	//      catch (Exception e) {
	//	throw  new Exception("Can't parse given options in " 
	//			     + "cross-validation!");
	//      }
	//    }
	//    CvAv = crossValidateModel((DensityBasedClusterer)clusterer, data, numFolds, random);
	//
	//    CvString.append("\n" + numFolds 
	//		    + " fold CV Log Likelihood: " 
	//		    + Utils.doubleToString(CvAv, 6, 4) 
	//		    + "\n");
	//    return  CvString.toString();
	//  }


	// ===============
	// Private methods
	// ===============
	/**
	 * Print the cluster statistics for either the training
	 * or the testing data.
	 *
	 * @param clusterer the clusterer to use for generating statistics.
	 * @param fileName the file to load
	 * @return a string containing cluster statistics.
	 * @throws Exception if statistics can't be generated.
	 */
	//  TODO private static String printClusterStats(SubspaceClusterer clusterer,
	//			String fileName) throws Exception {
	//		StringBuffer text = new StringBuffer();
	//		int i = 0;
	//		int cnum;
	//		double loglk = 0.0;
	//		int cc = clusterer.numberOfClusters();
	//		double[] instanceStats = new double[cc];
	//		int unclusteredInstances = 0;
	//
	//		if (fileName.length() != 0) {
	//			DataSource source = new DataSource(fileName);
	//			Instance inst;
	//			while (source.hasMoreElements()) {
	//				inst = source.nextElement();
	//				try {
	//					cnum = clusterer.clusterInstance(inst);
	//
	////					if (clusterer instanceof DensityBasedClusterer) {
	////						loglk += ((DensityBasedClusterer) clusterer)
	////								.logDensityForInstance(inst);
	////						// temp = Utils.sum(dist);
	////					}
	//					instanceStats[cnum]++;
	//				} catch (Exception e) {
	//					unclusteredInstances++;
	//				}
	//				i++;
	//			}
	//
	//			/*
	//			 * // count the actual number of used clusters int count = 0; for (i =
	//			 * 0; i < cc; i++) { if (instanceStats[i] > 0) { count++; } } if
	//			 * (count > 0) { double[] tempStats = new double [count]; count=0;
	//			 * for (i=0;i<cc;i++) { if (instanceStats[i] > 0) {
	//			 * tempStats[count++] = instanceStats[i]; } } instanceStats =
	//			 * tempStats; cc = instanceStats.length; }
	//			 */
	//
	//			int clustFieldWidth = (int) ((Math.log(cc) / Math.log(10)) + 1);
	//			int numInstFieldWidth = (int) ((Math.log(i) / Math.log(10)) + 1);
	//			double sum = Utils.sum(instanceStats);
	//			loglk /= sum;
	//			text.append("Clustered Instances\n");
	//
	//			for (i = 0; i < cc; i++) {
	//				if (instanceStats[i] > 0) {
	//					text.append(Utils.doubleToString((double) i,
	//							clustFieldWidth, 0)
	//							+ "      "
	//							+ Utils.doubleToString(instanceStats[i],
	//									numInstFieldWidth, 0)
	//							+ " ("
	//							+ Utils.doubleToString(
	//									(instanceStats[i] / sum * 100.0), 3, 0)
	//							+ "%)\n");
	//				}
	//			}
	//			if (unclusteredInstances > 0) {
	//				text
	//						.append("\nUnclustered Instances : "
	//								+ unclusteredInstances);
	//			}
	//
	////			if (clusterer instanceof DensityBasedClusterer) {
	////				text.append("\n\nLog likelihood: "
	////						+ Utils.doubleToString(loglk, 1, 5) + "\n");
	////			}
	//		}
	//
	//		return text.toString();
	//	}

	/**
	 * Print the cluster assignments for either the training or the testing
	 * data.
	 * 
	 * @param clusterer
	 *            the clusterer to use for cluster assignments
	 * @param trainFileName
	 *            the train file
	 * @param testFileName
	 *            an optional test file
	 * @param attributesToOutput
	 *            the attributes to print
	 * @return a string containing the instance indexes and cluster assigns.
	 * @throws Exception
	 *             if cluster assignments can't be printed
	 */
	//	TODO private static String printClusterings(SubspaceClusterer clusterer,
	//			String trainFileName, String testFileName, Range attributesToOutput)
	//			throws Exception {
	//
	//		StringBuffer text = new StringBuffer();
	//		int i = 0;
	//		int cnum;
	//		DataSource source = null;
	//		Instance inst;
	//
	//		if (testFileName.length() != 0)
	//			source = new DataSource(testFileName);
	//		else
	//			source = new DataSource(trainFileName);
	//
	//		while (source.hasMoreElements()) {
	//			inst = source.nextElement();
	//			try {
	//				cnum = clusterer.clusterInstance(inst);
	//
	//				text.append(i + " " + cnum + " "
	//						+ attributeValuesString(inst, attributesToOutput)
	//						+ "\n");
	//			} catch (Exception e) {
	//				/*
	//				 * throw new Exception('\n' + "Unable to cluster instance\n" +
	//				 * e.getMessage());
	//				 */
	//				text.append(i + " Unclustered "
	//						+ attributeValuesString(inst, attributesToOutput)
	//						+ "\n");
	//			}
	//			i++;
	//		}
	//
	//		return text.toString();
	//	}

	/**
	 * Builds a string listing the attribute values in a specified range of
	 * indices, separated by commas and enclosed in brackets.
	 * 
	 * @param instance
	 *            the instance to print the values from
	 * @param attRange
	 *            the range of the attributes to list
	 * @return a string listing values of the attributes in the range
	 */
	//  private static String attributeValuesString(Instance instance,  Range attRange) {
	//    StringBuffer text = new StringBuffer();
	//    if (attRange != null) {
	//      boolean firstOutput = true;
	//      attRange.setUpper(instance.numAttributes() - 1);
	//      for (int i=0; i<instance.numAttributes(); i++)
	//	if (attRange.isInRange(i)) {
	//	  if (firstOutput) text.append("(");
	//	  else text.append(",");
	//	  text.append(instance.toString(i));
	//	  firstOutput = false;
	//	}
	//      if (!firstOutput) text.append(")");
	//    }
	//    return text.toString();
	//  }

	/**
	 * Make up the help string giving all the command line options
	 *
	 * @param clusterer the clusterer to include options for
	 * @return a string detailing the valid command line options
	 */
	private static String makeOptionString (SubspaceClusterer clusterer) {
		StringBuffer optionsText = new StringBuffer("");
		// General options
		optionsText.append("\n\nGeneral options:\n\n");

		optionsText.append("-t <name of input file>\n");
		optionsText.append("\tSets input file.\n");

		optionsText.append("-M <cluster quality measures to evaluate>\n");
		optionsText.append("\tsubspace cluster quality measures in package weka.clusterquality\n");
		optionsText.append("\tseparate measures with ':' e.g. -M F1Measure:Entropy:CE \n");

		optionsText.append("-c <class index>\n");
		optionsText.append("\tSet class attribute, starting with 1. If supplied, class is ignored");
		optionsText.append("\n\tduring clustering but is used in a classes to");
		optionsText.append("\n\tclusters evaluation.\n");

		// Get scheme-specific options
		if (clusterer instanceof OptionHandler) {
			optionsText.append("\nOptions specific to " 
					+ clusterer.getClass().getName() + ":\n\n");
			Enumeration enu = ((OptionHandler)clusterer).listOptions();

			while (enu.hasMoreElements()) {
				Option option = (Option)enu.nextElement();
				optionsText.append(option.synopsis() + '\n');
				optionsText.append(option.description() + "\n");
			}
		}

		return  optionsText.toString();
	}

	//  /**
	//   * Tests whether the current evaluation object is equal to another
	//   * evaluation object
	//   *
	//   * @param obj the object to compare against
	//   * @return true if the two objects are equal
	//   */
	//  public boolean equals(Object obj) {
	//    if ((obj == null) || !(obj.getClass().equals(this.getClass())))
	//      return false;
	//    
	//    SubspaceClusterEvaluation cmp = (SubspaceClusterEvaluation) obj;
	//    
	//    if ((m_classToCluster != null) != (cmp.m_classToCluster != null)) return false;
	//    if (m_classToCluster != null) {
	//      for (int i = 0; i < m_classToCluster.length; i++) {
	//        if (m_classToCluster[i] != cmp.m_classToCluster[i])
	//  	return false;
	//      }
	//    }
	//    
	//    if ((m_clusterAssignments != null) != (cmp.m_clusterAssignments != null)) return false;
	//    if (m_clusterAssignments != null) {
	//      for (int i = 0; i < m_clusterAssignments.length; i++) {
	//        if (m_clusterAssignments[i] != cmp.m_clusterAssignments[i])
	//  	return false;
	//      }
	//    }
	//
	//    if (Double.isNaN(m_logL) != Double.isNaN(cmp.m_logL)) return false;
	//    if (!Double.isNaN(m_logL)) {
	//      if (m_logL != cmp.m_logL) return false;
	//    }
	//    
	//    if (m_numClusters != cmp.m_numClusters) return false;
	//    
	//    // TODO: better comparison? via members?
	//    String clusteringResults1 = m_clusteringResults.toString().replaceAll("Elapsed time.*", "");
	//    String clusteringResults2 = cmp.m_clusteringResults.toString().replaceAll("Elapsed time.*", "");
	//    if (!clusteringResults1.equals(clusteringResults2)) return false;
	//    
	//    return true;
	//  }

	/**
	 * Main method for testing this class.
	 *
	 * @param args the options
	 */
	public static void main (String[] args) {
		try {
			if (args.length == 0) {
				throw  new Exception("The first argument must be the name of a " 
						+ "clusterer");
			}

			String ClustererString = args[0];
			args[0] = "";
			SubspaceClusterer newClusterer = SubspaceClusterer.forName(ClustererString, null);
			System.out.println(evaluateClusterer(newClusterer, args));
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}

