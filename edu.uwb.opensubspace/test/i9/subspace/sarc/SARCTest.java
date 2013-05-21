package i9.subspace.sarc;

import static org.junit.Assert.*;
import i9.subspace.base.ArffStorage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uwb.subspace.sarc.SARC;
import uwb.subspace.sepc.SEPC;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class SARCTest {
  private SARC sarc;
  
  @Before
  public void setUp() throws Exception {
    DataSource source = new DataSource("Databases/synth_dbsizescale/S1500.arff");
    Instances dataSet = source.getDataSet();
    ArffStorage arff = new ArffStorage(dataSet);
    
//    sarc = new SARC(0.01,  /* alpha */ 
//                    0.3,   /* beta */
//                    0.01,  /* epsilon */
//                    100.0, /* minQuality */
//                    0,     /* numClusters*/
//                    arff   /* dbStorage */  
//                   );
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public final void testGetSampleSize() {
    assertEquals("should be 2", 2, sarc.getSampleSize());
  }

  @Test
  public final void testGetNumTrials() {
    assertEquals("should be ", 143245, sarc.getNumTrials());
  }

}
