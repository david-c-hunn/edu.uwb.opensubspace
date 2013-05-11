/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    weka.subspaceClusterer.Evaluator.java
 *    Copyright (C) 2013 Dave Hunn
 *
 */

package  weka.subspaceClusterer;

import i9.subspace.base.Cluster;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import weka.clusterquality.ClusterQualityMeasure;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.Normalize;

/**
 * A class for evaluating subspace clustering models. Most of this code
 * was already in OpenSubspace. This class is a refactored version of
 * SubspaceClusterEvaluation. I modified the code, to be more understandable
 * (to me at least). I also took time limit functionality from 
 * weka.gui.explorer.SubspaceClustererPanel. Now, clustering algorithms
 * are run in a thread. If the time limit is reached, before the algorithm
 * completes, it is interrupted. I also modified the output format for the
 * results. The output from a successful execution of this class is<p/>
 * 
 * [clusterer name][TAB][scheme][TAB][data set][TAB][run time][TAB]
 * [quality metric][TAB][quality metric][TAB]...</p>
 *
 * -t < name of the data set file > <br/>
 * <ul>Specify the data set file. <p/></ul>
 *
 * -T < true clusters file > <br/>
 * <ul>Specify a file containing the true clusters. This is needed for CE and 
 * RNIA metrics. <p/></ul>
 *
 * -c < class index > <br/>
 * <ul>Set the class attribute.<p/></ul>
 * 
 * -M < cluster quality measures > <br/>
 * <ul>Specify subspace cluster quality measures in package weka.clusterquality
 * to apply to clustering results. <p/>
 * separate measures with ':' e.g. -M F1Measure:Entropy:CE<p/></ul>
 * 
 * -sc < subspace clusterer > <br/>
 * <ul>Subspace clustering algorithms in package weka.subspaceClusterer.<p/></ul>
 * 
 * -timelimit < time limit in minutes > <br/>
 * <ul>Specify the time limit on clustering in minutes (whole numbers only). 
 * Applies only to clustering, not the time to evaluate the results.<p/></ul>
 * 
 * -outfile < output file > <br/>
 * <ul>Specify a file path to append the results of the clustering. If a file 
 * is not specified, then output is written to stdout.<p/></ul>
 * 
 * @author   Dave Hunn (david.c.hunn@gmail.com)
 * @version  Revision: 0.85
 */
public class Evaluator implements Serializable {
  /** for serialization */
  static final long serialVersionUID = -830188327319128005L;

  /** the clusterer */
  private SubspaceClusterer m_clusterer = null;

  /** holds a string describing the results of clustering the data set */
  private StringBuffer m_clusteringResults;

  /** The data set to perform the clustering on. */
  private Instances m_dataSet;

  /** The metrics to perform on the clustering result. */
  private ArrayList<ClusterQualityMeasure> m_metrics;

  /** 
   * The true clusters hidden in the data set. This is required for some
   *  metrics (RNIA, and CE).
   */
  private ArrayList<Cluster> m_trueClusters;

  /** 
   * A time limit in minutes. If the clustering is not finished after
   * m_timeLimit minutes, then it will be interrupted.
   */
  private long m_timeLimit;

  /**
   * set the clusterer
   * @param clusterer the clusterer to use
   */
  public void setClusterer(SubspaceClusterer clusterer) {
    m_clusterer = clusterer;
  }

  /**
   * Set  the time limit for clustering. The time limit is only modified if
   * t is greater than zero.
   * @param t A time in minutes.
   */
  public void setTimeLimit(long t) {
    if (t > 0) {
      m_timeLimit = t;
    }
  }

  /**
   * return the results of clustering.
   * @return a string detailing the results of clustering a data set
   */
  public String clusterResultsToString() {
    return m_clusteringResults.toString();
  }

  /**
   * Set the clusterer using the class name.
   * @param clusterer  A subspace clusterer class name.
   * @throws Exception If clusterer is not a valid class name.
   */
  public void setClusterer(String clusterer) throws Exception {
    m_clusterer = SubspaceClusterer.forName("weka.subspaceClusterer." 
        + clusterer, null);
  }

  /**
   * Sets the data set to use in the clustering from a file name.
   * @param fileName The name of an arff file containing data to cluster.
   * @throws Exception If there is a problem opening fileName or loading
   *                   the data set.
   */
  public void setDataSet(String fileName) throws Exception {
    DataSource source = new DataSource(fileName);
    m_dataSet = source.getDataSet();
  }

  /**
   * Sets the true clusters using the file referred to by fileName.
   * @param fileName
   * @throws Exception
   */
  public void setTrueClusters(String fileName) throws Exception {
    File trueClusterFile = new File(fileName);
    int numDims = m_dataSet.numAttributes() - 1; // class is one of the attributes
    m_trueClusters =  
        SubspaceClusterTools.getClusterList(trueClusterFile, numDims);
  }

  /**
   * Parses metricClassesString. Uses reflection to create metric classes
   * and adds them to m_metrics.
   * @param metricClassesString
   */
  public void setMetrics(String metricClassesString) { 
    if (m_metrics == null) {
      m_metrics = new ArrayList<ClusterQualityMeasure>();
    }

    String[] classStrings = metricClassesString.split(":");

    for (int i = 0; i < classStrings.length; i++) {
      try {
        Class<?> c = Class.forName("weka.clusterquality." 
            + classStrings[i]);
        m_metrics.add((ClusterQualityMeasure)c.newInstance());
      } catch (InstantiationException e1) {
        System.err.println("Not a valid subspace measure class: " +
            "weka.clusterquality."+classStrings[i]);
      } catch (IllegalAccessException e1) {
        System.err.println("Not a valid subspace measure class: " +
            "weka.clusterquality."+classStrings[i]);
      } catch (ClassNotFoundException e) {
        System.err.println("Not a valid subspace measure class: " +
            "weka.clusterquality."+classStrings[i]);
      }
    }
  }

  /**
   * Sets the class using classString.
   * @param classString
   * @throws Exception
   */
  public void setClassAttribute(String classString) throws Exception {
    int theClass = 0;

    if (m_dataSet == null) {
      throw new Exception("Attempted to set class without first setting" +
          " a data set.");
    }
    if (classString.length() != 0) {
      if (classString.compareTo("last") == 0)
        theClass = m_dataSet.numAttributes();
      else if (classString.compareTo("first") == 0)
        theClass = 1;
      else
        theClass = Integer.parseInt(classString);
    } else {
      // if the data set defines a class attribute, use it
      if (m_dataSet.classIndex() != -1) {
        theClass = m_dataSet.classIndex() + 1;
        System.err.println("Note: using class attribute from " +
            "dataset, i.e., attribute #" + theClass);
      }
    }
    if (theClass != -1) {
      if (theClass < 1 || theClass > m_dataSet.numAttributes())
        throw new Exception("Class is out of range!");

      if (! m_dataSet.attribute(theClass - 1).isNominal())
        throw new Exception("Class must be nominal!");

      m_dataSet.setClassIndex(theClass - 1);
    }
  }

  /**
   * Constructor. Sets defaults for each member variable.
   */
  public Evaluator () {
    m_clusterer = new Sepc();
    m_clusteringResults = new StringBuffer();
    m_timeLimit = 30;
  }

  /**
   * 
   * @param options
   * @throws Exception 
   */
  public void setOptions(String options[]) throws Exception {		
    try {
      if (Utils.getFlag('h', options)) {
        throw new Exception("Help requested.");
      }

      String scName = Utils.getOption("sc", options); 
      if (scName.length() == 0) {
        System.err.println("No algorithm specified. Using the default" +
            " (SEPC). Specify an algorithm with -sc.");
      } else {
        this.setClusterer(scName);  
      }

      String dataSetFileName = Utils.getOption('t', options);
      if (dataSetFileName.length() == 0) {
        throw new Exception("No input file, use -t");
      } else {
        setDataSet(dataSetFileName);
      }

      String measureOptionString = Utils.getOption('M', options);
      if (measureOptionString.length() == 0) {
        System.err.println("No metrics set. Use -M to specify quality metrics.");
      } else {
        setMetrics(measureOptionString);
      }

      String trueFileName = Utils.getOption('T', options);
      if (trueFileName.length() == 0) {
        System.err.println("No true cluster file set. Some metrics " +
            "will not function without a true cluster file " +
            "(CE and RNIA). Use -T to specify a true cluster file.");
      } else {
        setTrueClusters(trueFileName);
      }

      String classString = Utils.getOption('c', options);
      setClassAttribute(classString);

      String timeLimit = Utils.getOption("timelimit", options);
      if (timeLimit.length() > 0 ) {
        setTimeLimit(Long.parseLong(timeLimit));
      }

    } catch (Exception e) {
      throw new Exception('\n' + e.getMessage() + makeOptionString(m_clusterer));
    }
  }

  /**
   * Calculates all quality metrics specified in m_metrics on the clustering
   * result. Returns the results as a StringBuffer. 
   * @return The results of applying quality metrics to the clustering result.
   */
  private StringBuffer getClusteringQuality() {
    StringBuffer results = new StringBuffer();
    ArrayList<Cluster> clusterList = null;

    if (m_metrics == null || m_metrics.size() == 0) {
      results.append("No metrics set.");
    } 
    if (m_clusterer.getSubspaceClustering() == null) {
      clusterList = new ArrayList<Cluster>();
    } else {
      clusterList = (ArrayList<Cluster>)m_clusterer.getSubspaceClustering();
    }

    //calculate each quality metric
    for (ClusterQualityMeasure m : m_metrics) {
      m.calculateQuality(clusterList, m_dataSet, m_trueClusters);
    }

    //print values 
    for (ClusterQualityMeasure m : m_metrics) {
      String val = "";

      val = m.getName() + "=\t";
      if (m.getOverallValue() != null) {
        if (m.getOverallValue().equals(Double.NaN)) {
          val += "undef";
        }
        else {
          val += String.valueOf(m.getOverallValue());
        }
      } else {
        val = "";
        if (m.getCustomOutput() != null) {
          val += m.getCustomOutput();
          val = val.replace('\n', '\t');
        } else {
          val += "null";
        }
      }
      if (val != ""){
        results.append(val + "\t");
      }
    }

    return results;
  }

  /**
   * Evaluates a clusterer with the options given.
   * @param options An array of strings containing options for clustering.
   */
  public String evaluate(String[] options) throws Exception {
    long start = 0;
    long end = 0;
    
    // clear any previous results
    clear();
    // Set options for the Evaluator
    setOptions(options);
    // Set options for the clusterer
    if (m_clusterer instanceof OptionHandler) {
      ((OptionHandler) m_clusterer).setOptions(options);
    }	
    m_clusteringResults.append(currentTime() + "\t");
    m_clusteringResults.append(m_clusterer.getName() + "\t");
    m_clusteringResults.append(m_clusterer.getParameterString() + "\t");
    m_clusteringResults.append(m_dataSet.relationName() + "\t");
    
    NormalizeDataSet();
   
    // run the clusterer
    start = System.currentTimeMillis();
    if (runClusterer()) { // check to make sure there is something to eval
      end = System.currentTimeMillis();
      m_clusteringResults.append(end - start);
      m_clusteringResults.append("\t");
      
      StringBuffer qualResults = getClusteringQuality();

      if (qualResults != null) {
        m_clusteringResults.append(qualResults);
      }
    } else {
      m_clusteringResults.append("timed out after " + m_timeLimit + 
        " minutes" + "\t");
    }

    return m_clusteringResults.toString();
  }

  private void NormalizeDataSet() {
    Filter normalizer = new Normalize();
    
    try {
      normalizer.setInputFormat(m_dataSet);
      for (int i = 0; i < m_dataSet.numInstances(); i++) {
        normalizer.input(m_dataSet.instance(i));
      }
      normalizer.batchFinished();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    Instances newData = normalizer.getOutputFormat();
    Instance processed;
    
    while ((processed = normalizer.output()) != null) {
      newData.add(processed);
    }
    
    m_dataSet = newData;
    
  }

  /**
   * @return The current date and time formatted as "yyyy-mm-dd hh:mm".
   */
  private String currentTime() {
    Date curTime = new Date();
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm");

    return fmt.format(curTime);
  }

  /**
   * Clears existing evaluation results.
   */
  private void clear() {
    m_clusteringResults = new StringBuffer();
    m_trueClusters = null;
    m_metrics = null;
  }

  /**
   * 
   * @param inst The set of instances to remove the class label from.
   * @return A set of instances sans class label.
   */
  private static Instances removeClass(Instances inst) {
    Remove af = new Remove();
    Instances retI = null;

    try {
      if (inst.classIndex() < 0) {
        retI = inst;
      } else {
        af.setAttributeIndices("" + (inst.classIndex() + 1));
        af.setInvertSelection(false);
        af.setInputFormat(inst);
        retI = Filter.useFilter(inst, af);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return retI;
  }

  /**
   *
   * @return Returns true if the clusterer finishes within m_timeLimit.
   */
  private boolean runClusterer() {
    boolean timeout = false;

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<Void> future = executor.submit(new Task(m_clusterer, removeClass(m_dataSet)));
    
    try {
        future.get(m_timeLimit, TimeUnit.MINUTES);
    } catch (TimeoutException e) {
        // This is not an error. This is our timeout.
        timeout = true;
    } catch (InterruptedException e) {
        e.printStackTrace();
    } catch (ExecutionException e) {
        e.printStackTrace();
    }
    
    executor.shutdownNow();

    // Assume that no timeout means the clusterer ran successfully
    return !timeout;
  }
  
  private class Task implements Callable<Void> {
    SubspaceClusterer sc;
    Instances dataSet;

    // Constructor
    Task(SubspaceClusterer clusterer, Instances dataSet) {
        this.sc = clusterer;
        this.dataSet = dataSet;
    }

    @Override
    public Void call() throws Exception {

        sc.buildSubspaceClusterer(dataSet);
        return null;

    }

  }

  /**
   * 
   * @param time
   * @return
   */
  private static String formatTimeString(long time){
    StringBuffer outBuff = new StringBuffer();
    DecimalFormat format = new DecimalFormat();

    format.setMaximumFractionDigits(0);
    format.setMinimumIntegerDigits(2);

    int ms =(int) time;
    time/=1000;
    int h = (int)(time/3600);
    time-=(h*3600);
    int m = (int)(time/60);
    time-=(m*60);
    int s = (int)(time);

    outBuff.append(format.format(h)+"h ");
    outBuff.append(format.format(m)+"m ");
    outBuff.append(format.format(s)+"s (");
    outBuff.append((ms)+"ms)");

    return outBuff.toString();
  }

  /**
   * TODO: update the option string to include the options I have added.
   * Make up the help string giving all the command line options
   *
   * @param clusterer the clusterer to include options for
   * @return a string detailing the valid command line options
   */
  private static String makeOptionString (SubspaceClusterer clusterer) {
    StringBuffer optionsText = new StringBuffer("");

    // General options
    optionsText.append("\n\nGeneral options:\n\n");

    optionsText.append("-sc <subspace clusterer>\n");
    optionsText.append("\tSpecifies the subspace clustering algorithm to\n");
    optionsText.append("\tevaluate. It must be one of the algorithms in \n");
    optionsText.append("\tin the package weka.subspaceClusterer.\n");
    
    optionsText.append("-t <name of input file>\n");
    optionsText.append("\tSpecifies the input arff file containing the\n");
    optionsText.append("\tdata set to cluster.\n");

    optionsText.append("-T <name of true cluster file>\n");
    optionsText.append("\tSpecifies the .true file containing the\n");
    optionsText.append("\ttrue clustering.\n");

    optionsText.append("-M <cluster quality measures to evaluate>\n");
    optionsText.append("\tSpecifies the subspace cluster quality metrics\n");
    optionsText.append("\tin the weka.clusterquality package to apply.\n");
    optionsText.append("\tSeparate metrics with a colon (':').\n");
    optionsText.append("\t\te.g. -M F1Measure:Entropy:CE\n");

    optionsText.append("-c <class index>\n");
    optionsText.append("\tSpecifies the index of the class attribute,\n");
    optionsText.append("\tstarting with 1. If supplied, the class  is\n");
    optionsText.append("\tignored during clustering but is used in a\n");
    optionsText.append("\tclasses to clusters evaluation.\n");

    optionsText.append("-timelimit <time limit for clustering>\n");
    optionsText.append("\tSpecifies a time limit in minutes for\n");
    optionsText.append("\tclustering. The value should be a whole number\n");
    optionsText.append("\tgreater than zero.\n");

    optionsText.append("-outfile <output file>\n");
    optionsText.append("\tSpecifies a file path to append the results of the\n");
    optionsText.append("\tclustering. If a file is not specified, then output\n");
    optionsText.append("\tis written to stdout.\n");

    // Get scheme-specific options
    if (clusterer instanceof OptionHandler) {
      optionsText.append("\nOptions specific to " 
          + clusterer.getClass().getName() + ":\n\n");
      @SuppressWarnings("unchecked")
      Enumeration<Option> enu = ((OptionHandler)clusterer).listOptions();

      while (enu.hasMoreElements()) {
        Option option = (Option)enu.nextElement();
        optionsText.append(option.synopsis() + '\n');
        optionsText.append(option.description() + "\n");
      }
    }

    return  optionsText.toString();
  }

  /**
   * 
   * @return A copy of the data set with the assigned labels from running 
   *         the clustering algorithm.
   */
  private Instances clusteredInstances(List<Cluster> foundClusters) {
    // make a deep copy of the data set
    Instances insts = new Instances(m_dataSet);
    double label = 1;
    
    for (Cluster c : foundClusters) {
      for (int obj : c.m_objects) {
        insts.instance(obj).setClassValue(label);
      }
      ++label;
    }
    
    return insts;
  }
  
  private void writeInstances(Instances dataSet, File file) {
    ArffSaver saver = new ArffSaver();
    saver.setInstances(dataSet);
    try {
      saver.setFile(file);
      saver.writeBatch();
      saver.setUseRelativePath(true);
    } catch(IOException e) {
      System.out.println("Error writing output to arff file:" + e.getMessage()); 
    }
  }
  
  public void writeOutputToArff() {
    writeInstances(clusteredInstances(m_clusterer.getSubspaceClustering()),
        new File("cluster_results.arff"));
  }
  
  /**
   * Main method for using this class. The results of the evaluation are 
   * written to the file specified by -outfile. 
   *
   * @param args the options
   */
  public static void main (String[] args) {
    Evaluator eval = null;
    String outFileName = null;
    PrintWriter output = null;
    
    try {
      outFileName = Utils.getOption("outfile", args);
      if (outFileName.length() > 0) {
        output = new PrintWriter(new BufferedWriter(new FileWriter(outFileName, true)));
      } else {
        output = new PrintWriter(System.out);
      }
      eval = new Evaluator();
      eval.evaluate(args);
      output.println(eval.clusterResultsToString());
      output.flush();
      output.close();
      eval.writeOutputToArff();
    } catch (Exception e) {
      System.err.println(e.getMessage()); 
    } finally {
      if (output != null) {
        output.close();
      }
    } 
  }

  @SuppressWarnings("unused")
  private static void testHelpMessage() {
    Evaluator eval = null;
    String[] args = new String[10];

    try {
      eval = new Evaluator();
      args[0] = "-sc P3c";
      args[1] = "-h";
      eval.evaluate(args);
      System.out.println(eval.clusterResultsToString());
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}

