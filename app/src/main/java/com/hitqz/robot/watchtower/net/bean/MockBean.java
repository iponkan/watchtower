package com.hitqz.robot.watchtower.net.bean;

/**
 * 网络请求结果 基类,剥离出数据给上层
 */
public class MockBean {

    /**
     * code : 200
     * status : SUCCESS
     * msg :
     * data : [{"alarmLevel":1,"alarmTemperature":75},{"alarmLevel":2,"alarmTemperature":100},{"alarmLevel":3,"alarmTemperature":150}]
     */

    private int code;
    private String status;
    private String msg;
    private RegionTemperatureList data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public RegionTemperatureList getData() {
        return data;
    }

    public void setData(RegionTemperatureList data) {
        this.data = data;
    }
}
