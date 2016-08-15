package uantwerp.maximizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

// TODO: what is this for? and where is it?
import be.uantwerpen.adrem.cart.io.InputFile;

/**
 * Uses maximal frequent itemset mining to find the clusters in individual
 * dimensions.
 *
 * @author M. Emin Aksehirli
 *
 */
public class ItemsetMaximalMiner extends MaximalMinerCombiner {
    private static int area(Item[] dimItems, int start, int end) {
        return (dimItems[start].txE - dimItems[end].txS) * (end - start);
    }

    @Override
    protected void checkForFreq(List<Integer> dimsToCheck, List<Integer> freqDims, int[] aMined) {
        int checkedUntil = 0;
        for (int dim : dimsToCheck) {
            checkedUntil++;
            Item[] items = orderTheItems(aMined, dim);

            Map<Integer, Integer> localFreqs = findAllMaxes(items);
            if (localFreqs.size() <= 0) {
                continue;
            }

            int[][] freqSets = new int[localFreqs.size()][];
            int freqSetIx = 0;
            for (Entry<Integer, Integer> e : localFreqs.entrySet()) {
                final Integer start = e.getValue();
                final Integer end = e.getKey();

                int[] freqSet = new int[end - start];
                for (int freqIx = start; freqIx < end; freqIx++) {
                    freqSet[freqIx - start] = items[freqIx].id;
                }
                freqSets[freqSetIx++] = freqSet;
            }

            List<Integer> newFreqDims = new ArrayList<>(freqDims);
            newFreqDims.add(dim);

            for (int[] freqSet : freqSets) {
                foundFreq(freqSet, newFreqDims);
                List<Integer> newDimsToCheck =
                    new ArrayList<>(dimsToCheck.subList(checkedUntil, dimsToCheck.size()));
                checkForFreq(newDimsToCheck, newFreqDims, freqSet);
            }
        }
    }

    protected Map<Integer, Integer> findAllMaxes(Item[] items) {
        int minSup = minLen;
        Map<Integer, Integer> maxes = new HashMap<>();
        int start = 0;
        int end = start + minLen;

        while (end < items.length - 1) {
            while ((items[start].txE - items[end + 1].txS > minSup) && end < items.length - 2) {
                end++;
            }

            if (items[start].txE - items[end].txS > minSup) {
                maxes.put(end, start);
                end++;
            }

            while (items[start + 1].txE == items[start].txE && start < items.length - minLen) {
                start++;
            }

            start++;
            if (end - start < minLen) {
                end = start + minLen;
            }
        }
        return maxes;
    }

    public ItemsetMaximalMiner(InputFile inputFile) { super(inputFile); }

    public ItemsetMaximalMiner(ArrayList<double[]> inputData) { super(inputData); }
}