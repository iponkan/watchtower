package com.hitqz.robot.watchtower;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.blankj.utilcode.util.NetworkUtils;
import com.hitqz.robot.commonlib.util.FullScreenUtil;
import com.hitqz.robot.commonlib.util.ToastUtils;
import com.hitqz.robot.watchtower.camera.CameraActivity;
import com.hitqz.robot.watchtower.gallery.GalleryActivity;
import com.hitqz.robot.watchtower.setting.SettingActivity;
import com.hitqz.robot.watchtower.widget.AlertDialogFragment;
import com.orhanobut.logger.Logger;


import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.hitqz.robot.watchtower.constant.PermissionConstant.STORAGE_PERMISSION;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private static final int RC_STORAGE_PERM = 123;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullScreenUtil.initFullScreen(this);
        setContentView(R.layout.activity_main);
        askForPermissionTask();
    }

    public void go2Camera(View view) {
        CameraActivity.go2Camera(this);
    }

    public void go2Gallery(View view) {
        GalleryActivity.go2Gallery(this);
    }

    public void go2Setting(View view) {
        SettingActivity.go2Setting(this);
    }

    @AfterPermissionGranted(RC_STORAGE_PERM)
    public void askForPermissionTask() {
        if (!EasyPermissions.hasPermissions(this, STORAGE_PERMISSION)) {
            // Ask for one permission
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.need_permission),
                    RC_STORAGE_PERM,
                    STORAGE_PERMISSION);
        } else {
            LogManager.getInstance().init(this);
            AlertDialogFragment.showDhpDialog(this);
            if (!NetworkUtils.isConnected()) {
                ToastUtils.showToastShort(this, "网络未连接");
                Logger.t(TAG).e("网络未连接");
            } else {
                // 会占用主线程资源，这里不管
                HCSdkManager.getInstance().initAndLogin(this);
            }
        }
    }

}
