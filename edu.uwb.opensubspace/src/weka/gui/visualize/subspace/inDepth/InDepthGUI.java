package weka.gui.visualize.subspace.inDepth;


import i9.subspace.visa.densities.ObjectInformation;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import java.awt.Color;
import javax.swing.JTextField;

import weka.core.Instances;

public class InDepthGUI extends JPanel implements MouseMotionListener, MouseListener, FocusListener, KeyListener{

	private static final long serialVersionUID = 1L;
	private InDepthPanel upperPanel = null;
	private InDepthPanel zoomPanel = null;
	private InDepthPanel lowerPanel = null;
	private JScrollPane windowSlider = null;
	
	private int panelHeight;
	private int panelWidth;
	private int uP_height;

	private int dimWidth = 30;
	private int zP_height;
	private int zoom_faktor;
	private double org_faktor;
	private int lP_height;
	private JPanel panels = null;
	private JPanel contentPanel = null;
	private int slider_value = 0;
	private RankRect[][] m_data = null;
	
	private JFrame objectFrame = null;
	private JTextArea objectDetailsArea = null;
	private InDepthControlPanel controlPanel = null;
	private JTextField activeControlInput = null;
	
	private Instances instances = null;

	public void plotRanking(ArrayList<ObjectInformation> data) {
		
			m_data = getRectData(data);
		
			upperPanel.prepareRanking(m_data);
			zoomPanel.prepareRanking(m_data);
			lowerPanel.prepareRanking(m_data);
	
			
			//calcualte GUI relevant values
			org_faktor = ((double)getParent().getHeight()) / ((double)m_data.length);
			org_faktor = ((int)(org_faktor*10))/10.0;
			if(org_faktor<0.1)org_faktor=0.1;
			if(org_faktor>1)org_faktor=1;
			zoom_faktor = (int)(org_faktor * 5);
			if(zoom_faktor <= 1)
				zoom_faktor = 5;
			//org_faktor = Math.round(org_faktor*100)/100.0;
	
			//zoomPanel height = 1/5 of window height and multiple of 25 
			zP_height = (getParent().getHeight()/5/25)*25;
	
			//set values in GUI
			controlPanel.setZoomFaktor(zoom_faktor);
			controlPanel.setOrgFaktor(org_faktor);
			controlPanel.setZoomHeight(zP_height);
	
			panelHeight = (int)(upperPanel.get_dataSize()*org_faktor);
			
			setZoomSelection(slider_value);
			
			repaint();

	}

	public RankRect[][] getRectData(ArrayList<ObjectInformation> data){
		RankRect[][] rect_data = null;
		
		int dim = data.get(0).getColorCode().length;
		rect_data = new RankRect[data.size()][dim]; 
			
		for (int i = 0; i < data.size(); i++) {
			ObjectInformation o = data.get(i);
			for (int j = 0; j < o.getColorCode().length; j++) {
				int rgb[] = o.getColorCode()[j].toRGB();
				Color col = new Color(rgb[0],rgb[1],rgb[2]);
				rect_data[i][j] = new RankRect(i*dim+j, o.getClusterID(), o.getInterestingness(), col, i, j);
			}
		}
		return rect_data;
	}
	
	public InDepthGUI(Instances instances) {
		super();
		this.instances = instances;
		initialize();
		setVisible(true);
	}
	
	private void initialize(){	
		setLayout(new GridBagLayout());
		windowSlider = new JScrollPane(getContentPanel());
		windowSlider.getVerticalScrollBar().setVisible(true);
		
//		GridBagConstraints gb = new GridBagConstraints();
//		gb.anchor = GridBagConstraints.WEST;
//		gb.weightx = 1;
//		gb.weighty = 1;
//		gb.gridx = 0;
//		gb.gridy = 0;
//		add(getControlPanel(), gb);
		
		GridBagConstraints gb1 = new GridBagConstraints();
		gb1.weightx = 1;
		gb1.weighty = 1;
		gb1.gridx = 0;
		gb1.gridy = 0;
		gb1.anchor = GridBagConstraints.WEST;
		gb1.fill = GridBagConstraints.BOTH;
		add(windowSlider,gb1);
		
		
		initObjectFrame();
		this.addKeyListener(this);
	}
	
	private JFrame initObjectFrame(){
		if(objectFrame == null){
			objectFrame = new JFrame();
			objectFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
			objectFrame.setSize(400, 350);
			JPanel objectPane = new JPanel();
			objectPane.setBackground(Color.white);
			objectFrame.setContentPane(objectPane);
			objectPane.add(getobjectDetailsArea(), null);
		}
		return objectFrame;
	}
	
	private JPanel getContentPanel(){
		contentPanel = new JPanel();
		contentPanel.setSize(new Dimension(120, 95));
		contentPanel.setPreferredSize(new Dimension(100, 100));
		contentPanel.setLayout(new GridBagLayout());
		contentPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		
		contentPanel.addMouseMotionListener(this);
		contentPanel.addMouseListener(this);



		GridBagConstraints gb = new GridBagConstraints();
		gb.weightx = 1;
		gb.weighty = 1;
		gb.fill = GridBagConstraints.BOTH;
		gb.anchor = GridBagConstraints.NORTHWEST;
		gb.gridx = 0;
		gb.gridy = 0;
		contentPanel.add(getControlPanel(), gb);

		GridBagConstraints gb1 = new GridBagConstraints();
		gb1.weightx = 2;
		gb1.weighty = 1;
		gb1.fill = GridBagConstraints.BOTH;
		gb1.anchor = GridBagConstraints.WEST;
		gb1.gridx = 1;
		gb1.gridy = 0;
		contentPanel.add(getPanels(),gb1);
		
		return contentPanel; 
	}

	private JPanel getPanels() {
		if (panels == null) {
			panels = new JPanel();
			panels.setLayout(null);
			upperPanel = new InDepthPanel("upperPanel",false);
			zoomPanel = new InDepthPanel("zoomPanel",true);
			lowerPanel = new InDepthPanel("lowerPanel",false);

			panels.add(upperPanel);
			panels.add(zoomPanel);
			panels.add(lowerPanel);

		}
		return panels;
	}
	
	@Override
	public void paint(Graphics g) {
		if(m_data!=null){
			super.paint(g);
			panelHeight = (int)(upperPanel.get_dataSize()*org_faktor+zP_height+6);
			panelWidth =  upperPanel.get_dims()*dimWidth;
	
			panels.setPreferredSize(new Dimension(panelWidth,panels.getHeight()));
	
			contentPanel.setPreferredSize(new Dimension(panelWidth ,panelHeight));
			contentPanel.setSize(new Dimension(panelWidth ,panelHeight));
			uP_height = (panelHeight-zP_height)/2;
			lP_height = uP_height;
	
			upperPanel.setSize(panelWidth, uP_height);
			zoomPanel.setSize(panelWidth, zP_height);
			lowerPanel.setSize(panelWidth, lP_height);
	
			upperPanel.changeZoom(org_faktor);
			zoomPanel.changeZoom(zoom_faktor);
			lowerPanel.changeZoom(org_faktor);
	
			setZoomSelection(slider_value);
		}
	}

	
	private void setZoomSelection(int pixel){

		if(pixel < 0) pixel = 0;
		if(pixel < panelHeight-6){
			uP_height=pixel;
			lP_height=panelHeight-uP_height-zP_height-6;	
		}
		else{
				uP_height = panelHeight-zP_height-6;
				lP_height = 0;
		}

		int zP_offset = (int)(uP_height/org_faktor*zoom_faktor);
		int lP_offset = (int)(uP_height+zP_height/zoom_faktor*org_faktor);

		upperPanel.setSelection(0					,uP_height,panelWidth,0);
		zoomPanel.setSelection( uP_height			,zP_height,panelWidth,zP_offset); //5 rects
		lowerPanel.setSelection(uP_height+zP_height ,lP_height,panelWidth,lP_offset);

	}

	public void mouseDragged(MouseEvent e) {
			slider_value = e.getY();
			setZoomSelection(slider_value);
	}

	public void mouseMoved(MouseEvent arg0) {
	}

	private JTextArea getobjectDetailsArea() {
		if (objectDetailsArea == null) {
			objectDetailsArea = new JTextArea();
			objectDetailsArea.setSize(250, 250);
			objectDetailsArea.setRows(5);
			objectDetailsArea.setEditable(false);
			
		}
		return objectDetailsArea;
	}

	private void showObjectDetails(RankRect[] obj){
		objectFrame.setTitle("Object "+obj[0].nr+" in subspace cluster "+obj[0].cluster);
		objectFrame.setVisible(true);
		objectDetailsArea.setText("");
		objectDetailsArea.append("Object \t\t"+obj[0].nr+"\n");
		objectDetailsArea.append("Interestingness:"+Math.round(obj[0].getInterestingness())+"\n");
		objectDetailsArea.append("\n");
		
		if (instances==null){
			objectDetailsArea.append("\n\n load arff file for more information ");
		}
		else{
			int classindex = instances.classIndex();
			for (int i = 0; i < classindex; i++) {
				if(i != instances.classIndex()){
					objectDetailsArea.append("Dimension "+i+":\t"+instances.instance(obj[0].nr).value(i)+"\n");
				}
			}
			if(classindex>0)
				objectDetailsArea.append("Class:\t\t"+instances.instance(obj[0].nr).classValue()+"\n");
			
		}
	}

	private JPanel getControlPanel() {
		if (controlPanel == null) {
			controlPanel = new InDepthControlPanel(this,this);
			controlPanel.setSize(new Dimension(82, 87));
			activeControlInput = controlPanel.text_orgFaktor;
		}
		return controlPanel;
	}

	public void mouseClicked(MouseEvent e) {
		int pixel = e.getY();
		RankRect [] data;
		
		//check if the cklick happend over an object
		if (e.getX() >= panels.getLocation().getX() && 
			e.getX() < panels.getLocation().getX()+upperPanel.getWidth()){

			int zP_offset = (int)(uP_height/org_faktor*zoom_faktor);
			int lP_offset = (int)(uP_height+zP_height/zoom_faktor*org_faktor);
			
			
			//check which panel was clicked on
			if(pixel < uP_height){
				data = upperPanel.getObject((int)(pixel/org_faktor));
			}
			else{
				if(pixel < uP_height + zP_height){
					data = zoomPanel.getObject((int)((pixel- uP_height) + zP_offset));
				}
				else{
					data = lowerPanel.getObject((int)((pixel - uP_height - zP_height + lP_offset)/org_faktor));
				}
			}
			//show detail panel
			showObjectDetails(data);
		}
	}


	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}




	public void focusGained(FocusEvent e) {
		if (e.getComponent() instanceof javax.swing.JTextField) {
			JTextField tf = (JTextField)e.getComponent();
			activeControlInput = tf;
		}
	}

	public void focusLost(FocusEvent e) {
		if (e.getComponent() instanceof javax.swing.JTextField) {
			JTextField tf = (JTextField)e.getComponent();
			if (tf.getName().equals("orgFaktor")) {
				org_faktor = controlPanel.getOrgFaktor();
				
				repaint();
			}
			if (tf.getName().equals("zoomFaktor")) {
				zoom_faktor = controlPanel.getZoomFaktor();
				repaint();
			}
			
			if (tf.getName().equals("zoomHeight")) {
				zP_height = controlPanel.getZoomHeight();
				repaint();
			}
		}
	}
	

	public void keyPressed(KeyEvent k) {
		if(k.getKeyChar() == '+' || k.getKeyChar() == '-'){
		
			int value = Integer.parseInt(activeControlInput.getText());
			if (activeControlInput.getName().equals("orgFaktor")) {
				if(k.getKeyChar() == '+'){
					value+= 5;
				}
				else{
					value-= 5;
				}
				if(value<=0) value = 5;
				if(value>500) value = 500;
			}
			if (activeControlInput.getName().equals("zoomFaktor")) {
				if(k.getKeyChar() == '+'){
					value+= 1;
				}
				else{
					value-= 1;
				}
				if(value<1) value = 1;
			}
			
			if (activeControlInput.getName().equals("zoomHeight")) {
				if(k.getKeyChar() == '+'){
					value+=25;
				}
				else{
					value-=25;
				}
				if(value<=50) value = 50;
				if(value>500) value = 500;
			}
			activeControlInput.setText(Integer.toString(value));
		}
		if((k.getKeyCode() == KeyEvent.VK_UP || k.getKeyCode() == KeyEvent.VK_DOWN)
			   && k.isShiftDown()){
			
			if(k.getKeyCode() == KeyEvent.VK_UP){
				slider_value-=5;
				if(slider_value < 0) slider_value = 0;
			}
			else{
				slider_value+=5;
				if(slider_value > panelHeight-6) slider_value = panelHeight-6;
			}
			setZoomSelection(slider_value);
		}

	}

	public void keyReleased(KeyEvent arg0) {}

	public void keyTyped(KeyEvent k) {
		if (k.getKeyChar() == '+' || k.getKeyChar() == '-') {
			k.consume();
		}
	}



}

