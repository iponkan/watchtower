package com.hitqz.robot.watchtower;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.Nullable;

import com.hitqz.robot.watchtower.constant.LoginInfo;
import com.sonicers.commonlib.rx.RxSchedulers;
import com.sonicers.commonlib.util.ToastUtil;

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

@SuppressLint("CheckResult")

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

    private volatile boolean init;

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

    public void initAndLogin(Context context, @Nullable Callback callback) {

        if (init) {
            if (callback != null) {
                callback.onComplete();
            }
            return;
        }

        HCSdk normalHCSdk = getNormalHCSdk(context);
        HCSdk hotHCSdk = getHotHCSdk(context);
        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            // 高清摄像头初始化
            boolean initResult = normalHCSdk.init();
            if (initResult) {
                initResult = normalHCSdk.login();
                if (!initResult) {
                    emitter.onNext("高清摄像头登录失败");
                }
            } else {
                emitter.onNext("高清摄像头初始化失败");
            }

            // 热成像摄像头初始化
            boolean ir = hotHCSdk.init();
            if (ir) {
                ir = hotHCSdk.login();
                if (!ir) {
                    emitter.onNext("热成像摄像头登录失败");
                }
            } else {
                emitter.onNext("热成像摄像头初始化失败");
            }
            emitter.onComplete();
        }).compose(RxSchedulers.io_main())
                .subscribeWith(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String o) {
                        ToastUtil.showToastShort(context, o);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        if (normalHCSdk.isLogin() && hotHCSdk.isLogin()) {
                            init = true;
                        }
                        if (callback != null) {
                            callback.onComplete();
                        }
                    }
                });
    }

    public interface Callback {
        void onComplete();
    }
}
