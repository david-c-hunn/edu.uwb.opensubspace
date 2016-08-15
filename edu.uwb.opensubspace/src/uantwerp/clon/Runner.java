package uantwerp.clon;

import static java.lang.Integer.parseInt;

import java.util.List;

import be.uantwerpen.adrem.cart.io.InputFile;
import uantwerp.maximizer.Freq;
import uantwerp.maximizer.ItemsetMaximalMiner;

/**
 * Driver class for the algorithm.
 *
 * @author M. Emin Aksehirli
 *
 */
public class Runner {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java Runner datafile.mime k minLen");
            System.err.println("Output: (cluster size) [dimensions] -- [objects]");
            System.err.println("For more information http://adrem.uantwerpen.be/clon");

            return;
        }
        String fileName = args[0];
        int k = parseInt(args[1]);
        int minLen = parseInt(args[2]);

        ItemsetMaximalMiner miner = new ItemsetMaximalMiner(InputFile.forMime(fileName));
        List<Freq> clusters = miner.mineFor(k, minLen);

        for (Freq cluster : clusters) {
            System.out.printf("%d - ", cluster.freqSet.length);
            for (int dim : cluster.freqDims) {
                System.out.printf("%d ", dim);
            }
            System.out.printf("- ");

            for (int obj : cluster.freqSet) {
                System.out.printf("%d ", obj);
            }
            System.out.println();
        }
    }
}