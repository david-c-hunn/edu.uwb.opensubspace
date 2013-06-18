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
   
  @Override
  public void calculateQuality(ArrayList<Cluster> clusterList,
      Instances instances, ArrayList<Cluster> trueclusters) {
    int column = 0;
    int row = 0;
    int class_idx = 0;
    
    // TODO: check for invalid inputs
    m_matrix = new int[instances.numClasses()][clusterList.size()];
    m_matrix = new int[clusterList.size()][instances.numClasses()];
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
        column = m_classes.indexOf(the_class);
        if (column < 0) {
          m_classes.add(the_class);
          column = m_classes.indexOf(the_class);
        }
        column = m_classes.indexOf(the_class);
        m_matrix[row][column]++;
      }
      row++;
    }
  }
  
  @Override
  public String getCustomOutput() {
    StringBuilder ret_val = new StringBuilder();
    
    ret_val.append("\t");
    for (String label : m_classes) {
      ret_val.append(label + "\t");
    }
    ret_val.append("\n");
    
    for (int r = 0; r < m_matrix.length; ++r) {
      ret_val.append("F" + r + ":\t");
      for (int c = 0; c< m_matrix[r].length; ++c) {
        ret_val.append(m_matrix[r][c]);
        ret_val.append("\t");
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
