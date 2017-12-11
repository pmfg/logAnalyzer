package pt.lsts.loganalizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import pt.lsts.imc.lsf.batch.LsfBatch;

public class ProcessLog {

    private Object columnNames[] = { "STATUS", "System Name", "Log Name", "Date/Time", "Task", "Message",
            "Entity Name" };
    private Color COLOR_ERROR = new Color(250, 100, 100);
    private Color COLOR_WARNING = new Color(230, 190, 80);
    private Color COLOR_CRITICAL = new Color(170, 80, 230);

    private JFrame frame = null;
    private JPanel container;
    private int widhtFrame = 960;
    private int heightFrame = 640;
    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem menuItem;
    private LsfBatch batch;
    private Map<Integer, String> entityIdLabel = new HashMap<>();
    private GetLabelEntity labelEntity;
    private GetEntityStatus entityStatus;
    private ImageIcon loading;
    // private JPanel container;
    private JTextArea infoText;
    private JLabel image;
    private JTable tableState;
    private DefaultTableModel modelTableState;
    private int[] cntState;
    private boolean newPath;
    private String logPathSave;

    public ProcessLog() {
        super();
    }

    public boolean addInfoOfLog(String log_path, boolean graphicMode, String logPathOutput) {
        newPath = false;
        logPathSave = logPathOutput;
        System.out.println("AQUI1: " + logPathSave);
        if (graphicMode) {
            layoutInit();
            System.out.println("Log: " + log_path);
            return processLog(log_path, true);
        }
        else {
            System.out.println("Log: " + log_path);
            return processLog(log_path, false);
        }
    }

    private boolean processLog(String path, boolean graphicMode) {
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

            return processResults(graphicMode);
        }
        catch (Exception e) {
            System.out.println("ERROR loading log, is the correct path???");
            e.printStackTrace();
            return false;
        }
    }

    private void layoutInit() {
        frame = new JFrame("LogAnalyzer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setSize(widhtFrame, heightFrame);
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

        menuBar = new JMenuBar();
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);

        menuItem = new JMenuItem("Open folder");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new MenuActionListener());
        menu.add(menuItem);

        frame.setJMenuBar(menuBar);
    }

    private boolean processResults(boolean graphicMode) {
        cntState = entityStatus.getStatusCnt();
        System.out.println("\nError: " + cntState[entityStatus.CNT_ERROR]);
        System.out.println("Critical: " + cntState[entityStatus.CNT_CRITICAL]);
        System.out.println("Warning: " + cntState[entityStatus.CNT_WARNING]);
        System.out.println("All mesages: " + cntState[entityStatus.CNT_ALL] + "\n");

        if (graphicMode) {
            image.setVisible(false);
            infoText.setVisible(false);
            printToCSV();
            printToTable(true);
            printToPdf();
        }
        else {

            printToCSV();
            printToTable(false);
            printToPdf();
            System.exit(1);
        }

        boolean exitWhile = false;
        while (!exitWhile) {
            try {
                Thread.sleep(1000);
                if (newPath)
                    exitWhile = true;
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private void printToCSV() {
        if (logPathSave.equals("null")) {
            logPathSave = System.getProperty("user.home") + "/logResults";
            createDirLog(logPathSave);
        }
        else {
            createDirLog(logPathSave);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        String fileName = logPathSave + "/" + entityStatus.getSystemName() + "_"
                + entityStatus.getLogName().replace('/', '_') + "_#_" + sdf.format(new Date()) + ".csv";
        fileName = fileName.replace(' ', '_').replace(':', '-');
        PrintWriter csv = null;
        try {
            csv = new PrintWriter(new File(fileName));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Object[] text;
        for (int i = 0; i < cntState[entityStatus.CNT_ALL]; i++) {
            text = entityStatus.getAllString(i);
            String textCSV = text[0] + " ; " + text[1] + " ; " + text[2] + " ; " + text[3] + " ; " + text[4] + " ; "
                    + text[5] + " ; " + text[6] + " ;\n";
            // System.out.println(textCSV);
            csv.write(textCSV);
        }
        csv.close();
        System.out.println("done export to csv: " + fileName);
    }

    private boolean createDirLog(String path) {
        File theDir = new File(path);
        if (!theDir.exists()) {
            System.out.println("creating directory: " + theDir.getName());
            boolean result = false;
            try {
                theDir.mkdir();
                result = true;
            }
            catch (SecurityException se) {
                System.out.println("DIR not created");
                se.printStackTrace();
                return false;
            }

            if (result) {
                System.out.println("DIR created " + path);
                return true;
            }
            else {
                System.out.println("DIR not created");
                return false;
            }
        }
        else {
            return true;
        }
    }

    private void printToPdf() {
        if (logPathSave.equals("null")) {
            logPathSave = System.getProperty("user.home") + "/logResults";
            createDirLog(logPathSave);
        }
        else {
            createDirLog(logPathSave);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        String fileName = logPathSave + "/" + entityStatus.getSystemName() + "_"
                + entityStatus.getLogName().replace('/', '_') + "_#_" + sdf.format(new Date()) + ".pdf";
        fileName = fileName.replace(' ', '_').replace(':', '-');

        Font font = FontFactory.getFont("Times Roman", 9, Color.BLACK);
        Document doc = new Document(PageSize.A4.rotate());
        doc.setMargins(10, 10, 10, 10);

        try {
            PdfWriter.getInstance(doc, new FileOutputStream(fileName));
            doc.open();
            PdfPTable pdfTable = new PdfPTable(tableState.getColumnCount());
            pdfTable.setWidthPercentage(100);
            // adding table headers
            for (int i = 0; i < tableState.getColumnCount(); i++) {
                PdfPCell cell = new PdfPCell(new Phrase(tableState.getColumnName(i), font));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfTable.addCell(cell);
            }
            // extracting data from the JTable and inserting it to PdfPTable
            for (int rows = 0; rows < tableState.getRowCount(); rows++) {
                Object[] temp = entityStatus.getAllString(rows);
                for (int cols = 0; cols < tableState.getColumnCount(); cols++) {
                    try {
                        PdfPCell cell = new PdfPCell(
                                new Phrase(tableState.getModel().getValueAt(rows, cols).toString(), font));
                        if (temp[0].equals("ERROR")) {
                            cell.setBackgroundColor(COLOR_ERROR);
                        }
                        else if (temp[0].equals("CRITICAL")) {
                            cell.setBackgroundColor(COLOR_CRITICAL);
                        }
                        else if (temp[0].equals("WARNING")) {
                            cell.setBackgroundColor(COLOR_WARNING);
                        }

                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        pdfTable.addCell(cell);
                    }
                    catch (Exception e) {
                        // e.printStackTrace();
                    }
                }
            }
            doc.add(pdfTable);
            doc.close();
            System.out.println("done export to pdf: " + fileName);
        }
        catch (DocumentException ex) {
            ex.printStackTrace();
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private void printToTable(boolean graphicMode) {
        TableModel modelTable = new DefaultTableModel(null, columnNames) {
            private static final long serialVersionUID = 3219347207460269970L;

            public boolean isCellEditable(int rowIndex, int mColIndex) {
                return false;
            }
        };

        tableState = new JTable(modelTable);
        tableState.setFillsViewportHeight(true);
        tableState.setDefaultRenderer(Object.class, new TableCellRender());
        modelTableState = (DefaultTableModel) tableState.getModel();

        for (int i = 0; i < cntState[entityStatus.CNT_ALL]; i++)
            modelTableState.addRow(entityStatus.getAllString(i));

        if (graphicMode) {
            JScrollPane tableScrollPane = new JScrollPane(tableState);
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
            container.add(tableScrollPane, BorderLayout.CENTER);
            frame.setVisible(true);
        }
    }

    public class MenuActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Selected: " + e.getActionCommand());
            frame.dispose();
            newPath = true;
        }
    }
}
