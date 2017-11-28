package pt.lsts.loganalizer;

import java.io.File;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import pt.lsts.loganalizer.GetLabelEntity;

import pt.lsts.imc.LogBookEntry;
import pt.lsts.imc.LoggingControl;
import pt.lsts.imc.lsf.batch.LsfBatch;
import pt.lsts.imc.net.Consume;

public class ProcessLog {

    static int CNT_ERROR = 0;
    static int CNT_CRITICAL = 1;
    static int CNT_WARMING = 2;

    static JFrame frame = null;
    static int widhtFrame = 800;
    static int heightFrame = 460;
    static JMenuBar menuBar;
    static JMenu menu, submenu;
    static JMenuItem menuItem;
    static LsfBatch batch;
    static String log_name;
    static String[] entityIdLabel = new String[256];
    static GetLabelEntity m_label_entity;
    static int cntState[] = new int[4];
    static ImageIcon loading;
    static JPanel container;
    static JLabel m_text;

    public ProcessLog() {
        super();
    }

    public void addInfoOfLog(String log_path, boolean graphicMode) {
        if (graphicMode) {
            layoutInit();
            System.out.println(log_path);
            processLog(log_path, true);
            System.exit(1);
        }
        else {
            System.out.println(log_path);
            processLog(log_path, false);
            System.exit(1);
        }
    }

    private void layoutInit() {
        log_name = "NULL";
        frame = new JFrame("Filter Loader");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setSize(widhtFrame + 10, heightFrame + 35);
        frame.setVisible(true);
        frame.setFocusable(true);
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        URL pathUrl = LogAnalizer.class.getResource("/resources/img/load.gif");
        loading = new ImageIcon(pathUrl);
        container.add(new JLabel(loading, JLabel.CENTER), JFrame.CENTER_ALIGNMENT);
        m_text = new JLabel("Loading Entity Label id.", JLabel.CENTER);
        container.add(m_text);
        frame.add(container);
        for (int i = 0; i < 3; i++)
            cntState[i] = 0;
    }

    @Consume
    public void on(LogBookEntry msg) {
        if (msg.getType() == LogBookEntry.TYPE.ERROR) {
            System.out.println("LogBookEntry: " + msg.getDate() + " # " + log_name + " # " + msg.getContext() + " # "
                    + msg.getText() + " # " + entityIdLabel[msg.getSrcEnt()]);
            cntState[CNT_ERROR]++;
        }
        else if (msg.getType() == LogBookEntry.TYPE.CRITICAL) {
            System.out.println("LogBookEntry: " + msg.getDate() + " # " + log_name + " # " + msg.getContext() + " # "
                    + msg.getText() + " # " + entityIdLabel[msg.getSrcEnt()]);
            cntState[CNT_CRITICAL]++;
        }
        else if (msg.getType() == LogBookEntry.TYPE.WARNING) {
            if (!msg.getText().equals("now in 'CALIBRATION' mode")
                    && !msg.getText().equals("now in 'MANEUVERING' mode")) {
                System.out.println("LogBookEntry: " + msg.getDate() + " # " + log_name + " # " + msg.getContext()
                        + " # " + msg.getText() + " # " + entityIdLabel[msg.getSrcEnt()]);
                cntState[CNT_WARMING]++;
            }
        }
    }

    @Consume
    public void on(LoggingControl msg) {
        if (!msg.getName().equals(log_name) && msg.getName().length() > 8) {
            log_name = msg.getName();
        }
    }

    public void processLog(String path, boolean graphicMode) {
        try {
            File[] file = new File[1];
            file[0] = new File(path);
            batch = LsfBatch.processFolders(file);
            m_label_entity = new GetLabelEntity();
            batch.process(m_label_entity);
            entityIdLabel = m_label_entity.getEntityLabel();
            batch = LsfBatch.processFolders(file);
            System.out.println("##########################");
            if (graphicMode)
                m_text.setText("Loading Entity States of Tasks.");
            batch.process(new ProcessLog());
            System.out.println("\n\nEND VIEW");
            System.out.println("Error: " + cntState[CNT_ERROR]);
            System.out.println("Critical: " + cntState[CNT_CRITICAL]);
            System.out.println("Warming: " + cntState[CNT_WARMING]);
        }
        catch (Exception e) {
            System.out.println("ERROR loading log, is the correct path???");
        }
    }

}
