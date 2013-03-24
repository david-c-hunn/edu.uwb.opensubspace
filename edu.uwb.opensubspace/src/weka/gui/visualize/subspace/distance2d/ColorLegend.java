package weka.gui.visualize.subspace.distance2d;

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ColorLegend extends JPanel implements PropertyChangeListener{

	Color[] spektrum;
	int width = 25;
	int height = 15;
	int y = 10;
	private boolean activeDims[];
	private boolean markedDims[];

	
	public ColorLegend(Color[] spektrum) {
		super();
		this.spektrum = spektrum;
		activeDims = new boolean[spektrum.length];
		markedDims = new boolean[spektrum.length];
		for (int i = 0; i < activeDims.length; i++) {
			markedDims[i] = false;
			if(spektrum[i]!=null){
				activeDims[i] = true;
			}
			else			{
				activeDims[i] = false;
			}
		}
		initialize();
	}
	
	private void initialize() {
        this.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
		int count = 0;
        
		for (int i = 0; i < spektrum.length; i++) {
			if(spektrum[i]!=null){
				JPanel box = new ColorLegendBox(spektrum[i],i);
				box.addPropertyChangeListener(this);
				add(box);
				activeDims[i]=true;
				count++;
			}
		}
        this.setSize(new Dimension((width+5)*count, height+5));
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("activeDimensionChange")){
			firePropertyChange("activeDimensionChange", 0, evt.getNewValue());
		}
	}

	public boolean isDimensionActive(int i){
		return activeDims[i];
	}

	public boolean isDimensionMarked(int i){
		return markedDims[i];
	}

	
	private class ColorLegendBox extends JPanel  implements MouseListener{
		Color color = null;
		int id = 0;
		//boolean active = true;
		
		public ColorLegendBox(Color c, int id) {
			color = c;
			this.id = id;
			setPreferredSize(new Dimension(width,height));
			setSize(new Dimension(width,height));
			addMouseListener(this);
		}
		
		
		public void paint(Graphics g) {
			super.paint(g);
			g.setColor(color);
			g.fillRect(0,0,width-1,height-1);
			if(markedDims[id]){
				g.setColor(Color.RED);
			}
			else{
				if(activeDims[id])
					g.setColor(Color.black);
				else
					g.setColor(Color.GRAY);
			}
			g.drawRect(0,0,width-1,height-1);
			g.drawString(Integer.toString(id), 8, 12);
		}
		
		public void mouseClicked(MouseEvent e) {
			if(e.getModifiers() == java.awt.event.MouseEvent.BUTTON1_MASK){
				activeDims[id] = !activeDims[id];
				if(!activeDims[id])
					markedDims[id]= false;
			}
			if(e.getModifiers() == java.awt.event.MouseEvent.BUTTON3_MASK){
				markedDims[id] = !markedDims[id];
				activeDims[id] = true;
			}
			firePropertyChange("activeDimensionChange", 0, id);
			repaint();

		}
		
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		
	}


}
