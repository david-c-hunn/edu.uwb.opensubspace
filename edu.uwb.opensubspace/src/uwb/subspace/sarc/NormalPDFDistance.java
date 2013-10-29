package uwb.subspace.sarc;


/**
 * NormalPDFDistance or Normal Probability Density Function Distance
 * in English. This distance metric assumes that clusters are modeled
 * as multivariate Gaussian distributions. 
 * 
 * @author Dave Hunn
 *
 */
public class NormalPDFDistance extends uwb.subspace.sarc.Distance {
  
  /**
   * A higher likelihood is better.
   * 
   * @param score1 is a likelihood score to compare.
   * @param score2 is another likelihood score to compare.
   * 
   * @return -1 if dist1 is worse than dist2, 1 if dist1 is better than dist2,
   *         and 0 if dist1 is equal to dist2. 
   */
  @Override
  public int compareLikelihood(double score1, double score2) {
    if (score1 < score2) {
      return -1;
    } else if(score1 > score2) {
      return 1;
    } else {
      return 0;
    }
  }
  
  /**
   * Shorter distances are preferable.
   * 
   * @param dist is a distance to compare.
   * @param dist2 is another distance to compare.
   * 
   * @return -1 if dist1 is worse than dist2, 1 if dist1 is better than dist2,
   *         and 0 if dist1 is equal to dist2. 
   */
  @Override
  public int compareDistance(double dist1, double dist2) {
    if (dist1 > dist2) {
      return -1;
    } else if(dist1 < dist2) {
      return 1;
    } else {
      return 0;
    }
  }
    
  /**
   * Calculates a weighted distance between point <code>a</code> and point 
   * <code>b</code>. The distance is a modified city block or L1 distance.
   * It is calculated as the product of the weight and the squared difference  
   * between <code>a</code> and <code>b</code> along each dimension.
   * 
   * All of the parameters should have the same length.
   *   
   * @param a is an object/point.
   * @param b is an object/point.
   * @param spread is a measure of spread (e.g. standard deviation).
   * @param weight is a weight applied in each dimension that indicates the 
   *        relative importance of a given dimension.
   */
  @Override
  public double calcDistance(double[] a, double[] b, double[] spread, 
      double[] weight) {     
    double dist = 0.0;
    
    for (int i = 0; i < b.length; ++i) {
      //dist += weight[i] * Math.abs(a[i] - b[i]); 
      dist += weight[i] * Math.pow(a[i] - b[i], 2);
    }
    
    return dist;
  }
  
  /**
   * Returns a result between zero and one that represents the likelihood that
   * <code>a</code> is a member of the cluster centered at <code>b</code> (or
   * vis versa). 
   *   
   * @param a is an object/point.
   * @param b is an object/point.
   * @param spread is a measure of spread (e.g. standard deviation).
   * @param weight is not currently used, but it is a weight applied in each 
   *        dimension that indicates the  relative importance of a given 
   *        dimension.
   * @param lambda is not currently used. See SoftCluster for information on
   *        how lambda is used.
   */
  @Override
  public double calcLikelihood(double[] a, double[] b, double[] spread, 
      double[] weight, double lambda) {     
    double score = 0.0;
    
    for (int i = 0; i < b.length; ++i) {
      score += - Math.exp(- Math.pow(a[i] - b[i], 2) / (2 * spread[i])); 
    }
    score = Math.exp(score);    
         
    return score;
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }


}
