package weka.subspaceClusterer;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import weka.core.Utils;
import weka.subspaceClusterer.ParameterBracket.Operator;
import weka.subspaceClusterer.ParameterBracket.ParamType;


public class Experiment {
  ParameterBracket m_params;
  List<Path> m_dataSets;
  String m_outputFileName;
  String m_clusterer;
  StringBuilder m_results;
  int m_numThreads = Runtime.getRuntime().availableProcessors() - 1;
  int m_timeLimit = 30;
  
  Map<Path, String[]> m_runs = new HashMap<Path, String[]>();
  
  public void runExperiments() {
    ExecutorService exec = Executors.newFixedThreadPool(m_numThreads); // use number of cores - 1
    m_results = new StringBuilder();
    List<Future<String>> results = new ArrayList<Future<String>>();

    for (Path path : m_dataSets) {
      for (String[] options : m_params) {      
        Evaluator eval = new Evaluator();
        try {
          eval.setClusterer(m_clusterer);
          eval.setDataSet(path.toAbsolutePath().toString());
          eval.setNormalize(false);
          eval.setTimeLimit(m_timeLimit);
          eval.setClassAttribute("last");
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

        Callable<String> iter = new Iteration(eval, options);
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
    m_params = new ParameterBracket();
    m_dataSets = new ArrayList<Path>();
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    Experiment exp = new Experiment();
    
    exp.m_clusterer = "Doc";
    exp.m_outputFileName = "output.txt";
    
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
    exp.m_params.addParameter("-a", "0.001", 10, 3, Operator.MULTIPLY, ParamType.DOUBLE);
    exp.m_params.addParameter("-b", "0.2", 0.1, 2, Operator.ADD, ParamType.DOUBLE);
    exp.m_params.addParameter("-m", "1024", 0, 1, Operator.ADD, ParamType.INTEGER);
    exp.m_params.addParameter("-k", "16", 2, 2, Operator.MULTIPLY, ParamType.INTEGER);
    exp.m_params.addParameter("-w", "50", 2, 1, Operator.MULTIPLY, ParamType.DOUBLE);
    
    exp.runExperiments();
    
    PrintWriter output = null;
    try {
      if (exp.m_outputFileName.length() > 0) {
        output = new PrintWriter(new BufferedWriter(
            new FileWriter(exp.m_outputFileName, true)));
      } else {
        output = new PrintWriter(System.out);
      }
      output.println(exp.m_results.toString());
      output.flush();
      output.close();
    } catch (Exception e) {
      System.err.println(e.getMessage()); 
    } finally {
      if (output != null) {
        output.close();
      }
    }
  }

  private void runDocExperiments() {
    Experiment exp = new Experiment();

    exp.m_clusterer = "Doc";
    exp.m_outputFileName = "output.txt";
    
    // row count data sets
    exp.m_runs.put(Paths.get("Databases/synth_dbsizescale/S1500.arff"), 
        "-a 0.001 -b 0.2 -w 50.0 -m 1024 -k 32".split("\\s"));
    exp.m_runs.put(Paths.get("Databases/synth_dbsizescale/S2500.arff"), 
        "-a 0.01 -b 0.2 -w 100.0 -m 1024 -k 32".split("\\s"));
    exp.m_runs.put(Paths.get("Databases/synth_dbsizescale/S3500.arff"), 
        "-a 0.1 -b 0.2 -w 100.0 -m 1024".split("\\s"));
    exp.m_runs.put(Paths.get("Databases/synth_dbsizescale/S4500.arff"), 
        "-a 0.01 -b 0.2 -w 100.0 -m 1024 -k 32".split("\\s"));
    exp.m_runs.put(Paths.get("Databases/synth_dbsizescale/S5500.arff"), 
        "-a 0.01 -b 0.2 -w 100.0 -m 1024 -k 32".split("\\s"));

    // column count data sets
    exp.m_runs.put(Paths.get("Databases/synth_dimscale/D05.arff"), 
        "-a 0.001 -b 0.3 -w 50.0 -m 1024 -k 16".split("\\s"));
    exp.m_runs.put(Paths.get("Databases/synth_dimscale/D10.arff"), 
        "-a 0.001 -b 0.3 -w 100.0 -m 1024 -k 32".split("\\s"));
    exp.m_runs.put(Paths.get("Databases/synth_dimscale/D15.arff"), 
        "-a 0.1 -b 0.2 -w 100.0 -m 1024 -k 16".split("\\s"));
    exp.m_runs.put(Paths.get("Databases/synth_dimscale/D20.arff"), 
        "-a 0.01 -b 0.2 -w 100.0 -m 1024 -k 32".split("\\s"));
    exp.m_runs.put(Paths.get("Databases/synth_dimscale/D25.arff"), 
        "-a 0.01 -b 0.2 -w 50.0 -m 1024 -k 32".split("\\s"));
    exp.m_runs.put(Paths.get("Databases/synth_dimscale/D50.arff"), 
        "-a 0.1 -b 0.1 -w 100.0 -m 1024 -k 32".split("\\s"));
    exp.m_runs.put(Paths.get("Databases/synth_dimscale/D75.arff"), 
        "-a 0.01 -b 0.2 -w 100.0 -m 1024 -k 32".split("\\s"));

    // noise count data sets    
    exp.m_runs.put(Paths.get("Databases/synth_noisescale/N10.arff"), 
        "-a 0.01 -b 0.2 -w 100.0 -m 1024 -k 32".split("\\s"));
    exp.m_runs.put(Paths.get("Databases/synth_noisescale/N30.arff"), 
        "-a 0.01 -b 0.2 -w 100.0 -m 1024 -k 32".split("\\s"));
    exp.m_runs.put(Paths.get("Databases/synth_noisescale/N50.arff"), 
        "-a 0.01 -b 0.1 -w 50.0 -m 1024 -k 16".split("\\s"));
    exp.m_runs.put(Paths.get("Databases/synth_noisescale/N70.arff"), 
        "-a 0.01 -b 0.1 -w 100.0 -m 1024 -k 16".split("\\s"));

    exp.runExperiments();
    System.out.println(exp.m_results.toString());
    
    PrintWriter output = null;
    try {
      if (exp.m_outputFileName.length() > 0) {
        output = new PrintWriter(new BufferedWriter(
            new FileWriter(exp.m_outputFileName, true)));
      } else {
        output = new PrintWriter(System.out);
      }
      output.println(exp.m_results.toString());
      output.flush();
      output.close();
    } catch (Exception e) {
      System.err.println(e.getMessage()); 
    } finally {
      if (output != null) {
        output.close();
      }
    }
  }
  
  private void createPropertiesFile() {
    try {
      FileOutputStream out = new FileOutputStream("properties.xml");
      Properties props = new Properties();

      // row count data sets
      this.m_dataSets.add(Paths.get("Databases/synth_dbsizescale/S1500.arff"));
      this.m_dataSets.add(Paths.get("Databases/synth_dbsizescale/S2500.arff"));
      this.m_dataSets.add(Paths.get("Databases/synth_dbsizescale/S3500.arff"));
      this.m_dataSets.add(Paths.get("Databases/synth_dbsizescale/S4500.arff"));
      this.m_dataSets.add(Paths.get("Databases/synth_dbsizescale/S5500.arff"));

      // column count data sets
      this.m_dataSets.add(Paths.get("Databases/synth_dimscale/D05.arff"));
      this.m_dataSets.add(Paths.get("Databases/synth_dimscale/D10.arff"));
      this.m_dataSets.add(Paths.get("Databases/synth_dimscale/D15.arff"));
      this.m_dataSets.add(Paths.get("Databases/synth_dimscale/D20.arff"));
      this.m_dataSets.add(Paths.get("Databases/synth_dimscale/D25.arff"));
      this.m_dataSets.add(Paths.get("Databases/synth_dimscale/D50.arff"));
      this.m_dataSets.add(Paths.get("Databases/synth_dimscale/D75.arff"));

      // noise count data sets    
      this.m_dataSets.add(Paths.get("Databases/synth_noisescale/N10.arff"));
      this.m_dataSets.add(Paths.get("Databases/synth_noisescale/N30.arff"));
      this.m_dataSets.add(Paths.get("Databases/synth_noisescale/N50.arff"));
      this.m_dataSets.add(Paths.get("Databases/synth_noisescale/N70.arff"));

      props.setProperty("algorithms", "Doc");
      props.setProperty("dataSets", this.m_dataSets.toString());
      
      props.storeToXML(out, "---Experiment Configuration---");
      
      out.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  private void WriteProperties() {
    
  }
  
  private void LoadProperties() {
    Properties props = new Properties();
    FileInputStream in;
    try {
      in = new FileInputStream("properties.xml");
      props.loadFromXML(in);
      in.close();
      
      this.m_clusterer = props.getProperty("algorithms");
      String paths[] = props.getProperty("dataSets").split(",");
      for (String path : paths) {
        //TODO: clean out the "[" and the "]" on the first and last path.
        if (path.contains("[")) {
          path = path.replace("[", "");
        } else if (path.contains("]")) {
          path = path.replace("]", "");
        }
        this.m_dataSets.add(Paths.get(path));
      }
      
      this.m_dataSets.toString();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
      }
  
}





