package weka.gui.visualize.subspace.inDepth;

import java.awt.Color;

public class RankRect{
	private static final long serialVersionUID = 1L;
	    double x1,y1,x2,y2;
	    int x,y,w,h;
	    public Color col;
	    public int nr;
	    public int cluster;
	    int x_id,y_id;
	    double interestingness;

	    public RankRect(int _nr, int _cluster, double _interestingness, Color _col, int _x_id, int _y_id) {
	        col = _col;
	        interestingness = _interestingness;
	        nr = _nr;
	        cluster = _cluster;
	        x_id=_x_id;
	        y_id=_y_id;
	        x=0;
	        y=0;
	        w=0;
	        h=0;
	    }

	    public double getInterestingness(){
	    	return interestingness;
	    }
	    
}
