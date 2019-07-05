package com.hitqz.robot.watchtower.util;

import androidx.annotation.NonNull;

import com.hikvision.netsdk.NET_DVR_TIME;
import com.hitqz.robot.watchtower.bean.TimeStruct;

public class TimeUtil {

    public static String formatTimeS(long seconds) {
        if (seconds < 0) {
            seconds = 0;
        }
        int temp = 0;
        StringBuffer sb = new StringBuffer();
        if (seconds > 3600) {
            temp = (int) (seconds / 3600);
            sb.append((seconds / 3600) < 10 ? "0" + temp + ":" : temp + ":");
            temp = (int) (seconds % 3600 / 60);
            changeSeconds(seconds, temp, sb);
        } else {
            temp = (int) (seconds % 3600 / 60);
            changeSeconds(seconds, temp, sb);
        }
        return sb.toString();
    }

    private static void changeSeconds(long seconds, int temp, StringBuffer sb) {
        sb.append((temp < 10) ? "0" + temp + ":" : "" + temp + ":");
        temp = (int) (seconds % 3600 % 60);
        sb.append((temp < 10) ? "0" + temp : "" + temp);
    }

    public static float time2sb(int current, int duration) {
        return current / (duration * 1.0f);
    }

    public static NET_DVR_TIME plusSeconds(@NonNull TimeStruct timeStruct, int seconds) {
//        int current = timeStruct.toSeconds() + seconds;
//        Date date = new Date(current * 1000);
        NET_DVR_TIME net_dvr_time = new NET_DVR_TIME();
//        net_dvr_time.dwYear = date.getYear();
//        net_dvr_time.dwMonth = date.getMonth();
//        net_dvr_time.dwDay = date.getDay();
//        net_dvr_time.dwHour = date.getHours();
//        net_dvr_time.dwMinute = date.getMinutes();
//        net_dvr_time.dwSecond = date.getSeconds();
        net_dvr_time.dwYear = 2019;
        net_dvr_time.dwMonth = 7;
        net_dvr_time.dwDay = 1;
        net_dvr_time.dwHour = 9;
        net_dvr_time.dwMinute = 0;
        net_dvr_time.dwSecond = 0;
        return net_dvr_time;
    }
}
