package com.hitqz.robot.watchtower.bean;

import java.util.Date;

public class DonghuoRecord {

    public int year;
    public int month;
    public int day;
    public int hour;
    public int min;
    public String name;

    private DonghuoRecord(Builder builder) {
        year = builder.year;
        month = builder.month;
        day = builder.day;
        hour = builder.hour;
        min = builder.min;
        name = builder.name;
    }


    public static final class Builder {
        private int year;
        private int month;
        private int day;
        private int hour;
        private int min;
        private String name;

        public Builder() {
        }

        public Builder year(int val) {
            year = val;
            return this;
        }

        public Builder month(int val) {
            month = val;
            return this;
        }

        public Builder day(int val) {
            day = val;
            return this;
        }

        public Builder hour(int val) {
            hour = val;
            return this;
        }

        public Builder min(int val) {
            min = val;
            return this;
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public DonghuoRecord build() {
            return new DonghuoRecord(this);
        }
    }

    @Override
    public String toString() {
        return "DonghuoRecord{" +
                "year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", hour=" + hour +
                ", min=" + min +
                ", name='" + name + '\'' +
                '}';
    }

    public static String commonBuild() {
        Date date = new Date();
        Builder builder = new Builder();
        builder.year = date.getYear();
        builder.month = date.getMonth();
        builder.day = date.getDay();
        builder.hour = date.getHours();
        builder.min = date.getMinutes();
        DonghuoRecord donghuoRecord = builder.build();
        return donghuoRecord.toString();
    }
}
