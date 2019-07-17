package com.hitqz.robot.watchtower.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

/**
 * 记录一次动火作业的时间
 */
public class DonghuoRecord implements Parcelable {

    /**
     * 动火操作开始时间
     */
    public TimeStruct struStartTime = new TimeStruct();
    /**
     * 动火操作结束时间
     */
    public TimeStruct struStopTime = new TimeStruct();
    public String name;

    @NonNull
    @Override
    public String toString() {
        return "动火记录:" + struStartTime.toString() + "～" + struStopTime.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.struStartTime, flags);
        dest.writeParcelable(this.struStopTime, flags);
        dest.writeString(this.name);
    }

    public DonghuoRecord() {
    }

    protected DonghuoRecord(Parcel in) {
        this.struStartTime = in.readParcelable(TimeStruct.class.getClassLoader());
        this.struStopTime = in.readParcelable(TimeStruct.class.getClassLoader());
        this.name = in.readString();
    }

    public static final Parcelable.Creator<DonghuoRecord> CREATOR = new Parcelable.Creator<DonghuoRecord>() {
        @Override
        public DonghuoRecord createFromParcel(Parcel source) {
            return new DonghuoRecord(source);
        }

        @Override
        public DonghuoRecord[] newArray(int size) {
            return new DonghuoRecord[size];
        }
    };

    public static DonghuoRecord getHH() {
        DonghuoRecord donghuoRecord = new DonghuoRecord();
        TimeStruct start = new TimeStruct();
        start.dwYear = 2019;
        start.dwMonth = 7;
        start.dwDay = 2;
        start.dwHour = 0;
        start.dwMinute = 0;
        start.dwSecond = 0;

        TimeStruct stop = new TimeStruct();
        stop.dwYear = 2019;
        stop.dwMonth = 7;
        stop.dwDay = 3;
        stop.dwHour = 0;
        stop.dwMinute = 0;
        stop.dwSecond = 0;

        donghuoRecord.struStartTime = start;
        donghuoRecord.struStopTime = stop;
        return donghuoRecord;
    }


}
