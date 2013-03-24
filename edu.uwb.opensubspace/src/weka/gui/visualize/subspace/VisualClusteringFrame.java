package weka.gui.visualize.subspace;


import weka.core.Instances;
import weka.subspaceClusterer.SubspaceClusterer;
import javax.swing.JFrame;
import java.awt.Dimension;


public class VisualClusteringFrame extends JFrame{

	private static final long serialVersionUID = 1L;
	private VisualClusteringPanel visualTabbedPane = null;
	

	public VisualClusteringFrame(SubspaceClusterer clusterer, SubspaceVisualData svp, Instances instances) {
		super();
//		try {
//			javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
//		} catch(Exception e) {}

		
		this.setSize(new Dimension(808, 634));

		final JFrame f = this;
		this.addWindowListener(new java.awt.event.WindowAdapter() {
	    	  public void windowClosing(java.awt.event.WindowEvent e) {
	    		  f.dispose();
	    	  }
		});
		this.setLocationRelativeTo(null);
        this.setTitle("Cluster Visualization");
        visualTabbedPane = new VisualClusteringPanel(clusterer,svp,instances);
        this.setContentPane(visualTabbedPane);
        this.pack();
        this.setVisible(true);
		visualTabbedPane.repaintMDS2D();
	}

} 
