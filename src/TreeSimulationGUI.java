import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

import static java.lang.Double.valueOf;

/**
 * Created by IntelliJ IDEA.
 * User: mhall
 * Date: 09/12/2011
 * Time: 13:26
 * To change this template use File | Settings | File Templates.
 */

public class TreeSimulationGUI {

    private JFrame frame;
    private HashMap<String, String> fileNames = null;
    private HashMap<String, Double> parameters = null;
    private HashMap<String, Boolean> switches = null;
    private JCheckBox CSCheckBox;
    private JCheckBox DNCheckBox;
    private JCheckBox DSCheckBox;
    private JTextField CNFileNameField;
    private JTextField CSFileNameField;
    private JTextField DNFileNameField;
    private JTextField DSFileNameField;
    private JTextField incGammaShapeField;
    private JTextField incGammaScaleField;
    private JTextField infGammaShapeField;
    private JTextField infGammaScaleField;
    private JTextField R0Field;
    private JTextField samplingStartField;
    private JTextField samplingEndField;
    private JTextField samplingProbField;
    private JTextArea textArea;

    public TreeSimulationGUI(){
        frame = new JFrame("Rabies tree simulation");
        fileNames = new HashMap<String, String>();
        parameters = new HashMap<String, Double>();
        switches = new HashMap<String, Boolean>();
        switches.put("Also create a tree with unsampled information removed", false);
        switches.put("Also create a tree with branch lengths in generations", false);
        switches.put("Also create a tree with branch lengths in generations and unsampled information removed", false);
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));
        incGammaShapeField = labelledDoubleEntry("Incubation period gamma shape parameter:", contentPane, 3, "Incubation period gamma shape parameter", 1.0);
        parameters.put("Incubation period gamma shape parameter", 1.0);
        incGammaScaleField = labelledDoubleEntry("Incubation period gamma scale parameter:", contentPane, 3, "Incubation period gamma scale parameter", 1.0);
        parameters.put("Incubation period gamma scale parameter", 1.0);
        infGammaShapeField = labelledDoubleEntry("Infectious period gamma shape parameter:", contentPane, 3, "Infectious period gamma shape parameter", 1.0);
        parameters.put("Infectious period gamma shape parameter", 1.0);
        infGammaScaleField = labelledDoubleEntry("Infectious period gamma scale parameter:", contentPane, 3, "Infectious period gamma scale parameter", 1.0);
        parameters.put("Infectious period gamma scale parameter", 1.0);
        samplingStartField = labelledDoubleEntry("Start of sampling (years since root):", contentPane, 3, "Start of sampling", 0.0);
        parameters.put("Start of sampling", 0.0);
        samplingEndField = labelledDoubleEntry("End of sampling (years since root):", contentPane, 3, "End of sampling", 5.0);
        parameters.put("End of sampling", 5.0);
        samplingProbField = labelledDoubleEntry("Probability of sampling:", contentPane, 3, "Probability of sampling", 0.5);
        parameters.put("Probability of sampling", 0.5);
        R0Field = labelledDoubleEntry("R0:", contentPane, 3, "R0", 1.0);
        parameters.put("R0", 1.0);
        CNFileNameField = labelledTextEntry("Filename:", contentPane, 30, "Main file", "maintree.tre");
        fileNames.put("Main file", "maintree.tre");
        CSCheckBox = treeVersionOption("Also create a tree with unsampled information removed", contentPane);
        switches.put("Also create a tree with unsampled information removed",false);
        CSFileNameField = labelledTextEntry("Sampled-only filename:", contentPane, 30, "Pruned file", "prunedtree.tre");
        fileNames.put("Pruned file", "prunedtree.tre");
        CSFileNameField.setEnabled(false);
        DNCheckBox = treeVersionOption("Also create a tree with branch lengths in generations", contentPane);
        switches.put("Also create a tree with branch lengths in generations",false);
        DNFileNameField = labelledTextEntry("Discrete tree filename:", contentPane, 30, "Discrete file", "discrete.tre");
        fileNames.put("Discrete file", "discretetree.tre");
        DNFileNameField.setEnabled(false);
        DSCheckBox = treeVersionOption("Also create a tree with branch lengths in generations and unsampled information removed", contentPane);
        switches.put("Also create a tree with branch lengths in generations and unsampled information removed",false);
        DSFileNameField = labelledTextEntry("Sampled-only discrete tree filename:", contentPane, 30, "Discrete pruned file", "pruneddiscretetree.tre");
        fileNames.put("Discrete pruned file", "pruneddiscretetree.tre");
        DSFileNameField.setEnabled(false);
        JButton goButton = new JButton("Run");
        goButton.addActionListener(new goGoGo());
        contentPane.add(goButton);
        textArea = new JTextArea(4,1);
        contentPane.add(textArea);
        frame.pack();
        frame.setVisible(true);
    }

    public void itemStateChanged(ItemEvent event){
    }

    public void actionPerformed(ActionEvent event) {
    }

    class checkBoxListener implements ItemListener {

        public void itemStateChanged(ItemEvent event){
            String testing = ((JCheckBox)event.getItem()).getText();
            for(String key: switches.keySet()){
                if(((JCheckBox)event.getItem()).getText().equals(key)){
                    if (event.getStateChange()==ItemEvent.SELECTED){
                        switches.put(key,true);
                    } else {
                        switches.put(key,false);
                    }
                }
            }
        }
    }

    class textInputListener implements DocumentListener {

        public textInputListener(){
        }

        public void operate(DocumentEvent event) {
            for(String key: fileNames.keySet()){
                if(event.getDocument().getProperty("Identifier").equals(key)){
                    try{
                        if(!event.getDocument().getText(0,event.getDocument().getLength()).isEmpty()){
                        fileNames.put(key, event.getDocument().getText(0,event.getDocument().getLength()));
                        }
                    } catch(BadLocationException e) {
                        System.out.println("Something went wrong");
                    }
                }
            }
        }

        public void removeUpdate(DocumentEvent event) {
            operate(event);
        }

        public void insertUpdate(DocumentEvent event) {
            operate(event);
        }

        public void changedUpdate(DocumentEvent event) {
            operate(event);
        }

    }

    class doubleInputListener implements DocumentListener {

        public doubleInputListener(){
        }

        public void operate(DocumentEvent event) {
            for(String key: parameters.keySet()){
                if(event.getDocument().getProperty("Identifier").equals(key)){
                    try {
                        if(!event.getDocument().getText(0,event.getDocument().getLength()).isEmpty()){
                        parameters.put(key,Double.valueOf(event.getDocument().getText(0,event.getDocument().getLength())));
                        }
                    } catch(BadLocationException e) {
                        System.out.println("Something went wrong");
                    }
                }
            }
        }

        public void removeUpdate(DocumentEvent event) {
            operate(event);
        }

        public void insertUpdate(DocumentEvent event) {
            operate(event);
        }

        public void changedUpdate(DocumentEvent event) {
            operate(event);
        }
    }

    class goGoGo implements ActionListener {
        public void actionPerformed(ActionEvent event){
            Object[] options1={"Run", "Cancel"};
            final int n = JOptionPane.showOptionDialog(frame, "Incubation period mean: "+ parameters.get("Incubation period gamma scale parameter")*parameters.get("Incubation period gamma shape parameter") +" years\n" +
                    "Incubation period variance: "+ parameters.get("Incubation period gamma scale parameter")*parameters.get("Incubation period gamma shape parameter")*parameters.get("Incubation period gamma scale parameter") +" years\n" +
                    "Infectious period mean: "+ parameters.get("Infectious period gamma scale parameter")*parameters.get("Infectious period gamma shape parameter") +" years\n" +
                    "Infectious period variance: "+ parameters.get("Infectious period gamma scale parameter")*parameters.get("Infectious period gamma shape parameter")*parameters.get("Infectious period gamma scale parameter") +" years\n",
                    "Information", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options1, options1[0]);
            if (n==0) {
                ContinuousTreeSimulationTimeLimited rabiesSim = new ContinuousTreeSimulationTimeLimited(parameters.get("Probability of sampling"),parameters.get("Start of sampling"),parameters.get("End of sampling"),parameters.get("R0"),parameters.get("Incubation period gamma shape parameter"),parameters.get("Incubation period gamma scale parameter"),parameters.get("Infectious period gamma shape parameter"),parameters.get("Infectious period gamma scale parameter"));
                int[] numbers = rabiesSim.runSimulation(switches.get("Also create a tree with unsampled information removed"), switches.get("Also create a tree with branch lengths in generations"), switches.get("Also create a tree with branch lengths in generations and unsampled information removed"), fileNames.get("Main file"), fileNames.get("Pruned file"), fileNames.get("Discrete file"), fileNames.get("Discrete pruned file"));
                textArea.setText("");
                textArea.append("Last run: \n");
                if(!switches.get("Also create a tree with unsampled information removed")){
                    textArea.append("Total corpses: " + numbers[0] + "\n");
                } else {
                    textArea.append("Sampled corpses: " + numbers[0] + "\n" + "Sampled corpses: " + numbers[1] + "\n");
                }
            }
        }

    }

    private JCheckBox treeVersionOption(String text, Container contentPane) {
        JCheckBox checkbox = new JCheckBox(text);
        checkbox.setSelected(false);
        checkbox.addItemListener(new checkBoxListener());
        contentPane.add(checkbox);
        return checkbox;
    }

    private JTextField labelledTextEntry(String text, Container contentPane, int width, String id, String defaultEntry) {
        Container subContainer = new Container();
        subContainer.setLayout(new FlowLayout());
        JLabel entryLabel = new JLabel(text);
        JTextField fileName = new JTextField(defaultEntry, width);
        subContainer.add(entryLabel);
        subContainer.add(fileName);
        Document fileNameDoc = fileName.getDocument();
        fileNameDoc.putProperty("Identifier", id);
        fileNameDoc.addDocumentListener(new textInputListener());
        contentPane.add(subContainer);
        return fileName;
    }

    private JTextField labelledDoubleEntry(String text, Container contentPane, int width, String id, Double defaultEntry) {
        Container subContainer = new Container();
        subContainer.setLayout(new FlowLayout());
        JLabel entryLabel = new JLabel(text);
        JTextField fileName = new JTextField(defaultEntry.toString(), width);
        subContainer.add(entryLabel);
        subContainer.add(fileName);
        Document fileNameDoc = fileName.getDocument();
        fileNameDoc.putProperty("Identifier", id);
        fileNameDoc.addDocumentListener(new doubleInputListener());
        contentPane.add(subContainer);
        return fileName;
    }

    public static void main(String[] args) {
        TreeSimulationGUI rabiesSimGUI = new TreeSimulationGUI();
    }

}
