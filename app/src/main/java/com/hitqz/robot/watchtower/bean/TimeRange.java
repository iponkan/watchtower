package com.hitqz.robot.watchtower.bean;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimeRange {

    public TimeStruct struStartTime = new TimeStruct();

    public TimeStruct struStopTime = new TimeStruct();

    public static TimeRange getDayTimeRange(TimeStruct timeStruct) {

        int year = timeStruct.dwYear;
        int month = timeStruct.dwMonth;
        int day = timeStruct.dwDay;
        int hour = timeStruct.dwHour;
        int min = timeStruct.dwMinute;
        int sec = timeStruct.dwSecond;
        return getDayTimeRange(year, month, day, hour, min, sec);
    }

    public static TimeRange getDayTimeRange(int year, int month, int day, int hour, int min, int sec) {
        TimeRange timeRange = new TimeRange();
        TimeStruct start = new TimeStruct();
        start.dwYear = year;
        start.dwMonth = month;
        start.dwDay = day;
        start.dwHour = hour;
        start.dwMinute = min;
        start.dwSecond = sec;

        Date date = start.toDate();

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);
        Date stopDate = calendar.getTime();


        TimeStruct stop = TimeStruct.fromMillSeconds(stopDate.getTime());

        timeRange.struStartTime = start;
        timeRange.struStopTime = stop;

        return timeRange;
    }


}
