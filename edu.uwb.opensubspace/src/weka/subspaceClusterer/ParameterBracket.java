package weka.subspaceClusterer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class holds info about running otimization experiments for a 
 * subspace clustering algorithm.
 * @author dave
 *
 */
public class ParameterBracket implements Serializable, Iterable<String[]> {

  /** Determines if offset is multiplied or added. */
  public static enum Operator {
    ADD, MULTIPLY
  }

  public static enum ParamType {
    INTEGER, DOUBLE
  }
  
  /** For serialization/deserialization. */
  private static final long serialVersionUID = 1L;

  /** The flags used by the algorithm to specify parameter settings. */
  private ArrayList<String> m_parameterFlags;

  /** The initial values for each parameter. */
  private ArrayList<String> m_initialValues;

  /** The amount to offset each parameter by at each step. */
  private ArrayList<Double> m_offsets;

  /** The total number of steps for each parameter. */
  private ArrayList<Integer> m_steps;

  /** The operator to apply to each parameter at each step */
  private ArrayList<Operator> m_operators;
  
  /** The type of value for each parameter (e.g Integer, Double) */
  private ArrayList<ParamType> m_paramTypes; 
  
  public ParameterBracket() {
    m_parameterFlags = new ArrayList<String>();
    m_initialValues = new ArrayList<String>();
    m_offsets = new ArrayList<Double>();
    m_steps = new ArrayList<Integer>();
    m_operators = new ArrayList<Operator>();
    m_paramTypes = new ArrayList<ParamType>();
  }
  
  public boolean addParameter(String flag, String initialValue, double offset, 
      int numSteps, Operator operator, ParamType type) {

    m_parameterFlags.add(flag);
    m_initialValues.add(initialValue);
    m_offsets.add(offset);
    m_steps.add(numSteps-1);
    m_operators.add(operator);
    m_paramTypes.add(type);
    
    return true;
  }
  
  public void addParameter(String paramStr) {
    String[] params = paramStr.split("\\s");
    
    
  }
  
  public OptionsIterator iterator() {
    return new OptionsIterator();
  }
  
  public class OptionsIterator implements Iterator<String[]> {
    /** The current step of each parameter */
    private int m_currentStep[];
    
    /** The current options array */
    private String m_options[];
    
    /** A flag that determines of there are options arrays left to iterate. */ 
    private boolean m_hasNext = true;
    
    public OptionsIterator() {
      m_currentStep = new int[m_steps.size()];
      m_options = new String[2 * m_currentStep.length];
      for (int i = 0; i < m_currentStep.length; i++) {
        m_options[2*i] = m_parameterFlags.get(i);
        m_options[2*i+1] = m_initialValues.get(i);
      }
    }
    
    @Override
    public boolean hasNext() {
      return m_hasNext;
    }

    @Override
    public String[] next() {
      for (int i = 0; i < m_parameterFlags.size(); i++) {
        try {
          
          double val = Double.parseDouble(m_initialValues.get(i));
          
          if (m_operators.get(i) == Operator.ADD) {
            val += (m_offsets.get(i) * (double)m_currentStep[i]);  
          } else { // multiply
            val *= Math.pow(m_offsets.get(i), m_currentStep[i]);
          }
          if (m_paramTypes.get(i) == ParamType.DOUBLE) {
            m_options[2*i+1] = String.valueOf(val);
          } else if (m_paramTypes.get(i) == ParamType.INTEGER) {
            m_options[2*i+1] = String.valueOf((int)val);
          }

        } catch (NumberFormatException e) { // value is non-numeric
          m_options[2*i+1] = m_initialValues.get(i);
        }
      }
      incrementCurrentStep();
      
      return m_options;
    }

    @Override
    public void remove() {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Method not implented.");
    }

    private void incrementCurrentStep() {
      for (int i = m_currentStep.length - 1; i >= 0; i--) {
        if (m_currentStep[i]++ < m_steps.get(i)) {
          break;
        } else {
          m_currentStep[i] = 0;
        }
      }
      // check if m_currentStep has rolled back to all zeros. This implies
      // all iterations are complete
      m_hasNext = false;
      for (int i = 0; i < m_currentStep.length; i++) {
        if (m_currentStep[i] != 0) {
          m_hasNext = true;
          break;
        }
      }
    }
  }

  public static void main(String[] args) {
    ParameterBracket config = new ParameterBracket();
    
    config.addParameter("-a", "0.001", 10, 3, Operator.MULTIPLY, ParamType.DOUBLE);
    config.addParameter("-b", "0.1", 0.1, 1, Operator.ADD, ParamType.DOUBLE);
    config.addParameter("-m", "1024", 0, 1, Operator.ADD, ParamType.INTEGER);
    config.addParameter("-k", "8", 2, 1, Operator.MULTIPLY, ParamType.INTEGER);
    config.addParameter("-w", "50", 2, 1, Operator.MULTIPLY, ParamType.DOUBLE);
    
    for (String[] op : config) {
      for(String s : op) {
        System.out.print(s + " ");
      }
      System.out.println();
    } 
  }
}
