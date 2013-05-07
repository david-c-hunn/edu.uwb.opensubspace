package weka.clusterquality;

import i9.subspace.base.Cluster;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import jsc.util.Arrays;

import weka.core.Instance;
import weka.core.Instances;

public class ConfusionMatrix extends ClusterQualityMeasure {
  /** 
   * The matrix of counts. The row index corresponds to true clusters
   * and the columns correspond to the found clusters.
   */
  private int[][] m_matrix;
  
  private List<String> m_classes;
  
  public ConfusionMatrix() {
    // TODO Auto-generated constructor stub
  } 

  @Override
  public void calculateQuality(ArrayList<Cluster> clusterList,
      Instances instances, ArrayList<Cluster> trueclusters) {
    int column = 0;
    int row = 0;
    int class_idx = 0;
    
    // TODO: check for invalid inputs
    m_matrix = new int[instances.numClasses()][clusterList.size()];
    m_classes = new ArrayList<String>(instances.numClasses());
    
    // initialize all matrix entries to zero
    for (int r = 0; r < m_matrix.length; ++r) {
      for (int c = 0; c < m_matrix[r].length; ++c) {
        m_matrix[r][c] = 0;
      }
    }
    class_idx = instances.classIndex();
    for (Cluster foundCluster : clusterList) {
      for (int obj : foundCluster.m_objects) {
        Instance inst = instances.instance(obj);
        String the_class = inst.stringValue(class_idx);
        row = m_classes.indexOf(the_class);
        if (row < 0) {
          m_classes.add(the_class);
          row = m_classes.indexOf(the_class);
        }
        row = m_classes.indexOf(the_class);
        m_matrix[row][column]++;
      }
      column++;
    }
  }
  
  @Override
  public String getCustomOutput() {
    StringBuilder ret_val = new StringBuilder();
    
    for (int r = 0; r < m_matrix.length; ++r) {
      ret_val.append(m_classes.get(r) + " ");
      for (int c = 0; c< m_matrix[r].length; ++c) {
        ret_val.append(m_matrix[r][c]);
        ret_val.append(" ");
      }
      ret_val.append('\n');
    }
    
    return ret_val.toString();
  }
  
  @Override
  public String getName() {
    return "Confusion Matrix";
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
