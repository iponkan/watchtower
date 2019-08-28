package com.hitqz.robot.watchtower.net.bean;

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

    public static AlarmLevelSettingEntity getDefaultLevel1() {
        return new AlarmLevelSettingEntity(1, 100);
    }

    public static AlarmLevelSettingEntity getDefaultLevel2() {
        return new AlarmLevelSettingEntity(2, 200);
    }

    public static AlarmLevelSettingEntity getDefaultLevel3() {
        return new AlarmLevelSettingEntity(3, 300);
    }
}
