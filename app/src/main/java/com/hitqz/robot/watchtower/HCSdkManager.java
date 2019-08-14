package com.hitqz.robot.watchtower;

import android.content.Context;

import com.hitqz.robot.watchtower.constant.LoginInfo;
import com.sonicers.commonlib.util.ToastUtil;

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

    private static HCSdk getHCSdk(Context context, LoginInfo loginInfo) {
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
        HCSdk normalHCSdk = getNormalHCSdk(context);
        boolean initResult = normalHCSdk.init();
        if (initResult) {
            boolean result = normalHCSdk.login();
            if (!result) {
                ToastUtil.showToastShort(context, "高清摄像头登录失败");
            }
        } else {
            ToastUtil.showToastShort(context, "高清摄像头初始化失败");
        }

        // 热成像摄像头初始化
        HCSdk hotHCSdk = getHotHCSdk(context);
        boolean ir = hotHCSdk.init();
        if (ir) {
            boolean result = hotHCSdk.login();
            if (!result) {
                ToastUtil.showToastShort(context, "热成像摄像头登录失败");
            }
        } else {
            ToastUtil.showToastShort(context, "热成像摄像头初始化失败");
        }

        if (normalHCSdk.isLogin() && hotHCSdk.isLogin()) {
            init = true;
        }

    }
}
