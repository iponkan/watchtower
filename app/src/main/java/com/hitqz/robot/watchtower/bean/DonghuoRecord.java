package com.hitqz.robot.watchtower.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

/**
 * 记录一次动火作业的时间
 */
public class DonghuoRecord implements Parcelable {

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
    /**
     * 动火操作开始时间
     */
    public TimeStruct struStartTime = new TimeStruct();
    public long start;
    /**
     * 动火操作结束时间
     */
    public TimeStruct struStopTime = new TimeStruct();
    public long stop;

    public String name;

    public DonghuoRecord() {
    }

    protected DonghuoRecord(Parcel in) {
        this.struStartTime = in.readParcelable(TimeStruct.class.getClassLoader());
        this.struStopTime = in.readParcelable(TimeStruct.class.getClassLoader());
        this.name = in.readString();
    }

    @NonNull
    @Override
    public String toString() {
        return struStartTime.toString() + "～" + struStopTime.toString();
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

    public String toXml() {
        if (stop == 0) {
            return start + "～" + TimeStruct.farFeature().toMillSeconds();
        } else {
            return start + "～" + TimeStruct.farFeature().toMillSeconds();
        }
    }

    public static DonghuoRecord fromXml(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        String[] strings = str.split("～");
        DonghuoRecord donghuoRecord = new DonghuoRecord();
        donghuoRecord.start = Long.parseLong(strings[0]);
        donghuoRecord.stop = Long.parseLong(strings[1]);
        donghuoRecord.struStartTime = TimeStruct.fromMillSeconds(donghuoRecord.start);
        donghuoRecord.struStopTime = TimeStruct.fromMillSeconds(donghuoRecord.stop);
        return donghuoRecord;
    }

    public static String newDonghuoRecordXml() {
        return System.currentTimeMillis() + "～" + TimeStruct.farFeature().toMillSeconds();
    }
}
