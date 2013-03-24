package weka.gui.visualize.subspace.distance3d;

import java.awt.Color;
import java.io.Serializable;

import javax.vecmath.Color3f;

  public class Kugel implements Serializable{
	  private static final long serialVersionUID = 1L;
	  public int id;
	  public float x;
	  public float y;
	  public float z;
	  public float r;
	  public Color3f col;
	  public int  col_id;
	  public int num_objects = 0;
	  private double[] distances = null;
	  public int [] distance_ids = null;
	  private int activeDistance_id;

	  public Kugel(int _id, float _x,float _y,float _z,float _r, Color3f _col) {
			id = _id;
			x = _x;
			y = _y;
			z = _z;
			r = _r;
			activeDistance_id = 0;
			col = _col;
	  }

	  public Kugel(int _id, float _x,float _y,float _z,float _r, int _col_id) {
			id = _id;
			x = _x;
			y = _y;
			z = _z;
			r = _r;
			activeDistance_id = 0;
			col = null;
			col_id = _col_id;
	  }
	  
	  public int getNext(){
		  activeDistance_id++;
		  if(activeDistance_id >= distance_ids.length) activeDistance_id--; 
		  return distance_ids[activeDistance_id];
	  }
	  
	  public int getPrev(){
		  activeDistance_id--;
		  if(activeDistance_id < 0) activeDistance_id = 0;
		  return distance_ids[activeDistance_id];
	  }

	  
	  private void sortDistances(){
		  distance_ids = new int [distances.length];
		  boolean [] used = new boolean[distances.length];
		  for (int i = 0; i < used.length; i++) {
			used[i] = false;
		  }
		  //throw out ID of ball, not with dist = 0.0 (can occur multiple times)
		  used[id] = true;
		  distance_ids[0] = id;
		  for (int i = 1; i < distance_ids.length; i++) {
			  double dist = Double.POSITIVE_INFINITY;
			  int dist_id = -1;
			  for (int j = 0; j < distances.length; j++) {
				if(distances[j] <= dist && !used[j]){
					dist_id = j;
					dist = distances[j];
				}
			  }
			  distance_ids[i] = dist_id;
			  used[dist_id] = true;
		  }
	  }
	  
	  //init distances
	  private void calcDistances(Kugel [] points){
		  distances = new double[points.length];
		  for (int i = 0; i < points.length; i++) {
			  Kugel p = points[i];
			  double d = Math.pow(x-p.x,2)+Math.pow(y-p.y,2)+Math.pow(z-p.z,2);
			  distances[i] = Math.sqrt(d);
		}
	  }

	  public void setDistances(Kugel [] points){
		  calcDistances(points);
		  sortDistances();

	  }

	  public void setColor(Color[] spektrum){
		  col = new Color3f(spektrum[col_id]);
	  }
	  
	 @Override
	public String toString() {

		return "ID:"+ id+ "\t x:"+x+"\t y:"+y+"\t z:"+z+"\t r:"+r+" col:"+col +" numObj:"+num_objects;
	}
	 
  }