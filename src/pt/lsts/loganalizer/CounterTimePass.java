package pt.lsts.loganalizer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.JTextArea;

public class CounterTimePass implements Runnable {

    private int sec = 0;
    private boolean isToStop;

    private JTextArea mInfoText;
    private String backText;

    public CounterTimePass(JTextArea infoText) {
        mInfoText = infoText;
        backText = infoText.getText();
    }

    @Override
    public void run() {
        isToStop = false;
        while (!isToStop) {
            try {
                Thread.sleep(1000);
                mInfoText.setText(backText + " : " + secondsToTime(sec++));
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopThread() {
        isToStop = true;
        System.out.println("Task duration: " + secondsToTime(sec));
    }

    String secondsToTime(int minutes) {
        Date date = new Date((long) (sec) * 1000);
        SimpleDateFormat isoFormat = new SimpleDateFormat("HH:mm:ss");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return isoFormat.format(date);
    }

}
