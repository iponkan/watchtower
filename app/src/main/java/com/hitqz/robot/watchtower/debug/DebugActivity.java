package com.hitqz.robot.watchtower.debug;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SPUtils;
import com.hitqz.robot.watchtower.R;
import com.hitqz.robot.watchtower.constant.Constants;
import com.hitqz.robot.watchtower.net.ISkyNet;
import com.hitqz.robot.watchtower.net.RetrofitManager;
import com.hitqz.robot.watchtower.net.base.BaseObserver;
import com.hitqz.robot.watchtower.util.PathUtil;
import com.sonicers.commonlib.component.BaseActivity;
import com.sonicers.commonlib.rx.RxSchedulers;
import com.sonicers.commonlib.util.ToastUtil;
import com.sonicers.commonlib.util.UploadUtil;
import com.sonicers.commonlib.util.ZipUtils;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * debug面板
 */
public class DebugActivity extends BaseActivity {

    public static final String TAG = "DebugActivity";
    @BindView(R.id.btn_upload)
    Button btnUpload;
    @BindView(R.id.btn_show_temperature)
    Button btnTemperature;
    @BindView(R.id.btn_debug)
    Button btnDebug;
    @BindView(R.id.btn_light_switch)
    Button btnLightSwitch;
    @BindView(R.id.btn_multi_box)
    Button btnMultiBox;
    @BindView(R.id.btn_shutdown)
    Button btnShutDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        ButterKnife.bind(this);
        boolean showTemperature = SPUtils.getInstance(Constants.SP_FILE_NAME).getBoolean(Constants.SHOWTEMPERATURE, false);
        btnTemperature.setText(showTemperature ? "不显示温度" : "显示温度");

        boolean debug = SPUtils.getInstance(Constants.SP_FILE_NAME).getBoolean(Constants.DEBUG, false);
        btnDebug.setText(debug ? "正常模式" : "Debug模式");

        boolean aBoolean = SPUtils.getInstance(Constants.SP_FILE_NAME).getBoolean(Constants.LIGHT_SWITCH, false);
        btnLightSwitch.setText(aBoolean ? "不显示灯开关" : "显示灯开关");

        boolean boxBoolean = SPUtils.getInstance(Constants.SP_FILE_NAME).getBoolean(Constants.BOX_SWITCH, false);
        btnMultiBox.setText(boxBoolean ? "不支持多选框" : "支持多选框");

        skyNet = RetrofitManager.getInstance().create(ISkyNet.class);
    }

    public static final String URL = "http://47.92.118.121:4000/files/uploads?folder=WatchTower";

    private void upload(String folser) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                String tempZipPath = PathUtil.getZipPath();
                ZipUtils.zip(folser, tempZipPath);
                if (FileUtils.isFileExists(tempZipPath)) {
                    int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
                    int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                    String filename = month + "-" + day + ".zip";
                    String result = UploadUtil.upload(URL, tempZipPath, filename).toString();
                    Log.d(TAG, "result:" + result);
                }
            }
        }).compose(RxSchedulers.io_main()).subscribe(new Observer<Integer>() { // 第三步：订阅

            // 第二步：初始化Observer
            private int i;
            private Disposable mDisposable;

            @Override
            public void onSubscribe(@NonNull Disposable d) {
                mDisposable = d;
            }

            @Override
            public void onNext(@NonNull Integer integer) {
                Log.e(TAG, "onNext : value : ");
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.e(TAG, "onError : value : " + e.getMessage() + "\n");
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete" + "\n");
            }
        });
    }

    public static void go2Debug(Activity activity) {
        Intent intent = new Intent(activity, DebugActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    @OnClick(R.id.btn_upload)
    public void onBtnUploadClicked() {
        String folser = PathUtil.getLogFolderPath();
        upload(folser);
    }

    @OnClick(R.id.btn_show_temperature)
    public void onShowTemperatureClicked() {
        boolean now = SPUtils.getInstance(Constants.SP_FILE_NAME).getBoolean(Constants.SHOWTEMPERATURE, false);
        SPUtils.getInstance(Constants.SP_FILE_NAME).put(Constants.SHOWTEMPERATURE, !now);
        btnTemperature.setText(!now ? "不显示温度" : "显示温度");
    }

    @OnClick(R.id.btn_debug)
    public void onViewClicked() {
        boolean now = SPUtils.getInstance(Constants.SP_FILE_NAME).getBoolean(Constants.DEBUG, false);
        SPUtils.getInstance(Constants.SP_FILE_NAME).put(Constants.DEBUG, !now);
        btnDebug.setText(!now ? "正常模式" : "Debug模式");
    }

    @OnClick(R.id.btn_light_switch)
    public void onLightSwitchClick() {
        boolean now = SPUtils.getInstance(Constants.SP_FILE_NAME).getBoolean(Constants.LIGHT_SWITCH, false);
        SPUtils.getInstance(Constants.SP_FILE_NAME).put(Constants.LIGHT_SWITCH, !now);
        btnLightSwitch.setText(!now ? "不显示灯开关" : "显示灯开关");
    }

    @OnClick(R.id.btn_multi_box)
    public void onBoxSwitchClick() {
        boolean now = SPUtils.getInstance(Constants.SP_FILE_NAME).getBoolean(Constants.BOX_SWITCH, false);
        SPUtils.getInstance(Constants.SP_FILE_NAME).put(Constants.BOX_SWITCH, !now);
        btnMultiBox.setText(!now ? "不支持多选框" : "支持多选框");
    }

    private ISkyNet skyNet;

    @SuppressLint("CheckResult")
    @OnClick(R.id.btn_shutdown)
    public void onShutDownClick() {
        skyNet.shutdown()
                .compose(RxSchedulers.io_main())
                .subscribeWith(new BaseObserver<Boolean>(loadingDialog) {
                    @Override
                    public void onSuccess(Boolean model) {
                        ToastUtil.showToastShort(DebugActivity.this, "关机 success:");
                    }

                    @Override
                    public void onFailure(String msg) {
                        ToastUtil.showToastShort(DebugActivity.this, "关机 fail：");
                    }
                });
    }
}
