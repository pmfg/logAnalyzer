package pt.lsts.loganalizer;

import java.util.HashMap;
import java.util.Map;

import pt.lsts.imc.Current;
import pt.lsts.imc.EstimatedState;
import pt.lsts.imc.GpsFix;
import pt.lsts.imc.LogBookEntry;
import pt.lsts.imc.LoggingControl;
import pt.lsts.imc.Rpm;
import pt.lsts.imc.Voltage;
import pt.lsts.imc.net.Consume;
import pt.lsts.util.WGS84Utilities;

public class LogDataExtractor {

    public int CNT_ERROR = 0;
    public int CNT_CRITICAL = 1;
    public int CNT_WARNING = 2;
    public int CNT_ALL = 3;

    private String logName;
    private String systemName;
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

    private static int maxNumberOfEntity = 32;
    private static Map<Integer, String> currentEntity = new HashMap<>();
    private CurrentValues currentValues[] = new CurrentValues[maxNumberOfEntity];

    private static Map<Integer, String> voltageEntity = new HashMap<>();
    private VoltageValues voltageValues[] = new VoltageValues[maxNumberOfEntity];

    private double depth;
    private boolean haveRpmActuation;
    private double lastDepthValue;
    private double totalDist;
    private double lastXValue;
    private double lastYValue;
    boolean isFirstValue;

    public LogDataExtractor(Map<Integer, String> entityIdLabel) {
        logName = "null";
        systemName = "\"\"";
        for (int i = 0; i < 4; i++)
            cntState[i] = 0;

        for (int i = 0; i < maxNumberOfEntity; i++) {
            currentValues[i] = new CurrentValues();
            voltageValues[i] = new VoltageValues();
        }

        entityLabel = entityIdLabel;

        cntErrorMsg = 0;
        cntWarningMsg = 0;
        cntCriticalMsg = 0;

        haveRpmActuation = false;
        lastDepthValue = 0;
        totalDist = 0;
        lastXValue = 0;
        lastYValue = 0;
        isFirstValue = true;
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
                allState.put(cntAlllMsg, "WARNING");
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
        Object[] stringText = new Object[7];
        stringText[0] = allState.get(id);
        stringText[1] = getSystemName();
        stringText[2] = getLogName();
        stringText[3] = allDate.get(id);
        stringText[4] = allTask.get(id);
        stringText[5] = allMessage.get(id);
        stringText[6] = allEntity.get(id);

        return stringText;
    }

    @Consume
    public void on(LoggingControl msg) {
        if (!msg.getName().equals(logName) && msg.getName().length() > 8) {
            logName = msg.getName();
            systemName = msg.getSourceName();
        }
    }

    public String getLogName() {
        return logName;
    }

    public String getSystemName() {
        return systemName;
    }

    public int[] getStatusCnt() {
        return cntState;
    }

    @Consume
    public void on(Current msg) {
        if (currentEntity.size() == 0) {
            currentEntity.put(0, entityLabel.get((int) msg.getSrcEnt()));
        }
        else {
            boolean haveEntity = false;
            for (int i = 0; i < currentEntity.size(); i++) {
                if (currentEntity.get(i).equals(entityLabel.get((int) msg.getSrcEnt())))
                    haveEntity = true;
            }

            if (!haveEntity) {
                currentEntity.put(currentEntity.size(), entityLabel.get((int) msg.getSrcEnt()));
            }
        }

        for (int i = 0; i < currentEntity.size(); i++) {
            if (currentEntity.get(i).equals(entityLabel.get((int) msg.getSrcEnt()))) {
                currentValues[i].addValues(msg.getValue(), (double) msg.getTimestampMillis());
            }
        }
    }

    public int getSizeCurrentEntity() {
        return currentEntity.size();
    }

    public String getCurrentEntity(int id) {
        return currentEntity.get(id);
    }

    public Object[] getValuesOfEntityCurrent(int id, int sample) {
        return currentValues[id].getValues(sample);
    }

    public int getCntOfEntityCurrent(int id) {
        return currentValues[id].getSizeData();
    }

    @Consume
    public void on(Voltage msg) {
        if (voltageEntity.size() == 0) {
            voltageEntity.put(0, entityLabel.get((int) msg.getSrcEnt()));
        }
        else {
            boolean haveEntity = false;
            for (int i = 0; i < voltageEntity.size(); i++) {
                if (voltageEntity.get(i).equals(entityLabel.get((int) msg.getSrcEnt())))
                    haveEntity = true;
            }

            if (!haveEntity) {
                voltageEntity.put(voltageEntity.size(), entityLabel.get((int) msg.getSrcEnt()));
            }
        }

        for (int i = 0; i < voltageEntity.size(); i++) {
            if (voltageEntity.get(i).equals(entityLabel.get((int) msg.getSrcEnt()))) {
                voltageValues[i].addValues(msg.getValue(), (double) msg.getTimestampMillis());
            }
        }
    }

    public int getSizeVoltageEntity() {
        return voltageEntity.size();
    }

    public String getVoltageEntity(int id) {
        return voltageEntity.get(id);
    }

    public Object[] getValuesOfEntityVoltage(int id, int sample) {
        return voltageValues[id].getValues(sample);
    }

    public int getCntOfEntityVoltage(int id) {
        return voltageValues[id].getSizeData();
    }

    @Consume
    public void on(Rpm msg) {
        if (msg.getValue() != 0)
            haveRpmActuation = true;
        else
            haveRpmActuation = false;
    }

    @Consume
    public void on(EstimatedState msg) {
        if (isFirstValue) {
            lastXValue = msg.getX();
            lastYValue = msg.getY();
            lastDepthValue = msg.getDepth();
            isFirstValue = false;
        }
        else {
            if (haveRpmActuation) {
                double x = msg.getX();
                double y = msg.getY();
                double dist = Math.hypot((x - lastXValue), (y - lastYValue));
                depth = Math.abs(msg.getDepth());
                //System.out.println("dist: " + dist + " m");

                dist = Math.hypot(dist, (depth - lastDepthValue));
                totalDist = totalDist + dist;
                // System.out.println("dist: "+dist+ " total: "+totalDist);

                lastXValue = x;
                lastYValue = y;
                lastDepthValue = depth;
            }
        }
    }

    public double getTotalDistKm() {
        return getTotalDistM() * 0.001;
    }

    public double getTotalDistM() {
        return totalDist;
    }

    public double getTotalDistNauticalMiles() {
        return getTotalDistKm() * 0.539956803;
    }

}
