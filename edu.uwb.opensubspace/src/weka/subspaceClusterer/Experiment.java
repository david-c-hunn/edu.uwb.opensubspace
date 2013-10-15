package weka.subspaceClusterer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class Experiment {
  ArrayList<ArrayList<String>> m_args;
  List<Path> m_dataSets;
  String m_outputFileName;
  String m_clusterer;
  StringBuilder m_results;
  int m_numThreads = Runtime.getRuntime().availableProcessors() - 1;

  public void runExperiments() {
    ExecutorService exec = Executors.newFixedThreadPool(m_numThreads); // use number of cores - 1
    m_results = new StringBuilder();
    List<Future<String>> results = new ArrayList<Future<String>>();

    for (Path path : m_dataSets) {
      for (int i = 0; i < m_args.size(); i++) {
        String options[] = new String[1]; 
        Evaluator eval = new Evaluator();
        try {
          eval.setClusterer(m_clusterer);
          eval.setDataSet(path.toAbsolutePath().toString());
          eval.setNormalize(false);
          eval.setTimeLimit(1);
          eval.setClassAttribute("last");
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

        Callable<String> iter = new Iteration(eval, m_args.get(i).toArray(options));
        Future<String> submit = exec.submit(iter);
        results.add(submit);
      }
    }
    try {
      for (Future<String> future : results) {          
        m_results.append(future.get()); 
        m_results.append('\n');
      }
    } catch (InterruptedException | ExecutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    exec.shutdown();
  }

  private class Iteration implements Callable<String> {
    Evaluator eval;
    String[] options;

    // Constructor
    Iteration(Evaluator e, String[] options) {
      this.eval = e;
      this.options = options;
    }

    @Override
    public String call() throws Exception {
      eval.evaluate(options);
      return eval.clusterResultsToString();
    }

  }

  public Experiment() {
    m_args = new ArrayList<ArrayList<String>>();
    m_dataSets = new ArrayList<Path>();
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    Experiment exp = new Experiment();

    exp.m_clusterer = "Doc";
    exp.m_outputFileName = "output";

    // row count data sets
    exp.m_dataSets.add(Paths.get("Databases/synth_dbsizescale/S1500.arff"));
    exp.m_dataSets.add(Paths.get("Databases/synth_dbsizescale/S2500.arff"));
    exp.m_dataSets.add(Paths.get("Databases/synth_dbsizescale/S3500.arff"));
    exp.m_dataSets.add(Paths.get("Databases/synth_dbsizescale/S4500.arff"));
    exp.m_dataSets.add(Paths.get("Databases/synth_dbsizescale/S5500.arff"));

    // column count data sets
    exp.m_dataSets.add(Paths.get("Databases/synth_dimscale/D05.arff"));
    exp.m_dataSets.add(Paths.get("Databases/synth_dimscale/D10.arff"));
    exp.m_dataSets.add(Paths.get("Databases/synth_dimscale/D15.arff"));
    exp.m_dataSets.add(Paths.get("Databases/synth_dimscale/D20.arff"));
    exp.m_dataSets.add(Paths.get("Databases/synth_dimscale/D25.arff"));
    exp.m_dataSets.add(Paths.get("Databases/synth_dimscale/D50.arff"));
    exp.m_dataSets.add(Paths.get("Databases/synth_dimscale/D75.arff"));

    // noise count data sets    
    exp.m_dataSets.add(Paths.get("Databases/synth_noisescale/N10.arff"));
    exp.m_dataSets.add(Paths.get("Databases/synth_noisescale/N30.arff"));
    exp.m_dataSets.add(Paths.get("Databases/synth_noisescale/N50.arff"));
    exp.m_dataSets.add(Paths.get("Databases/synth_noisescale/N70.arff"));

    // add the sets of clustering parameters
    ArrayList<String> options = new ArrayList<String>();

    options.add("-a");
    options.add("0.001");
    options.add("-b");
    options.add("0.3");
    options.add("-m");
    options.add("1024");
    options.add("-k");
    options.add("32");
    options.add("-w");
    options.add("100");

    exp.m_args.add(options);
    
    options = new ArrayList<String>();

    options.add("-a");
    options.add("0.1");
    options.add("-b");
    options.add("0.2");
    options.add("-m");
    options.add("1024");
    options.add("-k");
    options.add("16");
    options.add("-w");
    options.add("100");

    exp.m_args.add(options);

    options = new ArrayList<String>();

    options.add("-a");
    options.add("0.01");
    options.add("-b");
    options.add("0.2");
    options.add("-m");
    options.add("1024");
    options.add("-k");
    options.add("32");
    options.add("-w");
    options.add("100");

    exp.m_args.add(options);

    
    exp.runExperiments();
    System.out.println(exp.m_results.toString());
  }

}




//  a variable number of option flags and values
//  for each flag, 
//  specify initial value
//  specify offset
//  specify number of steps
//  specify addition or multiplication for each flag

//  String flag;
//  String initialValue;
//  String offset;
//  int numSteps;
//  Operator operator; // + or *




