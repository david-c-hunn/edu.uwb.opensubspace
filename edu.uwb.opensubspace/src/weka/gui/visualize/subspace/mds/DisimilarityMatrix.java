package weka.gui.visualize.subspace.mds;

import Jama.Matrix;

/**
  * Distance matrix is a matrix containing distance values betweens pairs of objects.
  */ 
public class DisimilarityMatrix extends Matrix{

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -1190200602057782172L;

	/** Create a dissimilarity matrix for a 2dim double array 
	 * 
	 * @param data, array of distances
	 * 
	 */
	public DisimilarityMatrix(double[][] data) {
		super(data);
	}

	/**
	 * @param matrix
	 */
	public DisimilarityMatrix(Matrix matrix) {
		super(matrix.getArrayCopy());
	}
	
	/** Returns the sum of squared disimilarities
	 * @return sum of squared dissimilarities
	 */
	public double getSquaredDisimilarities(){
		double erg = 0.0;
		
		/* Sum up over upper right triangle */
		for (int i = 0; i < getRowDimension(); i++) {
			for (int j = i + 1; j < getColumnDimension(); j++) {
				erg += Math.pow(get(i,j),2);
			}
		}
		return erg;
	}
}
