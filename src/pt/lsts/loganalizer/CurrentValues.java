package pt.lsts.loganalizer;

public class CurrentValues {

    private Double[] value = new Double[32600];
    private Double[] time = new Double[32600];
    private int cntSize = 0;

    public CurrentValues() {
        super();
    }

    public void addValues(Double _value, Double _time) {
        value[cntSize] = _value;
        time[cntSize++] = _time;
    }

    public int getSizeData() {
        return cntSize;
    }

    public Object[] getValues(int id) {
        Object[] currentValues = new Object[2];
        currentValues[0] = value[id];
        currentValues[1] = time[id];

        return currentValues;
    }

    public void initCnt() {
        cntSize = 0;
    }
}
