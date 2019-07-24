package com.hitqz.robot.watchtower.net;

public class AlarmLevelSettingEntity {

    /**
     * alarmLevel : 1
     * alarmTemperature : 75
     */

    public AlarmLevelSettingEntity(int alarmLevel, int alarmTemperature) {
        this.alarmLevel = alarmLevel;
        this.alarmTemperature = alarmTemperature;
    }

    private int alarmLevel;
    private int alarmTemperature;

    public int getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(int alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    public int getAlarmTemperature() {
        return alarmTemperature;
    }

    public void setAlarmTemperature(int alarmTemperature) {
        this.alarmTemperature = alarmTemperature;
    }
}
