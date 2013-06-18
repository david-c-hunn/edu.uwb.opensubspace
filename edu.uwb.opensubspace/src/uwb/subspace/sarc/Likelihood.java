/**
 * 
 */
package uwb.subspace.sarc;

/**
 * @author dave
 *
 */
public class Likelihood extends Distance {

  @Override
  public int compareLikelihood(double dist1, double dist2) {
    if (dist1 < dist2) {
      return -1;
    } else if(dist1 > dist2) {
      return 1;
    } else {
      return 0;
    }
  }

  @Override
  public int compareDistance(double dist1, double dist2) {
    return compareLikelihood(dist1, dist2);
  }
  
  @Override
  public double calcLikelihood(double[] a, double[] b, double[] spread, double[] weight, double lambda) {
    double dist = 0.0;
    
    for (int i = 0; i < b.length; ++i) {
      dist += -Math.pow(a[i] - b[i], 2) / (2 * spread[i]);
    }
    dist = Math.exp(dist);    
         
    return dist;  
  }

  @Override
  public double calcDistance(double[] a, double[] b, double[] spread, double[] weight) {     
    return calcLikelihood(a, b, spread, weight, 0);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
