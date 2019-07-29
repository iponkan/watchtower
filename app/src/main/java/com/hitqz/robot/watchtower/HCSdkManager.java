package com.hitqz.robot.watchtower;

import android.content.Context;

import com.hitqz.robot.commonlib.util.ToastUtils;
import com.hitqz.robot.watchtower.constant.LoginInfo;

import java.util.HashMap;

public class HCSdkManager {

    private static HCSdkManager singleton;

    public static HCSdkManager getInstance() {
        if (singleton == null) {
            synchronized (HCSdkManager.class) {
                if (singleton == null) {
                    singleton = new HCSdkManager();
                }
            }
        }
        return singleton;
    }

    private boolean init;

    private static HashMap<LoginInfo, HCSdk> hcSdks = new HashMap<>();

    public static HCSdk getHCSdk(Context context, LoginInfo loginInfo) {
        HCSdk hcSdk = hcSdks.get(loginInfo);
        if (hcSdk == null) {
            hcSdk = HCSdk.getInstance(context, loginInfo);
            hcSdks.put(loginInfo, hcSdk);
        }
        return hcSdk;
    }

    public static HCSdk getNormalHCSdk(Context context) {
        return getHCSdk(context, LoginInfo.getNormalLogInfo());
    }

    public static HCSdk getHotHCSdk(Context context) {
        return getHCSdk(context, LoginInfo.getHotLogInfo());
    }


    public void initAndLogin(Context context) {

        if (init) {
            return;
        }
        // 高清摄像头初始化
        HCSdk normalSdkManager = getNormalHCSdk(context);
        boolean initResult = normalSdkManager.init();
        if (initResult) {
            boolean result = normalSdkManager.login();
            if (!result) {
                ToastUtils.showToastShort(context, "高清摄像头登录失败");
                return;
            }
        } else {
            ToastUtils.showToastShort(context, "高清摄像头初始化失败");
            return;
        }

        // 热成像摄像头初始化
        HCSdk hotSdkManager = getHotHCSdk(context);
        boolean ir = hotSdkManager.init();
        if (ir) {
            boolean result = hotSdkManager.login();
            if (!result) {
                ToastUtils.showToastShort(context, "热成像摄像头登录失败");
                return;
            }
        } else {
            ToastUtils.showToastShort(context, "热成像摄像头初始化失败");
            return;
        }

        init = true;
    }
}
