package com.hitqz.robot.watchtower.camera;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
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
import com.hitqz.robot.watchtower.HCSdk;
import com.hitqz.robot.watchtower.HCSdkManager;
import com.hitqz.robot.watchtower.R;
import com.hitqz.robot.watchtower.net.BaseObserver;
import com.hitqz.robot.watchtower.net.DataBean;
import com.hitqz.robot.watchtower.net.ISkyNet;
import com.hitqz.robot.watchtower.net.MonitorEntity;
import com.hitqz.robot.watchtower.net.RetrofitManager;
import com.hitqz.robot.watchtower.rx.RxSchedulers;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class CameraActivity extends AppCompatActivity {

    @BindView(R.id.iv_camera_minus)
    ImageView ivMinus;
    @BindView(R.id.iv_camera_plus)
    ImageView ivPlus;
    @BindView(R.id.iv_camera_far)
    ImageView ivFar;
    @BindView(R.id.iv_camera_near)
    ImageView ivNear;
    @BindView(R.id.iv_camera_start_monitor)
    ImageView ivStartMonitor;
    @BindView(R.id.sv_hot_camera)
    SurfaceView hotSurfaceView;
    @BindView(R.id.sv_normal_camera)
    SurfaceView normalSurfaceView;
    @BindView(R.id.pv_camera)
    ProductionView productionView;
    @BindView(R.id.iv_camera_clear_alarm)
    ImageView ivClearAlarm;

    HCSdk hotHCSdk;
    HCSdk normalHCSdk;
    ProductionManager productionManager;

    ISkyNet skyNet;
    boolean isMonitoring;

    public static void go2Camera(Activity activity) {
        Intent intent = new Intent(activity, CameraActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i("----------CameraActivity--------");

        FullScreenUtil.initFullScreen(this);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        hotHCSdk = HCSdkManager.getHotHCSdk(this);
        if (!hotHCSdk.isInit()) {
            ToastUtils.showToastShort(this, "摄像头未连接");
            finish();
        }

        normalHCSdk = HCSdkManager.getNormalHCSdk(this);
        if (!normalHCSdk.isInit()) {
            ToastUtils.showToastShort(this, "摄像头未连接");
            finish();
        }

        skyNet = RetrofitManager.getInstance().create(ISkyNet.class);

        resetHotCameraView(hotSurfaceView, productionView);
        resetNormalCameraView(normalSurfaceView);

        hotHCSdk.setSurfaceView(hotSurfaceView);
        normalHCSdk.setSurfaceView(normalSurfaceView);

        isMonitoring();
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
        if (hotHCSdk != null) {
            hotHCSdk.stopSinglePreview();
        }

        if (normalHCSdk != null) {
            normalHCSdk.stopSinglePreview();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hotHCSdk != null) {
            hotHCSdk.startSinglePreview();
        }
        if (normalHCSdk != null) {
            normalHCSdk.startSinglePreview();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R.id.iv_camera_near)
    void cameraNear() {
        hotHCSdk.focusNear();
    }

    @OnClick(R.id.iv_camera_far)
    void cameraFar() {
        hotHCSdk.focusFar();
    }

    @OnClick(R.id.iv_camera_plus)
    void rectPlus() {
        productionView.zoomIn();
    }

    @OnClick(R.id.iv_camera_minus)
    void rectMinus() {
        productionView.zoomOut();
    }

    @OnClick(R.id.iv_camera_start_monitor)
    void start() {
        if (isMonitoring) {
            stopMonitor();
        } else {
            startMonitor();
        }
    }

    @OnClick(R.id.iv_camera_clear_alarm)
    void clearAlarm() {
        resetAlarmLevel();
    }

    @SuppressLint("CheckResult")
    private void startMonitor() {
        MonitorEntity monitorEntity = new MonitorEntity();
        Point[] points = productionView.getPoints();
        if (points == null) {
            monitorEntity.setHasIgnoreRegion(false);
        } else {
            monitorEntity.setHasIgnoreRegion(true);
            MonitorEntity.IgnoreRegionBean ignoreRegionBean = MonitorEntity.fromPoints(points);
            monitorEntity.setIgnoreRegion(ignoreRegionBean);
        }
        ivStartMonitor.setImageResource(R.drawable.btn_wait_dis);
        skyNet.startMonitor(monitorEntity)
                .compose(RxSchedulers.io_main())
                .subscribeWith(new BaseObserver<DataBean>() {
                    @Override
                    public void onSuccess(DataBean model) {
                        onStartMonitor();
                        Logger.i("开始监火成功");
                        // 添加动火记录点
                        DonghuoRecordManager.getInstance().addTimePoint();
                        // 高清摄像头开始录像
//                        if (!normalHCSdk.recording) {
//                            normalHCSdk.startRecord();
//                        } else {
//                            normalHCSdk.stopRecord();
//                        }
                    }

                    @Override
                    public void onFailure(String msg) {
                        Logger.e("开始监火失败：" + msg);
                        ToastUtils.showToastShort(CameraActivity.this, "开始监火失败：" + msg);
                    }

                });

    }

    @SuppressLint("CheckResult")
    private void stopMonitor() {
        ivStartMonitor.setImageResource(R.drawable.btn_wait_dis);
        skyNet.stopMonitor()
                .compose(RxSchedulers.io_main())
                .subscribeWith(new BaseObserver<DataBean>() {
                    @Override
                    public void onSuccess(DataBean model) {
//                        ToastUtils.showToastShort(CameraActivity.this, "停止监控成功");
                        Logger.i("停止监火成功");
                        onStopMonitor();
                    }

                    @Override
                    public void onFailure(String msg) {
                        Logger.e("停止监火失败：" + msg);
                        ToastUtils.showToastShort(CameraActivity.this, "停止监控失败");
                    }

                });

    }

    @SuppressLint("CheckResult")
    private void isMonitoring() {
        skyNet.isMonitoring()
                .compose(RxSchedulers.io_main())
                .subscribeWith(new BaseObserver<MonitorEntity>() {
                    @Override
                    public void onSuccess(MonitorEntity model) {
                        if (model == null) {
                            Logger.e("MonitorEntity====null");
                            return;
                        }
                        if (model.isMonitor()) {
                            onStartMonitor();
                            Logger.i("正在监火");
                        } else {
                            onStopMonitor();
                            Logger.i("不在监火");
                        }

                        MonitorEntity.IgnoreRegionBean ignoreRegionBean = model.getIgnoreRegion();
                        if (ignoreRegionBean != null) {
                            MonitorEntity.IgnoreRegionBean.LeftDownPointBean leftDownPointBean = ignoreRegionBean.getLeftDownPoint();
                            MonitorEntity.IgnoreRegionBean.RightTopPointBean rightTopPointBean = ignoreRegionBean.getRightTopPoint();

                            if (leftDownPointBean != null && rightTopPointBean != null) {
                                Point point1 = new Point(leftDownPointBean.getX(), leftDownPointBean.getY());
                                Point point2 = new Point(rightTopPointBean.getX(), rightTopPointBean.getY());
                                productionView.setPoints(new Point[]{point1, point2});
                            }
                        }
                    }

                    @Override
                    public void onFailure(String msg) {
//                        ToastUtils.showToastShort(CameraActivity.this, "获取监控失败");
                        Logger.e("获取监火状态失败:" + msg);
                    }

                });

    }

    @SuppressLint("CheckResult")
    private void resetAlarmLevel() {
        skyNet.resetAlarmLevel()
                .compose(RxSchedulers.io_main())
                .subscribeWith(new BaseObserver<DataBean>() {
                    @Override
                    public void onSuccess(DataBean model) {
                        ToastUtils.showToastShort(CameraActivity.this, "成功");
                    }

                    @Override
                    public void onFailure(String msg) {
                        ToastUtils.showToastShort(CameraActivity.this, "失败");
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

    private void onStartMonitor() {
        productionView.antiTouch(true);
        ivStartMonitor.setImageResource(R.drawable.btn_end_active);
        isMonitoring = true;
        ivPlus.setClickable(false);
        ivMinus.setClickable(false);
        ivFar.setClickable(false);
        ivNear.setClickable(false);
    }

    private void onStopMonitor() {
        productionView.antiTouch(false);
        ivStartMonitor.setImageResource(R.drawable.btn_start_active);
        isMonitoring = false;
        ivPlus.setClickable(true);
        ivMinus.setClickable(true);
        ivFar.setClickable(true);
        ivNear.setClickable(true);
    }
}
