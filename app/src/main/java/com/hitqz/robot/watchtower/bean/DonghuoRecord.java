package com.hitqz.robot.watchtower.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

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
}
