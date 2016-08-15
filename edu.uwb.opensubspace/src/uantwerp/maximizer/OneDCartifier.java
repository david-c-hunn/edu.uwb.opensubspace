package uantwerp.maximizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import be.uantwerpen.adrem.cart.model.Pair;

/**
 * This class implements a basic k-NN cartifier for one-dimensional projections.
 *
 * @author M. Emin Aksehirli
 *
 */
public class OneDCartifier {
    /**
     * Find the starts of the k-NN neighborhoods, a.k.a. carts, for the
     * one-dimensional projection {@code dim}. Since the neighborhood size is
     * fixed, returns only the starting objects.
     *
     * Beware that this method first sorts the {@code dim} and the returns the
     * indices on this sorted list.
     *
     *
     * @param dim
     *          Values of one-dimensional projection
     * @param k
     *          Neighborhood size
     * @param extendDim
     *
     * @return
     */
    public static int[] findCartStarts(final double[] dim, int k, boolean extendDim) {
        double[] sortedDim = Arrays.copyOf(dim, dim.length);
        Arrays.sort(sortedDim);

        int exStart = 0;
        int exEnd = 0;
        if (extendDim) {
            sortedDim = expandTheDim(k, sortedDim);
            exStart = k / 2;
            exEnd = k - exStart;
        }
        int[] cartStarts = new int[sortedDim.length];
        int cartStart = 0;

        cartStarts[0] = cartStart;

        for (int objIx = 0; objIx < sortedDim.length; objIx++) {
            final double obj = sortedDim[objIx];
            int cartEnd = cartStart + k; // cartEnd is exclusive

            while (cartEnd < sortedDim.length &&
                   dist(obj, sortedDim[cartEnd]) < dist(obj, sortedDim[cartStart])) {
                cartStart++;
                cartEnd++;
            }

            cartStarts[objIx] = cartStart;
        }
        return Arrays.copyOfRange(cartStarts, exStart, cartStarts.length - exEnd);
    }

    /**
     * This method can be used to minimise the effect of artifacts caused by the
     * transformation. It adds auto-generated data objects before and after the
     * actual data-objects in the dataset.
     *
     * The generated values have the same distance between each consequtive pair.
     * And the distance is equal to the average distance between the consequent
     * pairs in the original dataset.
     *
     * Expanding the dimension can improve the accuracy if the dataset is noisy
     * enough.
     *
     * @param k
     *          neighborhood size, the total number of added objects will be equal
     *          to {@code k}.
     * @param sortedDim
     *          The values that are going to be expanded.
     * @return A new array with the size of {@code sortedDim.length + k}.
     */
    public static double[] expandTheDim(int k, double[] sortedDim) {
        double[] expandedDim = new double[sortedDim.length + k];
        final int expand = k / 2;
        System.arraycopy(sortedDim, 0, expandedDim, expand, sortedDim.length);
        double avg = 0;
        for (int i = 0; i < sortedDim.length - 1; i++) {
            avg += sortedDim[i + 1] - sortedDim[i];
        }
        avg /= sortedDim.length - 1;
        for (int i = 0; i < expand; i++) {
            expandedDim[i] = sortedDim[0] - ((expand - i) * avg);
        }
        for (int i = sortedDim.length + expand; i < expandedDim.length; i++) {
            expandedDim[i] =
                sortedDim[sortedDim.length - 1] + (i - sortedDim.length - expand + 1) * avg;
        }

        // System.out.println("Dimension extended by: " + expand + ", "
        // + sortedDim.length + " => " + expandedDim.length);
        return expandedDim;
    }

    public static double[][] transpose(ArrayList<double[]> data) {
        double[][] dims = new double[data.get(0).length][data.size()];

        for (int dimIx = 0; dimIx < dims.length; dimIx++) {
            int rowIx = 0;
            for (double[] row : data) {
                dims[dimIx][rowIx] = row[dimIx];
                rowIx++;
            }
        }
        return dims;
    }

    /**
     * Takes a list of objects, where each object is a double[], and returns a 2D
     * array of Pairs, where each row corresponds to an object and each column is
     * a pair of object ID and the value for the attribute.
     *
     * E.g., {{1,2},{3,4},{6,7}} becomes
     * {{[1,0],[2,0]},{[3,1],[4,1]},{[6,1],[7,1]}}
     *
     * @param objects
     * @return
     */
    public static Pair[][] toPairs(List<double[]> objects) {
        Pair[][] origData = new Pair[objects.size()][];
        int objIx = 0;
        for (double[] obj : objects) {
            Pair[] pairObj = new Pair[obj.length];
            for (int i = 0; i < obj.length; i++) {
                pairObj[i] = new Pair(obj[i], objIx);
            }
            origData[objIx] = pairObj;
            objIx++;
        }
        return origData;
    }

    public static double dist(double d, double e) { return Math.abs(d - e); }
}