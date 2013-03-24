package weka.gui.visualize.subspace.distance3d;

import weka.gui.visualize.subspace.SubspaceVisualData;
import i9.subspace.base.Cluster;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import javax.swing.JPanel;

import weka.core.Instances;


public class Distance3DTab extends JPanel{
	private static final long serialVersionUID = 1L;
	Distance3D applet = null;
	private  Instances instances;
	
	public Distance3DTab(SubspaceVisualData svp, Instances _instances) {
		super();
		instances = _instances;
	}
	
	public void plot(ArrayList<Cluster> clustering, Kugel [] spheres, double min_r, double max_r, Color[] spektrum){
		removeAll();
		try{
			if(spheres!=null){
				Plot3D frame_3D = new Plot3D(spheres, clustering, instances, 400, 300,min_r,max_r,spektrum);
				applet = (Distance3D)frame_3D.getApplet();
				add(applet);
			}
		}catch (Exception e) {
			System.out.println("Couldn't plot 3D");
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void repaint() {
		super.repaint();
		if (applet!=null) {
			applet.setSize(this.getWidth(),this.getHeight());
			applet.setPreferredSize(new Dimension(this.getWidth(), this.getHeight()));
		}
		
	}

	public void focusGained(FocusEvent arg0) {
	}
	
	public void closeFrames(){
		if(applet!=null)
			applet.closeFrames();
	}

	
	
	
}
