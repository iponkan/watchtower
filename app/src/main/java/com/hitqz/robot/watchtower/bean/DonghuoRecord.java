package com.hitqz.robot.watchtower.bean;

import androidx.annotation.NonNull;

import com.hikvision.netsdk.NET_DVR_TIME;

/**
 * 记录一次动火作业的时间
 */
public class DonghuoRecord {

    /**
     * 动火操作开始时间
     */
    public NET_DVR_TIME struStartTime = new NET_DVR_TIME();
    /**
     * 动火操作结束时间
     */
    public NET_DVR_TIME struStopTime = new NET_DVR_TIME();
    public String name;

    @NonNull
    @Override
    public String toString() {
        return "动火记录:" + struStartTime.ToString() + "～" + struStopTime.ToString();
    }
}
