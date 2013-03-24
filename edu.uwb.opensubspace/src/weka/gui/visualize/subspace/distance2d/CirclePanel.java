package weka.gui.visualize.subspace.distance2d;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;


public class CirclePanel extends JPanel{

  private static final long serialVersionUID = 1L;
  private float r_org;
  private float x_org;
  private float y_org;
  private int col_id;
  private int id = 0;
  private ScalingObject scale = null;
  private int order_id;
  private boolean marked = false;


//will be calculated in scale() before a repaint takes places
  private int r;
  private int x;
  private int y;
  private Color col = null;

  final Color col_border = Color.black;
  
  public CirclePanel(int _id, float _x, float _y, float _r, int _cl , ScalingObject _scale) {
	  id = _id;
	  scale = _scale;
	  r_org=_r;
	  x_org=_x;
	  y_org=_y;
	  scale = _scale;
	  col_id = _cl;
	  
	  setOpaque(false);
	  setSize(new Dimension(1,1));
	  setLocation(0,0);
  }

  
  public void scale(){
	  double min;
	  if(scale.x_min < scale.y_min)
		 min = scale.x_min;
	  else
		 min = scale.y_min;
	  
	  double max;
	  if(scale.x_max > scale.y_max)
		 max = scale.x_max;
	  else
		 max = scale.y_max;

	  int maxRadiusSize = (int)(scale.getWidth()*scale.refMaxRadiusSize);
	  x=(int)(  
			   ((x_org-min)/(max-min)) //auf 0-1 normieren
			   *(scale.getWidth()-2*(scale.border+maxRadiusSize))
			   +scale.border+maxRadiusSize
			  );
	  y=(int)(  
			   ((y_org-min)/(max-min)) //auf 0-1 normieren
			   *(scale.getWidth()-2*(scale.border+maxRadiusSize))
			   +scale.border+maxRadiusSize
			  );
	  r=(int)((r_org/scale.r_max)*(scale.getWidth()*scale.refMaxRadiusSize));
	  
	  col = scale.getColor(col_id);
  }

  public void paintComponent(Graphics g) {
	  scale();
	  
	  setSize(new Dimension(2*r,2*r));
	  setLocation(x-r,y-r);

	  if (col!=null) {
		  if(!marked){
			  g.setColor(col);
		  }
		  else{
			  g.setColor(Color.BLUE);
		  }
		  
		  g.fillOval(0, 0, (int)((2*r-1)),(int)((2*r-1)));
	  }
	  
	  if (col_border!=null){
		  g.setColor(col_border);
	  	  g.drawOval(0, 0, (int)((2*r-1)),(int)((2*r-1)));
	  }

  }

	@Override
	public boolean contains(int x, int y) {
		double d = Math.sqrt((x-r)*(x-r)+(y-r)*(y-r));
		return (d<r);
	}
	
	@Override
	public String toString() {
		return "ID:"+ id+ "\t x:"+x_org+"\t y:"+y_org+" at("+getX()+"/"+getY()+")"+"\t r:"+r+ "\t col:"+col+" col_id"+col_id+" \t scale:"+scale.getWidth();
	}
	
	
	public ScalingObject getScale() {
		return scale;
	}
	
	
	public void setScale(ScalingObject scale){
		this.scale = scale;
	}


	public int getId() {
		return id;
	}


	public float getR_org() {
		return r_org;
	}


	public float getX_org() {
		return x_org;
	}


	public float getY_org() {
		return y_org;
	}


	public int getCol_id() {
		return col_id;
	}


	public void setOrder_id(int id) {
		order_id = id;
	}

	public boolean isMarked() {
		return marked;
	}


	public void setMarked(boolean marked) {
		this.marked = marked;
	}


}  
