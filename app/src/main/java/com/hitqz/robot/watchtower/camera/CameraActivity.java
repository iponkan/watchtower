package com.hitqz.robot.watchtower.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.hitqz.robot.watchtower.DonghuoRecordManager;
import com.hitqz.robot.watchtower.HCSdk;
import com.hitqz.robot.watchtower.HCSdkManager;
import com.hitqz.robot.watchtower.R;
import com.hitqz.robot.watchtower.constant.Constants;
import com.hitqz.robot.watchtower.net.ISkyNet;
import com.hitqz.robot.watchtower.net.RetrofitManager;
import com.hitqz.robot.watchtower.net.base.BaseObserver;
import com.hitqz.robot.watchtower.net.bean.MockBean;
import com.hitqz.robot.watchtower.net.bean.MonitorEntity;
import com.hitqz.robot.watchtower.net.bean.RegionTemperatureList;
import com.hitqz.robot.watchtower.net.bean.TemperatureList;
import com.hitqz.robot.watchtower.util.AssetUtil;
import com.hitqz.robot.watchtower.widget.LongPressImageView;
import com.hitqz.robot.watchtower.widget.StateView;
import com.orhanobut.logger.Logger;
import com.sonicers.commonlib.component.BaseActivity;
import com.sonicers.commonlib.net.DataBean;
import com.sonicers.commonlib.rx.RxSchedulers;
import com.sonicers.commonlib.singleton.GsonUtil;
import com.sonicers.commonlib.util.ToastUtil;
import com.sonicers.commonlib.view.SteerView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

@SuppressLint("CheckResult")
public class CameraActivity extends BaseActivity implements HCSdkManager.Callback {

    public static final String TAG = "CameraActivity";

    @BindView(R.id.iv_camera_minus)
    ImageView ivMinus;
    @BindView(R.id.iv_camera_plus)
    ImageView ivPlus;
    @BindView(R.id.iv_camera_far)
    LongPressImageView ivFar;
    @BindView(R.id.iv_camera_near)
    LongPressImageView ivNear;
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
    @BindView(R.id.steer_camera)
    SteerView steerCamera;
    @BindView(R.id.steer_car)
    SteerView steerCar;
    @BindView(R.id.sv_ring)
    StateView svRing;
    @BindView(R.id.sv_cameraplatform)
    StateView svCameraPlateform;
    @BindView(R.id.sv_baseplate)
    StateView svBaseplate;
    @BindView(R.id.sv_emergencystop)
    StateView svEmergencyStop;
    @BindView(R.id.sv_soundlight)
    StateView svSoundlight;
    @BindView(R.id.tv_baseplate_electric)
    TextView tvBaseplateElectric;

    HCSdk hotHCSdk;
    HCSdk normalHCSdk;
    Handler handler = new Handler();

    ISkyNet skyNet;
    volatile boolean isMonitoring = false;
    volatile boolean plateStop = false;
    volatile boolean cameraStop = false;
    volatile boolean farStop = false;
    volatile boolean nearStop = false;
    @BindView(R.id.tv_lightSound_electric)
    TextView tvLightSoundElectric;
    @BindView(R.id.lv_temperature)
    ListView lvTemperature;
    boolean sdkInit = false;
    boolean resume = false;
    @BindView(R.id.iv_light)
    Button ivLight;
    @BindView(R.id.iv_cancel_kuang)
    ImageView ivCancelKuang;
    boolean lightTurnOn;
    @BindView(R.id.btn_debug_switch)
    Button btnDebugSwitch;
    @BindView(R.id.ll_debug_layout)
    LinearLayout llDebugLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i("----------CameraActivity--------");
        setContentView(R.layout.activity_camera);

        ButterKnife.bind(this);
        HCSdkManager.getInstance().initAndLogin(this, this);
        hotHCSdk = HCSdkManager.getHotHCSdk(this);
        if (!hotHCSdk.isInit()) {
            ToastUtil.showToastShort(this, "摄像头未连接");
        }

        normalHCSdk = HCSdkManager.getNormalHCSdk(this);
        if (!normalHCSdk.isInit()) {
            ToastUtil.showToastShort(this, "摄像头未连接");
        }

        skyNet = RetrofitManager.getInstance().create(ISkyNet.class);

        resetHotCameraView(hotSurfaceView, productionView);
        resetNormalCameraView(normalSurfaceView);

        hotHCSdk.setSurfaceView(hotSurfaceView);
        normalHCSdk.setSurfaceView(normalSurfaceView);
        steerCamera.setSteerListener(new CameraPlatformSteer());
        steerCar.setSteerListener(new BasePlateSteer());
        ivFar.setLongpressListener(new CameraFarListener());
        ivNear.setLongpressListener(new CameraNearListener());

        checkState();
        isMonitoring();
        if (SPUtils.getInstance(Constants.SP_FILE_NAME).getBoolean(Constants.DEBUG, false)) {
            String json = AssetUtil.loadJSONFromAsset(CameraActivity.this, "mockdata.json");
            MockBean mockBean = GsonUtil.getInstance().fromJson(json, MockBean.class);
            RegionTemperatureList model = mockBean.getData();
            debugShow(model);
        }

        if (SPUtils.getInstance(Constants.SP_FILE_NAME).getBoolean(Constants.LIGHT_SWITCH, false)) {
            ivLight.setVisibility(View.VISIBLE);
        } else {
            ivLight.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resume = true;
        startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (hotHCSdk != null) {
            hotHCSdk.stopSinglePreview();
        }

        if (normalHCSdk != null) {
            normalHCSdk.stopSinglePreview();
        }
        resume = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static void go2Camera(Activity activity) {
        Intent intent = new Intent(activity, CameraActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    @Override
    public void onComplete() {
        sdkInit = true;
        startPreview();
    }

    /**
     * 开始预览
     */
    private void startPreview() {
        if (resume && sdkInit) {
            if (hotHCSdk != null) {
                hotHCSdk.startSinglePreview();
            }
            if (normalHCSdk != null) {
                normalHCSdk.startSinglePreview();
            }
        }
    }

    /**
     * debug才显示
     *
     * @param model 忽略区域列表
     */
    private void debugShow(RegionTemperatureList model) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showModel(model);
                debugShow(model);
            }
        }, 1000);
    }

    /**
     * 根据屏幕调整相机控件位置
     *
     * @param views 控件
     */
    private void resetHotCameraView(View... views) {

        int leftMargin = SizeUtils.dp2px(10);
        int rightMargin = SizeUtils.dp2px(200);
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

        productionView.setParentSize(width, height);
    }

    /**
     * 根据屏幕调整相机控件位置
     *
     * @param normalCameraView 控件
     */
    private void resetNormalCameraView(View normalCameraView) {

        int leftMargin = SizeUtils.dp2px(10);
        int rightMargin = SizeUtils.dp2px(200);
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

    /**
     * 放大选框
     */
    @OnClick(R.id.iv_camera_plus)
    void rectPlus() {
        if (isMonitoring) {
            ToastUtil.showToastShort(this, "请停止监火方能放大选框");
        } else {
            productionView.zoomIn();
        }
    }

    /**
     * 缩小选框
     */
    @OnClick(R.id.iv_camera_minus)
    void rectMinus() {
        if (isMonitoring) {
            ToastUtil.showToastShort(this, "请停止监火方能缩小选框");
        } else {
            productionView.zoomOut();
        }
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

    /**
     * 开始监火
     */
    private void startMonitor() {
        MonitorEntity monitorEntity = new MonitorEntity();
        Point[] points = productionView.getPoints();
        if (points == null) {
            monitorEntity.setHasIgnoreRegion(false);
        } else {
            monitorEntity.setHasIgnoreRegion(true);
            if (points.length == 4 && points[2] != null) {//两个框
                List<MonitorEntity.IgnoreRegionBean> list = MonitorEntity.toIgnoreRegionList(points);
                monitorEntity.setIgnoreRegionList(list);
            } else {//一个框
                MonitorEntity.IgnoreRegionBean ignoreRegionBean = MonitorEntity.toIgnoreRegionBean(points);
                monitorEntity.setIgnoreRegion(ignoreRegionBean);
            }
        }
        ivStartMonitor.setImageResource(R.drawable.btn_wait_dis);
        skyNet.startMonitor(monitorEntity)
                .compose(RxSchedulers.io_main())
                .subscribeWith(new BaseObserver<DataBean>(loadingDialog) {
                    @Override
                    public void onSuccess(DataBean model) {
                        onStartMonitor();
                        Logger.i("开始监火成功");
                        // 添加动火记录点
                        DonghuoRecordManager.getInstance().addDonghuoRecord();
                        // 高清摄像头开始录像
//                        if (!normalHCSdk.recording) {
//                            normalHCSdk.startRecord();
//                        } else {
//                            normalHCSdk.stopRecord();
//                        }
                    }

                    @Override
                    public void onFailure(String msg) {
                        ivStartMonitor.setImageResource(R.drawable.btn_start_active);
                        Logger.e("开始监火失败：" + msg);
                        ToastUtil.showToastShort(CameraActivity.this, "开始监火失败：" + msg);
                    }
                });
    }

    /**
     * 停止监火
     */
    private void stopMonitor() {
        ivStartMonitor.setImageResource(R.drawable.btn_wait_dis);
        skyNet.stopMonitor()
                .compose(RxSchedulers.io_main())
                .subscribeWith(new BaseObserver<DataBean>(loadingDialog) {
                    @Override
                    public void onSuccess(DataBean model) {
//                        ToastUtil.showToastShort(CameraActivity.this, "停止监控成功");
                        DonghuoRecordManager.getInstance().updateDonghuoRecord();
                        Logger.i("停止监火成功");
                        onStopMonitor();
                    }

                    @Override
                    public void onFailure(String msg) {
                        Logger.e("停止监火失败：" + msg);
                        ToastUtil.showToastShort(CameraActivity.this, "停止监控失败");
                    }
                });
    }

    /**
     * 获取监火状态
     */
    private void isMonitoring() {
        skyNet.isMonitoring()
                .compose(RxSchedulers.io_main())
                .subscribeWith(new BaseObserver<MonitorEntity>(loadingDialog) {
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
                        List<MonitorEntity.IgnoreRegionBean> ignoreRegionBeanList = model.getIgnoreRegionList();
                        if (ignoreRegionBean != null) {
                            MonitorEntity.IgnoreRegionBean.LeftDownPointBean leftDownPointBean = ignoreRegionBean.getLeftDownPoint();
                            MonitorEntity.IgnoreRegionBean.RightTopPointBean rightTopPointBean = ignoreRegionBean.getRightTopPoint();

                            if (leftDownPointBean != null && rightTopPointBean != null) {
                                Point point1 = new Point(leftDownPointBean.getX(), leftDownPointBean.getY());
                                Point point2 = new Point(rightTopPointBean.getX(), rightTopPointBean.getY());
                                productionView.setPoints(new Point[]{point1, point2});
                            }
                        } else if (ignoreRegionBeanList != null && ignoreRegionBeanList.size() == 2) {
                            MonitorEntity.IgnoreRegionBean element1 = ignoreRegionBeanList.get(0);
                            MonitorEntity.IgnoreRegionBean element2 = ignoreRegionBeanList.get(1);
                            MonitorEntity.IgnoreRegionBean.LeftDownPointBean bean1 = element1.getLeftDownPoint();
                            MonitorEntity.IgnoreRegionBean.RightTopPointBean bean2 = element1.getRightTopPoint();
                            MonitorEntity.IgnoreRegionBean.LeftDownPointBean bean3 = element2.getLeftDownPoint();
                            MonitorEntity.IgnoreRegionBean.RightTopPointBean bean4 = element2.getRightTopPoint();

                            if (bean1 != null && bean2 != null && bean3 != null && bean4 != null) {
                                Point point1 = new Point(bean1.getX(), bean1.getY());
                                Point point2 = new Point(bean2.getX(), bean2.getY());
                                Point point3 = new Point(bean3.getX(), bean3.getY());
                                Point point4 = new Point(bean4.getX(), bean4.getY());
                                productionView.setPoints(new Point[]{point1, point2, point3, point4});
                            }
                        }
                    }

                    @Override
                    public void onFailure(String msg) {
//                        ToastUtil.showToastShort(CameraActivity.this, "获取监控失败");
                        Logger.e("获取监火状态失败:" + msg);
                    }
                });
    }

    /**
     * 取消警报
     */
    private void resetAlarmLevel() {
        skyNet.resetAlarmLevel()
                .compose(RxSchedulers.io_main())
                .subscribeWith(new BaseObserver<DataBean>(loadingDialog) {
                    @Override
                    public void onSuccess(DataBean model) {
                        ToastUtil.showToastShort(CameraActivity.this, "成功");
                    }

                    @Override
                    public void onFailure(String msg) {
                        ToastUtil.showToastShort(CameraActivity.this, "失败");
                    }
                });
    }

    /**
     * 开始监火回调
     */
    private void onStartMonitor() {
        productionView.antiTouch(true);
        steerCamera.antiTouch(true);
        steerCar.antiTouch(true);

        ivStartMonitor.setImageResource(R.drawable.btn_end_active);
        isMonitoring = true;
        refreshTemperature(true);
    }

    /**
     * 停止监火回调
     */
    private void onStopMonitor() {
        productionView.antiTouch(false);
        steerCamera.antiTouch(false);
        steerCar.antiTouch(false);
        ivStartMonitor.setImageResource(R.drawable.btn_start_active);
        isMonitoring = false;
        refreshTemperature(false);
    }

    /**
     * 获取各个硬件状态
     */
    private void checkState() {
        skyNet.getRingState()
                .compose(RxSchedulers.io_main())
                .subscribeWith(new BaseObserver<Boolean>(loadingDialog) {
                    @Override
                    public void onSuccess(Boolean model) {
                        svRing.setState(model);
                        Logger.t(TAG).i("getRingState success:" + model);
                    }

                    @Override
                    public void onFailure(String msg) {
                        Logger.t(TAG).e("getRingState fail：" + msg);
                    }
                });
        skyNet.getBaseplateState()
                .compose(RxSchedulers.io_main())
                .subscribeWith(new BaseObserver<Boolean>(loadingDialog) {
                    @Override
                    public void onSuccess(Boolean model) {
                        svBaseplate.setState(model);
                        Logger.t(TAG).i("getBaseplateState success:" + model);
                    }

                    @Override
                    public void onFailure(String msg) {
                        Logger.t(TAG).e("getBaseplateState fail：" + msg);
                    }
                });
        skyNet.getCameraPlatformState()
                .compose(RxSchedulers.io_main())
                .subscribeWith(new BaseObserver<Boolean>(loadingDialog) {
                    @Override
                    public void onSuccess(Boolean model) {
                        svCameraPlateform.setState(model);
                        Logger.t(TAG).i("getCameraPlatformState success:" + model);
                    }

                    @Override
                    public void onFailure(String msg) {
                        Logger.t(TAG).e("getCameraPlatformState fail：" + msg);
                    }
                });

        skyNet.getEmergencyStopState()
                .compose(RxSchedulers.io_main())
                .repeatWhen(objectObservable -> objectObservable.flatMap((Function<Object, ObservableSource<?>>) throwable -> Observable.just(1).delay(3, TimeUnit.SECONDS)))
                .compose(bindToLifecycle())
                .subscribeWith(new BaseObserver<Boolean>(loadingDialog) {
                    @Override
                    public void onSuccess(Boolean model) {
                        Logger.t(TAG).i("getEmergencyStopState success:" + model);
                        svEmergencyStop.setState(model);
                    }

                    @Override
                    public void onFailure(String msg) {
                        Logger.t(TAG).e("getEmergencyStopState fail：" + msg);
                    }
                });
        skyNet.getlightAndSoundState()
                .compose(RxSchedulers.io_main())
                .compose(bindToLifecycle())
                .subscribeWith(new BaseObserver<Boolean>(loadingDialog) {
                    @Override
                    public void onSuccess(Boolean model) {
                        Logger.t(TAG).i("getlightAndSoundState success:" + model);
                        svSoundlight.setState(model);
                    }

                    @Override
                    public void onFailure(String msg) {
                        Logger.t(TAG).e("getlightAndSoundState fail：" + msg);
                    }
                });
        skyNet.getBaseplateElectric()
                .compose(RxSchedulers.io_main())
                .repeatWhen(objectObservable -> objectObservable.flatMap((Function<Object, ObservableSource<?>>) throwable -> Observable.just(1).delay(3, TimeUnit.SECONDS)))
                .compose(bindToLifecycle())
                .subscribeWith(new BaseObserver<Integer>(loadingDialog) {
                    @Override
                    public void onSuccess(Integer model) {
                        Logger.t(TAG).i("getBaseplateElectric success:" + model);
                        tvBaseplateElectric.setText(String.valueOf(model));
                        tvBaseplateElectric.setTextColor(model >= 10 ? Color.parseColor("#00de72") : Color.parseColor("#CD5C5C"));
                    }

                    @Override
                    public void onFailure(String msg) {
                        Logger.t(TAG).e("getBaseplateElectric fail：" + msg);
                    }
                });
        skyNet.getLightSoundElectric()
                .compose(RxSchedulers.io_main())
                .repeatWhen(objectObservable -> objectObservable.flatMap((Function<Object, ObservableSource<?>>) throwable -> Observable.just(1).delay(3, TimeUnit.SECONDS)))
                .compose(bindToLifecycle())
                .subscribeWith(new BaseObserver<Integer>(loadingDialog) {
                    @Override
                    public void onSuccess(Integer model) {
                        Logger.t(TAG).i("getLightSoundElectric success:" + model);
                        tvLightSoundElectric.setText(String.valueOf(model));
                        tvLightSoundElectric.setTextColor(model >= 10 ? Color.parseColor("#00de72") : Color.parseColor("#CD5C5C"));
                    }

                    @Override
                    public void onFailure(String msg) {
                        Logger.t(TAG).e("getLightSoundElectric fail：" + msg);
                    }
                });
        skyNet.getCameraPlatformLight()
                .compose(RxSchedulers.io_main())
                .compose(bindToLifecycle())
                .subscribeWith(new BaseObserver<Boolean>(loadingDialog) {
                    @Override
                    public void onSuccess(Boolean model) {
                        Logger.t(TAG).i("getCameraPlatformLight success:" + model);
                        lightTurnOn = model;
                        ivLight.setText(lightTurnOn ? "关灯" : "开灯");
                    }

                    @Override
                    public void onFailure(String msg) {
                        Logger.t(TAG).e("getCameraPlatformLight fail：" + msg);
                    }
                });
    }

    @OnClick(R.id.iv_light)
    public void onIvLightClicked() {

        skyNet.cameraPlatformLight(lightTurnOn ? 0 : 1)
                .compose(RxSchedulers.io_main())
                .subscribeWith(new BaseObserver<DataBean>(loadingDialog) {
                    @Override
                    public void onSuccess(DataBean model) {
                        lightTurnOn = !lightTurnOn;
                        ivLight.setText(lightTurnOn ? "关灯" : "开灯");
                        Logger.t(TAG).i("cameraPlatformLight success:" + model);
                    }

                    @Override
                    public void onFailure(String msg) {
                        Logger.t(TAG).e("cameraPlatformLight fail：" + msg);
                    }
                });
    }

    @OnClick(R.id.iv_cancel_kuang)
    public void onCancelKuangClicked() {
        if (isMonitoring) {
            ToastUtil.showToastShort(this, "请停止监火方能取消选框");
        } else {
            if (productionView != null) {
                productionView.reset();
            }
        }
    }

    @OnClick(R.id.btn_debug_switch)
    public void onViewClicked() {
        if (lvTemperature.getVisibility() == View.VISIBLE) {
            lvTemperature.setVisibility(View.GONE);
        } else if (lvTemperature.getVisibility() == View.GONE) {
            lvTemperature.setVisibility(View.VISIBLE);
        }
    }

    private class CameraPlatformSteer implements SteerView.ISteerListener {

        @Override
        public void onPressDirection(int direction) {
            cameraTurn(direction);
        }

        @Override
        public void onRelease() {
            cameraStop = true;
            handler.postDelayed(CameraActivity.this::cameraStop, 200);
        }
    }

    private class BasePlateSteer implements SteerView.ISteerListener {

        @Override
        public void onPressDirection(int direction) {
            plateTurn(direction);
        }

        @Override
        public void onRelease() {
            plateStop = true;
            handler.postDelayed(CameraActivity.this::plateStop, 200);
        }
    }

    /**
     * 相机方向调整
     *
     * @param direction * 1：上
     *                  * 2：左
     *                  * 3：下
     *                  * 4：右
     */
    @SuppressLint("CheckResult")
    private void cameraTurn(int direction) {
        cameraStop = false;
        Logger.t("interval").d("plateStop false");
        // 在Function函数中，必须对输入的 Observable<Object>进行处理，此处使用flatMap操作符接收上游的数据
        skyNet.setCameraPlatformDirection(direction)
                .repeatWhen(objectObservable -> objectObservable.flatMap((Function<Object, ObservableSource<?>>) throwable -> {
                    if (cameraStop) {
                        return Observable.error(new Throwable(Constants.POLL_END));
                    }
                    return Observable.just(1).delay(200, TimeUnit.MILLISECONDS);
                }))
                .compose(RxSchedulers.io_main())
                .compose(bindToLifecycle())
                .subscribeWith(new BaseObserver<DataBean>() {
                    @Override
                    public void onSuccess(DataBean model) {
                        Logger.t(TAG).d("cameraTurn" + direction + "成功");
                    }

                    @Override
                    public void onFailure(String msg) {
                        if (Constants.POLL_END.equals(msg)) {
                            Logger.t(TAG).i("cameraTurn" + direction + "轮询停止");
                        } else {
                            Logger.t(TAG).e("cameraTurn" + direction + "失败：" + msg);
                        }
                    }
                });
    }

    /**
     * 停止调整相机方向
     */
    private void cameraStop() {
        skyNet.setCameraPlatformDirection(Constants.CAMERA_STOP)
                .compose(RxSchedulers.io_main())
                .subscribeWith(new BaseObserver<DataBean>() {
                    @Override
                    public void onSuccess(DataBean model) {
                        Logger.t(TAG).i("cameraStop成功");
                    }

                    @Override
                    public void onFailure(String msg) {
                        Logger.t(TAG).e("cameraStop失败：" + msg);
                    }
                });
    }

    /**
     * 设置底盘方向
     * 0：停止
     * * 1：前进
     * * 2：左转
     * * 3：后退
     * * 4：右转
     */
    @SuppressLint("CheckResult")
    private void plateTurn(int direction) {
        plateStop = false;
        Logger.t("interval").d("plateStop false");
        // 在Function函数中，必须对输入的 Observable<Object>进行处理，此处使用flatMap操作符接收上游的数据
        skyNet.setBaseplateDirection(direction)
                .repeatWhen(objectObservable -> objectObservable.flatMap((Function<Object, ObservableSource<?>>) throwable -> {
                    if (plateStop) {
                        return Observable.error(new Throwable(Constants.POLL_END));
                    }
                    return Observable.just(1).delay(200, TimeUnit.MILLISECONDS);
                }))
                .compose(RxSchedulers.io_main())
                .compose(bindToLifecycle())
                .subscribeWith(new BaseObserver<DataBean>() {
                    @Override
                    public void onSuccess(DataBean model) {
                        Logger.t(TAG).d("plateTurn" + direction + "成功");
                    }

                    @Override
                    public void onFailure(String msg) {
                        if (Constants.POLL_END.equals(msg)) {
                            Logger.t(TAG).i("plateTurn" + direction + "轮询停止");
                        } else {
                            Logger.t(TAG).e("plateTurn" + direction + "失败：" + msg);
                        }
                    }
                });
    }

    /**
     * 停止设置底盘方向
     */
    private void plateStop() {
        skyNet.setBaseplateDirection(Constants.PLATE_STOP)
                .compose(RxSchedulers.io_main())
                .subscribeWith(new BaseObserver<DataBean>() {
                    @Override
                    public void onSuccess(DataBean model) {
                        Logger.t(TAG).i("plateStop成功");
                    }

                    @Override
                    public void onFailure(String msg) {
                        Logger.t(TAG).e("plateStop失败：" + msg);
                    }
                });
    }

    private class CameraFarListener implements LongPressImageView.ILongPressListener {

        @Override
        public void onPress() {
            cameraFar();
            productionView.drawText(true);
        }

        @Override
        public void onRelease() {
            farStop = true;
            productionView.drawText(false);
        }
    }

    private class CameraNearListener implements LongPressImageView.ILongPressListener {

        @Override
        public void onPress() {
            cameraNear();
            productionView.drawText(true);
        }

        @Override
        public void onRelease() {
            nearStop = true;
            productionView.drawText(false);
        }
    }

    /**
     * 向远处聚焦
     */
    void cameraFar() {
        farStop = false;
        Observable.create(emitter -> {
            hotHCSdk.focusFar();
            emitter.onComplete();
        })
                .repeatWhen(objectObservable -> objectObservable.flatMap((Function<Object, ObservableSource<?>>) throwable -> {
                    if (farStop) {
                        return Observable.error(new Throwable(Constants.POLL_END));
                    }
                    return Observable.just(1).delay(50, TimeUnit.MILLISECONDS);
                }))
                .compose(RxSchedulers.io_main())
                .subscribeWith(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Object o) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 向近处聚焦
     */
    void cameraNear() {
        nearStop = false;
        Observable.create(emitter -> {
            hotHCSdk.focusNear();
            emitter.onComplete();
        })
                .repeatWhen(objectObservable -> objectObservable.flatMap((Function<Object, ObservableSource<?>>) throwable -> {
                    if (nearStop) {
                        return Observable.error(new Throwable(Constants.POLL_END));
                    }
                    return Observable.just(1).delay(50, TimeUnit.MILLISECONDS);
                }))
                .compose(RxSchedulers.io_main())
                .subscribeWith(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Object o) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 更新温度显示
     *
     * @param refresh true 开启
     *                false 关闭
     */
    private void refreshTemperature(boolean refresh) {
        boolean showTemperature = SPUtils.getInstance(Constants.SP_FILE_NAME).getBoolean(Constants.SHOWTEMPERATURE, false);
        if (!refresh || !showTemperature) {
            llDebugLayout.setVisibility(View.GONE);
            productionView.showTemperature(null);
        } else {
            skyNet.regionTemperature()
                    .retryWhen(objectObservable -> objectObservable.flatMap((Function<Object, ObservableSource<?>>) o -> {
                        if (!isMonitoring) {
                            return Observable.empty();
                        } else {
                            return Observable.timer(1, TimeUnit.SECONDS);
                        }
                    }))
                    .repeatWhen(objectObservable -> objectObservable.flatMap((Function<Object, ObservableSource<?>>) o -> {
                        if (!isMonitoring) {
                            return Observable.empty();
                        } else {
                            return Observable.timer(1, TimeUnit.SECONDS);
                        }
                    }))
                    .compose(RxSchedulers.io_main())
                    .compose(bindToLifecycle())
                    .subscribeWith(new BaseObserver<RegionTemperatureList>() {
                        @Override
                        public void onSuccess(RegionTemperatureList model) {
                            showModel(model);
                            Logger.t(TAG).i("regionTemperature success:" + model);
                        }

                        @Override
                        public void onFailure(String msg) {
                            Logger.t(TAG).e("regionTemperature fail：" + msg);
                        }
                    });
        }
    }

    /**
     * debug显示，显示各个块温度
     * @param model RegionTemperatureList
     */
    private void showModel(RegionTemperatureList model) {
        if (model != null) {
            llDebugLayout.setVisibility(View.VISIBLE);
            TemperatureList temperatureList = TemperatureList.fromRegionTemperatureList(model);
            productionView.showTemperature(temperatureList);
            TemperatureAdapter temperatureAdapter = new TemperatureAdapter(temperatureList.toList(), CameraActivity.this);
            lvTemperature.setAdapter(temperatureAdapter);
        }
    }
}
