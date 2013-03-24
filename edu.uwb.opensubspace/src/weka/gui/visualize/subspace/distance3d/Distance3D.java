package weka.gui.visualize.subspace.distance3d;

import weka.gui.visualize.subspace.distance2d.PendigitsClusterBox;
import i9.subspace.base.Cluster;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Material;
import javax.media.j3d.SceneGraphPath;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import weka.core.Instances;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.universe.SimpleUniverse;

public class Distance3D extends Applet implements MouseListener, KeyListener{
  private PickCanvas pickCanvas;
  private int activeSphereID = 1;
  private int activeNextSphereID = 1;
  private Kugel [] kugeln;
  private ArrayList<Sphere> spheres;
  private int m_radiusNorm = 10;
  private TransformGroup box;
  private double r_min;
  private double r_max;
  private Color[] spektrum;
  Canvas3D c;
  
  //pendigits
  public  Instances instances = null;
  private PendigitsClusterBox penbox;
  private ArrayList<Cluster> clustering = null;
	
  private TransformGroup createTG(float x, float y, float z) {
    Vector3f position = new Vector3f(x, y, z);
    Transform3D translate = new Transform3D();
    translate.set(position);
    TransformGroup trans1 = new TransformGroup(translate);
    return trans1;
  }

  private Appearance createMatAppear(Color3f dColor, Color3f sColor, float shine) {

    Appearance appear = new Appearance();
    Material material = new Material();
    material.setCapability(Material.ALLOW_COMPONENT_READ);
    material.setCapability(Material.ALLOW_COMPONENT_WRITE);
    material.setDiffuseColor(dColor);
    material.setSpecularColor(sColor);
    material.setShininess(shine);
    appear.setMaterial(material);

    return appear;
  }

  
  public Distance3D(Kugel [] org_kugeln, ArrayList<Cluster> _clustering, Instances _instances, double _r_min, double _r_max, Color[] _spektrum) {
	
	instances = _instances;
	r_min = _r_min;
	r_max = _r_max;
	spektrum = _spektrum;

	if (instances != null) {
		clustering = _clustering;
		penbox = new PendigitsClusterBox(instances);
	}
	int count = 0;
	for (int i = 0; i < org_kugeln.length; i++) {
		//Filter the spheres depending on the 
		//radius and dimension selection made in 2D
		if(r_min <= org_kugeln[i].r 
				&& org_kugeln[i].r <= r_max
				&& spektrum[org_kugeln[i].col_id]!=null){
			count++;
		}
	}


	kugeln =  new Kugel[count];
	double max_r = 0.0;
	int j = 0;
	for (int i = 0; i < org_kugeln.length; i++) {
		//Filter the spheres depending on the radius and dimension selection made in 2D
		//get max radius
		if(r_min <= org_kugeln[i].r 
			&& org_kugeln[i].r <= r_max 
			&& spektrum[org_kugeln[i].col_id]!=null){
				Kugel k = org_kugeln[i];
				kugeln[j] = new Kugel(j,k.x,k.y,k.z,k.r,k.col_id);
				kugeln[j].setColor(spektrum);
				j++;
		}
		if(org_kugeln[i].r > max_r) max_r = org_kugeln[i].r;
	}

	//set distances
	for (int i = 0; i < kugeln.length; i++) {
			kugeln[i].setDistances(kugeln);
	}

	
	setLayout(new BorderLayout());
    GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
    c = new Canvas3D(config);
    add("Center", c);
    

    Color3f white = new Color3f(0.0f, 0.0f, 0.0f);

    BranchGroup scene = new BranchGroup();

    BoundingSphere bounds = new BoundingSphere( new Point3d(0.0,0.0,0.0), 100.0 );

    pickCanvas = new PickCanvas(c, scene);
    c.addMouseListener(this);
    c.addKeyListener(this);
    
    box = createTG(0f,0f,0f);
    scene.addChild(box);
	box.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE );
	box.setCapability( TransformGroup.ALLOW_TRANSFORM_READ );
    
	MouseRotate rotatebehavior = new MouseRotate( box );
	scene.addChild( rotatebehavior );
	rotatebehavior.setSchedulingBounds( bounds );
	
	MouseWheelZoom zoombehavior = new MouseWheelZoom(box);
	scene.addChild( zoombehavior );
	zoombehavior.setSchedulingBounds( bounds );
	
//	MouseTranslate translatebehavior = new MouseTranslate(box); 
//	scene.addChild( translatebehavior );
//	translatebehavior.setSchedulingBounds( bounds );
	
	spheres = new ArrayList<Sphere>();
   
	
    for (int i = 0; i < kugeln.length; i++) {
    	Kugel k = kugeln[i];

    	TransformGroup trans = createTG(k.x,k.y,k.z);
	    Sphere s = new Sphere(k.r/(float)max_r/m_radiusNorm, Sphere.GENERATE_NORMALS, 60,createMatAppear(k.col, white, 300.0f));
	    s.setCapability(Appearance.ALLOW_MATERIAL_READ);
	    s.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
	    s.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
	    
	    //s.setName(String.valueOf(k.id));
	    s.setName(String.valueOf(i));
	    box.addChild(trans);
	    spheres.add(s);
	    trans.addChild(s);

    }

    AmbientLight lightA = new AmbientLight();
    lightA.setInfluencingBounds(bounds);
    scene.addChild(lightA);

    
    DirectionalLight lightD1 = new DirectionalLight();
    lightD1.setInfluencingBounds(bounds);
    Vector3f direction = new Vector3f(-2.0f, -2.0f, -2.0f);
    direction.normalize();
    lightD1.setDirection(direction);
    lightD1.setColor(new Color3f(1.0f, 1.0f, 1.0f));
    scene.addChild(lightD1);

    
    Background background = new Background();
    background.setApplicationBounds(bounds);
    background.setColor(1.0f, 1.0f, 1.0f);
    scene.addChild(background);

    SimpleUniverse u = new SimpleUniverse(c);

    // This will move the ViewPlatform back a bit so the
    // objects in the scene can be viewed.
    u.getViewingPlatform().setNominalViewingTransform();

    // setLocalEyeViewing
    u.getViewer().getView().setLocalEyeLightingEnable(true);

    u.addBranchGraph(scene);
  }


  	private void activateSphere(int next){
  		Sphere new_s = spheres.get(next);
		Sphere old_s = spheres.get(activeSphereID);
		
		if(activeNextSphereID!=activeSphereID){
			Sphere old_next_s = spheres.get(activeNextSphereID);
			old_next_s.getAppearance().getMaterial().setDiffuseColor(kugeln[activeNextSphereID].col);
		}
		old_s.getAppearance().getMaterial().setDiffuseColor(kugeln[activeSphereID].col);
		new_s.getAppearance().getMaterial().setDiffuseColor(new Color3f(0f, 0f, 1f));
		
		activeSphereID = next;
		activeNextSphereID = next;
  	}
  	

  	
  	private void activateNextSphere(){
  		int next = kugeln[activeSphereID].getNext();
  		
  		Sphere new_s = spheres.get(next);
		Sphere old_s = spheres.get(activeNextSphereID);

//		if(activeNextSphereID!=activeSphereID){
			old_s.getAppearance().getMaterial().setDiffuseColor(kugeln[activeNextSphereID].col);
//		}
		new_s.getAppearance().getMaterial().setDiffuseColor(new Color3f(0f, 0f, 1f));
		activeNextSphereID = next;

  	}

  	private void activatePrevSphere(){
  		int prev = kugeln[activeSphereID].getPrev();
  		
  		Sphere new_s = spheres.get(prev);
		Sphere old_s = spheres.get(activeNextSphereID);

//		if(activeNextSphereID!=activeSphereID){
			old_s.getAppearance().getMaterial().setDiffuseColor(kugeln[activeNextSphereID].col);
//		}
		new_s.getAppearance().getMaterial().setDiffuseColor(new Color3f(0f, 0f, 1f));
		activeNextSphereID = prev;

  	}

 
	  public void mouseClicked(MouseEvent e) {
		pickCanvas.setShapeLocation(e);
		PickResult result = pickCanvas.pickAny();

		if (result == null) {
			System.out.println("Nothing picked");
		} else {
//			Primitive p = (Primitive) result.getNode(PickResult.PRIMITIVE);
//			Shape3D s = (Shape3D) result.getNode(PickResult.SHAPE3D);
			SceneGraphPath myPath = result.getSceneGraphPath();
			if (myPath.getNode(0) instanceof Sphere) {
				int pick_id = Integer.parseInt(((Sphere) myPath.getNode(0))
						.getName());
				
				activateSphere(pick_id);
				
//				if(e.getModifiers() == java.awt.event.MouseEvent.BUTTON1_MASK){
//					boxplot.showClusterFrame(pick_id);
//				}
//				
//				if(e.getModifiers() == java.awt.event.MouseEvent.BUTTON3_MASK){
//					if(instances!=null && clustering!=null){
//						penbox.drawCluster(pick_id,clustering.get(pick_id));
//					}
//				}
				//System.out.println("Picked:" + pick_id + " " + kugeln[pick_id]);
			}

		}
	}	
	  

  
public void mouseEntered(MouseEvent arg0) {}

public void mouseExited(MouseEvent arg0) {}

public void mousePressed(MouseEvent arg0) {}

public void mouseReleased(MouseEvent arg0) {}

public void keyPressed(KeyEvent k) {
	if(k.getKeyCode() == KeyEvent.VK_RIGHT && k.isShiftDown()){
		activateNextSphere();
	}

	if(k.getKeyCode() == KeyEvent.VK_LEFT && k.isShiftDown()){
		activatePrevSphere();
	}

//	if(k.getKeyCode() == KeyEvent.VK_1 && boxplot!=null)
//			boxplot.showClusterFrame(kugeln[activeNextSphereID].id);
	
	if(k.getKeyCode() == KeyEvent.VK_2 && 
	   instances!=null && clustering!=null && instances.relationName().equals("Pendigits") ){
		penbox.drawCluster(kugeln[activeNextSphereID].id,clustering.get(kugeln[activeNextSphereID].id));
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {}
		((JPanel) getParent()).grabFocus();
		c.requestFocus();
	}
	
	
	if(k.getKeyCode() == KeyEvent.VK_LEFT ||
	   k.getKeyCode() == KeyEvent.VK_RIGHT ||
	   k.getKeyCode() == KeyEvent.VK_UP ||
	   k.getKeyCode() == KeyEvent.VK_DOWN
	){
		for (int i = 0; i <10; i++) {
			double translation_faktor = 0.0025;
			Transform3D rotation = new Transform3D();
			box.getTransform(rotation);
		    Vector3d pos = new Vector3d();
		    rotation.get(pos);
			if(k.getKeyCode() == KeyEvent.VK_LEFT && !k.isShiftDown()){
			      pos.x-=translation_faktor;
			}
			if(k.getKeyCode() == KeyEvent.VK_RIGHT && !k.isShiftDown()){
			      pos.x+=translation_faktor;
			}
			if(k.getKeyCode() == KeyEvent.VK_DOWN && !k.isShiftDown()){
			      pos.y-=translation_faktor;
			}
			if(k.getKeyCode() == KeyEvent.VK_UP && !k.isShiftDown()){
			      pos.y+=translation_faktor;
			}
			if(k.getKeyCode() == KeyEvent.VK_DOWN && k.isShiftDown()){
			      pos.z-=0.01;
			}
			if(k.getKeyCode() == KeyEvent.VK_UP && k.isShiftDown()){
			      pos.z+=0.01;
			}
		    rotation.setTranslation(pos);
		    box.setTransform(rotation);
		}
	}
    
}


public void keyTyped(KeyEvent arg0) {}

public void keyReleased(KeyEvent arg0) {}

public void closeFrames(){
//	if (boxplot!=null)
//		boxplot.setVisible(false);
	if(penbox!=null)
		penbox.dispose();
}

}


