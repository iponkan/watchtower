package com.hitqz.robot.watchtower;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.ImageView;

import com.blankj.utilcode.util.NetworkUtils;
import com.hitqz.robot.watchtower.camera.CameraActivity;
import com.hitqz.robot.watchtower.debug.DebugActivity;
import com.hitqz.robot.watchtower.gallery.GalleryActivity;
import com.hitqz.robot.watchtower.setting.SettingActivity;
import com.hitqz.robot.watchtower.widget.AlertDialogFragment;
import com.orhanobut.logger.Logger;
import com.sonicers.commonlib.component.BaseActivity;
import com.sonicers.commonlib.util.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.hitqz.robot.watchtower.constant.PermissionConstant.STORAGE_PERMISSION;

public class MainActivity extends BaseActivity {

    public static final String TAG = "MainActivity";
    private static final int RC_STORAGE_PERM = 123;
    @BindView(R.id.iv_logo)
    ImageView ivLogo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i("----------MainActivity--------");
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
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
            LogManager.getInstance().init(this, getString(R.string.app_name));
            AlertDialogFragment.showSoundDialog(this, "请注意核对动火票！", R.raw.alert);
            if (!NetworkUtils.isConnected()) {
                ToastUtil.showToastShort(this, "网络未连接");
                Logger.t(TAG).e("网络未连接");
            } else {
                // 会占用主线程资源，这里不管
                HCSdkManager.getInstance().initAndLogin(this, null);
            }
        }
    }

    // 数组长度代表点击次数
    long[] mHits = new long[3];

    @OnClick(R.id.iv_logo)
    public void onViewClicked() {
        // 三连击进入debug界面
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();// 开机后运行时间
        if (mHits[0] >= (mHits[mHits.length - 1] - 500)) {
            DebugActivity.go2Debug(this);
        }
    }
}
