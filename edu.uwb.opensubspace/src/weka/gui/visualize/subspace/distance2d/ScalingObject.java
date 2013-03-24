package weka.gui.visualize.subspace.distance2d;

import java.awt.Color;
import java.io.Serializable;

public class ScalingObject implements Serializable{
	private static final long serialVersionUID = 1L;
	public final int border = 15;
	public final double refMaxRadiusSize = 0.1;

	private int width;
	public double r_min, r_max, x_min, x_max, y_min, y_max;
	private int col_range_min, col_range_max;
	private Color[] colorspektrum = null;
	
	
	public ScalingObject(int _width){
		width = _width;
	}
	
	public ScalingObject() {
		width = 100;
	}
	
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}

	public Color getColor(int col_id){
	if(colorspektrum == null) System.out.println("no colorspektrum set");
		
	  return colorspektrum[col_id];
	}

	public void setMinMaxValues(double r_min, double r_max, double x_min, double x_max, double y_min, double y_max){
		this.r_min = r_min;
		this.r_max = r_max;
		this.x_min = x_min;
		this.x_max = x_max; 
		this.y_min = y_min;
		this.y_max = y_max;

	}

	public int getCol_range_max() {
		return col_range_max;
	}

	public void setCol_range_max(int col_range_max) {
		this.col_range_max = col_range_max;
	}

	public int getCol_range_min() {
		return col_range_min;
	}

	public void setCol_range_min(int col_range_min) {
		this.col_range_min = col_range_min;
	}

	public void setColorspektrum(Color[] colorspektrum) {
		this.colorspektrum = colorspektrum;
	}
	
	public Color[] getColorspektrum() {
		return colorspektrum;
	}

}
