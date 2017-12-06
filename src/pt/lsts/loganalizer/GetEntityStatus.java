package pt.lsts.loganalizer;

import java.util.HashMap;
import java.util.Map;

import pt.lsts.imc.LogBookEntry;
import pt.lsts.imc.LoggingControl;
import pt.lsts.imc.net.Consume;

public class GetEntityStatus {

    public int CNT_ERROR = 0;
    public int CNT_CRITICAL = 1;
    public int CNT_WARNING = 2;
    public int CNT_ALL = 3;

    private String logName;
    private static int cntState[] = new int[5];
    private static Map<Integer, String> entityLabel = new HashMap<>();

    private static Map<Integer, String> errorDate = new HashMap<>();
    private static Map<Integer, String> errorTask = new HashMap<>();
    private static Map<Integer, String> errorMessage = new HashMap<>();
    private static Map<Integer, String> errorEntity = new HashMap<>();
    private int cntErrorMsg;

    private static Map<Integer, String> warningDate = new HashMap<>();
    private static Map<Integer, String> warningTask = new HashMap<>();
    private static Map<Integer, String> warningMessage = new HashMap<>();
    private static Map<Integer, String> warningEntity = new HashMap<>();
    private int cntWarningMsg;

    private static Map<Integer, String> criticalDate = new HashMap<>();
    private static Map<Integer, String> criticalTask = new HashMap<>();
    private static Map<Integer, String> criticalMessage = new HashMap<>();
    private static Map<Integer, String> criticalEntity = new HashMap<>();
    private int cntCriticalMsg;

    private static Map<Integer, String> allState = new HashMap<>();
    private static Map<Integer, String> allDate = new HashMap<>();
    private static Map<Integer, String> allTask = new HashMap<>();
    private static Map<Integer, String> allMessage = new HashMap<>();
    private static Map<Integer, String> allEntity = new HashMap<>();
    private int cntAlllMsg;

    public GetEntityStatus(Map<Integer, String> entityIdLabel) {
        logName = "NULL";
        for (int i = 0; i < 4; i++)
            cntState[i] = 0;
        entityLabel = entityIdLabel;

        cntErrorMsg = 0;
        cntWarningMsg = 0;
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

            cntState[CNT_ALL]++;
            allState.put(cntAlllMsg, "ERROR");
            allDate.put(cntAlllMsg, msg.getDate().toString());
            allTask.put(cntAlllMsg, msg.getContext());
            allMessage.put(cntAlllMsg, msg.getText());
            allEntity.put(cntAlllMsg++, entityLabel.get((int) msg.getSrcEnt()));

        }
        else if (msg.getType() == LogBookEntry.TYPE.CRITICAL) {
            // System.out.println("LogBookEntry: " + msg.getDate() + " # " + logName + " # " + msg.getContext() + " # "
            // + msg.getText() + " # " + entityLabel.get((int) msg.getSrcEnt()));
            cntState[CNT_CRITICAL]++;
            criticalDate.put(cntCriticalMsg, msg.getDate().toString());
            criticalTask.put(cntCriticalMsg, msg.getContext());
            criticalMessage.put(cntCriticalMsg, msg.getText());
            criticalEntity.put(cntCriticalMsg++, entityLabel.get((int) msg.getSrcEnt()));

            cntState[CNT_ALL]++;
            allState.put(cntAlllMsg, "CRITICAL");
            allDate.put(cntAlllMsg, msg.getDate().toString());
            allTask.put(cntAlllMsg, msg.getContext());
            allMessage.put(cntAlllMsg, msg.getText());
            allEntity.put(cntAlllMsg++, entityLabel.get((int) msg.getSrcEnt()));

        }
        else if (msg.getType() == LogBookEntry.TYPE.WARNING) {
            if (!msg.getText().equals("now in 'CALIBRATION' mode")
                    && !msg.getText().equals("now in 'MANEUVERING' mode")) {
                // System.out.println("LogBookEntry: " + msg.getDate() + " # " + logName + " # " + msg.getContext() + "
                // # "
                // + msg.getText() + " # " + entityLabel.get((int) msg.getSrcEnt()));
                cntState[CNT_WARNING]++;
                warningDate.put(cntWarningMsg, msg.getDate().toString());
                warningTask.put(cntWarningMsg, msg.getContext());
                warningMessage.put(cntWarningMsg, msg.getText());
                warningEntity.put(cntWarningMsg++, entityLabel.get((int) msg.getSrcEnt()));

                cntState[CNT_ALL]++;
                allState.put(cntAlllMsg, "WARMING");
                allDate.put(cntAlllMsg, msg.getDate().toString());
                allTask.put(cntAlllMsg, msg.getContext());
                allMessage.put(cntAlllMsg, msg.getText());
                allEntity.put(cntAlllMsg++, entityLabel.get((int) msg.getSrcEnt()));
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

    public Object[] getWarningString(int id) {
        Object[] stringText = new Object[4];
        stringText[0] = warningDate.get(id);
        stringText[1] = warningTask.get(id);
        stringText[2] = warningMessage.get(id);
        stringText[3] = warningEntity.get(id);

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

    public Object[] getAllString(int id) {
        Object[] stringText = new Object[5];
        stringText[0] = allState.get(id);
        stringText[1] = allDate.get(id);
        stringText[2] = allTask.get(id);
        stringText[3] = allMessage.get(id);
        stringText[4] = allEntity.get(id);

        return stringText;
    }

    @Consume
    public void on(LoggingControl msg) {
        if (!msg.getName().equals(logName) && msg.getName().length() > 8) {
            logName = msg.getName();
        }
    }

    public String getLogName() {
        return logName;
    }
    
    public int[] getStatusCnt() {
        return cntState;
    }
}
