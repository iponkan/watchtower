package com.hitqz.robot.watchtower.util;

import android.os.Environment;

import com.blankj.utilcode.util.PathUtils;
import com.hitqz.robot.watchtower.R;
import com.hitqz.robot.watchtower.WatchTowerApp;

import java.io.File;

public class PathUtil {

    private static final String APPNAME = WatchTowerApp.getInstance().getString(R.string.app_name);

    public static String getSdkLogPath() {
        String path = PathUtils.getExternalStoragePath() + "/sdklog/";
        createFolder(path);
        return path;
    }

    public static boolean createFolder(String strFolder) {
        File file = new File(strFolder);
        if (!file.exists()) {
            if (file.mkdirs()) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    public static String getLogFolderPath() {
        String diskPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String folder = diskPath + File.separatorChar + APPNAME;
        return folder;
    }

    public static String getZipPath() {
        String diskPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String folder = diskPath + File.separatorChar + "temp.zip";
        return folder;
    }
}
