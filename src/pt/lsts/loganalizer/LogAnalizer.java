package pt.lsts.loganalizer;

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
    static boolean graphicMode;
    static ProcessLog processLog;

    public LogAnalizer() {
        super();
    }

    static void readInputArgs(String[] args) {
        Options options = new Options();
        Option input = new Option("l", null, true, "log path");
        input.setRequired(false);
        options.addOption(input);

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
            if (cmd.hasOption('l') && cmd.hasOption('g')) {
                graphicMode = true;
                logPath = cmd.getOptionValue('l');
            }
            else if (cmd.hasOption('l')) {
                logPath = cmd.getOptionValue('l');
            }
            else if (cmd.hasOption('g')) {
                System.out.println("GRAPHIC MODE - work in progress");
                System.exit(1);
            }
            else {
                formatter.printHelp("LogAnalizer [options]", options);
                System.exit(1);
            }
            return;
        }
    }

    public static void main(String[] args) {
        graphicMode = false;
        readInputArgs(args);
        processLog = new ProcessLog();
        processLog.addInfoOfLog(logPath, graphicMode);
        while (true) {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}