package uantwerp.maximizer;

import java.util.HashMap;
import java.util.Map;

/**
 * Finds maximal frequent itemsets in a one-dimensional projection.
 *
 * @author M. Emin Aksehirli
 *
 */
public class CartiMaximizer implements CartiFiner {
    private int minLen;

    public CartiMaximizer() {
        // for default minLen
    }

    public CartiMaximizer(int minLen) { this.minLen = minLen; }

    @Override
    public Map<Integer, Integer> mineOneDim(double[] dim, int k) {
        int minSup = (int)(k * 0.6);
        int minLen = minSup;

        if (minLen < this.minLen) {
            minLen = this.minLen;
            minSup = this.minLen;
        }

        int[] cartStarts = OneDCartifier.findCartStarts(dim, k, false);

        int start = 0;
        int end = start + minSup;

        Map<Integer, Integer> freqs = new HashMap<>();

        while (end < cartStarts.length - 1) {
            while (cartStarts[end + 1] == cartStarts[end] && end < cartStarts.length - 2) {
                end++;
            }
            // this is needed in case the end is increased in the prev. step
            start = end - minSup;

            if (cartStarts[start] + k - cartStarts[end] > minLen) {
                freqs.put(cartStarts[start] + k, cartStarts[end]);
            }

            while (start < cartStarts.length - minSup &&
                   cartStarts[start + 1] == cartStarts[start]) {
                start++;
            }

            start++;
            end = start + minSup;
        }
        return freqs;
    }

    @Override
    public Map<Integer, Map<Integer, Integer>> mineCarts(double[][] dims, int k) {
        Map<Integer, Map<Integer, Integer>> allFreqs = new HashMap<>();

        for (int dimIx = 0; dimIx < dims.length; dimIx++) {
            Map<Integer, Integer> freqs = mineOneDim(dims[dimIx], k);
            allFreqs.put(dimIx, freqs);
        }
        return allFreqs;
    }
}