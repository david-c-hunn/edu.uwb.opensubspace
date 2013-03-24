package weka.gui.visualize.subspace.mds;

import Jama.Matrix;

/** Interface for distance functions.
 * 
 * @author Sascha
 *
 */
public interface DistanceFunction {
	
	/** Returns distance between points i and j in given point matrix.
	 * @param ptsMatrix matrix of points
	 * @param i index of first point
	 * @param j index of second point
	 * @return
	 */
	public double getDistance(Matrix ptsMatrix, int i, int j);
	
	/** Returns matrix of dissimilarity values for given point matrix.
	 * @param ptsMatrix matrix of points
	 * @return matrix of dissimilarity values.
	 */
	public Matrix getDistances(Matrix ptsMatrix);
	
}