package pt.lsts.loganalizer;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class LogAnalizer extends JLabel {

    private static final long serialVersionUID = 1L;

    static String logPath;
    static String logPathOutput;
    static boolean graphicMode;
    static ProcessLog processLog;

    public LogAnalizer() {
        super();
    }

    private static void readInputArgs(String[] args) {
        Options options = new Options();
        Option input = new Option("l", null, true, "log path");
        input.setRequired(false);
        options.addOption(input);

        Option output = new Option("o", null, true, "log path to save results");
        output.setRequired(false);
        options.addOption(output);

        Option graphic = new Option("g", null, false, "graphic mode");
        graphic.setRequired(false);
        options.addOption(graphic);

        Option help = new Option("h", null, false, "help info");
        help.setRequired(false);
        options.addOption(help);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        }
        catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("LogAnalizer [options]", options);
            System.exit(1);
            return;
        }

        if (cmd.hasOption('h') || args.length == 0) {
            formatter.printHelp("LogAnalizer [options]", options);
            System.exit(1);
            return;
        }
        else {
            if (cmd.hasOption('l') && cmd.hasOption('g') && cmd.hasOption('o')) {
                graphicMode = true;
                logPath = cmd.getOptionValue('l');
                logPathOutput = cmd.getOptionValue('o');
            }
            else if (cmd.hasOption('l') && cmd.hasOption('g')) {
                graphicMode = true;
                logPath = cmd.getOptionValue('l');
            }
            else if (cmd.hasOption('l') && cmd.hasOption('o')) {
                logPath = cmd.getOptionValue('l');
                logPathOutput = cmd.getOptionValue('o');
            }
            else if (cmd.hasOption('g')) {
                JFileChooser f = new JFileChooser();
                f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                f.setDialogTitle("Log folder");
                f.setApproveButtonText("Open");
                if (f.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    logPath = f.getSelectedFile().toString();
                    graphicMode = true;
                }
                else {
                    System.out.println("No log selected");
                    System.exit(1);
                }
            }
            else {
                System.out.println(">>>  Missing config parameters???");
                formatter.printHelp("LogAnalizer [options]", options);
                System.exit(1);
            }
            return;
        }
    }

    public static void main(String[] args) {
        logPathOutput = "null";
        graphicMode = false;
        readInputArgs(args);
        processLog = new ProcessLog();
        while (true) {
            if (processLog.addInfoOfLog(logPath, graphicMode, logPathOutput)) {
                JFileChooser f = new JFileChooser();
                f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                f.setApproveButtonText("Open");
                f.setDialogTitle("Log folder");
                if (f.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    logPath = f.getSelectedFile().toString();
                    graphicMode = true;
                    processLog = new ProcessLog();
                }
                else {
                    System.out.println("No log selected");
                    System.exit(1);
                }
            }
            else
                System.exit(1);
        }
    }

}