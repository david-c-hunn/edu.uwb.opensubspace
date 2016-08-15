package uantwerp.maximizer;

import static java.util.Collections.singletonList;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import be.uantwerpen.adrem.cart.io.InputFile;
import be.uantwerpen.adrem.cart.model.Pair;

import com.google.common.collect.HashMultimap;

/**
 * This is the main algorithm.
 *
 * @author M. Emin Aksehirli
 *
 */
public abstract class MaximalMinerCombiner {
    protected static int skipCount;
    protected int numOfDims;
    protected Pair[][] origData;
    protected Pair[][] orderedDims;
    protected int[][] ids2Orders;
    private Item[][] allItems;
    protected HashMultimap<Integer, int[]> allMineds;
    protected int minLen;
    private double[][] dims;
    private FreqCollector freqCollector;
    private List<Integer> theAllDims;

    /**
     * This is the main algorithm. It reads the data from {@code inputFile} and
     * runs the {@code CLON}.
     *
     * @param inputFile
     */
    public MaximalMinerCombiner(InputFile inputFile) {
        try {
            ArrayList<double[]> data = inputFile.getData();
            dims = OneDCartifier.transpose(data);
            // System.out.println("Dims data read and transposed");
            origData = OneDCartifier.toPairs(data);
            // System.out.println("Data pairs are created.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        numOfDims = dims.length;

        orderedDims = new Pair[dims.length][];
        ids2Orders = new int[dims.length][];
        for (int dimIx = 0; dimIx < numOfDims; dimIx++) {
            orderedDims[dimIx] = getOrd2Id(origData, dimIx);
            ids2Orders[dimIx] = getId2Ord(orderedDims[dimIx]);
        }
    }

    public MaximalMinerCombiner(ArrayList<double[]> data) {

        dims = OneDCartifier.transpose(data);
        // System.out.println("Dims data read and transposed");
        origData = OneDCartifier.toPairs(data);
        // System.out.println("Data pairs are created.");

        numOfDims = dims.length;

        orderedDims = new Pair[dims.length][];
        ids2Orders = new int[dims.length][];
        for (int dimIx = 0; dimIx < numOfDims; dimIx++) {
            orderedDims[dimIx] = getOrd2Id(origData, dimIx);
            ids2Orders[dimIx] = getId2Ord(orderedDims[dimIx]);
        }
    }

    /**
     * Mine the {@code inputFile} for subspace clusters using the {@CLON}
     * algorithm.
     *
     * @param k
     *          Neighborhood size
     * @param minLen
     *          Minimum length of a cluster
     * @return List of subspace clusters.
     */
    public List<Freq> mineFor(int k, int minLen) {
        FreqCollection freqCollection = new FreqCollection();
        mineFor(k, minLen, freqCollection);
        return freqCollection.freqs;
    }

    /**
     * Mine the {@code inputFile} for subspace clusters using the {@CLON}
     * algorithm.
     *
     * @param k
     *          Neighborhood size
     * @param minLen
     *          Minimum length of a cluster
     * @param freqCol
     *          Passes each found cluster to the callback method of freqCol. Can
     *          be used for real-time mining.
     * @return List of subspace clusters.
     */
    public void mineFor(int k, int minLen, FreqCollector freqCol) {
        freqCollector = freqCol;
        this.minLen = minLen;

        convertToItems(k);

        CartiFiner miner = new CartiMaximizer(minLen);
        Map<Integer, Map<Integer, Integer>> mineds = miner.mineCarts(dims, k);
        allMineds = mineds2Ids(mineds);

        for (Integer startDimIx : allMineds.keySet()) {
            List<Integer> dimsToCheck = getAllDims();
            dimsToCheck.remove(startDimIx);

            List<Integer> freqDims = singletonList(startDimIx);

            Collection<int[]> dimMineds = allMineds.get(startDimIx);

            for (int[] aMined : dimMineds) {
                foundFreq(aMined, freqDims);
                checkForFreq(dimsToCheck, freqDims, aMined);
            }
        }
    }

    public List<Freq> mineFor(int[] aMined, int k, int minLen, Integer startDimIx) {
        final FreqCollection freqCollection = new FreqCollection();
        freqCollector = freqCollection;
        this.minLen = minLen;
        convertToItems(k);
        System.out.println("Items are created!");

        List<Integer> dimsToCheck = getAllDims();
        dimsToCheck.remove(startDimIx);
        List<Integer> freqDims = singletonList(startDimIx);
        checkForFreq(dimsToCheck, freqDims, aMined);
        return freqCollection.freqs;
    }

    private List<Integer> getAllDims() {
        if (theAllDims == null) {
            List<Integer> allDims = new ArrayList<>(numOfDims);
            for (int i = 0; i < numOfDims; i++) {
                allDims.add(i);
            }
            theAllDims = allDims;
        }
        return new ArrayList<Integer>(theAllDims);
    }

    protected void foundFreq(int[] freqSet, List<Integer> freqDims) {
        if (freqSet.length < minLen) {
            skipCount++;
            System.err.println("[" + this.getClass().getName() +
                               "] Small itemset found! This is an error: " +
                               Arrays.toString(freqSet));
            return;
        }

        freqCollector.foundFreq(new Freq(freqSet, freqDims));
    }

    protected Item[] orderTheItems(int[] aMined, int dimIx) {
        int[] ordered = new int[aMined.length];
        int itemIx = 0;
        for (int item : aMined) {
            ordered[itemIx++] = ids2Orders[dimIx][item];
        }
        Arrays.sort(ordered);

        Item[] items = new Item[ordered.length];
        for (int i = 0; i < items.length; i++) {
            items[i] = allItems[dimIx][orderedDims[dimIx][ordered[i]].ix];
        }
        return items;
    }

    /**
     * CLON works on sorted datasets. This method sorts the dataset according to
     * given dimension and returns the mapping for order-to-ID. {@link Pair}s can
     * be generated by {@link OneDCartifier.toPairs}.
     *
     * @see getId2Ord
     * @param origData
     *          Two-dimensional array of {@link Pair}s. Each row is a data object
     *          and each column is a dimension. Each {@link Pair} contains the ID
     *          of the object and the value. Naturally, Pairs on the same row all
     *          have the same IDs.
     * @param dimIx
     *          dimension index to sort the dataset.
     * @return Ordered dataset using Dimension-{@code dimIx} as reference.
     */
    public static Pair[] getOrd2Id(Pair[][] origData, int dimIx) {
        Pair[] dimArray = new Pair[origData.length];
        for (int i = 0; i < origData.length; i++) {
            dimArray[i] = origData[i][dimIx];
        }
        Arrays.sort(dimArray);
        return dimArray;
    }

    /**
     * Returns the mapping of orders for the given array of objects.
     *
     * @param orderedDim
     *          Ordered set of objects. Output of {@link getOrd2Id}.
     * @return Map of id to order in the dimension.
     */
    public static int[] getId2Ord(Pair[] orderedDim) {
        int[] dimArray = new int[orderedDim.length];

        for (int i = 0; i < orderedDim.length; i++) {
            dimArray[orderedDim[i].ix] = i;
        }
        return dimArray;
    }

    protected void convertToItems(int k) {
        Item[][] nAllItems = new Item[numOfDims][];
        for (int dimIx = 0; dimIx < numOfDims; dimIx++) {
            Item[] dimItems = new Item[dims[dimIx].length];
            for (int i = 0; i < dimItems.length; i++) {
                dimItems[i] = new Item(i);
            }
            int[] cartStarts = OneDCartifier.findCartStarts(dims[dimIx], k, false);
            for (int order = 0; order < cartStarts.length; order++) {
                for (int itemIx = cartStarts[order]; itemIx < cartStarts[order] + k; itemIx++) {
                    dimItems[orderedDims[dimIx][itemIx].ix].addTid(order);
                }
            }
            nAllItems[dimIx] = dimItems;
        }
        allItems = nAllItems;
    }

    protected HashMultimap<Integer, int[]> mineds2Ids(Map<Integer, Map<Integer, Integer>> mineds) {
        HashMultimap<Integer, int[]> allFIs = HashMultimap.create();

        for (Entry<Integer, Map<Integer, Integer>> entry : mineds.entrySet()) {
            Integer dimIx = entry.getKey();
            Map<Integer, Integer> dimMineds = entry.getValue();

            for (Entry<Integer, Integer> dimLined : dimMineds.entrySet()) {
                final int start = dimLined.getValue();
                final int end = dimLined.getKey();
                int[] fis = new int[end - start];
                for (int i = start; i < end; i++) {
                    fis[i - start] = orderedDims[dimIx][i].ix;
                }
                allFIs.put(dimIx, fis);
            }
        }
        return allFIs;
    }

    protected abstract void checkForFreq(List<Integer> dimsToCheck, List<Integer> freqDims,
                                         int[] aMined);

    protected static class Result {
        int a, s, e;

        Result(int a, int s, int e) {
            this.a = a;
            this.s = s;
            this.e = e;
        }
    }

    protected static class Item {
        int id;
        int txS = Integer.MAX_VALUE, txE;

        public Item(int id) { this.id = id; }

        public void addTid(int tid) {
            if (tid < txS) {
                txS = tid;
            }
            if (tid > txE) {
                txE = tid;
            }
        }

        @Override
        public String toString() {
            return "[" + id + ", " + txS + "=>" + txE + "]";
        }
    }

    public interface FreqCollector { void foundFreq(Freq freq); }

    public static class FreqCollection implements FreqCollector {
        List<Freq> freqs;

        public FreqCollection() { this.freqs = new ArrayList<>(); }

        @Override
        public void foundFreq(Freq freq) {
            freqs.add(freq);
        }
    }
}