package weka.gui.visualize.subspace;

import weka.gui.visualize.subspace.distance2d.Distance2DPanel;
import weka.subspaceClusterer.SubspaceClusterer;
import weka.core.Instances;
import i9.subspace.base.Cluster;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import javax.swing.JSplitPane;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.JScrollPane;



public class VisualClusteringOverview extends JFrame implements PropertyChangeListener{

	private static final long serialVersionUID = 1L;
	private JSplitPane splitMain = null;
	private JSplitPane splitPlots = null;
	private VisualClusteringPanel leftdetailPanel = null;
	private VisualClusteringPanel rightdetailPanel = null;
	private JPanel overviewPanel = null;
	private JScrollPane jScrollPane = null;
	private JPanel contentPanel = null;

	private ArrayList<Distance2DPanel> overviewPanels;
	private int selectedLeftPlot;
	private int selectedRightPlot;
	
	private SubspaceClusterer[] m_clusterer;
	private SubspaceVisualData[] m_svp;
	private Instances m_instances;
	

	public VisualClusteringOverview(SubspaceClusterer[] clusterer, SubspaceVisualData[] svp, Instances instances) {
		super();
		m_clusterer = clusterer;
		m_svp = svp;
		m_instances = instances;
		

		initialize();
	}

	private void setOverviewPanel(){
		overviewPanel.removeAll();
		overviewPanels = new ArrayList<Distance2DPanel>();
		int col_max = 0;
		int col_min = Integer.MAX_VALUE;
		for (int i = 0; i < m_clusterer.length; i++) {
			if (m_clusterer[i]!=null && m_clusterer[i].getSubspaceClustering()!= null && m_svp[i]!=null && m_svp[i].hasVisual()) {
				final Distance2DPanel panel2D = new Distance2DPanel(i,150,m_svp[i].getMDS2D(), (ArrayList<Cluster>)m_clusterer[i].getSubspaceClustering(),"",m_instances);
				if(!panel2D.isEmpty()){
					final int id = i;
					panel2D.addMouseListener(new java.awt.event.MouseAdapter(){
						public void mouseClicked(java.awt.event.MouseEvent e){
							if(e.getModifiers() == java.awt.event.MouseEvent.BUTTON1_MASK){
								selectedLeftPlot = id;
								firePropertyChange("plotchange_left", 0, 1);
								
							}
							if(e.getModifiers() == java.awt.event.MouseEvent.BUTTON3_MASK){
								selectedRightPlot = id;
								firePropertyChange("plotchange_right", 0, 1);
							}
						}
					});
					if(panel2D.getMaxColor()>col_max) col_max = panel2D.getMaxColor();
					if(panel2D.getMinColor()<col_min) col_min = panel2D.getMinColor();
					overviewPanels.add(panel2D);
				}
			}
		}
		for (int i = 0; i < overviewPanels.size(); i++) {
			overviewPanels.get(i).setColorSpektrum(col_min, col_max);
			overviewPanel.add(overviewPanels.get(i));
		}
	}
	
	private void updateOverviewPanels(){
		if(overviewPanel!=null){
			int height = splitMain.getDividerLocation();
			overviewPanel.setSize(new Dimension((height-15)*overviewPanels.size(),height-20));
			overviewPanel.setPreferredSize(new Dimension((height-15)*overviewPanels.size(),height-20));
			for (int i = 0; i < overviewPanels.size(); i++) {
				overviewPanels.get(i).setFullSizeOverview(height-20);
				overviewPanels.get(i).repaint();
			}
			
			overviewPanel.repaint();
		}
	}

	private void initialize() {
		this.setSize(new Dimension(600, 600));
		final JFrame f = this;
		this.addWindowListener(new java.awt.event.WindowAdapter() {
	    	  public void windowClosing(java.awt.event.WindowEvent e) {
	    		  f.dispose();
	    	  }
		});
		this.setLocationRelativeTo(null);
        this.setTitle("Cluster Visualization Overview");
        this.setContentPane(getContentPanel());
        this.addPropertyChangeListener(this);
        this.pack();
        this.setVisible(true);
        
	}
	
	private JPanel getContentPanel() {
		if (contentPanel == null) {
			contentPanel = new JPanel();
			contentPanel.setLayout(new GridBagLayout());
			
			overviewPanel = getoverviewPanel();
			jScrollPane = new JScrollPane(overviewPanel);
			jScrollPane.getHorizontalScrollBar().setVisible(true);
			jScrollPane.getVerticalScrollBar().setVisible(false);
			
	        splitPlots = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	        splitPlots.setLeftComponent(getleftdetailPanel());
	        splitPlots.setRightComponent(getrightdetailPanel());
	        
	        splitPlots.setResizeWeight(0.5); 
			splitPlots.setOneTouchExpandable(true);
	        splitMain = new JSplitPane(JSplitPane.VERTICAL_SPLIT,jScrollPane,splitPlots);
			splitMain.setOneTouchExpandable(true);
			
	        GridBagConstraints gb1 = new GridBagConstraints();
			gb1.fill = GridBagConstraints.BOTH;
			gb1.weightx = 1;
			gb1.weighty = 1;
			gb1.gridx = 0;
			gb1.gridy = 0;
	        
			contentPanel.add(splitMain, gb1);
			
			splitPlots.setDividerSize(6); 
			splitMain.setDividerLocation(160);
			splitMain.setDividerSize(6);
			splitMain.addPropertyChangeListener(this);
			
			setOverviewPanel();
		}
		return contentPanel;
	}

	
	@Override
	public void repaint() {
		super.repaint();
		if(leftdetailPanel!=null){
			leftdetailPanel.repaint();
		}
		if(rightdetailPanel!=null){
			rightdetailPanel.repaint();
		}
		updateOverviewPanels();
	}
	
	private JPanel getoverviewPanel() {
		if (overviewPanel == null) {
			overviewPanel = new JPanel();
			FlowLayout fl = new FlowLayout();
			fl.setAlignment(FlowLayout.LEFT);
			fl.setVgap(0);
			fl.setHgap(1);
			overviewPanel.setLayout(fl);
		}
		return overviewPanel;
	}


	private VisualClusteringPanel getleftdetailPanel() {
		if (leftdetailPanel == null) {
			leftdetailPanel = new VisualClusteringPanel();
			leftdetailPanel.setLayout(new GridBagLayout());
		}
		return leftdetailPanel;
	}
	
	private VisualClusteringPanel getrightdetailPanel() {
		if (rightdetailPanel == null) {
			rightdetailPanel = new VisualClusteringPanel();
			rightdetailPanel.setLayout(new GridBagLayout());
		}
		return rightdetailPanel;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("plotchange_left")){
			leftdetailPanel = new VisualClusteringPanel(m_clusterer[selectedLeftPlot],m_svp[selectedLeftPlot],m_instances);
	        splitPlots.setLeftComponent(leftdetailPanel);
		}
		if(evt.getPropertyName().equals("plotchange_right")){
			rightdetailPanel = new VisualClusteringPanel(m_clusterer[selectedRightPlot],m_svp[selectedRightPlot],m_instances);
	        splitPlots.setRightComponent(rightdetailPanel);
		}
		if(evt.getPropertyName().equals("dividerLocation")){
			updateOverviewPanels();
		}
	}
	
}
