package com.hitqz.robot.watchtower;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hitqz.robot.commonlib.util.FullScreenUtil;
import com.hitqz.robot.commonlib.util.ToastUtils;
import com.hitqz.robot.watchtower.bean.FileInfo;
import com.hitqz.robot.watchtower.camera.CameraActivity;
import com.hitqz.robot.watchtower.player.PlayerActivity;

import java.util.List;


import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.hitqz.robot.watchtower.constant.PermissionConstant.STORAGE_PERMISSION;


public class MainActivity extends AppCompatActivity {

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
    }

    public void go2Gallery(View view) {

        List<FileInfo> fileList = HCSdkManager.getInstance(this).findFile();
        if (fileList != null && fileList.size() > 0) {
            PlayerActivity.go2Player(MainActivity.this, fileList.get(0));
        } else {
            ToastUtils.showToastShort(this, "没有录像文件");
        }
    }

    public void go2Setting(View view) {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
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
            boolean initResult = HCSdkManager.getInstance(this).init();
            if (initResult) {
                boolean result = HCSdkManager.getInstance(this).login();
                if (!result) {
                    ToastUtils.showToastShort(this, "登录失败");
                }
            }

        }
    }

    private boolean hasStoragePermission() {
        return EasyPermissions.hasPermissions(this, STORAGE_PERMISSION);
    }
}
