/**
 * 
 */
package i9.subspace.sam;

/**
 * @author dave
 *
 * TODO: 
 */
public class SquaredDifference implements Distance {

  /**
   * @return -1 if dist1 is worse than dist2, 1 if dist1 is better than dist2,
   *         and 0 if dist1 is equal to dist2.
   */
  @Override
  public int compare(double dist1, double dist2) {
    if (dist1 > dist2) {
      return -1;
    } else if(dist1 < dist2) {
      return 1;
    } else{
      return 0;
    }
  }
  
  @Override
  public double calc(double[] a, double[] b) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public double calc(double[] a, double[] b, double[] spread) {
    double dist = 0.0;
    
    for (int i = 0; i < b.length; ++i) {
      dist += Math.pow(a[i] - b[i], 2) / (2 * Math.pow(spread[i], 2));
    }    
         
    return dist;
  }

  @Override
  public double calc(double[] a, double[] b, double[] spread, double[] weight, double lambda) {
    double dist = 0.0;
    
    for (int i = 0; i < b.length; ++i) {
      dist += Math.pow(a[i] - b[i], 2) / (2 * Math.pow(spread[i], 2));
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
