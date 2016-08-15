package uantwerp.maximizer;

import java.util.List;

/**
 * A pair of an object set and a dimension set.
 *
 * @author M. Emin Aksehirli
 *
 */
public class Freq {
    public int[] freqSet;
    public List<Integer> freqDims;
    public final int id;
    static int autoID = 0;

    public Freq(int[] freqSet, List<Integer> freqDims) {
        this.freqSet = freqSet;
        this.freqDims = freqDims;
        id = autoID++;
    }
}