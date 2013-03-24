package weka.gui.visualize.subspace.inDepth;

import javax.swing.JPanel;
import java.awt.Dimension;
import javax.swing.JTextField;
import java.awt.Rectangle;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import java.awt.Point;

public class InDepthControlPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	private JTextField text_zoomFaktor = null;
	public JTextField text_orgFaktor = null;
	private JLabel label_zoomFaktor = null;
	private JLabel label_orgFaktor = null;
	private JTextField text_zoomHeight = null;
	private JLabel label_zoomHeight = null;

	public InDepthControlPanel(FocusListener listener, KeyListener klistener) {
		super();
		initialize();
		text_zoomFaktor.addFocusListener(listener);
		text_zoomFaktor.addKeyListener(klistener);
		text_orgFaktor.addFocusListener(listener);
		text_orgFaktor.addKeyListener(klistener);
		text_zoomHeight.addFocusListener(listener);
		text_zoomHeight.addKeyListener(klistener);
		
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        label_zoomHeight = new JLabel();
        label_zoomHeight.setBounds(new Rectangle(10, 70, 81, 21));
        label_zoomHeight.setText("Zoom Height");
        label_orgFaktor = new JLabel();
        label_orgFaktor.setBounds(new Rectangle(10, 10, 81, 21));
        label_orgFaktor.setText("Orginal (in %)");
        label_zoomFaktor = new JLabel();
        label_zoomFaktor.setBounds(new Rectangle(10, 40, 81, 21));
        label_zoomFaktor.setText("Zoom");
        this.setLayout(null);
        this.setSize(new Dimension(180, 100));
        this.setPreferredSize(new Dimension(180, 100));
        this.add(getText_zoomFaktor(), null);
        this.add(getText_orgFaktor(), null);
        this.add(label_zoomFaktor, null);
        this.add(label_orgFaktor, null);
        this.add(getText_zoomHeight(), null);
        this.add(label_zoomHeight, null);
			
	}

	private JTextField getText_zoomFaktor() {
		if (text_zoomFaktor == null) {
			text_zoomFaktor = new JTextField();
			text_zoomFaktor.setText("");
			text_zoomFaktor.setLocation(new Point(100, 40));
			text_zoomFaktor.setSize(new Dimension(41, 21));
			text_zoomFaktor.setName("zoomFaktor");
//			text_zoomFaktor.setActionCommand("zoomFaktorChange");
		}
		return text_zoomFaktor;
	}

	private JTextField getText_orgFaktor() {
		if (text_orgFaktor == null) {
			text_orgFaktor = new JTextField();
			text_orgFaktor.setText("");
			text_orgFaktor.setLocation(new Point(100, 10));
			text_orgFaktor.setSize(new Dimension(41, 21));
			text_orgFaktor.setName("orgFaktor");
			//text_orgFaktor.setActionCommand("orgFaktorChange");
		}
		return text_orgFaktor;
	}

	public void setOrgFaktor(double value) {
		text_orgFaktor.setText(Integer.toString((int)(value*100)));
	}

	public void setZoomFaktor(int value) {
		text_zoomFaktor.setText(Integer.toString(value));
	}
	
	public double getOrgFaktor() {
		return Integer.parseInt(text_orgFaktor.getText())/100.0;
	}

	public int getZoomFaktor() {
		return Integer.parseInt(text_zoomFaktor.getText());
	}

	public void setZoomHeight(int value) {
		text_zoomHeight.setText(Integer.toString(value));
	}

	public int getZoomHeight() {
		return Integer.parseInt(text_zoomHeight.getText());
	}

	private JTextField getText_zoomHeight() {
		if (text_zoomHeight == null) {
			text_zoomHeight = new JTextField();
			text_zoomHeight.setName("zoomHeight");
			text_zoomHeight.setSize(new Dimension(41, 21));
			text_zoomHeight.setLocation(new Point(100, 70));
		}
		return text_zoomHeight;
	}

}
