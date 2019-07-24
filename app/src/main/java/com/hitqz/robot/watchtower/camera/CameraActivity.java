package com.hitqz.robot.watchtower.camera;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.hitqz.robot.commonlib.util.FullScreenUtil;
import com.hitqz.robot.commonlib.util.ToastUtils;

import com.hitqz.robot.commonlib.view.SteerView;
import com.hitqz.robot.watchtower.DonghuoRecordManager;
import com.hitqz.robot.watchtower.HCSdkManager;
import com.hitqz.robot.watchtower.R;
import com.hitqz.robot.watchtower.net.BaseObserver;
import com.hitqz.robot.watchtower.net.DataBean;
import com.hitqz.robot.watchtower.net.ISkyNet;
import com.hitqz.robot.watchtower.net.MonitorEntity;
import com.hitqz.robot.watchtower.net.RetrofitManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.iv_camera_minus)
    ImageView ivMinus;
    @BindView(R.id.iv_camera_plus)
    ImageView ivPlus;
    @BindView(R.id.iv_camera_focus)
    ImageView ivFocus;
    @BindView(R.id.iv_camera_confirm)
    ImageView ivConfirm;
    @BindView(R.id.sv_hot_camera)
    SurfaceView hotSurfaceView;
    @BindView(R.id.sv_normal_camera)
    SurfaceView normalSurfaceView;
    @BindView(R.id.pv_camera)
    ProductionView productionView;

    HCSdkManager hotHCSdkManager;
    HCSdkManager normalHCSdkManager;
    ProductionManager productionManager;

    ISkyNet skyNet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullScreenUtil.initFullScreen(this);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        hotHCSdkManager = HCSdkManager.getHotHCSdkManager(this);
        if (!hotHCSdkManager.isInit()) {
            ToastUtils.showToastShort(this, "热成像摄像头Sdk未初始化");
            return;
        }

        normalHCSdkManager = HCSdkManager.getNormalHCSdkManager(this);
        if (!normalHCSdkManager.isInit()) {
            ToastUtils.showToastShort(this, "高清摄像头Sdk未初始化");
            return;
        }

        skyNet = RetrofitManager.getInstance().create(ISkyNet.class);


        ivConfirm.setOnClickListener(this);

        resetHotCameraView(hotSurfaceView, productionView);
        resetNormalCameraView(normalSurfaceView);

        hotHCSdkManager.setSurfaceView(hotSurfaceView);
        normalHCSdkManager.setSurfaceView(normalSurfaceView);

        ivPlus.setOnClickListener(this);
        ivMinus.setOnClickListener(this);
        ivFocus.setOnClickListener(this);
    }

    private void resetHotCameraView(View... views) {

        int leftMargin = SizeUtils.dp2px(40);
        int rightMargin = SizeUtils.dp2px(40);
        int topMargin = SizeUtils.dp2px(10);

        int width = ScreenUtils.getScreenWidth() - leftMargin - rightMargin;
        int height = (int) (width / 16f * 9);

        for (View view : views) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.width = width;
            layoutParams.height = height;
            layoutParams.leftMargin = leftMargin;
            layoutParams.rightMargin = rightMargin;
            layoutParams.topMargin = topMargin;

            view.setLayoutParams(layoutParams);
        }


        float centerWidth = getResources().getDimension(R.dimen.fr_center_width);
        productionManager = new ProductionManager(width, height, centerWidth);

        productionView.setParentSize(width, height);
        productionView.setProductionManager(productionManager);
    }

    private void resetNormalCameraView(View normalCameraView) {

        int leftMargin = SizeUtils.dp2px(40);
        int rightMargin = SizeUtils.dp2px(40);
        int topMargin = SizeUtils.dp2px(10);

        int width = ScreenUtils.getScreenWidth() - leftMargin - rightMargin;
        int height = (int) (width / 16f * 9);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) normalCameraView.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        layoutParams.leftMargin = leftMargin;
        layoutParams.rightMargin = rightMargin;
        layoutParams.topMargin = topMargin;

        normalCameraView.setLayoutParams(layoutParams);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
        if (hotHCSdkManager != null) {
            hotHCSdkManager.stopSinglePreview();
        }

        if (normalHCSdkManager != null) {
            normalHCSdkManager.stopSinglePreview();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hotHCSdkManager != null) {
            hotHCSdkManager.startSinglePreview();
        }
        if (normalHCSdkManager != null) {
            normalHCSdkManager.startSinglePreview();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onClick(View v) {
        if (v == ivPlus) {
            productionView.zoomIn();
        } else if (v == ivMinus) {
            productionView.zoomOut();
        } else if (v == ivFocus) {
            //hotHCSdkManager.focusNear();
            // test
//                DonghuoRecordManager.getInstance().clearRecods();

//                normalHCSdkManager.testGetAbility();
            if (!normalHCSdkManager.recording) {
                normalHCSdkManager.startRecord();

            } else {
                normalHCSdkManager.stopRecord();

            }
        } else if (v == ivConfirm) {
            DonghuoRecordManager.getInstance().addTimePoint();

            startMonitor();
        }
    }

    @SuppressLint("CheckResult")
    private void startMonitor() {
        MonitorEntity monitorEntity = new MonitorEntity();
        monitorEntity.setHasIgnoreRegion(false);
        skyNet.startMonitor(monitorEntity).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new BaseObserver<DataBean>() {
                    @Override
                    public void onSuccess(DataBean model) {
                        ToastUtils.showToastShort(CameraActivity.this, "开始监控成功");
                    }

                    @Override
                    public void onFailure(String msg) {
                        ToastUtils.showToastShort(CameraActivity.this, "开始监控失败");
                    }

                });

    }

    @SuppressLint("CheckResult")
    private void stopMonitor() {
        skyNet.stopMonitor().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new BaseObserver<DataBean>() {
                    @Override
                    public void onSuccess(DataBean model) {
                        ToastUtils.showToastShort(CameraActivity.this, "停止监控成功");
                    }

                    @Override
                    public void onFailure(String msg) {
                        ToastUtils.showToastShort(CameraActivity.this, "停止监控失败");
                    }

                });

    }

    @SuppressLint("CheckResult")
    private void isMonitoring() {
        skyNet.isMonitoring().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new BaseObserver<DataBean>() {
                    @Override
                    public void onSuccess(DataBean model) {
                        if (model.isData()) {
                            ToastUtils.showToastShort(CameraActivity.this, "正在监控");
                        } else {
                            ToastUtils.showToastShort(CameraActivity.this, "不在监控");
                        }

                    }

                    @Override
                    public void onFailure(String msg) {
                        ToastUtils.showToastShort(CameraActivity.this, "获取监控失败");
                    }

                });

    }

    public void circlePressed(View view) {
        SteerView azimuthCircle = (SteerView) view;
        if (azimuthCircle.getPressDirection() == SteerView.LEFT_PRESS) {
            Toast.makeText(this, "LEFT_PRESS", Toast.LENGTH_SHORT).show();
        } else if (azimuthCircle.getPressDirection() == SteerView.TOP_PRESS) {
            Toast.makeText(this, "TOP_PRESS", Toast.LENGTH_SHORT).show();
        } else if (azimuthCircle.getPressDirection() == SteerView.RIGHT_PRESS) {
            Toast.makeText(this, "RIGHT_PRESS", Toast.LENGTH_SHORT).show();
        } else if (azimuthCircle.getPressDirection() == SteerView.BOTTOM_PRESS) {
            Toast.makeText(this, "BOTTOM_PRESS", Toast.LENGTH_SHORT).show();
        }
    }
}
