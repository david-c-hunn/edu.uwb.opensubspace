package weka.gui.visualize.subspace.mds;

import java.util.Random;

import Jama.Matrix;

/** This class implements a configuration of n points in m dimensions. It is saved inside a matrix provided by JAMA package. Rows are points, columns are dimensions.
 *  
 */
public class ConfigurationMatrix extends Matrix {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * Stress of the configuration.
	 */
	private double stress = 0.0;

	/** Construct a configuration matrix with #rows equals size of display set.
	 * Column dimension is determined via static class variable.
	 * Each value is random.
	 * 
	 * @param list list of image container objects
	 */
	public ConfigurationMatrix(int rows,double val, int cols) {
		super(rows,cols,val);
	}
	
	/** Creates a configuration matrix from a given matrix.
	 * @param matrix
	 */
	public ConfigurationMatrix(Matrix matrix){
		super(matrix.getArrayCopy());
	}
	
	public static ConfigurationMatrix random(int rowDim, int cols, int seed) {
			Random r = new Random(seed);
			//Matrix tmp = Matrix.random(rowDim, cols);
		    Matrix A = new Matrix(rowDim, cols);
	        for (int i = 0; i < rowDim; i++){
	            for (int j = 0; j < cols; j++){
	            	double v = r.nextDouble();
	            	A.set(i, j, v);
	            }
	        }
		return new ConfigurationMatrix(A);
	}

	/** Return distance matrix for this configuration using given distance function.
	 *
	 * @return matrix with distances
	 */
	public Matrix getDistanceMatrix(DistanceFunction df) {
		return df.getDistances(this);
	}

	/** Get maximum coefficient in given matrix column.
	 * @param dim index of matrix column
	 * @return maximum coefficient
	 */
	public double getDimensionalMax(int dim) {
		double max = Double.MIN_VALUE;
		for (int i = 0; i < getRowDimension(); i++) {
			max = Math.max(max, get(i,dim));
		}
		return max;
	}
	
	/** Get minimum coefficient in given matrix column.
	 * @param dim index of matrix column
	 * @return minimum coefficient
	 */
	public double getDimensionalMin(int dim) {
		double min = Double.MAX_VALUE;
		for (int i = 0; i < getRowDimension(); i++) {
			min = Math.min(min, get(i,dim));
		}
		return min;
	}

	/** Set stress of this configuration.
	 * @param stress
	 */
	public void setStress(double stress) {
		this.stress = stress;
	}

	/** Get stress of this configuration.
	 * @return stress
	 */
	public double getStress() {
		return stress;
	}
}