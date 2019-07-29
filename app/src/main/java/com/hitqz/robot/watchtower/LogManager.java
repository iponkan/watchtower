package com.hitqz.robot.watchtower;

import android.content.Context;

import com.hitqz.robot.commonlib.CommonDiskLogStrategy;
import com.hitqz.robot.commonlib.util.CrashUtil;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.CsvFormatStrategy;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

public class LogManager {

    private static LogManager singleton;
    private boolean init;

    private LogManager() {

    }

    public static LogManager getInstance() {
        if (singleton == null) {
            synchronized (LogManager.class) {
                if (singleton == null) {
                    singleton = new LogManager();
                }
            }
        }
        return singleton;
    }

    public void init(Context context) {
        if (init) {
            return;
        }
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
                .methodCount(0)         // (Optional) How many method line to show. Default 2
                .methodOffset(7)        // (Optional) Hides internal method calls up to offset. Default 5
                .tag("WatchTower")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));

        FormatStrategy csvFormatStrategy = CsvFormatStrategy.newBuilder()      // (Optional) Hides internal method calls up to offset. Default 5
                .tag("WatchTowerApp")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .logStrategy(CommonDiskLogStrategy.getInstance())
                .build();
        Logger.addLogAdapter(new DiskLogAdapter(csvFormatStrategy));

        CrashUtil.getInstance().init(context.getApplicationContext(), "WatchTower");
        init = true;
    }
}
