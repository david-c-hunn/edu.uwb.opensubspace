/**
 * 
 */
package i9.subspace.sarc;

/**
 * @author dave
 *
 */
public interface Distance {
  public int compare(double dist1, double dist2);
  public double calc(double[] a, double[] b);
  public double calc(double[] a, double[] b, double[] spread);
  public double calc(double[] a, double[] b, double[] spread, double[] weights, double lambda);
}
