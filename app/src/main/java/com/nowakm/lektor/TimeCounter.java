package com.nowakm.lektor;

import android.os.SystemClock;

/**
 * Created by Marcin on 4/1/2015.
 */
public class TimeCounter {
    long startTime;

    public TimeCounter() {
        startTime = SystemClock.elapsedRealtime();
    }

    public long getTimePassed() {
        return SystemClock.elapsedRealtime() - startTime;
    }

    public String getFormattedTimePassed() {
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        long capturedTime = getTimePassed();
        while (capturedTime > 3600000) {
            capturedTime -= 3600000;
            hours++;
        }
        while (capturedTime > 60000) {
            capturedTime -= 60000;
            minutes++;
        }
        while (capturedTime > 1000) {
            capturedTime -= 1000;
            seconds++;
        }
        String builtResult = "";
        if (hours < 10)
            builtResult = builtResult + "0";
        builtResult = builtResult + hours + ":";
        if (minutes < 10)
            builtResult = builtResult + "0";
        builtResult = builtResult + minutes + ":";
        if (seconds < 10)
            builtResult = builtResult + "0";
        builtResult = builtResult + seconds + ".";
        capturedTime /= 100;
        builtResult = builtResult + capturedTime;
        return builtResult;
    }
}