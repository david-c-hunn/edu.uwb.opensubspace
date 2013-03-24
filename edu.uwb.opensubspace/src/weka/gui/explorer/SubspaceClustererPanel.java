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
 *    ClustererPanel.java
 *    Copyright (C) 1999 Mark Hall
 *
 */

package weka.gui.explorer;


import weka.subspaceClusterer.SubspaceClusterEvaluation;
import weka.subspaceClusterer.SubspaceClusterer;
import weka.clusterquality.ClusterQualityMeasure;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.SerializedObject;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.gui.BracketingPanel;
import weka.gui.EvaluationPanel;
import weka.gui.ExtensionFileFilter;
import weka.gui.GenericObjectEditor;
import weka.gui.InstancesSummaryPanel;
import weka.gui.ListSelectorDialog;
import weka.gui.Logger;
import weka.gui.PropertyPanel;
import weka.gui.ResultHistoryPanel;
import weka.gui.SaveBuffer;
import weka.gui.SysErrLog;
import weka.gui.TaskLogger;
import weka.gui.explorer.Explorer.CapabilitiesFilterChangeEvent;
import weka.gui.explorer.Explorer.CapabilitiesFilterChangeListener;
import weka.gui.explorer.Explorer.ExplorerPanel;
import weka.gui.explorer.Explorer.LogHandler;
import weka.gui.visualize.subspace.VisualClusteringFrame;
import weka.gui.visualize.subspace.SubspaceVisualData;
import weka.gui.visualize.subspace.VisualClusteringOverview;

import i9.subspace.base.Cluster;

import java.awt.BorderLayout;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;


/** 
 * This panel allows the user to select and configure a clusterer, and evaluate
 * the clusterer using a number of testing modes (test on the training data,
 * train/test on a percentage split, test on a
 * separate split). The results of clustering runs are stored in a result
 * history so that previous results are accessible.
 *
 * @author Mark Hall (mhall@cs.waikato.ac.nz)
 * @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
 * @version $Revision: 1.56 $
 */
public class SubspaceClustererPanel extends JPanel implements ExplorerPanel, CapabilitiesFilterChangeListener, LogHandler {

	/** for serialization */
	static final long serialVersionUID = -2474932792950820990L;

	/** the parent frame. */
	protected Explorer m_Explorer = null;
	
	/** The filename extension that should be used for model files */
	public static String MODEL_FILE_EXTENSION = ".model";

	/** Lets the user configure the clusterer */
	protected GenericObjectEditor m_ClustererEditor = new GenericObjectEditor();

	/** The panel showing the current clusterer selection */
	protected PropertyPanel m_CLPanel = new PropertyPanel(m_ClustererEditor);

	/** The output area for classification results */
	protected JTextArea m_OutText = new JTextArea(20, 40);

	/** The destination for log/status messages */
	protected Logger m_Log = new SysErrLog();

	/** The buffer saving object for saving output */
	SaveBuffer m_SaveOut = new SaveBuffer(m_Log, this);

	/** A panel controlling results viewing */
	protected ResultHistoryPanel m_History = new ResultHistoryPanel(m_OutText);



	/** Click to set test mode to classes to clusters based evaluation */
	protected JButton m_BracketingBut = new JButton("Edit");

	/** Click to set test mode to classes to clusters based evaluation */
	protected JButton m_EvaluationBut = new JButton("Edit");

	/** Lets the user select the class column for classes to clusters based
	 evaluation */
	protected JComboBox m_ClassCombo = new JComboBox();


	/** The button used to popup a list for choosing attributes to ignore while
	 clustering */
	protected JButton m_ignoreBut = new JButton("Ignore attributes");

	protected DefaultListModel m_ignoreKeyModel = new DefaultListModel();

	protected JList m_ignoreKeyList = new JList(m_ignoreKeyModel);

	/** Click to start running the clusterer */
	protected JButton m_StartBut = new JButton("Start");

	private Dimension COMBO_SIZE = new Dimension(250, m_StartBut
			.getPreferredSize().height);

	/** Click to stop a running clusterer */
	protected JButton m_StopBut = new JButton("Stop");

	/** The main set of instances we're playing with */
	protected Instances m_Instances;
	

	/** The current visualization object */
	protected SubspaceVisualData m_CurrentVis = null;

	/** Check to save the predictions in the results list for visualizing
	 later on */
	protected JCheckBox m_EnableStoreVisual = new JCheckBox(
			"Store clusters for visualization");

	protected JCheckBox m_EnableBracketing = new JCheckBox("Enable Bracketing");
	protected JCheckBox m_EnableTimer = new JCheckBox("Enable Timer");
	
	protected JCheckBox m_EnableEvaluation = new JCheckBox("Calculate Ouality Measures");
	
	protected JCheckBox m_EnableClassesToClusters = new JCheckBox("Enable"); 

	/** A thread that clustering runs in */
	protected Thread m_RunThread;

	/** The instances summary panel displayed by m_SetTestFrame */
	protected InstancesSummaryPanel m_Summary;

	/** Filter to ensure only model files are selected */
	protected FileFilter m_ModelFilter = new ExtensionFileFilter(
			MODEL_FILE_EXTENSION, "Model object files");

	/** The file chooser for selecting model files */
	protected JFileChooser m_FileChooser = new JFileChooser(new File(System
			.getProperty("user.dir")));

	protected BracketingPanel m_bracketingPanel = new BracketingPanel();
	protected EvaluationPanel m_evaluationPanel = new EvaluationPanel();

	protected JTextField m_timer = new JTextField("30");
	

	/* Register the property editors we need */
	static {
		GenericObjectEditor.registerEditors();
	}
	
  	
	/**
	 * Creates the clusterer panel
	 */
	public SubspaceClustererPanel() {

		// Connect / configure the components
		m_OutText.setEditable(false);
		m_OutText.setFont(new Font("Monospaced", Font.PLAIN, 12));
		m_OutText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		m_OutText.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != InputEvent.BUTTON1_MASK) {
					m_OutText.selectAll();
				}
			}
		});
		m_History.setBorder(BorderFactory.createTitledBorder("Result list"));

		m_ClustererEditor.setClassType(SubspaceClusterer.class);
		//TODO m_ClustererEditor.setValue(ExplorerDefaults.getSubspaceClusterer());
		m_ClustererEditor.setValue(new weka.subspaceClusterer.Proclus());
		m_ClustererEditor
				.addPropertyChangeListener(new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent e) {
						repaint();
					}
				});
		
		m_ClassCombo
				.setToolTipText("Select the class attribute for class based"
						+ " evaluation");
		m_StartBut.setToolTipText("Starts the clustering");
		m_StopBut.setToolTipText("Stops a running clusterer");
		m_EnableStoreVisual
				.setToolTipText("Store predictions in the result list for later "
						+ "visualization");
		m_EnableStoreVisual
				.setToolTipText("Enable Bracketing over Parameters");

		m_ignoreBut.setToolTipText("Ignore attributes during clustering");

		m_ClassCombo.setPreferredSize(COMBO_SIZE);
		m_ClassCombo.setMaximumSize(COMBO_SIZE);
		m_ClassCombo.setMinimumSize(COMBO_SIZE);
		m_ClassCombo.setEnabled(true);
		
		m_EnableClassesToClusters.setSelected(true);
		m_BracketingBut.setEnabled(false);
		m_timer.setEnabled(true);

		m_EnableStoreVisual.setSelected(
				ExplorerDefaults.getClustererStoreClustersForVis());
		m_EnableEvaluation.setSelected(true);
		m_EnableTimer.setSelected(true);
		
		m_StartBut.setEnabled(false);
		m_StopBut.setEnabled(false);
		m_ignoreBut.setEnabled(false);
		m_StartBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startClusterer();
			}
		});
		m_StopBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopClusterer();
			}
		});

		m_BracketingBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(m_RunThread == null)
					bracketingFrame();
			}
		});
		
		m_EvaluationBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(m_RunThread == null)
					evaluationFrame();
			}
		});	
		
		m_EnableClassesToClusters.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(m_EnableClassesToClusters.isSelected()){
					m_ClassCombo.setEnabled(true);
				}
				else{
					m_ClassCombo.setEnabled(false);
				}
			}
		});


		m_EnableBracketing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(m_EnableBracketing.isSelected()){
					m_StartBut.setText("Start Bracketing");
					m_BracketingBut.setEnabled(true);
					if(m_RunThread == null)
						bracketingFrame();
				}
				else{
					m_StartBut.setText("Start");
					m_BracketingBut.setEnabled(false);
				}
			}
		});
		
		
		m_EnableTimer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(m_EnableTimer.isSelected()){
					m_timer.setEnabled(true);
				}
				else{
					m_StartBut.setText("Start");
					m_timer.setEnabled(false);
				}
			}
		});
		

		
		m_EnableEvaluation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(m_EnableEvaluation.isSelected()){
					m_EvaluationBut.setEnabled(true);
					if(m_RunThread == null)
						evaluationFrame();
				}
				else{
					m_EvaluationBut.setEnabled(false);
				}
			}
		});


		m_ignoreBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setIgnoreColumns();
			}
		});

		m_History.setHandleRightClicks(false);
		// see if we can popup a menu for the selected result
		m_History.getList().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (((e.getModifiers() & InputEvent.BUTTON1_MASK) != InputEvent.BUTTON1_MASK)
						|| e.isAltDown()) {
					int index = m_History.getList().locationToIndex(
							e.getPoint());
					if (index != -1) {
						String name = m_History.getNameAtIndex(index);
						visualizeClusterer(name, e.getX(), e.getY());
					} else {
						visualizeClusterer(null, e.getX(), e.getY());
					}
				}
			}
		});

		m_ClassCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateCapabilitiesFilter(m_ClustererEditor
						.getCapabilitiesFilter());
			}
		});
		
		//allow MultiSelection to load multiple models at a time
		m_FileChooser.setMultiSelectionEnabled(true);

		// Layout the GUI
		JPanel p1 = new JPanel();
		p1.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createTitledBorder("SubspaceClusterer"), BorderFactory
				.createEmptyBorder(0, 5, 5, 5)));
		p1.setLayout(new BorderLayout());
		p1.add(m_CLPanel, BorderLayout.NORTH);

		JPanel p2 = new JPanel();
		
		p2.setLayout(new GridBagLayout());
		p2.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createTitledBorder("Class to cluster evaluation"),
						BorderFactory.createEmptyBorder(0, 5, 5, 5)));

		GridBagConstraints gbC; 
		gbC = new GridBagConstraints();
		gbC.anchor = GridBagConstraints.WEST;
		gbC.fill = GridBagConstraints.BOTH;
		gbC.gridy = 0;
		gbC.gridx = 0;
		gbC.weightx = 1;
		p2.add(m_EnableClassesToClusters,gbC);
		
		m_ClassCombo.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
		gbC = new GridBagConstraints();
		gbC.anchor = GridBagConstraints.WEST;
		gbC.fill = GridBagConstraints.BOTH;
		gbC.gridy = 1;
		gbC.gridx = 0;
		gbC.weightx = 1;
		p2.add(m_ClassCombo,gbC);

		gbC = new GridBagConstraints();
		gbC.anchor = GridBagConstraints.WEST;
		gbC.fill = GridBagConstraints.BOTH;
		gbC.gridy = 2;
		gbC.gridx = 0;
		gbC.weightx = 1;
		p2.add(m_EnableStoreVisual,gbC);
		

		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(2, 1));
		JPanel ssButs = new JPanel();
		ssButs.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		ssButs.setLayout(new GridLayout(1, 2, 5, 5));
		ssButs.add(m_StartBut);
		ssButs.add(m_StopBut);

		JPanel ib = new JPanel();
		ib.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		ib.setLayout(new GridLayout(1, 1, 5, 5));
		ib.add(m_ignoreBut);
		buttons.add(ib);
		buttons.add(ssButs);
		
		JPanel p4 = new JPanel();
		p4.setLayout(new GridBagLayout());
		p4.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createTitledBorder("Bracketing"),
						BorderFactory.createEmptyBorder(0, 5, 5, 5)));
		gbC = new GridBagConstraints();
		gbC.fill = GridBagConstraints.BOTH;
		gbC.anchor = GridBagConstraints.WEST;
		gbC.gridy = 0;
		gbC.gridx = 0;
		gbC.weightx = 1;

		p4.add(m_EnableBracketing, gbC);
		
		gbC = new GridBagConstraints();
		gbC.fill = GridBagConstraints.BOTH;
		gbC.anchor = GridBagConstraints.WEST;
		gbC.gridy = 0;
		gbC.gridx = 1;
		p4.add(m_BracketingBut, gbC);
		
		gbC = new GridBagConstraints();
		gbC.fill = GridBagConstraints.BOTH;
		gbC.anchor = GridBagConstraints.WEST;
		gbC.gridy = 1;
		gbC.gridx = 0;
		gbC.weightx = 1;
		p4.add(m_EnableTimer, gbC);
		
		gbC = new GridBagConstraints();
		gbC.fill = GridBagConstraints.BOTH;
		gbC.anchor = GridBagConstraints.WEST;
		gbC.gridy = 1;
		
		m_timer.setHorizontalAlignment(JTextField.RIGHT);
		p4.add(m_timer, gbC);
		JPanel p5 = new JPanel();
		p5.setLayout(new GridBagLayout());
		p5.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createTitledBorder("Evaluation"),
						BorderFactory.createEmptyBorder(0, 5, 5, 5)));
		gbC = new GridBagConstraints();
		gbC.fill = GridBagConstraints.BOTH;
		gbC.anchor = GridBagConstraints.WEST;
		gbC.gridy = 0;
		gbC.gridx = 0;
		gbC.weightx = 1;

		p5.add(m_EnableEvaluation, gbC);
		
		gbC = new GridBagConstraints();
		gbC.fill = GridBagConstraints.BOTH;
		gbC.anchor = GridBagConstraints.WEST;
		gbC.gridy = 0;
		gbC.gridx = 1;
		p5.add(m_EvaluationBut, gbC);
	

		JPanel p3 = new JPanel();
		p3.setBorder(BorderFactory.createTitledBorder("SubspaceClusterer output"));
		p3.setLayout(new BorderLayout());
		final JScrollPane js = new JScrollPane(m_OutText);
		p3.add(js, BorderLayout.CENTER);
		js.getViewport().addChangeListener(new ChangeListener() {
			private int lastHeight;

			public void stateChanged(ChangeEvent e) {
				JViewport vp = (JViewport) e.getSource();
				int h = vp.getViewSize().height;
				if (h != lastHeight) { // i.e. an addition not just a user scrolling
					lastHeight = h;
					int x = h - vp.getExtentSize().height;
					vp.setViewPosition(new Point(0, x));
				}
			}
		});

		JPanel mondo = new JPanel();
		GridBagLayout gbL = new GridBagLayout();
		gbL = new GridBagLayout();
		mondo.setLayout(gbL);
		gbC = new GridBagConstraints();
		gbC.fill = GridBagConstraints.HORIZONTAL;
		gbC.gridy = 0;
		gbC.gridx = 0;
		gbL.setConstraints(p2, gbC);
		mondo.add(p2);
		gbC = new GridBagConstraints();
		gbC.anchor = GridBagConstraints.NORTH;
		gbC.fill = GridBagConstraints.HORIZONTAL;
		gbC.gridy = 1;
		gbC.gridx = 0;
		gbL.setConstraints(p4, gbC);
		mondo.add(p4);
		gbC = new GridBagConstraints();
		gbC.anchor = GridBagConstraints.NORTH;
		gbC.fill = GridBagConstraints.HORIZONTAL;
		gbC.gridy = 2;
		gbC.gridx = 0;
		gbL.setConstraints(p5, gbC);
		mondo.add(p5);

		
		gbC = new GridBagConstraints();
		gbC.anchor = GridBagConstraints.NORTH;
		gbC.fill = GridBagConstraints.HORIZONTAL;
		gbC.gridy = 3;
		gbC.gridx = 0;
		gbL.setConstraints(buttons, gbC);
		mondo.add(buttons);
		gbC = new GridBagConstraints();
		gbC.fill = GridBagConstraints.BOTH;
		gbC.gridy = 4;
		gbC.gridx = 0;
		gbC.weightx = 0;
		gbL.setConstraints(m_History, gbC);
		mondo.add(m_History);
		gbC = new GridBagConstraints();
		gbC.fill = GridBagConstraints.BOTH;
		gbC.gridy = 0;
		gbC.gridx = 1;
		gbC.gridheight = 5;
		gbC.weightx = 100;
		gbC.weighty = 100;
		gbL.setConstraints(p3, gbC);
		mondo.add(p3);

		setLayout(new BorderLayout());
		add(p1, BorderLayout.NORTH);
		add(mondo, BorderLayout.CENTER);
	}


	
	/**
	 * Opens a frame with a bracketing table of the parameters 
	 * of the currently selected SubspaceClusterer
	 */
	private void bracketingFrame(){
		m_BracketingBut.setEnabled(false);
	  	JPanel moreOptionsPanel = new JPanel();
	  	moreOptionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
	  	moreOptionsPanel.setLayout(new GridLayout());

	  	SubspaceClusterer clusterer = (SubspaceClusterer) m_ClustererEditor.getValue();
	  	//Set subspace cluster class, so the right parameter shows up
	  	m_bracketingPanel.setSubspaceClusterClass(clusterer);
	  	moreOptionsPanel.add(m_bracketingPanel);
	  	
	  	JPanel all = new JPanel();
	  	all.setLayout(new BorderLayout());	

	  	JButton oK = new JButton("OK");
	  	JPanel okP = new JPanel();
	  	okP.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	  	okP.setLayout(new GridLayout(1,1,5,5));
	  	okP.add(oK);

	  	all.add(moreOptionsPanel, BorderLayout.CENTER);
	  	all.add(okP, BorderLayout.SOUTH);
	  	
	  	//Bracketing Frame
	  	final javax.swing.JFrame jf = 
	  	  new javax.swing.JFrame("Subspacecluster Bracketing");
	  	jf.getContentPane().setLayout(new BorderLayout());
	  	jf.getContentPane().add(all, BorderLayout.CENTER);
	  	jf.addWindowListener(new java.awt.event.WindowAdapter() {
	  	  public void windowClosing(java.awt.event.WindowEvent w) {
	  	    jf.dispose();
	  	    m_BracketingBut.setEnabled(true);
	  	  }
	  	});
	  	oK.addActionListener(new ActionListener() {
	  	  public void actionPerformed(ActionEvent a) {
	  		m_BracketingBut.setEnabled(true);
	  	    jf.dispose();
	  	    
	  	  }
	  	});
	  	jf.pack();
	  	jf.setLocation(m_BracketingBut.getLocationOnScreen());
	  	jf.setVisible(true);
	}

	/**
	 * Opens a frame with the avaliable quality measures that can be 
	 * selected. Choosen quality measures will be calculated and be 
	 * integarted into the output
	 */
	private void evaluationFrame(){
		m_EvaluationBut.setEnabled(false);
	  	
	  	JPanel all = new JPanel();
	  	all.setLayout(new BorderLayout());	
	  	
	  	JButton oK = new JButton("OK");
	  	JPanel okP = new JPanel();
	  	okP.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	  	okP.setLayout(new GridLayout(1,1,5,5));
	  	okP.add(oK);

    	int dim = m_Instances.numAttributes();
    	if(m_EnableClassesToClusters.isSelected()) dim--;
	  	m_evaluationPanel.updateEvaluationSelectPanel(dim);
	  	all.add(m_evaluationPanel, BorderLayout.CENTER);
	  	all.add(okP, BorderLayout.SOUTH);
	  	
	  	//Eval Frame
	  	final javax.swing.JFrame jf = 
	  	  new javax.swing.JFrame("Calculate Ouality Measures");
	  	jf.getContentPane().setLayout(new BorderLayout());
	  	jf.getContentPane().add(all, BorderLayout.CENTER);
	  	jf.addWindowListener(new java.awt.event.WindowAdapter() {
	  	  public void windowClosing(java.awt.event.WindowEvent w) {
	  	    jf.dispose();
	  	    m_EvaluationBut.setEnabled(true);
	  	  }
	  	});
	  	oK.addActionListener(new ActionListener() {
	  	  public void actionPerformed(ActionEvent a) {
	  		m_EvaluationBut.setEnabled(true);
	  	    jf.dispose();
	  	    
	  	  }
	  	});
	  	jf.pack();
	  	jf.setLocation(m_BracketingBut.getLocationOnScreen());
	  	jf.setVisible(true);
	}



	/**
	 * Sets the Logger to receive informational messages
	 *
	 * @param newLog the Logger that will now get info messages
	 */
	public void setLog(Logger newLog) {
		m_Log = newLog;
	}

	/**
	 * Tells the panel to use a new set of instances.
	 *
	 * @param inst a set of Instances
	 */
	public void setInstances(Instances inst) {

		m_Instances = inst;

		m_ignoreKeyModel.removeAllElements();

		String[] attribNames = new String[m_Instances.numAttributes()];
		for (int i = 0; i < m_Instances.numAttributes(); i++) {
			String name = m_Instances.attribute(i).name();
			m_ignoreKeyModel.addElement(name);

			String type = "";
			switch (m_Instances.attribute(i).type()) {
			case Attribute.NOMINAL:
				type = "(Nom) ";
				break;
			case Attribute.NUMERIC:
				type = "(Num) ";
				break;
			case Attribute.STRING:
				type = "(Str) ";
				break;
			case Attribute.DATE:
				type = "(Dat) ";
				break;
			case Attribute.RELATIONAL:
				type = "(Rel) ";
				break;
			default:
				type = "(???) ";
			}
			String attnm = m_Instances.attribute(i).name();

			attribNames[i] = type + attnm;
		}

		m_StartBut.setEnabled(m_RunThread == null);
		m_StopBut.setEnabled(m_RunThread != null);
		m_ignoreBut.setEnabled(true);
		m_ClassCombo.setModel(new DefaultComboBoxModel(attribNames));
		if (inst.classIndex() == -1)
			m_ClassCombo.setSelectedIndex(attribNames.length - 1);
		else
			m_ClassCombo.setSelectedIndex(inst.classIndex());
		//TODO KernelPanel

	}

	

	class runSingleClustering extends Thread {
		public boolean exception = false;
		Exception e = null;
		
		SubspaceClusterer clusterer;
		Instances trainInst;
		
		public runSingleClustering(SubspaceClusterer clusterer,Instances trainInst) {
			this.clusterer = clusterer;
			this.trainInst = trainInst;
		}
			public void run(){
				
				try {
					clusterer.buildSubspaceClusterer(removeClass(trainInst));
				} catch (Exception e) {
					exception = true;
					this.e = e;
				}
			}
		}
	
	/**
	 * Starts running the currently configured clusterer with the current
	 * settings. This is run in a separate thread, and will only start if there
	 * is no clusterer already running. The clusterer output is sent to the
	 * results history panel.
	 */
	protected void startClusterer() {
		if (m_RunThread == null) {
			m_StartBut.setEnabled(false);
			m_timer.setEnabled(false);
			m_StopBut.setEnabled(true);
			m_ignoreBut.setEnabled(false);
			m_RunThread = new Thread() {
				Instances trainInst = null;
				public void run() {
					boolean errors = false;
					long start,end;
					try{
						// Copy the current state of things
						m_Log.statusMessage("Setting up...");
						Instances inst = new Instances(m_Instances);
						inst.setClassIndex(-1);
	
						int[] ignoredAtts = null;
						trainInst = new Instances(inst);
	
						if (m_EnableClassesToClusters.isSelected()) {
							trainInst.setClassIndex(m_ClassCombo.getSelectedIndex());
							inst.setClassIndex(m_ClassCombo.getSelectedIndex());
							if (inst.classAttribute().isNumeric()) {
								throw new Exception("Class must be nominal for class based evaluation!");
							}
						}
						
						if (!m_ignoreKeyList.isSelectionEmpty()) {
							trainInst = removeIgnoreCols(trainInst);
						}
						
						if (!m_ignoreKeyList.isSelectionEmpty()) {
							ignoredAtts = m_ignoreKeyList.getSelectedIndices();
						}
	
						if (m_EnableClassesToClusters.isSelected()) {
							// add class to ignored list
							if (ignoredAtts == null) {
								ignoredAtts = new int[1];
								ignoredAtts[0] = m_ClassCombo.getSelectedIndex();
							} else {
								int[] newIgnoredAtts = new int[ignoredAtts.length + 1];
								System.arraycopy(ignoredAtts, 0,
										newIgnoredAtts, 0, ignoredAtts.length);
								newIgnoredAtts[ignoredAtts.length] = m_ClassCombo
										.getSelectedIndex();
								ignoredAtts = newIgnoredAtts;
							}
						}
						int clustering_amount = 1;
						if(m_BracketingBut.isEnabled()){
							clustering_amount = m_bracketingPanel.getNumberClusterings();
						}

						//add tasks
						for (int i = 0; i < clustering_amount; i++) {
							if (m_Log instanceof TaskLogger) {
								((TaskLogger) m_Log).taskStarted();
							}
						}
						
						for (int i = 0; i < clustering_amount ; i++) {
							
							SerializedObject so = new SerializedObject((SubspaceClusterer) m_ClustererEditor.getValue());
							SubspaceClusterer clusterer = (SubspaceClusterer) so.getObject();
							if(m_BracketingBut.isEnabled()){
								m_bracketingPanel.setBracketingParameter(clusterer, i);
							}

							String name = (new SimpleDateFormat("HH:mm:ss - ")).format(new Date());
							String cname = clusterer.getClass().getName();
							if (cname.startsWith("weka.subspaceClusterer.")) {
								name += cname.substring("weka.subspaceClusterer.".length());
							} else {
								name += cname;
							}
							String parameter_name = "";
							
							if(m_BracketingBut.isEnabled()){
								parameter_name+= m_bracketingPanel.getParameterString(clusterer,i);
								name+=parameter_name;
							}
							
							String cmd = clusterer.getClass().getName();
							if (m_ClustererEditor.getValue() instanceof OptionHandler)
								cmd += " " + Utils.joinOptions(((OptionHandler)clusterer).getOptions());

							//add measure options to command line
							if(m_EnableEvaluation.isSelected()){
								ArrayList<ClusterQualityMeasure> cmdMeasureList = m_evaluationPanel.getSelectedMeasures();
								if(cmdMeasureList.size() > 0) cmd+= " -M ";
								for (int c = 0; c < cmdMeasureList.size(); c++) {
									String c_name = cmdMeasureList.get(c).getClass().getName();
									if (c_name.startsWith("weka.clusterquality.")) {
										cmd+= c_name.substring("weka.clusterquality.".length());
										if(c < cmdMeasureList.size()-1) cmd+= ":";
									}
									
								}
							}
							
							try {
								m_Log.logMessage("Started " + cname);
								m_Log.logMessage("Command: " + cmd);
								m_Log.logMessage("Clustering: Started");

								
								// Build the model and output it.
								m_Log.statusMessage("Clusterer running...");
	
								StringBuffer outBuffer = new StringBuffer();
								
								// remove the class attribute (if set) and build the clusterer
								
								BuildSubspaceClustererThread clusterthread = new BuildSubspaceClustererThread(clusterer,removeClass(trainInst));
								start = System.currentTimeMillis();

								clusterthread.start();
								
								int timer  = Integer.parseInt(m_timer.getText());
								if(!m_EnableTimer.isSelected() || timer <= 0 || timer > 1000000000){
									timer = 0;
								}
								clusterthread.join(timer*60*1000);
								end = System.currentTimeMillis();
								if(clusterthread.isAlive()) {
									clusterthread.interrupt();
									clusterthread.stop();
									throw new Exception("Timeout after "+timer+" minutes");	
								}
								clusterthread.join();
								if(clusterthread.getException()!=null) {
									throw clusterthread.getException();
								}
								outBuffer.append(getClusterInformation(clusterer,inst,end-start));
								
								
								m_Log.logMessage("Clustering: done");
								
								//Evaluation stuff, catch Exceptions, most likely out of memory
								if(m_EnableEvaluation.isSelected()){
									try{
										if(inst.classIndex() >= 0){
											m_Log.statusMessage("Evaluation running...");
											m_Log.logMessage("Evaluation: Start");
											
											ArrayList<ClusterQualityMeasure> measures = m_evaluationPanel.getSelectedMeasures();
											ArrayList<Cluster> m_TrueClusters = m_evaluationPanel.getTrueClusters();
											
											//Run evaluation
											start = System.currentTimeMillis();
											StringBuffer qualBuffer = SubspaceClusterEvaluation.evaluateClustersQuality(clusterer, inst, measures, m_TrueClusters, m_evaluationPanel.getTrueClusterFile());
											end = System.currentTimeMillis();
											outBuffer.append(qualBuffer);
											outBuffer.append("\n\nCalculating Evaluation took: "+formatTimeString(end-start)+"\n");
											m_Log.logMessage("Evaluation: Finished");
										}
									}catch (Exception e) {
										errors = true;
										m_Log.logMessage(e.getMessage());
										m_Log.logMessage("Problem evaluating clustering (number of clusters: "+clusterer.getSubspaceClustering().size()+")");
										e.printStackTrace();
									}catch (OutOfMemoryError e) {
										errors = true;
										System.out.println("Out of memory");
										m_Log.logMessage(e.getMessage());
										m_Log.statusMessage("See error log");
									}
								}

								//Visual stuff, catch Exceptions, most likely out of memory
								m_CurrentVis = new SubspaceVisualData();
								if (!isInterrupted() && m_EnableStoreVisual.isSelected()) {
									try{
										m_Log.statusMessage("Calculating visualization...");
										m_Log.logMessage("Calculate visualization: Start");
										
										//calculate visual stuff
										start = System.currentTimeMillis();
										m_CurrentVis.calculateVisual((ArrayList<Cluster>)clusterer.getSubspaceClustering(), removeClass(trainInst));
										end = System.currentTimeMillis();
										//where is the name being used???
										m_CurrentVis.setName(name + " (" + inst.relationName()+ ")");
										m_CurrentVis.setHistoryName(parameter_name);
										outBuffer.append("Calculating visualization took: "+formatTimeString(end-start)+"\n");
										m_Log.logMessage("Calculate visualization: Finished");
									}catch (Exception e) {
										errors = true;
										e.printStackTrace();
										m_Log.logMessage(e.getMessage());
										m_Log.logMessage("Problem calculating visualization (number of clusters: "+clusterer.getSubspaceClustering().size()+")");
									}
									catch(OutOfMemoryError e){
										errors = true;
										System.out.println("Out of memory");
										m_Log.logMessage(e.getMessage());
										m_Log.statusMessage("See error log");
									}
								}
								//put buffer into cluster so it can be safed with the cluster
								clusterer.setConsole(outBuffer);
								m_Log.logMessage("Finished " + cmd);
								
								m_History.addResult(name, outBuffer);
								m_History.setSingle(name);
								m_History.updateResult(name);

								
							} catch (Exception ex) {
								errors = true;
								m_Log.logMessage(ex.getMessage());
								m_Log.statusMessage("Problem evaluating clusterer");
								ex.printStackTrace();
							}
							catch(OutOfMemoryError e){
								m_Log.logMessage(e.getMessage());
								m_Log.statusMessage("See error log");
								System.out.println("Out of memory");
								//e.printStackTrace();
							} finally {
								FastVector vv = new FastVector();
								vv.addElement(clusterer);
								Instances trainHeader = new Instances(m_Instances, 0);
								vv.addElement(trainHeader);
								if (ignoredAtts != null)
									vv.addElement(ignoredAtts);
								vv.addElement(m_CurrentVis);
	
								m_History.addObject(name, vv);
								if (isInterrupted()) {
									m_Log.logMessage("Bracketing interrupted:" + cname);
									m_Log.statusMessage("See error log");
								}
								if (m_Log instanceof TaskLogger) {
									((TaskLogger) m_Log).taskFinished();
								}
							}
						}
					} catch (Exception ex) {
						errors = true;
						ex.printStackTrace();
						m_Log.logMessage(ex.getMessage());
						m_Log.statusMessage("Problem setting up clusterer");
					} catch (OutOfMemoryError ex) {
						errors = true;
						System.out.println("Out of memory");
						m_Log.logMessage(ex.getMessage());
						m_Log.statusMessage("See error log");
					} 
					finally {

						m_RunThread = null;
						m_StartBut.setEnabled(true);
						m_StopBut.setEnabled(false);
						m_ignoreBut.setEnabled(true);
						
						//kill all other tasks in the logger so the poor bird can stop running
						//belongs somewhere else, but doesnt work in finally after for-bracketing anymore 
						int clustering_amount = 1;
						if(m_BracketingBut.isEnabled()){
							clustering_amount = m_bracketingPanel.getNumberClusterings();
						}
						for (int j = 0; j < clustering_amount; j++) {
							if (m_Log instanceof TaskLogger) {
								((TaskLogger) m_Log).taskFinished();
							}
						}
						
						if(errors){ 
							m_Log.statusMessage("Errors accured, see error logs");
							JOptionPane
								.showMessageDialog(SubspaceClustererPanel.this,
								"Problems occured during clusterig, check error log for more details",
								"Evaluate clusterer",
								JOptionPane.ERROR_MESSAGE);
						}
						else{ 
							m_Log.statusMessage("OK");
						}
					}
				}
			};
			m_RunThread.setPriority(Thread.MIN_PRIORITY);
			m_RunThread.start();
		}
	}
	

	// timer for clustering
	class BuildSubspaceClustererThread extends Thread {

		Exception e = null;

		SubspaceClusterer clusterer;
		Instances trainInst;

		public BuildSubspaceClustererThread(SubspaceClusterer clusterer, Instances trainInst) {
			this.clusterer = clusterer;
			this.trainInst = trainInst;
		}
		
		public Exception getException(){
			return e;
		}

		public void run() {
			try {
				clusterer.buildSubspaceClusterer(trainInst);
			} catch (Exception e) {
				this.e = e;
			}
		}
	}
	
	private StringBuffer getClusterInformation(SubspaceClusterer clusterer, Instances inst, long time){

		StringBuffer outBuff = new StringBuffer();

		String name = (new SimpleDateFormat("HH:mm:ss - ")).format(new Date());
		String cname = clusterer.getClass().getName();
		if (cname.startsWith("weka.subspaceClusterer.")) {
			name += cname.substring("weka.subspaceClusterer.".length());
		} else {
			name += cname;
		}

		// Output some header information
		outBuff.append("=== Run information ===\n\n");
		outBuff.append("Scheme:       " + cname);
		if (clusterer instanceof OptionHandler) {
			String[] o = ((OptionHandler) clusterer)
					.getOptions();
			outBuff.append(" " + Utils.joinOptions(o));
		}
		outBuff.append("\n");
		outBuff.append("Relation:     " + inst.relationName()
				+ '\n');
		outBuff.append("Instances:    " + inst.numInstances()
				+ '\n');
		outBuff.append("Attributes:   " + inst.numAttributes()
				+ '\n');
		if (inst.numAttributes() < 100) {
			boolean[] selected = new boolean[inst
					.numAttributes()];
			for (int i = 0; i < inst.numAttributes(); i++) {
				selected[i] = true;
			}
			if (!m_ignoreKeyList.isSelectionEmpty()) {
				int[] indices = m_ignoreKeyList
						.getSelectedIndices();
				for (int i = 0; i < indices.length; i++) {
					selected[indices[i]] = false;
				}
			}
			if (m_EnableClassesToClusters.isSelected()) {
				selected[m_ClassCombo.getSelectedIndex()] = false;
			}
			for (int i = 0; i < inst.numAttributes(); i++) {
				if (selected[i]) {
					outBuff.append("              "
							+ inst.attribute(i).name() + '\n');
				}
			}
			if (!m_ignoreKeyList.isSelectionEmpty()
					|| m_EnableClassesToClusters.isSelected()) {
				outBuff.append("Ignored:\n");
				for (int i = 0; i < inst.numAttributes(); i++) {
					if (!selected[i]) {
						outBuff.append("              "
								+ inst.attribute(i).name()
								+ '\n');
					}
				}
			}
		} else {
			outBuff.append("              [list of attributes omitted]\n");
		}

		outBuff.append("Classes to clusters evaluation on training data");
		outBuff.append("\n");

		//output clusters
		outBuff.append("Result: [relevant dimensions] [amount] {clustered objects} \n");
		outBuff.append(clusterer.toString());
		

		//unclustered instances
		int unclusteredInstances = 0;
		List<Integer> [] clusterAssignments;
		clusterer.calculateClusterAssignments(inst.numInstances());
		clusterAssignments=clusterer.getClusterAssignments();
		for (int j = 0; j < inst.numInstances(); j++) {
			if(clusterAssignments[j] == null){
				unclusteredInstances++;
			}
		}
		
		if (unclusteredInstances > 0)
			outBuff.append("\nUnclustered instances : "
					+ unclusteredInstances+"\n");
		
		//output time taken up
		outBuff.append("Clustering took: ");
		outBuff.append(formatTimeString(time));
		
		return outBuff;
	}
	
	private String formatTimeString(long time){
		StringBuffer outBuff = new StringBuffer();
		DecimalFormat format = new DecimalFormat();
		format.setMaximumFractionDigits(0);
		format.setMinimumIntegerDigits(2);
		
		int ms =(int) time;
		time/=1000;
		int h = (int)(time/3600);
		time-=(h*3600);
		int m = (int)(time/60);
		time-=(m*60);
		int s = (int)(time);
		
		outBuff.append(format.format(h)+"h ");
		outBuff.append(format.format(m)+"m ");
		outBuff.append(format.format(s)+"s (");
		outBuff.append((ms)+"ms) \n");
		
		return outBuff.toString();
	}
	
	
	private StringBuffer getEvaluationOutput(SubspaceClusterEvaluation eval){

		StringBuffer outBuff = new StringBuffer(); 
		outBuff.append("=== Model and evaluation on training set ===\n\n");

		outBuff.append(eval.clusterResultsToString());
	
		outBuff.append("\n");
		
		return outBuff;
	}

	
	
	private Instances removeClass(Instances inst) {
		Remove af = new Remove();
		Instances retI = null;

		try {
			if (inst.classIndex() < 0) {
				retI = inst;
			} else {
				af.setAttributeIndices("" + (inst.classIndex() + 1));
				af.setInvertSelection(false);
				af.setInputFormat(inst);
				retI = Filter.useFilter(inst, af);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retI;
	}

	private Instances removeIgnoreCols(Instances inst) {

		// If the user is doing classes to clusters evaluation and
		// they have opted to ignore the class, then unselect the class in
		// the ignore list
		if (m_EnableClassesToClusters.isSelected()) {
			int classIndex = m_ClassCombo.getSelectedIndex();
			if (m_ignoreKeyList.isSelectedIndex(classIndex)) {
				m_ignoreKeyList.removeSelectionInterval(classIndex, classIndex);
			}
		}
		int[] selected = m_ignoreKeyList.getSelectedIndices();
		Remove af = new Remove();
		Instances retI = null;

		try {
			af.setAttributeIndicesArray(selected);
			af.setInvertSelection(false);
			af.setInputFormat(inst);
			retI = Filter.useFilter(inst, af);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return retI;
	}


	/**
	 * Stops the currently running clusterer (if any).
	 */
	protected void stopClusterer() {

		if (m_RunThread != null) {
			m_RunThread.interrupt();

			// This is deprecated (and theoretically the interrupt should do).
			m_RunThread.stop();

		}
	}

	/**
	 * Pops up a visualize panel to display cluster assignments
	 * @param sp the visualize panel to display
	 */
	protected void visualizeClusterAssignments(SubspaceClusterer cluster, SubspaceVisualData sp, Instances instances) {
		if (sp != null) {
			new VisualClusteringFrame(cluster, sp,instances);
		}
	}

	protected void visualizeAllClusterAssignments(Instances instances) {
		//what a nice way to get the list size...
		int size = m_History.getList().getModel().getSize(); 
		
		final SubspaceVisualData svp [] =  new SubspaceVisualData[size];
		final SubspaceClusterer clusterer []= new SubspaceClusterer[size];
		
		FastVector rlist_item = null;
		for (int i = 0; i < size; i++) {
			rlist_item = (FastVector) m_History.getNamedObject(m_History.getNameAtIndex(i));
	
			SubspaceVisualData temp_svp = null;
			SubspaceClusterer temp_clusterer = null;
	
			if (rlist_item != null) {
				for (int j = 0; j < rlist_item.size(); j++) {
					Object temp = rlist_item.elementAt(j);
					if (temp instanceof SubspaceClusterer) {
						temp_clusterer = (SubspaceClusterer) temp;
					} else if (temp instanceof SubspaceVisualData) { // normal errors
						temp_svp = (SubspaceVisualData) temp;
					}
				}
			}
			svp[i]=temp_svp;
			clusterer[i]=temp_clusterer;
		}
		new VisualClusteringOverview(clusterer,svp,instances);
	}
	
	/**
	 * Handles constructing a popup menu with visualization options
	 * @param name the name of the result history list entry clicked on by
	 * the user
	 * @param x the x coordinate for popping up the menu
	 * @param y the y coordinate for popping up the menu
	 */
	protected void visualizeClusterer(String name, int x, int y) {
		final String selectedName = name;
		final JPopupMenu resultListMenu = new JPopupMenu();

		JMenuItem visMainBuffer = new JMenuItem("View in main window");
		if (selectedName != null) {
			visMainBuffer.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					m_History.setSingle(selectedName);
				}
			});
		} else {
			visMainBuffer.setEnabled(false);
		}
		resultListMenu.add(visMainBuffer);

		JMenuItem visSepBuffer = new JMenuItem("View in separate window");
		if (selectedName != null) {
			visSepBuffer.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					m_History.openFrame(selectedName);
				}
			});
		} else {
			visSepBuffer.setEnabled(false);
		}
		resultListMenu.add(visSepBuffer);

		JMenuItem saveOutput = new JMenuItem("Save result buffer");
		if (selectedName != null) {
			saveOutput.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveBuffer(selectedName);
				}
			});
		} else {
			saveOutput.setEnabled(false);
		}
		resultListMenu.add(saveOutput);

		JMenuItem saveAllOutputs = new JMenuItem("Save all result buffers");
			saveAllOutputs.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveAllBuffers();
				}
			});
		resultListMenu.add(saveAllOutputs);

		
		JMenuItem deleteOutput = new JMenuItem("Delete result");
		if (selectedName != null) {
			deleteOutput.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					m_History.removeResult(selectedName);
				}
			});
		} else {
			deleteOutput.setEnabled(false);
		}
		resultListMenu.add(deleteOutput);

		JMenuItem deleteAllOutput = new JMenuItem("Clear all results");
		deleteAllOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//TODO				
				//what a nice way to get the list size...
//				int size = m_History.getList().getModel().getSize();
//				FastVector rlist_item = null;
//				for (int i = 0; i < size; i++) {
//					m_History.removeResult(m_History.getNameAtIndex(i));
//				}

			}
		});
//		resultListMenu.add(deleteAllOutput);

		resultListMenu.addSeparator();

		JMenuItem loadModel = new JMenuItem("Load model");
		loadModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadClusterer();
			}
		});
		resultListMenu.add(loadModel);

		FastVector o = null;
		if (selectedName != null) {
			o = (FastVector) m_History.getNamedObject(selectedName);
		}

		SubspaceVisualData temp_svp = null;
		SubspaceClusterer temp_clusterer = null;
		Instances temp_trainHeader = null;
		int[] temp_ignoreAtts = null;

		if (o != null) {
			for (int i = 0; i < o.size(); i++) {
				Object temp = o.elementAt(i);
				if (temp instanceof SubspaceClusterer) {
					temp_clusterer = (SubspaceClusterer) temp;
				} else if (temp instanceof Instances) { // training header
					temp_trainHeader = (Instances) temp;
				} else if (temp instanceof int[]) { // ignored attributes
					temp_ignoreAtts = (int[]) temp;
				} else if (temp instanceof SubspaceVisualData) { // normal errors
					temp_svp = (SubspaceVisualData) temp;
				}
			}
		}

		final SubspaceVisualData svp = temp_svp;
		final SubspaceClusterer clusterer = temp_clusterer;
		final Instances trainHeader = temp_trainHeader;
		final int[] ignoreAtts = temp_ignoreAtts;

		JMenuItem saveModel = new JMenuItem("Save model");
		if (clusterer != null) {
			saveModel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveClusterer(selectedName, clusterer, svp, trainHeader,
							ignoreAtts);
				}
			});
		} else {
			saveModel.setEnabled(false);
		}
		resultListMenu.add(saveModel);

		resultListMenu.addSeparator();

		final JMenuItem delVisClusts = new JMenuItem("Delete visualization from model");
		final JMenuItem viewVisClusts = new JMenuItem("Show visualization");
		final JMenuItem calVisClusts = new JMenuItem("Calculate visualization");
		if (svp != null) {
			calVisClusts.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//calculate visual stuff
					svp.calculateVisual((ArrayList<Cluster>)clusterer.getSubspaceClustering(), removeClass(m_Instances));
					svp.setName(selectedName + " (" + m_Instances.relationName()+ ")");

					FastVector vv = (FastVector) m_History.getNamedObject(selectedName);
					if (vv != null) {
						vv.addElement(svp);
						m_History.addObject(selectedName, vv);
						calVisClusts.setEnabled(false);
						delVisClusts.setEnabled(true);
						viewVisClusts.setEnabled(true);
					}
				}
			});
		} else {
			calVisClusts.setEnabled(false);
		}
		resultListMenu.add(calVisClusts);
		
//TODO: somehow svp can still be read from m_History??? so disable for now
		
//		if (svp != null && svp.hasVisual()) {
//			delVisClusts.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//					FastVector o_tmp = (FastVector) m_History.getNamedObject(selectedName);
//					if (o_tmp != null) {
//						for (int i = 0; i < o_tmp.size(); i++) {
//							if (o_tmp.elementAt(i) instanceof SubspaceVisualData);
////							o_tmp.removeElementAt(i);
//							((FastVector)(m_History.getNamedObject(selectedName))).removeElementAt(i);
//							break;
//						}
//					}
//					calVisClusts.setEnabled(true);
//					delVisClusts.setEnabled(false);
//					viewVisClusts.setEnabled(false);
//				}
//			});
//
//		} else {
//			delVisClusts.setEnabled(false);
//		}
//		resultListMenu.add(delVisClusts);

		if (svp != null && svp.hasVisual()) {
			viewVisClusts.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					visualizeClusterAssignments(clusterer, svp, m_Instances);
				}
			});

		} else {
			viewVisClusts.setEnabled(false);
		}
		resultListMenu.add(viewVisClusts);

		//ALL section
		resultListMenu.addSeparator();
		final JMenuItem viewAllVisClusts = new JMenuItem("Show all visualizations");
		if (true) {
			viewAllVisClusts.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					visualizeAllClusterAssignments(m_Instances);
				}
			});

		} else {
			viewAllVisClusts.setEnabled(false);
		}
		resultListMenu.add(viewAllVisClusts);
		
		
		final JMenuItem saveAllModels = new JMenuItem("Save all Models");
		if (false) {
//			saveAllModels.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//					//TODO
//				}
//			});

		} else {
			saveAllModels.setEnabled(false);
		}
		resultListMenu.add(saveAllModels);

		resultListMenu.show(m_History.getList(), x, y);
	}

	/**
	 * Save the currently selected clusterer output to a file.
	 * @param name the name of the buffer to save
	 */
	protected void saveBuffer(String name) {
		StringBuffer sb = m_History.getNamedBuffer(name);
		if (sb != null) {
			if (m_SaveOut.save(sb)) {
				m_Log.logMessage("Save successful.");
			}
		}
	}

	/**
	 * Save the currently selected clusterer output to a file.
	 * @param name the name of the buffer to save
	 */
	protected void saveAllBuffers() {

		String save_dir = "";
		
	    JFileChooser fileChooser =  new JFileChooser(new File(System.getProperty("user.dir")));
	    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	    int state = fileChooser.showDialog(null, "Select Directory");
        if (state == JFileChooser.APPROVE_OPTION ){
        	if(fileChooser.getSelectedFile().isDirectory())
        		save_dir = fileChooser.getSelectedFile().getPath();
        	if(fileChooser.getSelectedFile().isFile())
        		save_dir = fileChooser.getSelectedFile().getParent();
        }
		//what a nice way to get the list size...
		int size = m_History.getList().getModel().getSize();
		FastVector rlist_item = null;
		for (int i = 0; i < size; i++) {
			String name = m_History.getNameAtIndex(i);
			StringBuffer buf = m_History.getNamedBuffer(name);
		    try {
		      String file = save_dir+"\\"+name.substring(11).replace("--> ", "")+".txt";
		      if (m_Log != null) {
		    		  m_Log.statusMessage("Saving to file...");
		      }
		      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
		      out.write(buf.toString(),0,buf.toString().length());
		      out.close();
		      if (m_Log != null) {
		    	  m_Log.statusMessage("OK");
		      }
		    } catch (Exception ex) {
		      ex.printStackTrace();
		      if (m_Log != null) {
		    	  m_Log.logMessage(ex.getMessage());
		      }
		    }
		}
	    m_Log.logMessage("Saved all result buffers successfully.");
	}

	
	
	private void setIgnoreColumns() {
		ListSelectorDialog jd = new ListSelectorDialog(null, m_ignoreKeyList);

		// Open the dialog
		int result = jd.showDialog();

		if (result != ListSelectorDialog.APPROVE_OPTION) {
			// clear selected indices
			m_ignoreKeyList.clearSelection();
		}
	}

	/**
	 * Saves the currently selected clusterer
	 */
	protected void saveClusterer(String name, SubspaceClusterer clusterer,
			SubspaceVisualData svp, Instances trainHeader, int[] ignoredAtts) {

		File sFile = null;
		boolean saveOK = true;

		int returnVal = m_FileChooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			sFile = m_FileChooser.getSelectedFile();
			if (!sFile.getName().toLowerCase().endsWith(MODEL_FILE_EXTENSION)) {
				sFile = new File(sFile.getParent(), sFile.getName()
						+ MODEL_FILE_EXTENSION);
			}
			m_Log.statusMessage("Saving model to file...");

			try {
				OutputStream os = new FileOutputStream(sFile);
				if (sFile.getName().endsWith(".gz")) {
					os = new GZIPOutputStream(os);
				}
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
				objectOutputStream.writeObject(clusterer);
				if (trainHeader != null)
					objectOutputStream.writeObject(trainHeader);
				if (ignoredAtts != null)
					objectOutputStream.writeObject(ignoredAtts);
				if (svp != null && svp.hasVisual())
					objectOutputStream.writeObject(svp);
				objectOutputStream.flush();
				objectOutputStream.close();
			} catch (Exception e) {

				JOptionPane.showMessageDialog(null, e, "Save Failed",
						JOptionPane.ERROR_MESSAGE);
				saveOK = false;
			}
			if (saveOK)
				m_Log.logMessage("Saved model (" + name + ") to file '"
						+ sFile.getName() + "'");
			m_Log.statusMessage("OK");
		}
	}

	/**
	 * Loads a clusterer
	 */
	protected void loadClusterer() {

		int returnVal = m_FileChooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File[] selected = m_FileChooser.getSelectedFiles();
			m_Log.statusMessage("Loading model from file...");
			
			//iterate over selected files
			for (int i = 0; i < selected.length; i++) {
				SubspaceClusterer clusterer = null;
				Instances trainHeader = null;
				int[] ignoredAtts = null;
				SubspaceVisualData svp = null;
	
				
	
				try {
					InputStream is = new FileInputStream(selected[i]);
					if (selected[i].getName().endsWith(".gz")) {
						is = new GZIPInputStream(is);
					}
					ObjectInputStream objectInputStream = new ObjectInputStream(is);
					clusterer = (SubspaceClusterer) objectInputStream.readObject();
					try { // see if we can load the header & ignored attribute info
						trainHeader = (Instances) objectInputStream.readObject();
						ignoredAtts = (int[]) objectInputStream.readObject();
					} catch (Exception e) {
					} // don't fuss if we can't
					try { // see if we can load the visualization
						svp = (SubspaceVisualData) objectInputStream.readObject();
					} catch (Exception e) {
					} // don't fuss if we can't
	
					objectInputStream.close();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, e, "Load Failed",
							JOptionPane.ERROR_MESSAGE);
				}
	
				
	
				if (clusterer != null) {
					m_Log.logMessage("Loaded model from file '"+ selected[i].getName() + "'");
					String name = (new SimpleDateFormat("HH:mm:ss - ")).format(new Date());
					String cname = clusterer.getClass().getName();
					if (cname.startsWith("weka.subspaceClusterer."))
						cname = cname.substring("weka.subspaceClusterer.".length());
					name += cname;
					StringBuffer outBuff = clusterer.getConsole();
	
					FastVector vv = new FastVector();
					vv.addElement(clusterer);
					if (trainHeader != null)
						vv.addElement(trainHeader);
					if (ignoredAtts != null)
						vv.addElement(ignoredAtts);
					//add loaded visual or at least empty container
					if (svp == null){
						svp = new SubspaceVisualData();
					}
					else{
						name += svp.getHistoryName();	
					}
					
					vv.addElement(svp);
					m_History.addResult(name, outBuff);
					m_History.setSingle(name);
					m_History.addObject(name, vv);
				}
			}
			m_Log.statusMessage("OK");
		}
	}

	//TODO
	/**
	 * Re-evaluates the named clusterer with the current test set. Unpredictable
	 * things will happen if the data set is not compatible with the clusterer.
	 *
	 * @param name the name of the clusterer entry
	 * @param clusterer the clusterer to evaluate
	 * @param trainHeader the header of the training set
	 * @param ignoredAtts ignored attributes
	 */
	/*
	 protected void reevaluateModel(final String name,
	 final SubspaceClusterer clusterer, final Instances trainHeader,
	 final int[] ignoredAtts) {

	 if (m_RunThread == null) {
	 m_StartBut.setEnabled(false);
	 m_StopBut.setEnabled(true);
	 m_ignoreBut.setEnabled(false);
	 m_RunThread = new Thread() {
	 public void run() {
	 // Copy the current state of things
	 m_Log.statusMessage("Setting up...");

	 StringBuffer outBuff = m_History.getNamedBuffer(name);
	 Instances userTest = null;

	 PlotData2D predData = null;
	 if (m_TestInstances != null) {
	 userTest = new Instances(m_TestInstances);
	 }

	 boolean saveVis = m_StorePredictionsBut.isSelected();
	 String grph = null;

	 try {
	 if (userTest == null) {
	 throw new Exception(
	 "No user test set has been opened");
	 }
	 if (trainHeader != null
	 && !trainHeader.equalHeaders(userTest)) {
	 throw new Exception(
	 "Train and test set are not compatible");
	 }

	 m_Log.statusMessage("Evaluating on test data...");
	 m_Log.logMessage("Re-evaluating clusterer (" + name
	 + ") on test set");

	 m_Log.logMessage("Started reevaluate model");
	 if (m_Log instanceof TaskLogger) {
	 ((TaskLogger) m_Log).taskStarted();
	 }
	 SubspaceClusterEvaluation eval = new SubspaceClusterEvaluation();
	 eval.setClusterer(clusterer);

	 Instances userTestT = new Instances(userTest);
	 if (ignoredAtts != null) {
	 userTestT = removeIgnoreCols(userTestT, ignoredAtts);
	 }

	 eval.evaluateClusterer(userTestT);

	 predData = setUpVisualizableInstances(userTest, eval);

	 outBuff
	 .append("\n=== Re-evaluation on test set ===\n\n");
	 outBuff.append("User supplied test set\n");
	 outBuff.append("Relation:     "
	 + userTest.relationName() + '\n');
	 outBuff.append("Instances:    "
	 + userTest.numInstances() + '\n');
	 outBuff.append("Attributes:   "
	 + userTest.numAttributes() + "\n\n");
	 if (trainHeader == null)
	 outBuff
	 .append("NOTE - if test set is not compatible then results are "
	 + "unpredictable\n\n");

	 outBuff.append(eval.clusterResultsToString());
	 outBuff.append("\n");
	 m_History.updateResult(name);
	 m_Log.logMessage("Finished re-evaluation");
	 m_Log.statusMessage("OK");
	 } catch (Exception ex) {
	 ex.printStackTrace();
	 m_Log.logMessage(ex.getMessage());
	 JOptionPane
	 .showMessageDialog(SubspaceClustererPanel.this,
	 "Problem evaluating clusterer:\n"
	 + ex.getMessage(),
	 "Evaluate clusterer",
	 JOptionPane.ERROR_MESSAGE);
	 m_Log.statusMessage("Problem evaluating clusterer");

	 } finally {
	 if (predData != null) {
	 m_CurrentVis = new VisualizePanel();
	 m_CurrentVis.setName(name + " ("
	 + userTest.relationName() + ")");
	 m_CurrentVis.setLog(m_Log);
	 predData.setPlotName(name + " ("
	 + userTest.relationName() + ")");

	 try {
	 m_CurrentVis.addPlot(predData);
	 } catch (Exception ex) {
	 System.err.println(ex);
	 }

	 FastVector vv = new FastVector();
	 vv.addElement(clusterer);
	 if (trainHeader != null)
	 vv.addElement(trainHeader);
	 if (ignoredAtts != null)
	 vv.addElement(ignoredAtts);
	 if (saveVis) {
	 vv.addElement(m_CurrentVis);
	 if (grph != null) {
	 vv.addElement(grph);
	 }

	 }
	 m_History.addObject(name, vv);

	 }
	 if (isInterrupted()) {
	 m_Log.logMessage("Interrupted reevaluate model");
	 m_Log.statusMessage("See error log");
	 }
	 m_RunThread = null;
	 m_StartBut.setEnabled(true);
	 m_StopBut.setEnabled(false);
	 m_ignoreBut.setEnabled(true);
	 if (m_Log instanceof TaskLogger) {
	 ((TaskLogger) m_Log).taskFinished();
	 }
	 }
	 }

	 };
	 m_RunThread.setPriority(Thread.MIN_PRIORITY);
	 m_RunThread.start();
	 }
	 }
	 */
	

	/**
	 * updates the capabilities filter of the GOE
	 * 
	 * @param filter	the new filter to use
	 */
	protected void updateCapabilitiesFilter(Capabilities filter) {
		if (filter == null) {
			m_ClustererEditor.setCapabilitiesFilter(new Capabilities(null));
			return;
		}

		// clusterer doesn't need the class attribute, so we can skip that here

		m_ClustererEditor.setCapabilitiesFilter(filter);
	}
  
  /**
   * method gets called in case of a change event.
   * 
   * @param e		the associated change event
   */
  public void capabilitiesFilterChanged(CapabilitiesFilterChangeEvent e) {
    if (e.getFilter() == null)
      updateCapabilitiesFilter(null);
    else
      updateCapabilitiesFilter((Capabilities) e.getFilter().clone());
  }

	/**
	 * Tests out the clusterer panel from the command line.
	 *
	 * @param args may optionally contain the name of a dataset to load.
	 */
	public static void main(String[] args) {

		try {
			final javax.swing.JFrame jf = new javax.swing.JFrame(
					"Weka Explorer: Cluster");
			jf.getContentPane().setLayout(new BorderLayout());
			final SubspaceClustererPanel sp = new SubspaceClustererPanel();
			jf.getContentPane().add(sp, BorderLayout.CENTER);
			weka.gui.LogPanel lp = new weka.gui.LogPanel();
			sp.setLog(lp);
			jf.getContentPane().add(lp, BorderLayout.SOUTH);
			jf.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					jf.dispose();
					System.exit(0);
				}
			});
			jf.pack();
			jf.setSize(800, 600);
			jf.setVisible(true);
			if (args.length == 1) {
				System.err.println("Loading instances from " + args[0]);
				java.io.Reader r = new java.io.BufferedReader(
						new java.io.FileReader(args[0]));
				Instances i = new Instances(r);
				sp.setInstances(i);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println(ex.getMessage());
		}
	}

	  /**
	   * Sets the Explorer to use as parent frame (used for sending notifications
	   * about changes in the data).
	   * 
	   * @param parent	the parent frame
	   */
	  public void setExplorer(Explorer parent) {
	    m_Explorer = parent;
	  }
	  
	  /**
	   * returns the parent Explorer frame.
	   * 
	   * @return		the parent
	   */
	  public Explorer getExplorer() {
	    return m_Explorer;
	  }
	  
	  /**
	   * Returns the title for the tab in the Explorer.
	   * 
	   * @return 		the title of this tab
	   */
	  public String getTabTitle() {
	    return "SubspaceClusterer";
	  }
	  
	  /**
	   * Returns the tooltip for the tab in the Explorer.
	   * 
	   * @return 		the tooltip of this tab
	   */
	  public String getTabTitleToolTip() {
	    return "Perform SubspaceClusterings";
	  }	
	
}
