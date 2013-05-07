package weka.gui;


import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import weka.core.OptionHandler;
import weka.core.Utils;
import weka.gui.PropertyPanel;
import weka.gui.PropertyText;
import weka.gui.PropertyValueSelector;
import weka.subspaceClusterer.SubspaceClusterer;


public class BracketingPanel extends JPanel implements PropertyChangeListener, ActionListener{

  private Class m_subspaceClustererClass = null;

  /** Holds properties of the target */
  private PropertyDescriptor m_Properties[];

  /** Holds the methods of the target */
  private MethodDescriptor m_Methods[];

  /** Holds property editors of the object */
  private PropertyEditor m_Editors_para[];
  private PropertyEditor m_Editors_times[];
  private PropertyEditor m_Editors_step[];

  /** Holds current object values for each bracketing property */
  private Object m_Editors_para_values[];
  private Object m_Editors_times_values[];
  private Object m_Editors_step_values[];

  /** Indicates if a property can be used for bracketing */
  private boolean m_activeParameter[];

  /** The labels for each property */
  private JLabel m_Labels[];

  /** Lables for the TO bracketing value **/
  private JLabel m_TOLabels[];

  private JLabel totalNumberOfExperiments;

  /** Choose the operator for each braketing*/
  private JComboBox[] m_operators;

  /** The tool tip text for each property */
  private String m_TipTexts[];

  /** StringBuffer containing help text for the object being edited */
  private StringBuffer m_HelpText;


  /** A count of the number of properties we have an editor for */
  private int m_NumEditable = 0;

  /** Numer bracketings we have to run **/
  private int m_numberClusterings = 1; 

  public BracketingPanel() {
    //setPreferredSize(new Dimension(300,200));
  }


  public synchronized void setSubspaceClusterClass(SubspaceClusterer clusterer) {
    //no updated needed if its still the same clusterer
    if(clusterer!= null && m_subspaceClustererClass!= null && clusterer.getClass().equals(m_subspaceClustererClass)){
      //rewrite the values in case of illegal parameter settings
      //TODO: The values are being rewritten, but it doesn't show somehow
      for (int i = 0; i < m_Editors_para.length; i++) {
        if(m_activeParameter[i]){
          m_Editors_para[i].setValue(m_Editors_para_values[i]);
          m_Editors_step[i].setValue(m_Editors_step_values[i]);
          m_Editors_times[i].setValue(m_Editors_times_values[i]);
        }
      }
      //doesn't do anything either, needs fixing
      //			for (int j = 0; j < this.getComponentCount(); j++) {
      //				this.getComponent(j).repaint();
      //			}

      return;
    }

    m_subspaceClustererClass = clusterer.getClass();
    // used to offset the components for the properties of targ
    // if there happens to be globalInfo available in targ
    int componentOffset = 1;

    setVisible(false);
    m_NumEditable = 0;

    // Close any child windows at this point
    removeAll();

    GridBagLayout gbLayout = new GridBagLayout();
    setLayout(gbLayout);

    GridBagConstraints gbC = new GridBagConstraints();
    gbC.fill = GridBagConstraints.HORIZONTAL;
    gbC.gridy = 0;
    gbC.gridx = 1;
    gbC.weightx = 1;
    gbC.insets = new Insets(10, 5, 0, 10);
    add(new JLabel("From"),gbC);

    gbC = new GridBagConstraints();
    gbC.fill = GridBagConstraints.HORIZONTAL;
    gbC.gridy = 0;
    gbC.gridx = 2;
    gbC.weightx = 1;
    gbC.insets = new Insets(10, 5, 0, 10);
    add(new JLabel("Offset"),gbC);

    gbC = new GridBagConstraints();
    gbC.fill = GridBagConstraints.HORIZONTAL;
    gbC.gridy = 0;
    gbC.gridx = 3;
    gbC.weightx = 1;
    gbC.insets = new Insets(10, 5, 0, 10);
    add(new JLabel("Op"),gbC);

    gbC = new GridBagConstraints();
    gbC.fill = GridBagConstraints.HORIZONTAL;
    gbC.gridy = 0;
    gbC.gridx = 4;
    gbC.weightx = 1;
    gbC.insets = new Insets(10, 5, 0, 10);
    add(new JLabel("Steps"),gbC);

    gbC = new GridBagConstraints();
    gbC.fill = GridBagConstraints.HORIZONTAL;
    gbC.gridy = 0;
    gbC.gridx = 5;
    gbC.weightx = 1;
    gbC.insets = new Insets(10, 5, 0, 10);
    add(new JLabel("To"),gbC);

    try {
      BeanInfo bi = Introspector.getBeanInfo(clusterer.getClass());
      m_Properties = bi.getPropertyDescriptors();
      m_Methods = bi.getMethodDescriptors();
    } catch (IntrospectionException ex) {
      System.err.println("BracketingPanel: Couldn't introspect");
      return;
    }

    m_Editors_para = new PropertyEditor[m_Properties.length];
    m_Editors_times = new PropertyEditor[m_Properties.length];
    m_Editors_step = new PropertyEditor[m_Properties.length];
    m_Editors_para_values = new Object[m_Properties.length];
    m_Editors_times_values = new Object[m_Properties.length];
    m_Editors_step_values = new Object[m_Properties.length];


    m_operators = new JComboBox[m_Properties.length];
    m_Labels = new JLabel[m_Properties.length];
    m_TOLabels = new JLabel[m_Properties.length];
    m_TipTexts = new String[m_Properties.length];
    m_activeParameter = new boolean [m_Properties.length];
    boolean firstTip = true;
    for (int i = 0; i < m_Properties.length; i++) {

      //disable parameter by default
      m_activeParameter[i] = false;

      // Don't display hidden or expert properties.
      if (m_Properties[i].isHidden() || m_Properties[i].isExpert()) {
        continue;
      }

      String name = m_Properties[i].getDisplayName();
      Class type = m_Properties[i].getPropertyType();
      Method getter = m_Properties[i].getReadMethod();
      Method setter = m_Properties[i].getWriteMethod();

      // Only display read/write properties.
      if (getter == null || setter == null) {
        continue;
      }

      //Filtering of bracketing parameter
      if ( !type.equals(double.class) && !type.equals(int.class)
          && !type.equals(Double.class) && !type.equals(Integer.class)) {
        continue;
      }

      //activate parameter if we have come so far
      m_activeParameter[i] = true;

      JComponent view_para = null;
      JComponent view_times = null;
      JComponent view_steps = null;

      try {
        Object args[] = {};
        Object value = getter.invoke(clusterer, args);
        m_Editors_para_values[i] = value;

        PropertyEditor editor_lower = null;
        Class pec = m_Properties[i].getPropertyEditorClass();
        if (pec != null) {
          try {
            editor_lower = (PropertyEditor) pec.newInstance();

          } catch (Exception ex) {
            // Drop through.
          }
        }
        if (editor_lower == null) {
          editor_lower = PropertyEditorManager.findEditor(type);
        }

        m_Editors_para[i] = editor_lower;
        m_Editors_times[i] = PropertyEditorManager.findEditor(int.class);
        m_Editors_step[i]  = PropertyEditorManager.findEditor(type);

        m_Editors_times[i].setAsText("1");
        m_Editors_step[i].setAsText("0");

        m_Editors_para_values[i] = editor_lower.getValue();
        m_Editors_times_values[i] = m_Editors_times[i].getValue();
        m_Editors_step_values[i] = m_Editors_step[i].getValue();

        m_Editors_para[i].addPropertyChangeListener(this);
        m_Editors_times[i].addPropertyChangeListener(this);
        m_Editors_step[i].addPropertyChangeListener(this);

        String[] ops = {"+","*"};
        m_operators[i] = new JComboBox(ops);		
        m_operators[i].addActionListener(this);


        // If we can't edit this component, skip it.
        if (editor_lower == null) {
          // If it's a user-defined property we give a warning.
          String getterClass = m_Properties[i].getReadMethod()
              .getDeclaringClass().getName();
          continue;
        }

        // Don't try to set null values:
        if (value == null) {
          // If it's a user-defined property we give a warning.
          String getterClass = m_Properties[i].getReadMethod()
              .getDeclaringClass().getName();
          continue;
        }

        m_Editors_para[i].setValue(value);

        // now look for a TipText method for this property
        String tipName = name + "TipText";
        for (int j = 0; j < m_Methods.length; j++) {
          String mname = m_Methods[j].getDisplayName();
          Method meth = m_Methods[j].getMethod();
          if (mname.equals(tipName)) {
            if (meth.getReturnType().equals(String.class)) {
              try {
                String tempTip = (String) (meth.invoke(
                    clusterer, args));
                int ci = tempTip.indexOf('.');
                if (ci < 0) {
                  m_TipTexts[i] = tempTip;
                } else {
                  m_TipTexts[i] = tempTip.substring(0, ci);
                }
                if (m_HelpText != null) {
                  if (firstTip) {
                    m_HelpText.append("OPTIONS\n");
                    firstTip = false;
                  }
                  m_HelpText.append(name).append(" -- ");
                  m_HelpText.append(tempTip).append("\n\n");
                  //jt.setText(m_HelpText.toString());
                }
              } catch (Exception ex) {

              }
              break;
            }
          }
        }

        // Now figure out how to display it...
        if (editor_lower.isPaintable() && editor_lower.supportsCustomEditor()) {
          view_para = new PropertyPanel(editor_lower);
        } else if (editor_lower.getTags() != null) {
          view_para = new PropertyValueSelector(editor_lower);
        } else if (editor_lower.getAsText() != null) {
          view_para = new PropertyText(editor_lower);
        } else {
          System.err.println("Warning: Property \"" + name
              + "\" has non-displayabale editor.  Skipping.");
          continue;
        }
        view_times = new PropertyText(m_Editors_times[i]);
        view_steps = new PropertyText(m_Editors_step[i]);
        if (view_times==null) {
          System.out.println("null");
        }

      } catch (InvocationTargetException ex) {
        System.err.println("Skipping property " + name
            + " ; exception on target: " + ex.getTargetException());
        ex.getTargetException().printStackTrace();
        continue;
      } catch (Exception ex) {
        System.err.println("Skipping property " + name
            + " ; exception: " + ex);
        ex.printStackTrace();
        continue;
      }

      m_Labels[i] = new JLabel(name, SwingConstants.RIGHT);
      m_Labels[i].setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 5));
      GridBagConstraints gbConstraints = new GridBagConstraints();
      gbConstraints.anchor = GridBagConstraints.EAST;
      gbConstraints.fill = GridBagConstraints.HORIZONTAL;
      gbConstraints.gridy = i + componentOffset;
      gbConstraints.gridx = 0;
      gbLayout.setConstraints(m_Labels[i], gbConstraints);
      add(m_Labels[i]);

      if (m_TipTexts[i] != null) {
        view_para.setToolTipText(m_TipTexts[i]);
      }

      gbConstraints = new GridBagConstraints();
      gbConstraints.anchor = GridBagConstraints.WEST;
      gbConstraints.fill = GridBagConstraints.BOTH;
      gbConstraints.gridy = i + componentOffset;
      gbConstraints.gridx = 1;
      gbConstraints.weightx = 50;
      gbConstraints.insets = new Insets(10, 5, 0, 10);
      view_para.setPreferredSize(new Dimension(60,20));
      add(view_para,gbConstraints);

      gbConstraints = new GridBagConstraints();
      gbConstraints.anchor = GridBagConstraints.WEST;
      gbConstraints.fill = GridBagConstraints.HORIZONTAL;
      gbConstraints.gridy = i + componentOffset;
      gbConstraints.gridx = 2;
      gbConstraints.weightx = 50;
      gbConstraints.insets = new Insets(10, 5, 0, 10);
      view_steps.setPreferredSize(new Dimension(60,20));
      add(view_steps,gbConstraints);

      gbConstraints = new GridBagConstraints();
      gbConstraints.anchor = GridBagConstraints.WEST;
      gbConstraints.fill = GridBagConstraints.HORIZONTAL;
      gbConstraints.gridy = i + componentOffset;
      gbConstraints.gridx = 3;
      gbConstraints.weightx = 50;
      gbConstraints.insets = new Insets(10, 5, 0, 10);
      add(m_operators[i],gbConstraints);

      gbConstraints = new GridBagConstraints();
      gbConstraints.anchor = GridBagConstraints.WEST;
      gbConstraints.fill = GridBagConstraints.HORIZONTAL;
      gbConstraints.gridy = i + componentOffset;
      gbConstraints.gridx = 4;
      gbConstraints.weightx = 50;
      gbConstraints.insets = new Insets(10, 5, 0, 10);
      view_times.setPreferredSize(new Dimension(30,20));
      add(view_times,gbConstraints);

      m_TOLabels[i] = new JLabel("", SwingConstants.LEFT);
      gbConstraints = new GridBagConstraints();
      gbConstraints.anchor = GridBagConstraints.EAST;
      gbConstraints.fill = GridBagConstraints.HORIZONTAL;
      gbConstraints.gridy = i + componentOffset;
      gbConstraints.gridx = 5;
      gbConstraints.weightx = 50;
      gbConstraints.insets = new Insets(10, 5, 0, 10);
      view_times.setPreferredSize(new Dimension(60,20));
      add(m_TOLabels[i],gbConstraints);

      m_NumEditable++;
    }

    gbC = new GridBagConstraints();
    gbC.fill = GridBagConstraints.HORIZONTAL;
    gbC.gridy = m_Properties.length + componentOffset;
    gbC.gridx = 1;
    gbC.weightx = 1;
    gbC.insets = new Insets(10, 5, 0, 10);
    add(new JLabel("Total number of experiments: "),gbC);

    gbC = new GridBagConstraints();
    gbC.fill = GridBagConstraints.HORIZONTAL;
    gbC.gridy = m_Properties.length + componentOffset;
    gbC.gridx = 2;
    gbC.weightx = 1;
    gbC.insets = new Insets(10, 5, 0, 10);
    totalNumberOfExperiments = new JLabel("1");
    add(totalNumberOfExperiments,gbC);


    if (m_NumEditable == 0) {
      JLabel empty = new JLabel("No editable propertiesss",
          SwingConstants.CENTER);
      Dimension d = empty.getPreferredSize();
      empty.setPreferredSize(new Dimension(d.width * 2, d.height * 2));
      empty.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 10));
      GridBagConstraints gbConstraints = new GridBagConstraints();
      gbConstraints.anchor = GridBagConstraints.CENTER;
      gbConstraints.fill = GridBagConstraints.HORIZONTAL;
      gbConstraints.gridy = componentOffset;
      gbConstraints.gridx = 0;
      gbLayout.setConstraints(empty, gbConstraints);
      add(empty);
    }

    validate();
    setVisible(true);

  }

  /**
   * Calculates the number of clusterings resulting from 
   * brackting parameter settings
   */
  private void updateNumberClusterings() {
    m_numberClusterings = 1;
    if(m_activeParameter!=null){
      for (int i = 0; i < m_activeParameter.length; i++) {
        if(m_activeParameter[i])
          m_numberClusterings*=(Integer)(m_Editors_times[i].getValue()); 
      }
    }
    if (m_numberClusterings == 0) m_numberClusterings = 1;
  }


  public String getParameterString(SubspaceClusterer clusterer, int p) throws Exception{
    if(clusterer!= null && m_subspaceClustererClass!= null && !clusterer.getClass().equals(m_subspaceClustererClass)){
      throw new Exception("Selected clusterer does not match bracketing parameters");
    }
    String name = " --> ";
    name+=Utils.joinOptions(((OptionHandler)clusterer).getOptions());

    return name;
  }


  /** Returns the number of clusterings resulting from brackting parameter settings
   * @return number of clusterings
   */
  public int getNumberClusterings(){
    updateNumberClusterings();
    return m_numberClusterings;
  }


  public void setBracketingParameter(SubspaceClusterer clusterer, int p) throws Exception{
    if(clusterer!= null && m_subspaceClustererClass!= null && !clusterer.getClass().equals(m_subspaceClustererClass)){
      throw new Exception("Selected clusterer does not match bracketing parameters");
    }

    updateNumberClusterings();		

    int total = m_numberClusterings;

    for (int i = m_activeParameter.length-1; i>= 0; i--) {
      if(m_activeParameter[i]){
        //get setter
        Method setter = m_Properties[i].getWriteMethod();

        //get offset for each parameter
        total = total/(Integer)(m_Editors_times_values[i]);

        int offset = p / total;
        p = p % total;

        //prepare setting
        Object value = null;


        Class typeSetter = m_Properties[i].getPropertyType();

        try{
          double para  = ((Number)(m_Editors_para_values[i])).doubleValue();

          if(m_operators[i].getSelectedIndex() == 1){
            para = para * Math.pow(((Number)(m_Editors_step_values[i])).doubleValue(),(double) offset);
          }
          else{
            para = para + (double) offset * ((Number)(m_Editors_step_values[i])).doubleValue();
          }

          if(typeSetter.equals(double.class)) {
            value = (double) para;
          }
          if(typeSetter.equals(int.class)) {
            value = (int) para;
          }


        }
        catch (Exception e) {
          e.printStackTrace();
        }

        if(value == null) throw new Exception("Unkown parameter class type:"+setter.getParameterTypes()[0]);

        //set the parameter
        try {
          Object args[] = { value };
          setter.invoke(clusterer, args);
        } catch (Exception ex) {
          ex.printStackTrace();
          throw new Exception("Couldn't set bracketing parameter");
        }

      }
    }
  }


  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    wasModified(evt);
    updateLabels();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() instanceof JComboBox) {
      updateLabels();
    }		
  }

  //update Labels
  private void updateLabels(){
    for (int i = 0; i < m_TOLabels.length; i++) {
      if(m_TOLabels[i]!=null){
        double para  = ((Number)(m_Editors_para_values[i])).doubleValue();
        double steps = (double)(Integer)(m_Editors_times[i].getValue());
        double offset = ((Number)(m_Editors_step_values[i])).doubleValue();

        double val = 0.0;
        if(m_operators[i].getSelectedIndex() == 1){
          val = para * Math.pow(offset,steps-1);
        }
        else{
          val =  para + offset * (steps-1);
        }
        m_TOLabels[i].setText(Double.toString(val));
        m_TOLabels[i].repaint();
      }
    }

    if(totalNumberOfExperiments!=null)
      totalNumberOfExperiments.setText(Integer.toString(getNumberClusterings()));

  }

  synchronized void wasModified(PropertyChangeEvent evt) {

    if (evt.getSource() instanceof PropertyEditor) {
      PropertyEditor editor = (PropertyEditor) evt.getSource();
      for (int i = 0; i < m_Editors_para.length; i++) {
        if (m_Editors_para[i] == editor) {
          m_Editors_para_values[i] = editor.getValue();
          return;
        }
      }
      for (int i = 0; i < m_Editors_step.length; i++) {
        if (m_Editors_step[i] == editor) {
          m_Editors_step_values[i] = editor.getValue();
          return;
        }
      }
      for (int i = 0; i < m_Editors_times.length; i++) {
        if (m_Editors_times[i] == editor) {
          m_Editors_times_values[i] = editor.getValue();
          return;
        }
      }
    }

  }
}
