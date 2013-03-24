package weka.gui;

import i9.subspace.base.Cluster;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import weka.clusterquality.ClusterQualityMeasure;
import weka.subspaceClusterer.SubspaceClusterTools;



public class EvaluationPanel extends JPanel{
	
  	ClusterQualityMeasure m_measures[] = null;
  	boolean m_measures_selected[] = null;
  	private ArrayList<Cluster> m_TrueClusters = null;
  	private File m_true_cluster_file = null;

	public EvaluationPanel() {

		//get a list of all available ClusterQualityMeasure 
		Class classtype = ClusterQualityMeasure.class;
		Class[] measure_classes = null;
	   	String class_string = GenericObjectEditor.EDITOR_PROPERTIES.getProperty(classtype.getName());
		String[] classes_string = class_string.split(",");
		measure_classes = new Class[classes_string.length];
		for (int i = 0; i < classes_string.length; i++) {
			try {
				measure_classes[i] = Class.forName(classes_string[i]);
			} catch (ClassNotFoundException e) {
				measure_classes[i] = null;
			}
		}

		//Create array with Instances of measures
		m_measures = new ClusterQualityMeasure[measure_classes.length];
		for (int i = 0; i < measure_classes.length; i++) {
			try {
				m_measures[i] = (ClusterQualityMeasure)measure_classes[i].newInstance();
			} catch (InstantiationException e1) {
				System.out.println("Not a valid subspace measure class:"+measure_classes[i].getName());
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				System.out.println("Not a valid subspace measure class:"+measure_classes[i].getName());
				e1.printStackTrace();
			}
		}
		
		
		//init measure select array with "all selected"
		m_measures_selected = new boolean [m_measures.length];
		for (int j = 0; j < m_measures_selected.length; j++) {
			m_measures_selected[j] = true;
		}

	}

  	public ArrayList<ClusterQualityMeasure> getSelectedMeasures(){ 
		ArrayList<ClusterQualityMeasure> measures = new ArrayList<ClusterQualityMeasure>();
		for (int j = 0; j < m_measures.length; j++) {
			if(m_measures[j]!=null && m_measures_selected[j]){
				measures.add(m_measures[j]);
			}
		}
		return measures;
  	}

	
	public void updateEvaluationSelectPanel(int dimension){
		removeAll();
	  	setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
	  	setLayout(new GridBagLayout());
	  	
	  	for (int i = 0; i < m_measures.length; i++) {
			if(m_measures[i]!=null){
				GridBagConstraints gb = new GridBagConstraints();
				gb.weightx = 1;
				gb.weighty = 1;
				gb.gridx = 0;
				gb.gridy = i;
				gb.fill = GridBagConstraints.BOTH;
				gb.insets = new Insets(2,5,2,20);
				JLabel label = new JLabel(m_measures[i].getClass().getSimpleName());
				add(label,gb);
				
				gb = new GridBagConstraints();
				gb.weightx = 0;
				gb.gridx = 1;
				gb.gridy = i;
				gb.fill = GridBagConstraints.NONE;
				gb.insets = new Insets(2,0,2,0);
				final JCheckBox cb = new JCheckBox();
				cb.setSelected(m_measures_selected[i]);
				final int id = i;
				cb.addActionListener(new ActionListener() {
				  	  public void actionPerformed(ActionEvent a) {
					  	    	if(cb.isSelected()){
					  	    		m_measures_selected[id] = true;
					  	    	}
					  	    	else{
					  	    		m_measures_selected[id] = false;
					  	    	}
					  	  }
					  	});
				add(cb, gb);
			}
	  	}
	  	
	  	
		JButton truecluster_button = new JButton("Load");
		String filename = "none";
		if(m_true_cluster_file!=null){
			filename = m_true_cluster_file.getName();
		}
		final JLabel truecluster_label = new JLabel("File: "+filename);
		final int dim = dimension;
		truecluster_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				JFileChooser trueClusterChooser = new JFileChooser();
				trueClusterChooser.setFileHidingEnabled(false);
				trueClusterChooser.setMultiSelectionEnabled(false);
				trueClusterChooser
						.setFileSelectionMode(JFileChooser.FILES_ONLY);
				trueClusterChooser.setDialogType(JFileChooser.OPEN_DIALOG);

				int state = trueClusterChooser.showDialog(null,
						"Choose TrueCluster File");
				if (state == JFileChooser.APPROVE_OPTION) {
					try {
						m_true_cluster_file = trueClusterChooser.getSelectedFile();
						ArrayList<Cluster> tmp_TrueClusters = SubspaceClusterTools.getClusterList(m_true_cluster_file, dim);
						if(tmp_TrueClusters!=null){
							m_TrueClusters = tmp_TrueClusters;
							truecluster_label.setText("File: "+ m_true_cluster_file.getName());
						}
					} catch (Exception e) {
						System.out.println("Unable to load file");
					}
				}
			}
		});

		JPanel truepanel = new JPanel();
		truepanel.setBorder(BorderFactory
				.createTitledBorder("True Cluster File"));
		truepanel.setLayout(new GridBagLayout());

		// add true cluster label
		truecluster_label.setPreferredSize(new Dimension(150,20));
		GridBagConstraints gb = new GridBagConstraints();
		gb.weightx = 1;
		gb.gridx = 0;
		gb.gridy = 0;
		gb.fill = GridBagConstraints.NONE;
		truepanel.add(truecluster_label, gb);
		

		// add load button
		gb = new GridBagConstraints();
		gb.weightx = 1;
		gb.gridx = 1;
		gb.gridy = 0;
		gb.fill = GridBagConstraints.NONE;
		truepanel.add(truecluster_button, gb);

		// add true cluster panel
		gb = new GridBagConstraints();
		gb.weighty = 1;
		gb.gridwidth = 2;
		gb.gridx = 0;
		gb.gridy = m_measures.length + 1;
		gb.fill = GridBagConstraints.NONE;
		add(truepanel, gb);
	}

	public File getTrueClusterFile() {
		return m_true_cluster_file;
	}

	public ArrayList<Cluster> getTrueClusters() {
		return m_TrueClusters;
	}
	

}