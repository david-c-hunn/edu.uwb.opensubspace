package weka.gui.visualize.subspace.distance3d;


import i9.subspace.base.Cluster;

import java.applet.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;

import java.util.*;

import weka.core.Instances;

//Wrapper classed, copied and changed from JMainFrame from Java3D
public class Plot3D implements AppletStub, AppletContext {

    private static int instances = 0;
    private String name;
    private Applet applet;
    private Label label = null;
    private Dimension appletSize;

    private static final String PARAM_PROP_PREFIX = "parameter.";

    public Plot3D(Kugel [] spheres, ArrayList<Cluster> clustering, Instances instances, int width, int height, double r_min, double r_max, Color[] spektrum) {

    	Applet applet = new Distance3D(spheres, clustering,instances, r_min, r_max, spektrum);
    	build(applet, width, height);
    }

    private void build(Applet applet, int width, int height) {
        ++instances;
        this.applet = applet;
        applet.setStub( this );
        appletSize = applet.getSize();
        applet.setSize( width, height );
    }

    public Applet getApplet(){
    	return applet;
    }

    // Methods from AppletStub.
    public boolean isActive() {
        return true;
    }

    public URL getDocumentBase() {
        // Returns the current directory.
        String dir = System.getProperty( "user.dir" );
        String urlDir = dir.replace( File.separatorChar, '/' );
        try {
            return new URL( "file:" + urlDir + "/");
        }
        catch ( MalformedURLException e ) {
            return null;
        }
    }

    public URL getCodeBase() {
        // Hack: loop through each item in CLASSPATH, checking if
        // the appropriately named .class file exists there.  But
        // this doesn't account for .zip files.
        String path = System.getProperty( "java.class.path" );
        Enumeration st = new StringTokenizer( path, ":" );
        while ( st.hasMoreElements() ) {
            String dir = (String) st.nextElement();
            String filename = dir + File.separatorChar + name + ".class";
            File file = new File( filename );
            if ( file.exists() ) {
                String urlDir = dir.replace( File.separatorChar, '/' );
                try {
                    return new URL( "file:" + urlDir + "/" );
                }
                catch ( MalformedURLException e ) {
                    return null;
                }
            }
        }
        return null;
    }

    public String getParameter( String name ) {
        // Return a parameter via the munged names in the properties list.
        return System.getProperty( PARAM_PROP_PREFIX + name.toLowerCase() );
    }

    public void appletResize( int width, int height ) {
        // Change the frame's size by the same amount that the applet's
        // size is changing.
    }

    public AppletContext getAppletContext() {
        return this;
    }


    // Methods from AppletContext.

    public AudioClip getAudioClip( URL url ) {
        // This is an internal undocumented routine.  However, it
        // also provides needed functionality not otherwise available.
        // I suspect that in a future release, JavaSoft will add an
        // audio content handler which encapsulates this, and then
        // we can just do a getContent just like for images.
        sun.applet.AppletAudioClip appletAudioClip = new sun.applet.AppletAudioClip( url );
        return appletAudioClip;
    }

    public Image getImage( URL url ) {
        Toolkit tk = Toolkit.getDefaultToolkit();
        try {
            ImageProducer prod = (ImageProducer) url.getContent();
            return tk.createImage( prod );
        }
        catch ( IOException e ) {
            return null;
        }
    }

    public Applet getApplet( String name ) {
        // Returns this Applet or nothing.
        if ( name.equals( this.name ) )
            return applet;
        return null;
    }

    public Enumeration getApplets() {
        // Just yields this applet.
        Vector v = new Vector();
        v.addElement( applet );
        return v.elements();
    }

    public void showDocument( URL url ) {
        // Ignore.
    }

    public void showDocument( URL url, String target ) {
        // Ignore.
    }

    public void showStatus( String status ) {
        if ( label != null )
            label.setText( status );
    }

    public void setStream( String key, java.io.InputStream stream ) {
        throw new RuntimeException("Not Implemented");
        // TODO implement setStream method
    }

    public java.io.InputStream getStream( String key ) {
		throw new RuntimeException("Not Implemented");
	// TODO implement getStream method
    }

    public java.util.Iterator getStreamKeys() {
		throw new RuntimeException("Not Implemented");
	// TODO implement getStreamKeys method
    }
}
