/**
 * 
 */
package uwb.subspace.sarc;

/**
 * @author dave
 *
 */
public class NegativeEntropyNormalPDFDistance extends Distance {

  @Override
  public int compareLikelihood(double dist1, double dist2) {
    if (dist1 > dist2) { 
      return 1; // dist1 is better than dist2
    } else if(dist1 < dist2) { 
      return -1; // dist2 is better than dist1
    } else{
      return 0; // dist1 is equal to dist2
    }
  }

  @Override
  public int compareDistance(double dist1, double dist2) {
    if (dist1 < dist2) { 
      return 1; // dist1 is better than dist2
    } else if(dist1 > dist2) { 
      return -1; // dist2 is better than dist1
    } else{
      return 0; // dist1 is equal to dist2
    }
  }
  
  @Override
  public double calcLikelihood(double[] a, double[] b, double[] spread, double[] weight, double lambda) {
    double dist = 0.0;

    for (int i = 0; i < b.length; ++i) {
      dist += - weight[i] * Math.pow(a[i] - b[i], 2) / (2 * spread[i])
              + lambda * weight[i] * Math.log(weight[i]);
    }
    dist = Math.exp(dist);    
    if (dist < 0)
      System.err.println("Negative distance!");

    if (Double.isNaN(dist)) {
      System.err.println("We have a problem!");
    }

    return dist;
  }

  @Override
  public double calcDistance(double[] a, double[] b, double[] spread, double[] weight) {
    double dist = 0.0;

    for (int i = 0; i < b.length; ++i) {
      dist += Math.pow(a[i] - b[i], 2) / weight[i];
    }    
    
    return dist;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
