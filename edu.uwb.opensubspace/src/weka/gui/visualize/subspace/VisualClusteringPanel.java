package weka.gui.visualize.subspace;

import i9.subspace.base.Cluster;

import weka.core.Instances;
import weka.gui.visualize.subspace.distance3d.Distance3DTab;
import weka.gui.visualize.subspace.inDepth.InDepthGUI;
import weka.gui.visualize.subspace.distance2d.Distance2DGUI;
import weka.subspaceClusterer.SubspaceClusterer;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class VisualClusteringPanel extends JTabbedPane{
	private static final long serialVersionUID = 1L;
	private Distance2DGUI MDS2dPanel = null;
	private Distance3DTab MDS3dPanel = null;
	private InDepthGUI RankingPanel = null;
	private Instances instances = null;
	private ArrayList<Cluster> clustering = null;
	private SubspaceVisualData svp = null;
	
	/**
	 * This method initializes 
	 * 
	 */
	public VisualClusteringPanel() {
		super();
//		try {
//			javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
//		} catch(Exception e) {}
		this.setPreferredSize(new Dimension(400, 400));
		this.setVisible(true);
	}
	
	
	public VisualClusteringPanel(SubspaceClusterer clusterer, SubspaceVisualData _svp, Instances _instances) {
		super();
		try {
			javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch(Exception e) {}

		instances = _instances;
		svp = _svp;
		clustering = (ArrayList<Cluster>)clusterer.getSubspaceClustering();

		if(svp==null || !svp.hasVisual()){
				System.out.println("No visualization has been calculated ");
				return;
		}
		this.setPreferredSize(new Dimension(400, 400));
		initialize();
		this.setVisible(true);
	}


	private void initialize() {
		this.setPreferredSize(new Dimension(400, 400));
		this.addTab("Cluster Overview", null, getMDS2dPanel(), null);
		this.addTab("3D-Browsing", null, getMDS3dPanel(), null);
		this.addTab("Object Details", null, getRankingPanel(), null);
		this.setSelectedIndex(0);
		this.addChangeListener(new ChangeListener() {
		        public void stateChanged(ChangeEvent evt) {
		            JTabbedPane pane = (JTabbedPane)evt.getSource();
		            int sel = pane.getSelectedIndex();

		            if(MDS2dPanel!=null)MDS2dPanel.getDistancePanel().closeFrames();
		            if(MDS3dPanel!=null)MDS2dPanel.getDistancePanel().closeFrames();
		            if(sel == 1){

		            	double min = MDS2dPanel.getDistancePanel().getMin_radius_threshold();
		            	double max = MDS2dPanel.getDistancePanel().getMax_radius_threshold();
		            	Color [] spektrum = MDS2dPanel.getDistancePanel().getColorspektrumFor3D();
		            	MDS3dPanel.plot(clustering,svp.getMDS3D(),min,max,spektrum);
		            	MDS3dPanel.repaint();
		            }
		            if(sel == 2){
		            	RankingPanel.plotRanking(svp.getInDepth());
		            }
		        }
		    });
	}

	public void repaintMDS2D(){
		MDS2dPanel.repaint();
	}

	private JPanel getMDS2dPanel() {
		if (MDS2dPanel == null) {
			String parameter = svp.getHistoryName();
			if(parameter.length() > 4)
				parameter = svp.getHistoryName().substring(4);
			
			MDS2dPanel = new Distance2DGUI(svp.getMDS2D(),clustering, parameter, instances);
		}
		return MDS2dPanel;
	}

	private Distance3DTab getMDS3dPanel() {
		if (MDS3dPanel == null) {

			MDS3dPanel = new Distance3DTab(svp,instances);
			MDS3dPanel.setLayout(new GridBagLayout());
			MDS3dPanel.setVisible(true);
		}
		return MDS3dPanel;
	}

	
	private InDepthGUI getRankingPanel() {
		if (RankingPanel == null) {
			RankingPanel = new InDepthGUI(instances);
		}
		return RankingPanel;
	}

	
}
