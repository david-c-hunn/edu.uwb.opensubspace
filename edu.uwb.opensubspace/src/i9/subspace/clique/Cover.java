/**
 * Used to store the cover of clusters by the set of k-dimensional rectangles
 * 
 * @author pishchulin
 */

package i9.subspace.clique;

import java.util.HashSet;
import java.util.List;

import i9.subspace.base.Cluster;

public class Cover extends Cluster{
		
	private HashSet<Rectangle> m_rectangles;  
	
	public Cover(boolean[] subspace, List<Integer> objects){
		
		super(subspace, objects);
				
		m_rectangles = new HashSet<Rectangle>();
			
	}
	
	public Cover(boolean[] subspace, List<Integer> objects, HashSet<Rectangle> rectangles){
		
		super(subspace, objects);
				
		m_rectangles = rectangles;
			
	}
	
	public HashSet<Rectangle> getRectangles(){
		
		return m_rectangles;
	}
	
	public void addRectangle(Rectangle r){
		m_rectangles.add(r);
	}
	

}
