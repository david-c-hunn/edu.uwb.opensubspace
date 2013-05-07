/**
 * 
 */
package i9.subspace.sarc;



/**
 * @author dave
 *
 */
public class NormalPDFDistance extends i9.subspace.sarc.Distance {
  
  /**
   * @return -1 if dist1 is worse than dist2, 1 if dist1 is better than dist2,
   *         and 0 if dist1 is equal to dist2.
   */
  public int compare(double dist1, double dist2) {
    if (dist1 < dist2) {
      return -1;
    } else if(dist1 > dist2) {
      return 1;
    } else{
      return 0;
    }
  }
  
  public double calc(double[] a, double[] b, double[] spread) {
    double dist = 0.0;
    
    for (int i = 0; i < b.length; ++i) {
      dist += - Math.pow(a[i] - b[i], 2) / (2 * spread[i]);
    }
    dist = Math.exp(dist);    
         
    return dist;
  }

  @Override
  public double calc(double[] a, double[] b, double[] spread, double[] weight, double lambda) {     
    return calc(a, b, spread);
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }


}
