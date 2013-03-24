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

package weka.subspaceClusterer;

import weka.core.Capabilities;
import weka.core.CapabilitiesHandler;
import weka.core.Instances;
import weka.core.SerializedObject;
import weka.core.Utils;
import weka.core.Capabilities.Capability;
import weka.gui.visualize.subspace.SubspaceVisualData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import i9.subspace.base.Cluster;

/**
 * Abstract SubspaceClusterer.
 * 
 * @author Jansen
 * @version $Revision: 1.0 $
 */
public abstract class SubspaceClusterer implements Cloneable, Serializable,
		CapabilitiesHandler {
	private static final long serialVersionUID = 1L;

	private List<Cluster> m_Clusters = null; // actual Clustering
	private ArrayList<Integer>[] m_clusterAssignments; // Instance to Cluster mapping
	private StringBuffer m_console = null; // save result output buffer with evaluation measurements
	private SubspaceVisualData svp = null; // saves the visualization

	/**
	 * Generates a clusterer. Has to initialize all fields of the clusterer that
	 * are not being set via options.
	 * 
	 * @param data
	 *            set of instances serving as training data
	 * @exception Exception
	 *                if the clusterer has not been generated successfully
	 */
	public abstract void buildSubspaceClusterer(Instances data)
			throws Exception;

	public List<Cluster> getSubspaceClustering() {
		return m_Clusters;
	}

	public void setSubspaceClustering(List<Cluster> clustering)
			throws Exception {
		m_Clusters = clustering;
	}

	/**
	 * Returns the number of clusters.
	 * 
	 * @return the number of clusters generated for a training dataset.
	 * @exception Exception
	 *                if number of clusters could not be returned successfully
	 */
	public int numberOfClusters() throws Exception {
		if (m_Clusters != null)
			return m_Clusters.size();
		else
			return -1;
	}

	/**
	 * Creates a new instance of a clusterer given it's class name and
	 * (optional) arguments to pass to it's setOptions method. If the clusterer
	 * implements OptionHandler and the options parameter is non-null, the
	 * clusterer will have it's options set.
	 * 
	 * @param clustererName
	 *            the fully qualified class name of the clusterer
	 * @param options
	 *            an array of options suitable for passing to setOptions. May be
	 *            null.
	 * @return the newly created search object, ready for use.
	 * @exception Exception
	 *                if the clusterer class name is invalid, or the options
	 *                supplied are not acceptable to the clusterer.
	 */
	public static SubspaceClusterer forName(String clustererName,
			String[] options) throws Exception {
		return (SubspaceClusterer) Utils.forName(SubspaceClusterer.class,
				clustererName, options);
	}

	/**
	 * Creates a deep copy of the given clusterer using serialization.
	 * 
	 * @param model
	 *            the clusterer to copy
	 * @return a deep copy of the clusterer
	 * @exception Exception
	 *                if an error occurs
	 */
	public static SubspaceClusterer makeCopy(SubspaceClusterer model)
			throws Exception {
		return (SubspaceClusterer) new SerializedObject(model).getObject();
	}

	/**
	 * Creates copies of the current clusterer. Note that this method now uses
	 * Serialization to perform a deep copy, so the Clusterer object must be
	 * fully Serializable. Any currently built model will now be copied as well.
	 * 
	 * @param model
	 *            an example clusterer to copy
	 * @param num
	 *            the number of clusterer copies to create.
	 * @return an array of clusterers.
	 * @exception Exception
	 *                if an error occurs
	 */
	public static SubspaceClusterer[] makeCopies(SubspaceClusterer model,
			int num) throws Exception {
		if (model == null) {
			throw new Exception("No model clusterer set");
		}
		SubspaceClusterer[] clusterers = new SubspaceClusterer[num];
		SerializedObject so = new SerializedObject(model);
		for (int i = 0; i < clusterers.length; i++) {
			clusterers[i] = (SubspaceClusterer) so.getObject();
		}
		return clusterers;
	}

	public void calculateClusterAssignments(int numInstances) {
		m_clusterAssignments = new ArrayList[numInstances];

		for (int k = 0; k < m_Clusters.size(); k++) {
			for (int i = 0; i < m_Clusters.get(k).m_objects.size(); i++) {
				int index = m_Clusters.get(k).m_objects.get(i);
				if (m_clusterAssignments[index] == null) {
					ArrayList<Integer> inst_clist = new ArrayList<Integer>();
					inst_clist.add(k);
					m_clusterAssignments[index] = inst_clist;
				} else {
					m_clusterAssignments[index].add(k);
				}
			}
		}
	}

	public ArrayList<Integer>[] getClusterAssignments() {
		return m_clusterAssignments;
	}

	public String toString() {

		StringBuffer text = new StringBuffer();

		if (m_Clusters == null || m_Clusters.size() == 0) {
			text.append("no clusters");
		} else {
			for (int i = 0; i < m_Clusters.size(); i++) {
				text
						.append("SC_" + i + ": "
								+ m_Clusters.get(i).toStringWeka());
			}
		}
		return text.toString();
	}

	/**
	 * Returns the Capabilities of this clusterer. Derived classifiers have to
	 * override this method to enable capabilities.
	 * 
	 * @return the capabilities of this object
	 * @see Capabilities
	 */
	public Capabilities getCapabilities() {
		Capabilities result;

		result = new Capabilities(this);
		result.enable(Capability.NO_CLASS);

		return result;
	}

	/**
	 * runs the clusterer instance with the given options.
	 * 
	 * @param clusterer
	 *            the clusterer to run
	 * @param options
	 *            the commandline options
	 */
	protected static void runSubspaceClusterer(SubspaceClusterer clusterer, String[] options) {
		try {
				//Buildes and evaluates the clustering
				//returns a result string, full cluster list can be accessed through clusterer.getSubspaceClustering()
				String eval = SubspaceClusterEvaluation.evaluateClusterer(clusterer, options);
				System.out.println(eval);
		} catch (Exception e) {
			if ((e.getMessage() == null)
					|| ((e.getMessage() != null) && (e.getMessage().indexOf(
							"General options") == -1)))
				e.printStackTrace();
			else
				System.err.println(e.getMessage());
		}
	}

	public abstract String getName();

	public abstract String getParameterString();

	public void setConsole(StringBuffer buffer) {
		m_console = buffer;
	}

	public StringBuffer getConsole() {
		return m_console;
	}

	public SubspaceVisualData getVisual() {
		return svp;
	}

	public void setVisual(SubspaceVisualData _svp) {
		svp = _svp;
	}

	public void delVisual() {
		svp = null;
	}

}
