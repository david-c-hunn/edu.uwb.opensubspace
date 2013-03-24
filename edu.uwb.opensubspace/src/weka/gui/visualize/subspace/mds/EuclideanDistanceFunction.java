package weka.gui.visualize.subspace.mds;

import Jama.Matrix;

/** Implements the euclidean (minkowski) distance function.
 *
 */
public class EuclideanDistanceFunction implements DistanceFunction {

	/* (non-Javadoc)
	 * @see i9.mdsexplore.tools.DistanceFunction#getDistance(Jama.Matrix)
	 */
	public double getDistance(Matrix ptsMatrix, int i, int j) {
		int[] rowInd = {i,j};
		double erg = getDistances(ptsMatrix.getMatrix(rowInd, 0, ptsMatrix.getColumnDimension()-1)).get(0,1);
		return erg;
	}
	
	/* (non-Javadoc)
	 * @see i9.mdsexplore.tools.DistanceFunction#getDistances(Jama.Matrix)
	 */
	public Matrix getDistances(Matrix ptsMatrix) {
		
		/* Initialize some matrices */
		Matrix ones = new Matrix(ptsMatrix.getRowDimension(),1,1); // ones
		Matrix b = ptsMatrix.times(ptsMatrix.transpose()); // = X*X'
		Matrix c = new Matrix(b.getRowDimension(),1); // diag entries of B
		for (int i = 0; i < b.getRowDimension(); i++) {
			c.set(i, 0, b.get(i,i)); // fill with diagonal elements of B
		}
		
		/* Main calculation step */
		Matrix erg = (c.times(ones.transpose()));
		erg = erg.plus(ones.times(c.transpose()));
		erg = erg.minus(b.times(2));
		
		/* Take square root of entries */
		for (int i = 0; i < erg.getRowDimension(); i++) {
			for (int j = 0; j < erg.getRowDimension(); j++) {
				erg.set(i, j, Math.sqrt(erg.get(i, j)));
			}
		}
		return erg;
	}
}
