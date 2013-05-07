/**
 * 
 */
package i9.subspace.sarc;

/**
 * @author dave
 *
 */
public abstract class Distance {
  /** 
   * @return -1 if dist1 is worse than dist2, 1 if dist1 is better than dist2,
   *         and 0 if dist1 is equal to dist2.
   */
  public int compare(double dist1, double dist2) {
    return -1;
  }
  
  public double calc(double[] a, double[] b, double[] spread, double[] weights, double lambda) {
    return 0;
  }
}
