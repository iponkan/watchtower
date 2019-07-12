package com.hitqz.robot.watchtower.hcnetw;

import com.hikvision.netsdk.NET_DVR_CONFIG;

public class NET_DVR_REALTIME_THERMOMETRY_COND extends NET_DVR_CONFIG {
    public long dwChan;
    public byte byRuleID;
    public byte byMode;
    public byte[] byRes = new byte[62];

}
