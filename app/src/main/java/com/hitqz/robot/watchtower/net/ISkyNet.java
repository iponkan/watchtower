package com.hitqz.robot.watchtower.net;


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

    @GET("/monitor/end")
    Observable<BaseRespond<DataBean>> stopMonitor();

    @GET("/monitor/now")
    Observable<BaseRespond<DataBean>> isMonitoring();

    @GET("/alarm/level")
    Observable<BaseRespond<DataBean>> getAlarmLevel();

    @GET(" /alarm/reset")
    Observable<BaseRespond<DataBean>> resetAlarmLevel();

}
