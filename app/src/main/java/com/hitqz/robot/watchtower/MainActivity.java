package com.hitqz.robot.watchtower;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.blankj.utilcode.util.NetworkUtils;
import com.hitqz.robot.commonlib.util.CrashUtil;
import com.hitqz.robot.commonlib.util.FullScreenUtil;
import com.hitqz.robot.commonlib.util.ToastUtils;
import com.hitqz.robot.watchtower.camera.CameraActivity;
import com.hitqz.robot.watchtower.gallery.GalleryActivity;
import com.hitqz.robot.watchtower.widget.DhpDialog;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.CsvFormatStrategy;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;


import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.hitqz.robot.watchtower.constant.PermissionConstant.STORAGE_PERMISSION;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        askForPermissionTask();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            FullScreenUtil.initFullScreen(this);
        }
        super.onWindowFocusChanged(hasFocus);
    }

    public void go2Camera(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    public void go2Gallery(View view) {
        GalleryActivity.go2Gallery(this);
    }

    public void go2Setting(View view) {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private static final int RC_STORAGE_PERM = 123;

    @AfterPermissionGranted(RC_STORAGE_PERM)
    public void askForPermissionTask() {
        if (!hasStoragePermission()) {
            // Ask for one permission
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.need_permission),
                    RC_STORAGE_PERM,
                    STORAGE_PERMISSION);
        } else {
            FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                    .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
                    .methodCount(0)         // (Optional) How many method line to show. Default 2
                    .methodOffset(7)        // (Optional) Hides internal method calls up to offset. Default 5
                    .tag("WatchTowerApp")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                    .build();
            Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));

            FormatStrategy csvFormatStrategy = CsvFormatStrategy.newBuilder()      // (Optional) Hides internal method calls up to offset. Default 5
                    .tag("WatchTowerApp")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                    .build();
            Logger.addLogAdapter(new DiskLogAdapter(csvFormatStrategy));

            CrashUtil.getInstance().init(this, getResources().getString(R.string.app_name));

            DhpDialog.showDhpDialog(this, "请注意核对动火票");
            if (!NetworkUtils.isConnected()) {
                ToastUtils.showToastShort(this, "网络未连接");
                Logger.t(TAG).e("网络未连接");
            } else {
                // 高清摄像头初始化
                HCSdkManager normalSdkManager = HCSdkManager.getNormalHCSdkManager(this);
                boolean initResult = normalSdkManager.init();
                if (initResult) {
                    boolean result = normalSdkManager.login();
                    if (!result) {
                        ToastUtils.showToastShort(this, "高清摄像头登录失败");
                    }
                } else {
                    ToastUtils.showToastShort(this, "高清摄像头初始化失败");
                }

                // 热成像摄像头初始化
                HCSdkManager hotSdkManager = HCSdkManager.getHotHCSdkManager(this);
                boolean ir = hotSdkManager.init();
                if (ir) {
                    boolean result = hotSdkManager.login();
                    if (!result) {
                        ToastUtils.showToastShort(this, "热成像摄像头登录失败");
                    }
                } else {
                    ToastUtils.showToastShort(this, "热成像摄像头初始化失败");
                }
            }
        }
    }

    private boolean hasStoragePermission() {
        return EasyPermissions.hasPermissions(this, STORAGE_PERMISSION);
    }
}
