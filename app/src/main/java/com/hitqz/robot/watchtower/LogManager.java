package com.hitqz.robot.watchtower;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hitqz.robot.watchtower.util.PathUtil;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.CsvFormatStrategy;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.LogcatLogStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.sonicers.commonlib.log.CommonDiskLogStrategy;
import com.sonicers.commonlib.util.CrashUtil;

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

    public void init(Context context, String appName) {
        if (init) {
            return;
        }
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
                .methodCount(0)         // (Optional) How many method line to show. Default 2
                .methodOffset(7)        // (Optional) Hides internal method calls up to offset. Default 5
                .tag(appName)   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .logStrategy(new LogcatLogStrategy() {
                    @Override
                    public void log(int priority, @Nullable String tag, @NonNull String message) {
                        if (priority > 2) {
                            super.log(priority, tag, message);
                        }
                    }
                })
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));

        FormatStrategy csvFormatStrategy = CsvFormatStrategy.newBuilder()      // (Optional) Hides internal method calls up to offset. Default 5
                .tag(appName)   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .logStrategy(CommonDiskLogStrategy.getInstance(PathUtil.getLogFolderPath()))
                .build();
        Logger.addLogAdapter(new DiskLogAdapter(csvFormatStrategy));

        CrashUtil.getInstance().init(context.getApplicationContext(), appName);
        init = true;
    }
}
