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

    private static Map<Integer, String> errorDate = new HashMap<>();
    private static Map<Integer, String> errorTask = new HashMap<>();
    private static Map<Integer, String> errorMessage = new HashMap<>();
    private static Map<Integer, String> errorEntity = new HashMap<>();
    private int cntErrorMsg;

    private static Map<Integer, String> warmingDate = new HashMap<>();
    private static Map<Integer, String> warmingTask = new HashMap<>();
    private static Map<Integer, String> warmingMessage = new HashMap<>();
    private static Map<Integer, String> warmingEntity = new HashMap<>();
    private int cntWarmingMsg;

    private static Map<Integer, String> criticalDate = new HashMap<>();
    private static Map<Integer, String> criticalTask = new HashMap<>();
    private static Map<Integer, String> criticalMessage = new HashMap<>();
    private static Map<Integer, String> criticalEntity = new HashMap<>();
    private int cntCriticalMsg;

    public GetEntityStatus(Map<Integer, String> entityIdLabel) {
        logName = "NULL";
        for (int i = 0; i < 3; i++)
            cntState[i] = 0;
        entityLabel = entityIdLabel;

        cntErrorMsg = 0;
        cntWarmingMsg = 0;
        cntCriticalMsg = 0;
    }

    @Consume
    public void on(LogBookEntry msg) {
        if (msg.getType() == LogBookEntry.TYPE.ERROR) {
            // System.out.println("LogBookEntry: " + msg.getDate() + " # " + logName + " # " + msg.getContext() + " # "
            // + msg.getText() + " # " + entityLabel.get((int) msg.getSrcEnt()));
            cntState[CNT_ERROR]++;

            errorDate.put(cntErrorMsg, msg.getDate().toString());
            errorTask.put(cntErrorMsg, msg.getContext());
            errorMessage.put(cntErrorMsg, msg.getText());
            errorEntity.put(cntErrorMsg++, entityLabel.get((int) msg.getSrcEnt()));

        }
        else if (msg.getType() == LogBookEntry.TYPE.CRITICAL) {
            // System.out.println("LogBookEntry: " + msg.getDate() + " # " + logName + " # " + msg.getContext() + " # "
            // + msg.getText() + " # " + entityLabel.get((int) msg.getSrcEnt()));
            cntState[CNT_CRITICAL]++;

            criticalDate.put(cntCriticalMsg, msg.getDate().toString());
            criticalTask.put(cntCriticalMsg, msg.getContext());
            criticalMessage.put(cntCriticalMsg, msg.getText());
            criticalEntity.put(cntCriticalMsg++, entityLabel.get((int) msg.getSrcEnt()));

        }
        else if (msg.getType() == LogBookEntry.TYPE.WARNING) {
            if (!msg.getText().equals("now in 'CALIBRATION' mode")
                    && !msg.getText().equals("now in 'MANEUVERING' mode")) {
                // System.out.println("LogBookEntry: " + msg.getDate() + " # " + logName + " # " + msg.getContext() + "
                // # "
                // + msg.getText() + " # " + entityLabel.get((int) msg.getSrcEnt()));
                cntState[CNT_WARMING]++;
                warmingDate.put(cntWarmingMsg, msg.getDate().toString());
                warmingTask.put(cntWarmingMsg, msg.getContext());
                warmingMessage.put(cntWarmingMsg, msg.getText());
                warmingEntity.put(cntWarmingMsg++, entityLabel.get((int) msg.getSrcEnt()));
            }
        }
    }

    public Object[] getErrorString(int id) {
        Object[] stringText = new Object[4];
        stringText[0] = errorDate.get(id);
        stringText[1] = errorTask.get(id);
        stringText[2] = errorMessage.get(id);
        stringText[3] = errorEntity.get(id);

        return stringText;
    }

    public Object[] getWarmingString(int id) {
        Object[] stringText = new Object[4];
        stringText[0] = warmingDate.get(id);
        stringText[1] = warmingTask.get(id);
        stringText[2] = warmingMessage.get(id);
        stringText[3] = warmingEntity.get(id);

        return stringText;
    }

    public Object[] getCriticalString(int id) {
        Object[] stringText = new Object[4];
        stringText[0] = criticalDate.get(id);
        stringText[1] = criticalTask.get(id);
        stringText[2] = criticalMessage.get(id);
        stringText[3] = criticalEntity.get(id);

        return stringText;
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
