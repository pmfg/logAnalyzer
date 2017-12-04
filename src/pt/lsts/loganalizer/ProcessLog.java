package pt.lsts.loganalizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import pt.lsts.imc.lsf.batch.LsfBatch;

public class ProcessLog {

    private Object columnNames[] = { "Date/Time", "Task", "Message", "Entity Name" };

    private JFrame frame = null;
    private JPanel container;
    private int widhtFrame = 800;
    private int heightFrame = 460;
    // private JMenuBar menuBar;
    // private JMenu menu, submenu;
    // private JMenuItem menuItem;
    private LsfBatch batch;
    private Map<Integer, String> entityIdLabel = new HashMap<>();
    private GetLabelEntity labelEntity;
    private GetEntityStatus entityStatus;
    private ImageIcon loading;
    // private JPanel container;
    private JTextArea infoText;
    private JLabel image;
    private JTable error;
    private JTable warming;
    private JTable critical;
    private DefaultTableModel modelErrorTable;
    private DefaultTableModel modelWarmingTable;
    private DefaultTableModel modelCriticalTable;
    private int[] cntState;

    public ProcessLog() {
        super();
    }

    public void addInfoOfLog(String log_path, boolean graphicMode) {
        if (graphicMode) {
            layoutInit();
            System.out.println("Log: " + log_path);
            processLog(log_path, true);
        }
        else {
            System.out.println("Log: " + log_path);
            processLog(log_path, false);
        }
    }

    private void processLog(String path, boolean graphicMode) {
        try {
            if (graphicMode) {
                infoText.setText("Log: " + path);
                infoText.setText(infoText.getText() + " \nLoading Entity Label id.");
            }

            System.out.println("Loading Entity Label id.");
            batch = LsfBatch.processFolders(new File[] { new File(path) });
            labelEntity = new GetLabelEntity();
            batch.process(labelEntity);
            entityIdLabel = labelEntity.getEntityLabel();

            if (graphicMode)
                infoText.setText(infoText.getText() + " \nLoading Entity States of Tasks.");

            System.out.println("Loading Entity States of Tasks.");
            batch = LsfBatch.processFolders(new File[] { new File(path) });
            entityStatus = new GetEntityStatus(entityIdLabel);
            batch.process(entityStatus);

            processResults(graphicMode);
        }
        catch (Exception e) {
            System.out.println("ERROR loading log, is the correct path???");
            e.printStackTrace();
        }
    }

    private void layoutInit() {
        frame = new JFrame("LogAnalyzer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setSize(widhtFrame, heightFrame);
        // frame.getContentPane().setBackground( Color.red );
        frame.setVisible(true);
        frame.setFocusable(true);
        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        URL pathUrl = LogAnalizer.class.getResource("/resources/img/load.gif");
        loading = new ImageIcon(pathUrl);
        image = new JLabel(loading, JLabel.CENTER);
        container.add(image, JFrame.CENTER_ALIGNMENT);
        infoText = new JTextArea();
        infoText.setEditable(false);
        container.add(infoText);
        frame.add(container);
    }

    private void processResults(boolean graphicMode) {
        cntState = entityStatus.getStatusCnt();
        System.out.println("\n\nEND VIEW");
        System.out.println("Error: " + cntState[entityStatus.CNT_ERROR]);
        System.out.println("Critical: " + cntState[entityStatus.CNT_CRITICAL]);
        System.out.println("Warming: " + cntState[entityStatus.CNT_WARMING]);

        if (graphicMode) {
            /*
             * infoText.setText(infoText.getText()+"\n\nError: " + cntState[entityStatus.CNT_ERROR]);
             * infoText.setText(infoText.getText()+"\nCritical: " + cntState[entityStatus.CNT_CRITICAL]);
             * infoText.setText(infoText.getText()+"\nWarming: " + cntState[entityStatus.CNT_WARMING]);
             * infoText.setText(infoText.getText()+"\nDONE");
             */
            image.setVisible(false);
            infoText.setVisible(false);
            printToTable();
        }
        else {
            Object[] text;
            for (int i = 0; i < cntState[entityStatus.CNT_ERROR]; i++) {
                text = entityStatus.getErrorString(i);
                System.out.println(text[0] + " ; " + text[1] + " ; " + text[2] + " ; " + text[3] + " ; ");
            }

            for (int i = 0; i < cntState[entityStatus.CNT_CRITICAL]; i++) {
                text = entityStatus.getCriticalString(i);
                System.out.println(text[0] + " ; " + text[1] + " ; " + text[2] + " ; " + text[3] + " ; ");
            }

            for (int i = 0; i < cntState[entityStatus.CNT_WARMING]; i++) {
                text = entityStatus.getWarmingString(i);
                System.out.println(text[0] + " ; " + text[1] + " ; " + text[2] + " ; " + text[3] + " ; ");
            }

            System.exit(1);
        }

        while (true) {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void printToTable() {
        // error table
        TableModel modelError = new DefaultTableModel(null, columnNames) {
            private static final long serialVersionUID = 3219347207460269967L;

            public boolean isCellEditable(int rowIndex, int mColIndex) {
                return false;
            }
        };
        error = new JTable(modelError);
        error.setFillsViewportHeight(true);
        error.setBackground(new Color(250, 100, 100));
        modelErrorTable = (DefaultTableModel) error.getModel();
        for (int i = 0; i < cntState[entityStatus.CNT_ERROR]; i++)
            modelErrorTable.addRow(entityStatus.getErrorString(i));

        JScrollPane errorScrollPane = new JScrollPane(error);

        // warming table
        TableModel modelWarming = new DefaultTableModel(null, columnNames) {
            private static final long serialVersionUID = 3219347207460269968L;

            public boolean isCellEditable(int rowIndex, int mColIndex) {
                return false;
            }
        };
        warming = new JTable(modelWarming);
        warming.setFillsViewportHeight(true);
        warming.setBackground(new Color(230, 190, 80));
        modelWarmingTable = (DefaultTableModel) warming.getModel();
        for (int i = 0; i < cntState[entityStatus.CNT_WARMING]; i++)
            modelWarmingTable.addRow(entityStatus.getWarmingString(i));

        JScrollPane warmingScrollPane = new JScrollPane(warming);

        // critical table
        TableModel modelCritical = new DefaultTableModel(null, columnNames) {
            private static final long serialVersionUID = 3219347207460269969L;

            public boolean isCellEditable(int rowIndex, int mColIndex) {
                return false;
            }
        };
        critical = new JTable(modelCritical);
        critical.setFillsViewportHeight(true);
        critical.setBackground(new Color(170, 80, 230));
        modelCriticalTable = (DefaultTableModel) critical.getModel();
        for (int i = 0; i < cntState[entityStatus.CNT_CRITICAL]; i++)
            modelCriticalTable.addRow(entityStatus.getCriticalString(i));
        JScrollPane criticalScrollPane = new JScrollPane(critical);

        // text label for tables
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        JTextArea errorText = new JTextArea("\nERROR\n");
        errorText.setEditable(false);
        JTextArea warmingText = new JTextArea("\nWARMING\n");
        warmingText.setEditable(false);
        JTextArea criticalText = new JTextArea("\nCRITICAL\n");
        criticalText.setEditable(false);

        container.add(errorText, BorderLayout.CENTER);
        container.add(errorScrollPane, BorderLayout.CENTER);
        container.add(warmingText, BorderLayout.CENTER);
        container.add(warmingScrollPane, BorderLayout.CENTER);
        container.add(criticalText, BorderLayout.CENTER);
        container.add(criticalScrollPane, BorderLayout.CENTER);
        // frame.pack();
        frame.setVisible(true);
    }
}
