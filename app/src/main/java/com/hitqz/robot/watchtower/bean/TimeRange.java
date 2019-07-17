package com.hitqz.robot.watchtower.bean;

import java.util.Calendar;
import java.util.Date;

public class TimeRange {

    public TimeStruct struStartTime = new TimeStruct();

    public TimeStruct struStopTime = new TimeStruct();

    public static TimeRange getDayTimeRange(TimeStruct timeStruct) {

        int year = timeStruct.dwYear;
        int month = timeStruct.dwMonth;
        int day = timeStruct.dwDay;
        return getDayTimeRange(year, month, day);
    }

    public static TimeRange getDayTimeRange(int year, int month, int day) {
        TimeRange timeRange = new TimeRange();
        TimeStruct start = new TimeStruct();
        start.dwYear = year;
        start.dwMonth = month;
        start.dwDay = day;
        start.dwHour = 0;
        start.dwMinute = 0;
        start.dwSecond = 0;

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        calendar.add(Calendar.DATE, 1);
        Date date = calendar.getTime();


        TimeStruct stop = TimeStruct.fromMillSeconds(date.getTime());

        timeRange.struStartTime = start;
        timeRange.struStopTime = stop;

        return timeRange;
    }
}
