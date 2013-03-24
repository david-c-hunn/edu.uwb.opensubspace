package weka.gui.visualize.subspace.inDepth;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class InDepthPanel extends JPanel implements MouseListener{
	private static final long serialVersionUID = 1L;
	private String m_name;
	private InDepthBackground m_backPanel;
	private JPanel m_overlayPanel = null;
	private Boolean is_zoompanel;
	private double m_zoom;
	
	private RankRect[][] m_data;
	private int m_dims;
	private int m_data_size;
	

	private class InDepthBackground extends JPanel{
		private static final long serialVersionUID = 1L;
		int m_rect_width;
		int m_rect_height;
		
		public InDepthBackground() {	
			super();
		}
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			int delta = 1;
			m_rect_width = this.getWidth()/m_dims;
			m_rect_height = (int)(m_zoom);
			if(m_rect_height<1){
				m_rect_height = 1;
				delta = (int)(1/m_zoom);
			}
			for (int i = 0; i < m_data_size; i=i+delta){
				int line_nr = i;
				//check for white cluster lines
				for (int j = i; j < i+delta && j< m_data_size; j++) {
					if(m_data[j][0].col.equals(new Color(16777215))){
						line_nr = j;
					}
				}
				//paint line
				for (int j = 0; j < m_dims; j++) {
					g.setColor(m_data[line_nr][j].col);
					g.fillRect(j*m_rect_width, line_nr*m_rect_height/delta,m_rect_width,m_rect_height);
				}
			}
		}
	}
	
	public RankRect [] getObject(int y){
		int delta = 1;
		int m_rect_height = (int)(m_zoom);
		if(m_rect_height<1){
			m_rect_height = 1;
			delta = (int)(1/m_zoom);
		}
		
		int line_nr = (int) (y / m_rect_height); 

		//check for white cluster lines
//		for (int j = i; j < i+delta && j< m_data_size; j++) {
//			if(m_data[j][0].col.equals(new Color(16777215))){
//				line_nr = j;
//			}
//		}
		return m_data[line_nr];
	}
	
	public InDepthPanel(String _name,Boolean _is_zoompanel) {
		super();
		m_name = _name;
		is_zoompanel = _is_zoompanel;
		initialize();
	}
	
	
	public void prepareRanking(RankRect[][] data){
		m_data = data;
		m_data_size = m_data.length;
		m_dims = m_data[0].length;
	}
	
	
	public void setSelection(int pos, int size,int width, int offset){
		setLocation(0, pos);
		setBounds(0, pos,getWidth(), size);
		m_backPanel.setLocation(0, -offset);
	}
	
	public void changeZoom(double _faktor){
		m_zoom = _faktor;
		m_backPanel.setSize(getWidth(), (int)(m_zoom*m_data_size));
	}


	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if(is_zoompanel){
			m_overlayPanel.setSize(getWidth(),getHeight());
		}
	}
	
	private void initialize() {
        this.setLayout(null);
        this.setSize(new Dimension(500, 200));
        m_backPanel = new InDepthBackground();
        m_backPanel.setPreferredSize(new Dimension(getWidth(),getHeight()));
        m_backPanel.setSize(new Dimension(getWidth(), getHeight()));
        m_backPanel.setLocation(0, 0);
        m_backPanel.setLayout(null);

        if(is_zoompanel){
        	this.add(getM_overlayPanel());
        }
       	this.add(m_backPanel);
        
	}

	public RankRect[][] get_data() {
		return m_data;
	}

	public int get_dataSize() {
		return m_data_size;
	}
	
	public int get_dims() {
		return m_dims;
	}

	/**
	 * This method initializes m_overlayPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getM_overlayPanel() {
		if (m_overlayPanel == null) {
			m_overlayPanel = new JPanel();
			m_overlayPanel.setLayout(null);
			m_overlayPanel.setPreferredSize(new Dimension(getWidth(),getHeight()));
			m_overlayPanel.setSize(new Dimension(getWidth(),getHeight()));
			//m_overlayPanel.setSize(new Dimension(499, 194));
			m_overlayPanel.setLocation(0, 0);
			m_overlayPanel.setOpaque(false);
			Border border = BorderFactory.createLineBorder(Color.yellow, 3);
			m_overlayPanel.setBorder(border);
			
		}
		return m_overlayPanel;
	}

	/**
	 * @return the m_data_size
	 */
	public int getM_data_size() {
		return m_data_size;
	}

	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}


}
