package com.hitqz.robot.watchtower.hcnetw;

import com.hikvision.netsdk.NET_DVR_CONFIG;

public class NET_DVR_THERMOMETRY_BASICPARAM extends NET_DVR_CONFIG {

    public static final int NET_DVR_GET_THERMOMETRY_BASICPARAM = 3621;

    public byte byEnabled;
    public byte byStreamOverlay;
    public byte byPictureOverlay;
    public byte byThermometryRange;
    public byte byThermometryUnit;
    public byte byThermometryCurve;
    public byte byFireImageModea;
    public byte byShowTempStripEnable;
    public float fEmissivity;
    public byte byDistanceUnit;
    public byte byEnviroHumidity;
    public byte[] byRes2 = new byte[2];
    public NET_DVR_TEMPERATURE_COLOR struTempColor;
    public int iEnviroTemperature;
    public int iCorrectionVolume;
    public byte bySpecialPointThermType;
    public byte byReflectiveEnabled;
    public int wDistance;
    public float fReflectiveTemperature;
    public float fAlert;
    public float fAlarm;
    public float fThermalOpticalTransmittance;
    public float fExternalOpticsWindowCorrection;
    public byte byDisplayMaxTemperatureEnabled;
    public byte byDisplayMinTemperatureEnabled;
    public byte byDisplayAverageTemperatureEnabled;
    public byte byThermometryInfoDisplayposition;
    public long dwAlertFilteringTime;
    public long dwAlarmFilteringTime;
    public byte[] byRes = new byte[52];


    public NET_DVR_THERMOMETRY_BASICPARAM() {
    }
}