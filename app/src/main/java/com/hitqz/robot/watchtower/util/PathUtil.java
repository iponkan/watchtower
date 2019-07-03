package com.hitqz.robot.watchtower.util;

import com.blankj.utilcode.util.PathUtils;

import java.io.File;

public class PathUtil {
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

}
