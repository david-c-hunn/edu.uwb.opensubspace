package uantwerp.maximizer;

import java.util.Map;

public interface CartiFiner {
    Map<Integer, Integer> mineOneDim(final double[] dim, int k);

    /**
     * Mines the carts in all of the dimensions and returns a map of them.
     *
     * @param dims
     * @param k
     * @return First key is the dimension. For each dimension carts are formed as
     *         {@code end -> start}.
     */
    Map<Integer, Map<Integer, Integer>> mineCarts(double[][] dims, int k);
}