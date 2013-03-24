package weka.gui.visualize.subspace.distance2d;


import java.awt.Color;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import i9.subspace.base.Cluster;
import weka.core.Instances;


public class Distance2DPanel extends JPanel implements MouseListener, PropertyChangeListener{
	private static final long serialVersionUID = 1L;
	private CirclePanel[] m_circles = null;
	private ScalingObject scale = null;
	private int id;
	private ArrayList<Integer> sort = null;
	private boolean type_overview = true; 
	private String parameter = "";
	private boolean is_empty = true;
	private ColorLegend colLeg = null;
	private double min_radius_threshold = 0;
	private double max_radius_threshold = 1;

	//pendigits
	private Instances instances = null;
	private PendigitsClusterBox penbox;
	private ArrayList<Cluster> clustering = null;

	public Distance2DPanel(){
		super();
		addMouseListener(this);
	}
	
	public Distance2DPanel(int _id, int size, CirclePanel[] circles, ArrayList<Cluster> _clustering, String _parameter, Instances _instances) {
		super();
		id =_id; 
		m_circles = circles;
		instances = _instances;
		clustering = _clustering;
		
		parameter = _parameter;		
		setSize(size, size);
        setPreferredSize(new Dimension(size,size));
        setLayout(null);
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        addMouseListener(this);
		//TODO
		 
		if(m_circles != null){
			is_empty = false;
			//get a scale object (all panels refer to the same scale object)
			scale = m_circles[0].getScale();
			
			scale.setWidth(size);
			//default, will be updated later on, with reference to the other plots to keep colors consitent
			scale.setColorspektrum(getColorSpektrum(clustering));

			//get min max
			double r_min, r_max, x_min, x_max, y_min, y_max;
			r_min = x_min = y_min = Double.MAX_VALUE;
			r_max = x_max = y_max = Double.MIN_VALUE;
			for (int i = 0; i < m_circles.length; i++) {
				if(m_circles[i].getR_org() > r_max) r_max = m_circles[i].getR_org();
				if(m_circles[i].getR_org() < r_min) r_min = m_circles[i].getR_org();
				if(m_circles[i].getX_org() > x_max) x_max = m_circles[i].getX_org();
				if(m_circles[i].getX_org() < x_min) x_min = m_circles[i].getX_org();
				if(m_circles[i].getY_org() > y_max) y_max = m_circles[i].getY_org();
				if(m_circles[i].getY_org() < y_min) y_min = m_circles[i].getY_org();
			}
			scale.setMinMaxValues(r_min, r_max, x_min, x_max, y_min, y_max);
			
			
			//sort bubbles by size and color
			sort = new ArrayList<Integer>();
			for (int i = 0; i < m_circles.length; i++) {
				int j = 0;
				while(j < sort.size() && m_circles[sort.get(j)].getCol_id() < m_circles[i].getCol_id()){
					j++;	
				}
				while(j < sort.size() && m_circles[sort.get(j)].getR_org() > m_circles[i].getR_org() && m_circles[sort.get(j)].getCol_id() == m_circles[i].getCol_id()){
					j++;	
				}
				sort.add(j,i);
			}
	
			//set order id
			for (int i = 0; i < m_circles.length; i++) {
				m_circles[i].setOrder_id(sort.get(i));
			}

			//add bubbles
			for (int j = sort.size()-1; j>=0; j--) {
				int i = sort.get(j);
				add(m_circles[i]);
			}
			
			
		}
	}
	
	public void setScalingSize(int size){
		scale.setWidth(size);
	}

	private void setFullSize(){
		//TODO get real slider width
		int slider_width = 25*2;
		if(getParent()!=null){
			if(getParent().getWidth()-slider_width > getParent().getHeight()){
				setScalingSize(getParent().getHeight());
				setSize(getParent().getHeight(),getParent().getHeight());
			}
			else{
				setScalingSize(getParent().getWidth()-slider_width);
				setSize(getParent().getWidth()-slider_width,getParent().getWidth()-slider_width);
			}
		}
	}
	
	public void setFullSizeOverview(int height){
			setScalingSize(height);
			setSize(height, height);
			setPreferredSize(new Dimension(height, height));
			//setLocation(getX(), 0);
	}
	
	@Override
	public void paint(Graphics g){
			super.paint(g);
			g.drawString(parameter, 10, 15);
	}
	
	public void repaint(){
		super.repaint();
		if(!is_empty){
			
			if(!type_overview){
				setFullSize();
				if(m_circles!=null){
					removeAll();
					for (int i = sort.size()-1; i>=0; i--) {
						CirclePanel c = m_circles[sort.get(i)];
						if(c.getR_org() <= max_radius_threshold*(scale.r_max)
						   && c.getR_org() >= min_radius_threshold*(scale.r_max)
						   && colLeg.isDimensionActive(c.getCol_id())){
							//changes the color of the bubble, if resp. dimension is marked
							if(colLeg.isDimensionMarked(c.getCol_id()))
								c.setMarked(true);
							else
								c.setMarked(false);
							add(c);
						}
					}
				}
				if(colLeg!=null) this.add(colLeg);
			}
			else{

			}
		}
	}
	
	public void setColorSpektrum(int min, int max){
		scale.setColorspektrum(getColorSpektrum(clustering, min, max));
	}
	
	public static Color[] getColorSpektrum(ArrayList<Cluster> clustering) {
		return getColorSpektrum(clustering,0,0);
	}
	
	public static Color[] getColorSpektrum(ArrayList<Cluster> cl, int col_range_min_total, int col_range_max_total) {
		int dims = cl.get(0).m_subspace.length;
		
		//if min max is not set (means we calc the spektrum without respect to other clusterings)
		//if(col_range_min == 0 && col_range_max == 0){
		if(col_range_min_total == 0 && col_range_max_total == 0){
			col_range_max_total = dims;
		}
			int col_range_min = Integer.MAX_VALUE;
			int col_range_max = 0;
			for (int i = 0; i < cl.size(); i++) {
				boolean[] subspaces = cl.get(i).m_subspace;
				int count = 0;
				for (int j = 0; j < subspaces.length; j++) {
					if (subspaces[j])
						count++;
				}
				if (count < col_range_min)
					col_range_min = count;
				if (count > col_range_max)
					col_range_max = count;
			}
		//}
		
		
		//Predefined colors
		Color[] color_base = { 
				new Color(222, 251, 45), // hell grün
				new Color(251, 254, 1), // gelb
//				new Color(252, 183, 2), // orange
				new Color(255,101,0), //dunkel orange
				new Color(255, 0, 6), // rot
				
		};

		Color[] colorspektrum_tmp = new Color[col_range_max_total - col_range_min_total + 1];
		//Color[] colorspektrum_tmp = new Color[dims + 1];

		//TODO finetuning +1 
		int base_range = ((int) Math.ceil(colorspektrum_tmp.length / color_base.length))+1;
		if(base_range == 0 )base_range = 1;
		for (int i = 0; i < color_base.length; i++) {
			Color base = color_base[i];
			float[] hsb_base = Color.RGBtoHSB(base.getRed(), base.getGreen(),
					base.getBlue(), null);
			float hue = hsb_base[0];
			float bright = hsb_base[2];
			for (int j = 0; j < base_range; j++) {
				if (i * base_range + j < colorspektrum_tmp.length) {
					float sat = 0.7f + (0.3f * (float)(j+1) / (float)base_range);
					colorspektrum_tmp[i * base_range + j] = new Color(Color.HSBtoRGB(hue, sat, bright));
				}
			}
		}

		//add null for direct access over amount of relevant dimensions
		//otherwise we would have to take col_range_min with us
		Color[] colorspektrum = new Color[dims+1];
//		for (int i = 0; i < colorspektrum.length; i++) {
//			colorspektrum[i]=null;
//		}
		System.arraycopy(colorspektrum_tmp, col_range_min-col_range_min_total, colorspektrum, col_range_min, col_range_max - col_range_min +1);
		
		return colorspektrum;
	}

	
	public void initPenBox(){
		if(instances!=null){
			penbox = new PendigitsClusterBox(instances);
		}
	}
	
	public void mouseClicked(MouseEvent e) {
		try {
			CirclePanel cp = (CirclePanel)findComponentAt(e.getPoint());
			if(e.getModifiers() == java.awt.event.MouseEvent.BUTTON1_MASK){
//				if(boxplot!=null)
//					boxplot.showClusterFrame(cp.id);
			}

			if(e.getModifiers() == java.awt.event.MouseEvent.BUTTON3_MASK){
				if(instances!=null && instances.relationName().equals("Pendigits") && clustering!=null){
					penbox.drawCluster(cp.getId(),clustering.get(cp.getId()));
					Thread.sleep(100);
					this.grabFocus();
				}
			}
		} catch (Exception ex) {
		}
	}
	
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	
	public void printPanels(){
		for (int i = 0; i < m_circles.length; i++) {
			System.out.println(m_circles[i]);
		}
	}
	
	public void setColorLegend(){
		colLeg = new ColorLegend(scale.getColorspektrum());
		colLeg.addPropertyChangeListener(this);
		colLeg.setLocation(20, 20);
		this.add(colLeg);
	}

	/**
	 * @param instances the instances to set
	 */
	public void setInstances(Instances _instances) {
		instances = _instances;
	}

	public boolean isPendigits(){
		if(instances!=null && instances.relationName().equalsIgnoreCase("pendigits"))
			return true;
		else
			return false;
	}

	public void closeFrames(){
//		if(boxplot!=null)
//			boxplot.setVisible(false);
		if(penbox!=null)
			penbox.dispose();
	}

	/**
	 * @return the absolute max_radius_threshold
	 */
	public double getMax_radius_threshold() {
		return max_radius_threshold*(scale.r_max);
	}

	/**
	 * @param set the relative max radius
	 */
	public void setMax_radius_threshold(double max_radius_threshold) {
		this.max_radius_threshold = max_radius_threshold;
	}

	/**
	 * @return the absolute min_radius_threshold
	 */
	public double getMin_radius_threshold() {
		return min_radius_threshold*(scale.r_max);
	}

	/**
	 * @param set the relative min radius
	 */
	public void setMin_radius_threshold(double min_radius_threshold) {
		this.min_radius_threshold = min_radius_threshold;
	}
	
	public int getMinColor(){
		return scale.getCol_range_min();
	}
	
	public int getMaxColor(){
		return scale.getCol_range_max();
	}
	
	public Color[] getColorspektrumFor3D(){
		Color [] copy = new Color[scale.getColorspektrum().length];
		//Copy color spektrum
		System.arraycopy(scale.getColorspektrum(), 0, copy, 0, scale.getColorspektrum().length);
		//Filter the disabled dimensions so the don't show up in the 3D plot and mark selected Dimensions
		for (int i = 0; i < copy.length; i++) {
			if(!colLeg.isDimensionActive(i)){
				copy[i]=null;
			}
			if(colLeg.isDimensionMarked(i)){
				copy[i]=Color.BLUE;
			}

		}
		return copy;
	}
	
	public void setIsOverview(boolean type_overview) {
		if(type_overview){
			this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		}
		else{
			this.setBorder(null);
		}
		this.type_overview = type_overview;
	}

	public boolean isEmpty() {
		return is_empty;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("activeDimensionChange")){
			repaint();
		}

		
	}
	
}
