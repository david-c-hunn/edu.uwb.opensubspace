package weka.gui.visualize.subspace.mds;


import weka.gui.visualize.subspace.mds.DistanceFunction;

import java.util.logging.Level;
import java.util.logging.Logger;

import Jama.Matrix;

public abstract class MultidimensionalScaling {

	public static Logger logger = Logger.getAnonymousLogger();
	
	
	/** Creates a matrix with given row dimensionality and 2 columns. 
	 * Entries are random 
	 * 
	 * @param rowDimension row dimensionality
	 * @param dimension Dimensionality of the plot
	 * @return random configuration matrix
	 */
	private static ConfigurationMatrix createRandomMatrix(int rowDimension, int dimension, int seed) {
		return ConfigurationMatrix.random(rowDimension, dimension, seed);
	}

	private static Matrix getMPInverse(Matrix V) {
		int n = V.getRowDimension();
		Matrix erg;
		Matrix ones = new Matrix(n, n, 1.0);
		
		erg = V.plus(ones);
		erg = erg.inverse();
		erg = erg.minus(ones.times(1.0/Math.pow(n,2)));
		
		return erg;
	}

	private static Matrix getV(WeightMatrix wMatrix) {
		int dim = wMatrix.getRowDimension();
		double rowSum;
		double tmpVal;
		Matrix V = new Matrix(dim, dim, 0.0);
		
		/* Fill V with values */
		for (int i = 0; i < dim; i++) {
			rowSum = 0.0;
			for (int j = 0; j < dim; j++) {
				if (i != j) {
					tmpVal = wMatrix.get(i, j);
					V.set(i,j,-tmpVal);
					rowSum += tmpVal;
				}
			}
			V.set(i,i,rowSum);
		}
		return V;
	}

	public static double calculateNormalizedSTRESS(ConfigurationMatrix confMatrix, DisimilarityMatrix disMatrix){
		return calculateRawSTRESS(confMatrix, disMatrix, new EuclideanDistanceFunction()) / disMatrix.getSquaredDisimilarities();
	}

	/** Determines normalized STRESS of current configuration using the provided distance matrix.
	 * 
	 * @param disMatrix disparities
	 * @return STRESS value
	 */
	public static double calculateRawSTRESS(ConfigurationMatrix confMatrix, DisimilarityMatrix disMatrix, DistanceFunction df){
		double res = 0.0;		// result
		double tmp = 0.0;		// temp var
		
		/* Calculate differences between dissimiliarities and distances in the configuration
		 * for all points.
		 */
		for (int i = 0; i < disMatrix.getRowDimension(); i++){
			for (int j = i+1; j < disMatrix.getColumnDimension(); j++){
				tmp = disMatrix.get(i, j)-df.getDistance(confMatrix,i,j); // delta(i,j)-dist(i,j)
				tmp = Math.pow(tmp,2);	// (delta(i,j)-dist(i,j))^2
				res += tmp; 				// sum_i=1^n{(delta(i,j)-dist(i,j))^2}
			}
		}
		// logger.info("STRESS is: "+res);
		return res;
	}
	
	private static Matrix getB(ConfigurationMatrix confMatrix,
			DisimilarityMatrix disMatrix,
			DistanceFunction df) {
		
	double entryValB;
	double dist;
	double tmpDiag;
	Matrix B = new Matrix(confMatrix.getRowDimension(),confMatrix.getRowDimension(),0.0);
	
	/* Iterate over upper-right triangle of configuration matrix */
	for (int i = 0; i < B.getRowDimension(); i++){
		for (int j = i+1; j < B.getColumnDimension(); j++){
			entryValB = 0.0;
			
			/* Determine entry value for matrix B.
			 * If distance between configuration points i and j is zero, entry stays zero.
			 */
			dist = df.getDistance(confMatrix,i,j);
			if (dist > 0) {
				entryValB -= (disMatrix.get(i,j) / dist);
			}
			
			/* B is symmetric so enter value two times */
			B.set(i,j,entryValB);
			B.set(j,i,entryValB);
		}
	}
	/* for each row of B ... */
	for (int i = 0; i < B.getRowDimension(); i++){
		tmpDiag = 0.0;			// reset tmpDiag
		
		/* Build negative sum of all row elements */
		for (int j = 0; j < B.getColumnDimension(); j++){
			tmpDiag -= B.get(i,j);
		}
		B.set(i,i,tmpDiag);		// set diagonal entry
	}
		return B;
	}

	/** Perform Guttman transformation on this configuration.
	 *  For details refer to "Modern Multidimensional scaling" pp. 190ff.
	 * 
	 *  @param disMatrix distance matrix
	 */
	public static ConfigurationMatrix guttmanTransform(
			ConfigurationMatrix confMatrix, 
			DisimilarityMatrix disMatrix, 
			DistanceFunction df, 
			WeightMatrix wMatrix) {
		
		/* Initialize some temporary matrices */
		Matrix B = getB(confMatrix,disMatrix,df); 
		Matrix V = getV(wMatrix);
		Matrix invV = getMPInverse(V);
		Matrix updatedConf = B.times(confMatrix);
		
		// updatedConf = updatedConf.times(Math.pow(confMatrix.getRowDimension(),-1));
		
		/* Perform update rule */
		updatedConf = invV.times(updatedConf);
		
		/* Return resulting configuration matrix */
		return (new ConfigurationMatrix(updatedConf));
	}

	/** Perform SMACOF multidimensional scaling algorithm to determine a configuration matrix being a local minimum
	 * of the STRESS function using a provided dissimilarity matrix. This is achieved by iterative majorization approach, in
	 * which the starting configuration is iteratively improved, until the function values of the STRESS
	 * function between two iterations falls below a given threshold.
	 * 
	 * @param startConf starting configuration matrix
  	 * @param dimension Dimensionality of the plot
	 * @param disMatrix dissimilarity matrix
	 * @param epsilon final threshold for stress improvement 
	 * @param maxiter maximum # of iterations
	 * @param wMatrix weight matrix
	 * @return target configuration matrix
	 * 
	 */
	public static ConfigurationMatrix smacof(DisimilarityMatrix disMatrix,
											int dimension,
											double epsilon, 
											double maxiter, DistanceFunction df, WeightMatrix wMatrix, int seed) {
		
		/* iteration counter */
		int k = 0;
		
		/* STRESS improvement of iteration */
		double stressDiff = Double.MAX_VALUE;		// stress of last iteration
		
		/* STRESS of last iteration */
		double stressOld = Double.MAX_VALUE;
		
		/* STRESS of current iteration */
		double stressNew = Double.MAX_VALUE;						// current stress
		
		/* Backup temporary copy of starting configuration */
		ConfigurationMatrix tmpMatrix = createRandomMatrix(disMatrix.getRowDimension(),dimension, seed);

		/* Main loop - leave when maximum of iterations is reached or STRESS improvement is below epsilon */
		while ((stressDiff >= epsilon) && (k <= maxiter)){
						
			/* Increment iteration count */
			k++;
			/* Backup STRESS value from last iteration step */
			stressOld = stressNew;

			/* Perform Guttman-Transformation step */
			tmpMatrix = guttmanTransform(tmpMatrix,disMatrix,df,wMatrix);

			/* Calculate STRESS improvement of this iteration */
			stressNew = calculateNormalizedSTRESS(tmpMatrix,disMatrix);
			stressDiff = stressOld - stressNew;
		}
		
		/* Save stress in target configuration object */
		tmpMatrix.setStress(stressNew);
		
		/* Return the final target configuration */
		return tmpMatrix;
	}
	
	/** Computes smacof with random restart
	 * @param k number of restarts
	 * @param dimension Dimensionality of the plot
	 * @param disMatrix disimilarity matrix
	 * @param epsilon epsilon threshold
	 * @param maxiter maximum of iterations
	 * @param wMatrix 
	 * @return
	 */
	
	//k = 5; epsilon = 1/10000; maxiter = 500; wMatrix = 1
	public static ConfigurationMatrix smacofWithRandomRestart(int k, DisimilarityMatrix disMatrix,
														int dimension,
														double epsilon, 
														double maxiter, DistanceFunction df, WeightMatrix wMatrix){
		ConfigurationMatrix ergMatrix = null;
		ConfigurationMatrix tmpMatrix = null;
		double bestSTRESS = Double.MAX_VALUE;
		logger.setLevel(Level.OFF);
		logger.info("Starting SMACOF algorithm with "+k+" random restart runs and "+maxiter+" iterations per run.");
		for (int i = 1; i <= k; i++) {
			logger.info("Starting "+i+". run ...");
			tmpMatrix = smacof(disMatrix, dimension, epsilon, maxiter, df, wMatrix, i*5);
			if (bestSTRESS > tmpMatrix.getStress()) {
				ergMatrix = tmpMatrix;
				bestSTRESS = tmpMatrix.getStress();
				logger.info("Found configuration with new STRESS-minimum: "+bestSTRESS);
			}
			logger.info("Completed "+i+" of "+k+" runs.");
		}
		logger.info("Completed! Lowest STRESS: "+bestSTRESS);
		return ergMatrix;
	}
}
