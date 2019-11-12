package com.hitqz.robot.watchtower.net;

import com.hitqz.robot.watchtower.net.base.BaseRespond;
import com.hitqz.robot.watchtower.net.bean.AlarmLevelSettingEntity;
import com.hitqz.robot.watchtower.net.bean.MonitorEntity;
import com.hitqz.robot.watchtower.net.bean.RegionTemperatureList;
import com.sonicers.commonlib.net.DataBean;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ISkyNet {

    /**
     * 1.1.1.	设置报警温度规则
     */
    @POST("/set/alarmLevel")
    Observable<BaseRespond<DataBean>> setAlarmLevelConfig(@Body List<AlarmLevelSettingEntity> entity);

    /**
     * 1.1.2.	获取报警温度规则
     */
    @GET("/set/alarmLevel")
    Observable<BaseRespond<List<AlarmLevelSettingEntity>>> getAlarmLevelConfig();

    /**
     * 1.1.3.	开启监控模式
     */
    @POST("/monitor/start")
    Observable<BaseRespond<DataBean>> startMonitor(@Body MonitorEntity entity);

    /**
     * 1.1.4.	停止监控
     */
    @GET("/monitor/stop")
    Observable<BaseRespond<DataBean>> stopMonitor();

    /**
     * 1.1.5.	当前是否处于监控模式
     */
    @GET("/monitor/now")
    Observable<BaseRespond<MonitorEntity>> isMonitoring();

    /**
     * 1.1.6.	获取当前报警等级
     */
    @GET("/monitor/alarm/level")
    Observable<BaseRespond<Integer>> getAlarmLevel();

    /**
     * 1.1.7.	重置报警等级
     */
    @GET("/monitor/alarm/reset")
    Observable<BaseRespond<DataBean>> resetAlarmLevel();

    /**
     * 1.1.8.	查询手环天线工作状态
     *
     * @return false:状态异常
     * true:状态正常
     */
    @GET("/system/status/ring")
    Observable<BaseRespond<Boolean>> getRingState();

    /**
     * 1.1.9.	查询底盘通讯串口工作状态
     *
     * @return false:状态异常
     * true:状态正常
     */
    @GET("/system/status/baseplate")
    Observable<BaseRespond<Boolean>> getBaseplateState();

    /**
     * 1.1.10.	查询云台通讯串口工作状态
     *
     * @return false:状态异常
     * true:状态正常
     */
    @GET("/system/status/cameraPlatform")
    Observable<BaseRespond<Boolean>> getCameraPlatformState();

    /**
     * 1.1.12.	查询底盘急停按钮状态
     *
     * @return false:松开
     * true:按下
     */
    @GET("/baseplate/emergencyStop")
    Observable<BaseRespond<Boolean>> getEmergencyStopState();

    /**
     * 声光状态
     *
     * @return false:掉线
     * true:正常
     */
    @GET("system/status/lightAndSound")
    Observable<BaseRespond<Boolean>> getlightAndSoundState();

    /**
     * 1.1.13.	查询底盘电量
     *
     * @return 0~100
     */
    @GET("/baseplate/electric")
    Observable<BaseRespond<Integer>> getBaseplateElectric();

    /**
     * 云台电量
     *
     * @return 0~100
     */
    @GET("/lightSound/electric")
    Observable<BaseRespond<Integer>> getLightSoundElectric();

    /**
     * 1.1.14.	设置底盘方向
     * 0：停止
     * 1：前进
     * 2：左转
     * 3：后退
     * 4：右转
     */
    @GET("/baseplate/direction/{direction}")
    Observable<BaseRespond<DataBean>> setBaseplateDirection(@Path("direction") int direction);

    /**
     * 云台旋转
     * 0：停止
     * <p>
     * 1：上
     * 2：左
     * 3：下
     * 4：右
     */
    @GET("/cameraPlatform/direction/{direction}")
    Observable<BaseRespond<DataBean>> setCameraPlatformDirection(@Path("direction") int direction);

    /**
     * 区域温度
     *
     * @return
     */
    @GET("/monitor/region/temperature")
    Observable<BaseRespond<RegionTemperatureList>> regionTemperature();

    /**
     * 开关灯
     * 0,关 ，1 开
     */
    @POST("/cameraPlatform/light/{action}")
    Observable<BaseRespond<DataBean>> cameraPlatformLight(@Path("action") int action);

    /**
     * 灯光状态
     *
     * @return false:掉线
     * true:正常
     */
    @GET("/cameraPlatform/light")
    Observable<BaseRespond<Boolean>> getCameraPlatformLight();

    /**
     * 关闭工控机
     *
     * @return false:失败
     * true:成功
     */
    @GET("/system/shutdown")
    Observable<BaseRespond<Boolean>> shutdown();
}
