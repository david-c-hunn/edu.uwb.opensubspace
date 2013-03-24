package weka.gui.visualize.subspace.distance2d;

import i9.subspace.base.Cluster;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.util.List;

import javax.swing.JFrame;

import weka.core.Instance;
import weka.core.Instances;


public class PendigitsClusterBox extends JFrame{
	private static final long serialVersionUID = 1L;
	private int[][] x_values;
	private int[][] y_values;
	private int[] x_mean;
	private int[] y_mean;
	
	public static int faktor = 5;
	
	private boolean[] subspace;
	private Instances instances;

	public PendigitsClusterBox(Instances instances) {
		super();
		this.instances = instances; 
	}
	
	
	public void drawCluster(int cluster_id, Cluster cluster){
		this.setTitle("Pendigits plot of cluster "+cluster_id);
		
		List<Integer> objects = cluster.m_objects;
		subspace = cluster.m_subspace;
		int dim = subspace.length/2;
		

		x_values = new int[objects.size()][dim];
		y_values = new int[objects.size()][dim];
		x_mean = new int [dim];
		y_mean = new int [dim];
		
		for (int i = 0; i < dim; i++) {
			x_mean[i]=0;
			y_mean[i]=0;
		}

		for(int i = 0; i < objects.size();i++){
			Instance inst = instances.instance(objects.get(i));
			double x = 0;
			double y = 0;
			for (int j = 0; j < dim; j++) {
					x = inst.value(2*j);
					x_values[i][j] = faktor * (int) x;
					x_mean[j]+= (faktor * (int) x);

					y = inst.value(2*j+1);
					y_values[i][j] = (faktor*100 - (int) y * faktor);
					y_mean[j]+= (faktor*100 - (int) y * faktor);
			}
		}
		
		for (int i = 0; i < dim; i++) {
			x_mean[i]/=objects.size();
			y_mean[i]/=objects.size();
		}
		
		getContentPane().removeAll();
		Panel drawpanel = new DigitPanel(x_values, y_values, x_mean, y_mean);
		getContentPane().add(drawpanel, BorderLayout.CENTER);
		setSize(faktor*100, faktor*100+30);
		setVisible(true);
		repaint();

	}
	
}

class DigitPanel extends Panel {
	private static final long serialVersionUID = 1L;
	private int[][] x;
	private int[][] y;
	private int[] x_m;
	private int[] y_m;

	public DigitPanel(int[][] x, int[][] y, int[] x_m, int[] y_m) {
		this.x=x;
		this.y=y;
		this.x_m=x_m;
		this.y_m=y_m;
	}

	public void paint(Graphics g) {
	    Graphics2D g2d = (Graphics2D)g;
	    g2d.setStroke(new BasicStroke(0.5f));
	    
	    //only draw 50 digits
	    int incr = x.length/100;
	    if(incr==0)incr = 1;
		for (int i = 0; i < x.length; i=i+incr) {
			g2d.drawPolyline(x[i], y[i], x[i].length);
			
		}
		g2d.setColor(Color.RED);
		g2d.setStroke(new BasicStroke(3));
		g2d.drawPolyline(x_m, y_m, x_m.length);
		
	}
}

