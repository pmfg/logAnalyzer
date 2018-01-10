package pt.lsts.loganalizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DecimalFormat;
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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import pt.lsts.imc.lsf.batch.LsfBatch;

public class ProcessLog {

    private Object columnNames[] = { "STATUS", "System Name", "Log Name", "Date/Time", "Task", "Message",
            "Entity Name" };
    private Color COLOR_ERROR = new Color(250, 100, 100);
    private Color COLOR_WARNING = new Color(230, 190, 80);
    private Color COLOR_CRITICAL = new Color(170, 80, 230);

    private static DecimalFormat df2 = new DecimalFormat("#.##");

    private JFrame frame = null;
    private JPanel container;
    private int widhtFrame = 640;
    private int heightFrame = 360;
    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem menuItem;
    private LsfBatch batch;
    private Map<Integer, String> entityIdLabel = new HashMap<>();
    private EntityLabelInfo labelEntity;
    private LogDataExtractor entityStatus;
    private ImageIcon loading;
    // private JPanel container;
    private JTextArea infoText;
    private JLabel image;
    private JTable tableState;
    private DefaultTableModel modelTableState;
    private int[] cntState;
    private boolean newPath;
    private String logPathSave;
    private JPanel plotData;
    private JFreeChart chartCurrent;
    private JFreeChart chartVoltage;
    private int xPlotSizePdf = 840;
    private int yPlotSizePdf = 480;

    public ProcessLog() {
        super();
    }

    public boolean addInfoOfLog(String log_path, boolean graphicMode, String logPathOutput) {
        newPath = false;
        logPathSave = logPathOutput;
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
            Thread t = null;
            CounterTimePass counterTime = null;
            if (graphicMode) {
                infoText.setText("Log: " + path);
                infoText.setText(infoText.getText() + " \nLoading Entity Label id.");
                counterTime = new CounterTimePass(infoText);
                t = new Thread(counterTime);
                t.start();
            }

            System.out.println("Loading Entity Label id.");
            batch = LsfBatch.processFolders(new File[] { new File(path) });

            labelEntity = new EntityLabelInfo();
            batch.process(labelEntity);
            entityIdLabel = labelEntity.getEntityLabel();

            if (graphicMode) {
                counterTime.stopThread();
                t.join(1000);
                infoText.setText(infoText.getText() + " \nLoading Entity States of Tasks.");
                counterTime = new CounterTimePass(infoText);
                t = new Thread(counterTime);
                t.start();
            }

            System.out.println("Loading Entity States of Tasks.");
            batch = LsfBatch.processFolders(new File[] { new File(path) });
            entityStatus = new LogDataExtractor(entityIdLabel);
            batch.process(entityStatus);
            if (graphicMode) {
                counterTime.stopThread();
                t.join(1000);
            }

            return processResults(graphicMode);
        }
        catch (Exception e) {
            System.out.println("ERROR processing log, is the correct path???");
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
        System.out.println("Total distance: " + df2.format(entityStatus.getTotalDistKm()) + " km  ( "
                + df2.format(entityStatus.getTotalDistNauticalMiles()) + " nautical miles )");

        if (graphicMode) {
            image.setVisible(false);
            infoText.setVisible(false);
            printToCSV();
            printToTable(true);
            plotGraphic(true);
            printToPdf();
        }
        else {

            printToCSV();
            printToTable(false);
            plotGraphic(false);
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
            csv.write(textCSV);
        }
        csv.write("\nTotal distance;" + df2.format(entityStatus.getTotalDistKm()) + ";km;"
                + df2.format(entityStatus.getTotalDistNauticalMiles()) + ";nautical miles;\n");
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
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(fileName));
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
                    }
                }
            }
            doc.add(pdfTable);
            doc.newPage();

            PdfContentByte cb = writer.getDirectContent();
            PdfTemplate tp = cb.createTemplate(xPlotSizePdf, yPlotSizePdf);
            Graphics2D g2 = tp.createGraphics(xPlotSizePdf, yPlotSizePdf, new DefaultFontMapper());
            JFreeChart chart = chartCurrent;
            chart.draw(g2, new Rectangle(xPlotSizePdf, yPlotSizePdf));
            g2.dispose();
            cb.addTemplate(tp, 0, 0);
            doc.newPage();

            PdfContentByte cb2 = writer.getDirectContent();
            PdfTemplate tp2 = cb2.createTemplate(xPlotSizePdf, yPlotSizePdf);
            Graphics2D g22 = tp2.createGraphics(xPlotSizePdf, yPlotSizePdf, new DefaultFontMapper());
            JFreeChart chart2 = chartVoltage;
            chart2.draw(g22, new Rectangle(xPlotSizePdf, yPlotSizePdf));
            g22.dispose();
            cb2.addTemplate(tp2, 0, 0);

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

    private void plotGraphic(boolean graphicMode) {
        if (graphicMode) {
            plotData = new JPanel();
            plotData.setLayout(new BoxLayout(plotData, BoxLayout.X_AXIS));
        }
        plotCurrent(graphicMode);
        plotVoltage(graphicMode);

        if (graphicMode) {
            container.add(plotData);
            frame.pack();
        }
    }

    private void plotVoltage(boolean graphicMode) {
        final XYSeries series[] = new XYSeries[entityStatus.getSizeVoltageEntity()];

        for (int i = 0; i < entityStatus.getSizeVoltageEntity(); i++)
            series[i] = new XYSeries(entityStatus.getVoltageEntity(i));

        for (int t = 0; t < entityStatus.getSizeVoltageEntity(); t++) {
            int cnt = entityStatus.getCntOfEntityVoltage(t);
            for (int i = 0; i < cnt; i++) {
                Object[] values = entityStatus.getValuesOfEntityVoltage(t, i);
                series[t].add((Double) values[1], (Double) values[0]);
            }
        }

        final XYSeriesCollection data = new XYSeriesCollection();
        for (int i = 0; i < entityStatus.getSizeVoltageEntity(); i++)
            data.addSeries(series[i]);

        chartVoltage = ChartFactory.createXYLineChart("Voltage", "Time", "Value", data, PlotOrientation.VERTICAL, true,
                true, false);

        XYPlot plot = (XYPlot) chartVoltage.getPlot();
        DateAxis dateAxis = new DateAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("H:mm:s.S"));
        plot.setDomainAxis(dateAxis);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.lightGray);

        if (graphicMode) {
            final ChartPanel chartPanel = new ChartPanel(chartVoltage);
            plotData.add(chartPanel);
        }
    }

    private void plotCurrent(boolean graphicMode) {
        final XYSeries series[] = new XYSeries[entityStatus.getSizeCurrentEntity()];

        for (int i = 0; i < entityStatus.getSizeCurrentEntity(); i++)
            series[i] = new XYSeries(entityStatus.getCurrentEntity(i));

        for (int t = 0; t < entityStatus.getSizeCurrentEntity(); t++) {
            int cnt = entityStatus.getCntOfEntityCurrent(t);
            for (int i = 0; i < cnt; i++) {
                Object[] values = entityStatus.getValuesOfEntityCurrent(t, i);
                series[t].add((Double) values[1], (Double) values[0]);
            }
        }

        final XYSeriesCollection data = new XYSeriesCollection();
        for (int i = 0; i < entityStatus.getSizeCurrentEntity(); i++)
            data.addSeries(series[i]);

        chartCurrent = ChartFactory.createXYLineChart("Current", "Time", "Value", data, PlotOrientation.VERTICAL, true,
                true, false);

        XYPlot plot = (XYPlot) chartCurrent.getPlot();
        DateAxis dateAxis = new DateAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("H:mm:s.S"));
        plot.setDomainAxis(dateAxis);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.lightGray);

        if (graphicMode) {
            final ChartPanel chartPanel = new ChartPanel(chartCurrent);
            plotData.add(chartPanel);
        }
    }
}
