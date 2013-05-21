package i9.subspace.sepc;

import static org.junit.Assert.*;

import java.io.File;

import i9.data.core.DBStorage;
import i9.data.core.DataSet;
import i9.subspace.base.ArffStorage;

import org.junit.Test;

import uwb.subspace.sepc.SEPC;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class SEPCTest {
  
  @Test
  public final void testGetMu_0() throws Exception {
    DataSource source = new DataSource("Databases/synth_dbsizescale/S1500.arff");
    Instances dataSet = source.getDataSet();
    ArffStorage arff = new ArffStorage(dataSet);
    SEPC sepc = new SEPC(0.01,   /* alpha */
                         0.3,    /* beta */
                         0.01,   /* epsilon */
                         0,      /* mu_0 */
                         0,      /* numClusters*/
                         100,    /* width */
                         0.75,   /* maxOverlap */
                         5,      /* maxUnmatchedSubspaces */ 
                         0.5,    /* minSubspaceSize */
                         true,   /* disjointMode */ 
                         arff    /* dbStorage */  );
    double mu_0 = sepc.getMu_0();
    System.out.println(mu_0);
    assertEquals("mu_0 should be approx. 2540263", 9032046.0, mu_0, 1);
  }
  
  @Test
  public final void testGetK() throws Exception {
    DataSource source = new DataSource("Databases/synth_dbsizescale/S1500.arff");
    Instances dataSet = source.getDataSet();
    ArffStorage arff = new ArffStorage(dataSet);
    SEPC sepc = new SEPC(0.01,   /* alpha */
                         0.3,    /* beta */
                         0.01,   /* epsilon */
                         0,      /* mu_0 */
                         0,      /* numClusters*/
                         100,    /* width */
                         0.75,   /* maxOverlap */
                         5,      /* maxUnmatchedSubspaces */ 
                         0.5,    /* minSubspaceSize */
                         true,   /* disjointMode */ 
                         arff    /* dbStorage */  );
    int k = sepc.getK();
    assertEquals("k should be ", 143245, k);
  }
  
  @Test
  public final void testGetS() throws Exception {
    DataSource source = new DataSource("Databases/synth_dbsizescale/S1500.arff");
    Instances dataSet = source.getDataSet();
    ArffStorage arff = new ArffStorage(dataSet);
    SEPC sepc = new SEPC(0.01,   /* alpha */
                         0.3,    /* beta */
                         0.01,   /* epsilon */
                         0,      /* mu_0 */
                         0,      /* numClusters*/
                         100,    /* width */
                         0.75,   /* maxOverlap */
                         5,      /* maxUnmatchedSubspaces */ 
                         0.5,    /* minSubspaceSize */
                         true,   /* disjointMode */ 
                         arff    /* dbStorage */  );
    int s = sepc.getS();
    assertEquals("s should be 2", 2, s);
  }

}
