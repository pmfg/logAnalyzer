package pt.lsts.loganalizer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class CounterTimePass implements Runnable {

    private int sec = 0;
    private boolean isToStop;

    @Override
    public void run() {
        isToStop = false;
        while (!isToStop) {
            try {
                Thread.sleep(1000);
                System.out.println("Time pass: " + secondsToTime(sec++));
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopThread() {
        isToStop = true;
    }

    String secondsToTime(int minutes) {
        Date date = new Date((long) (sec) * 1000);
        SimpleDateFormat isoFormat = new SimpleDateFormat("HH:mm:ss");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return isoFormat.format(date);
    }

}
