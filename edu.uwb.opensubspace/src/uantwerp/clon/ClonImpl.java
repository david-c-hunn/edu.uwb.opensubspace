package uantwerp.clon;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.System;

import i9.data.core.DBStorage;
import weka.core.Instance;
import i9.data.core.DataSet;
import i9.subspace.base.Cluster;

import uantwerp.maximizer.Freq;
import uantwerp.maximizer.ItemsetMaximalMiner;

public class ClonImpl {
    private int k = 170;
    private int minLen = 90;
    private int numDims = 0;
    private ArrayList<double[]> data = new ArrayList<double[]>();

    public ClonImpl(DBStorage dbstorage, int k, int minLen) {
        this.k = k;
        this.minLen = minLen;
        DataSet dataSet = dbstorage.getDataSet();
        this.numDims = dataSet.getNumDimensions();

        for (int i = 0; i < dataSet.numInstances(); ++i) {
            Instance instance = dataSet.instance(i);
            this.data.add(instance.toDoubleArray());
        }
    }

    public List<Cluster> runClustering() {
        ItemsetMaximalMiner miner = new ItemsetMaximalMiner(this.data);
        List<Freq> freqs = miner.mineFor(this.k, this.minLen);
        List<Cluster> clusters = new ArrayList<Cluster>();

        // convert the Freqs to Clusters
        for (int i = 0; i < freqs.size(); ++i) {
            Freq freq = freqs.get(i);
            Cluster cluster = new Cluster(new boolean[this.numDims],
                                          new ArrayList<Integer>());

            for (int j = 0; j < freq.freqDims.size(); ++j) {
                int dim = freq.freqDims.get(j);
                cluster.m_subspace[dim] = true;
            }

            for (int j = 0; j < freq.freqSet.length; ++j) {
                cluster.m_objects.add(freq.freqSet[j]);
            }

            clusters.add(cluster);
        }

        return clusters;
    }
}
