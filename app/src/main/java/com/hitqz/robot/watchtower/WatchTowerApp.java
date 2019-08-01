package com.hitqz.robot.watchtower;


import com.sonicers.commonlib.BaseApplication;

public class WatchTowerApp extends BaseApplication {

    private static BaseApplication sInstance;

    public static BaseApplication getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("Application has not been created");
        }
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (sInstance == null) {
            sInstance = this;
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
