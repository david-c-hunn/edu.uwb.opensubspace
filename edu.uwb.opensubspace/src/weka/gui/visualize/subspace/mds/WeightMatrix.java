package weka.gui.visualize.subspace.mds;

import Jama.Matrix;

/** A weight matrix stores the weights between several items.
 * @author wiedenfeld
 *
 */
public class WeightMatrix extends Matrix {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -5642369315057162017L;

	
	/** Create a new weight matrix with given dimensionalities and entries all equal to one
	 * @param items number of items
	 */
	public WeightMatrix(int items) {
		super(items, items, 1.0);
	}

}
