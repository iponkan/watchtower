package com.hitqz.robot.watchtower;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.blankj.utilcode.util.NetworkUtils;
import com.hitqz.robot.commonlib.util.FullScreenUtil;
import com.hitqz.robot.commonlib.util.ToastUtils;
import com.hitqz.robot.watchtower.camera.CameraActivity;
import com.hitqz.robot.watchtower.gallery.GalleryActivity;
import com.hitqz.robot.watchtower.widget.DhpDialog;
import com.orhanobut.logger.Logger;


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
            LogManager.getInstance().init(this);
            DhpDialog.showDhpDialog(this, "请注意核对动火票");
            if (!NetworkUtils.isConnected()) {
                ToastUtils.showToastShort(this, "网络未连接");
                Logger.t(TAG).e("网络未连接");
            } else {
                HCSdkManager.getInstance().initAndLogin(this);
            }
        }
    }

    private boolean hasStoragePermission() {
        return EasyPermissions.hasPermissions(this, STORAGE_PERMISSION);
    }
}
