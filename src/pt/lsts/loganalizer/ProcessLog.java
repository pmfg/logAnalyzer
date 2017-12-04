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
import pt.lsts.imc.lsf.batch.LsfBatch;

public class ProcessLog {

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
    private JTable table;

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
        frame = new JFrame("Filter Loader");
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
        int[] cntState = entityStatus.getStatusCnt();
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
        // System.exit(1);
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
        Object rowData[][] = { { "Row1-Column1", "Row1-Column2", "Row1-Column3" },
                { "Row2-Column1", "Row2-Column2", "Row2-Column3" } };
        Object columnNames[] = { "Column One", "Column Two", "Column Three" };

        JTable error = new JTable(rowData, columnNames);
        error.setFillsViewportHeight(true);
        error.setBackground(new Color(200, 70, 70));
        JScrollPane errorScrollPane = new JScrollPane(error);

        JTable warming = new JTable(rowData, columnNames);
        warming.setFillsViewportHeight(true);
        warming.setBackground(new Color(230, 190, 80));
        JScrollPane warmingScrollPane = new JScrollPane(warming);

        JTable critical = new JTable(rowData, columnNames);
        critical.setFillsViewportHeight(true);
        critical.setBackground(new Color(170, 80, 230));
        JScrollPane criticalScrollPane = new JScrollPane(critical);

        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        container.add(errorScrollPane, BorderLayout.CENTER);
        container.add(warmingScrollPane, BorderLayout.CENTER);
        container.add(criticalScrollPane, BorderLayout.CENTER);
        // frame.add(container);
        // frame.pack();
        frame.setVisible(true);
    }
}
