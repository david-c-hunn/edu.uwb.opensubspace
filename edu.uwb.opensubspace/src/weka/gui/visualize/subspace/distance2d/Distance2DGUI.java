package weka.gui.visualize.subspace.distance2d;

import i9.subspace.base.Cluster;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JSlider;

import weka.core.Instances;

public class Distance2DGUI extends JPanel {
	private static final long serialVersionUID = 1L;
	private Distance2DPanel distancePanel = null;
	private JSlider minSlider = null;
	private JSlider maxSlider = null;

	public Distance2DGUI() {
		super();
	}

	public Distance2DGUI(CirclePanel[] circles, ArrayList<Cluster> clustering, String parameter, Instances instances) {
		setDistance2DGUI(circles,clustering,parameter,instances);
	}

	
	public void setDistance2DGUI(CirclePanel[] circles, ArrayList<Cluster> clustering, String parameter, Instances _instances) {

		distancePanel = new Distance2DPanel(0,0, circles, clustering, parameter, _instances);
		distancePanel.setIsOverview(false);
		distancePanel.setColorLegend();
		if(distancePanel.isPendigits())
			distancePanel.initPenBox();
		
		setLayout(new GridBagLayout());
		GridBagConstraints gb = new GridBagConstraints();
		gb.gridx = 0;
		gb.gridy = 0;
		gb.fill = GridBagConstraints.VERTICAL;
		gb.weightx=0;
		gb.weighty=1;
		add(getMinSlider(),gb);

		GridBagConstraints gb3 = new GridBagConstraints();
		gb3.gridx = 2;
		gb3.gridy = 0;
		gb3.fill = GridBagConstraints.VERTICAL;
		gb3.weightx=0;
		gb3.weighty=1;
		add(getMaxSlider(),gb3);

		GridBagConstraints gb2 = new GridBagConstraints();
		gb2.gridx = 1;
		gb2.gridy = 0;
		gb2.fill = GridBagConstraints.BOTH;
		gb2.anchor = GridBagConstraints.SOUTH;
		gb2.weightx=1;
		gb2.weighty=1;
		add(distancePanel,gb2);

	}
	
	public Distance2DPanel getDistancePanel() {
		return distancePanel;
	}
	
	private JSlider getMinSlider() {
		if (minSlider == null) {
			minSlider = new JSlider();
			minSlider.setOrientation(JSlider.VERTICAL);
			minSlider.setValue(0);
			minSlider.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
				public void mouseDragged(java.awt.event.MouseEvent e) {
					distancePanel.setMin_radius_threshold(minSlider.getValue()/100.0);
					distancePanel.repaint();
				}
			});
		}
		return minSlider;
	}

	private JSlider getMaxSlider() {
		if (maxSlider == null) {
			maxSlider = new JSlider();
			maxSlider.setOrientation(JSlider.VERTICAL);
			maxSlider.setValue(100);
			maxSlider.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
				public void mouseDragged(java.awt.event.MouseEvent e) {
					distancePanel.setMax_radius_threshold(maxSlider.getValue()/100.0);
					distancePanel.repaint();
				}
			});
		}
		return maxSlider;
	}
	

	public void clearPlot(){
		if(distancePanel!=null){
			remove(distancePanel);
		}
	}
	
	@Override
	public void repaint() {
		super.repaint();
		if(distancePanel!=null)
			distancePanel.repaint();
		
	}

}
