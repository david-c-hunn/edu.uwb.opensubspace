/**
 * 
 */
package uwb.subspace.sarc;

/**
 * @author dave
 *
 */
public abstract class Distance {
  
  /** 
   * @return -1 if dist1 is worse than dist2, 1 if dist1 is better than dist2,
   *         and 0 if dist1 is equal to dist2.
   */
  public int compareLikelihood(double dist1, double dist2) {
    return -1;
  }
  public int compareDistance(double dist1, double dist2) {
    return -1;
  }
  
  public double calcDistance(double[] a, double[] b, double[] spread, double[] weights) {
    return 0;
  }
  
  public double calcLikelihood(double[] a, double[] b, double[] spread, double[] weights, double lambda) {
    return 0;
  }
  
}
