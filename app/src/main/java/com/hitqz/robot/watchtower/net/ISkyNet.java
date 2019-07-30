package com.hitqz.robot.watchtower.net;


import com.hitqz.robot.commonlib.net.BaseRespond;
import com.hitqz.robot.commonlib.net.DataBean;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ISkyNet {

    @GET("/set/alarmLevel")
    Observable<BaseRespond<List<AlarmLevelSettingEntity>>> getAlarmLevelConfig();

    @POST("/set/alarmLevel")
    Observable<BaseRespond<DataBean>> setAlarmLevelConfig(@Body List<AlarmLevelSettingEntity> entity);

    @POST("/monitor/start")
    Observable<BaseRespond<DataBean>> startMonitor(@Body MonitorEntity entity);

    @GET("/monitor/stop")
    Observable<BaseRespond<DataBean>> stopMonitor();

    @GET("/monitor/now")
    Observable<BaseRespond<MonitorEntity>> isMonitoring();

    @GET("/monitor/alarm/level")
    Observable<BaseRespond<Integer>> getAlarmLevel();

    @GET("/monitor/alarm/reset")
    Observable<BaseRespond<DataBean>> resetAlarmLevel();

}
