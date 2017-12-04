package pt.lsts.loganalizer;

import java.util.HashMap;
import java.util.Map;

import pt.lsts.imc.LogBookEntry;
import pt.lsts.imc.LoggingControl;
import pt.lsts.imc.net.Consume;

public class GetEntityStatus {

    public int CNT_ERROR = 0;
    public int CNT_CRITICAL = 1;
    public int CNT_WARMING = 2;

    private String logName;
    private static int cntState[] = new int[4];
    private static Map<Integer, String> entityLabel = new HashMap<>();

    public GetEntityStatus(Map<Integer, String> entityIdLabel) {
        logName = "NULL";
        for (int i = 0; i < 3; i++)
            cntState[i] = 0;
        entityLabel = entityIdLabel;
    }

    @Consume
    public void on(LogBookEntry msg) {
        if (msg.getType() == LogBookEntry.TYPE.ERROR) {
            System.out.println("LogBookEntry: " + msg.getDate() + " # " + logName + " # " + msg.getContext() + " # "
                    + msg.getText() + " # " + entityLabel.get((int) msg.getSrcEnt()));
            cntState[CNT_ERROR]++;
        }
        else if (msg.getType() == LogBookEntry.TYPE.CRITICAL) {
            System.out.println("LogBookEntry: " + msg.getDate() + " # " + logName + " # " + msg.getContext() + " # "
                    + msg.getText() + " # " + entityLabel.get((int) msg.getSrcEnt()));
            cntState[CNT_CRITICAL]++;
        }
        else if (msg.getType() == LogBookEntry.TYPE.WARNING) {
            if (!msg.getText().equals("now in 'CALIBRATION' mode")
                    && !msg.getText().equals("now in 'MANEUVERING' mode")) {
                System.out.println("LogBookEntry: " + msg.getDate() + " # " + logName + " # " + msg.getContext() + " # "
                        + msg.getText() + " # " + entityLabel.get((int) msg.getSrcEnt()));
                cntState[CNT_WARMING]++;
            }
        }
    }

    @Consume
    public void on(LoggingControl msg) {
        if (!msg.getName().equals(logName) && msg.getName().length() > 8) {
            logName = msg.getName();
        }
    }

    public int[] getStatusCnt() {
        return cntState;
    }
}
