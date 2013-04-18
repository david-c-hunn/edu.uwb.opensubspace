/**
 * 
 */
package i9.subspace.sarc;

/**
 * @author dave
 *
 */
public class NegativeEntropyNormalPDFDistance implements Distance {
  
  /* (non-Javadoc)
   * @see i9.subspace.sarc.Distance#compare(double, double)
   */
  @Override
  public int compare(double dist1, double dist2) {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see i9.subspace.sarc.Distance#calc(double[], double[])
   */
  @Override
  public double calc(double[] a, double[] b) {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see i9.subspace.sarc.Distance#calc(double[], double[], double[])
   */
  @Override
  public double calc(double[] a, double[] b, double[] spread) {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see i9.subspace.sarc.Distance#calc(double[], double[], double[], double[], double)
   */
  @Override
  public double calc(double[] a, double[] b, double[] spread, double[] weight, double lambda) {
    double dist = 0.0;
    
    for (int i = 0; i < b.length; ++i) {
      dist += - weight[i] * Math.pow(a[i] - b[i], 2) / (2 * Math.pow(spread[i], 2));
      dist += - lambda * weight[i] * Math.log(weight[i]);
    }
    dist = Math.exp(dist);    
         
    return dist;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
