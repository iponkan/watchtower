package com.hitqz.robot.watchtower;


import com.hitqz.robot.commonlib.BaseApplication;
import com.hitqz.robot.commonlib.util.CrashUtil;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

public class WatchTowerApp extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(true)  // (Optional) Whether to show thread info or not. Default true
                .methodCount(0)         // (Optional) How many method line to show. Default 2
                .methodOffset(7)        // (Optional) Hides internal method calls up to offset. Default 5
                .tag("WatchTowerApp")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));

        CrashUtil.getInstance().init(this, getResources().getString(R.string.app_name));

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
