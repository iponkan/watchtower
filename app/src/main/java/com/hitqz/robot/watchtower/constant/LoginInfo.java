package com.hitqz.robot.watchtower.constant;

public class LoginInfo {

    public static final String HOT_IP = "192.168.8.84";
    public static final String HOT_PORT = "8000";
    public static final String HOT_USER = "admin";
    public static final String HOT_PSD = "abcd1234";

    public static final String NORMAL_IP = "192.168.8.101";
    public static final String NORMAL_PORT = "8000";
    public static final String NORMAL_USER = "admin";
    public static final String NORMAL_PSD = "hit123456";

    public String m_oIPAddr;
    public String m_oPort;
    public String m_oUser;
    public String m_oPsd;
    public String name;

    private LoginInfo(String ip, String port, String user, String psd, String n) {
        this.m_oIPAddr = ip;
        this.m_oPort = port;
        this.m_oUser = user;
        this.m_oPsd = psd;
        this.name = n;
    }

    private static LoginInfo normalLoginInfo = new LoginInfo(NORMAL_IP, NORMAL_PORT, NORMAL_USER, NORMAL_PSD, "高清摄像头");
    private static LoginInfo hotLoginInfo = new LoginInfo(HOT_IP, HOT_PORT, HOT_USER, HOT_PSD, "热成像摄像头");


    public static LoginInfo getNormalLogInfo() {
        return normalLoginInfo;
    }

    public static LoginInfo getHotLogInfo() {
        return hotLoginInfo;
    }
}
