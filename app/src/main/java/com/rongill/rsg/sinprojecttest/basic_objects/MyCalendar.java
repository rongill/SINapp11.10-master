package com.rongill.rsg.sinprojecttest.basic_objects;

import java.io.Serializable;
import java.util.Calendar;

public class MyCalendar implements Serializable {
    private String date;
    private String time;
    private long millisFromEpoch;


    public MyCalendar(){
        Calendar c = Calendar.getInstance();
        date = String.valueOf(c.get(Calendar.DAY_OF_MONTH)) + "/" +
                String.valueOf(c.get(Calendar.MONTH)+1) + "/" +
                String.valueOf(c.get(Calendar.YEAR));
        time = String.valueOf(c.get(Calendar.HOUR_OF_DAY)) + ":" +
                String.valueOf(c.get(Calendar.MINUTE)) + ":" +
                String.valueOf(c.get(Calendar.SECOND));
        millisFromEpoch = c.getTimeInMillis();
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getMillisFromEpoch() {
        return millisFromEpoch;
    }

    public void setMillisFromEpoch(long millisFromEpoch) {
        this.millisFromEpoch = millisFromEpoch;
    }

    public int timeDiffInSeconds(MyCalendar currentTime){
        return (int)((currentTime.getMillisFromEpoch() - this.millisFromEpoch)/1000);
    }
}
