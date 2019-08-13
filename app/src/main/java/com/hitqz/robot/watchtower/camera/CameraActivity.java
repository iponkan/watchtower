package com.hitqz.robot.watchtower.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.hitqz.robot.watchtower.BaseActivity;
import com.hitqz.robot.watchtower.DonghuoRecordManager;
import com.hitqz.robot.watchtower.HCSdk;
import com.hitqz.robot.watchtower.HCSdkManager;
import com.hitqz.robot.watchtower.R;
import com.hitqz.robot.watchtower.constant.Constants;
import com.hitqz.robot.watchtower.net.ISkyNet;
import com.hitqz.robot.watchtower.net.MonitorEntity;
import com.hitqz.robot.watchtower.net.RetrofitManager;
import com.hitqz.robot.watchtower.widget.StateView;
import com.orhanobut.logger.Logger;
import com.sonicers.commonlib.net.BaseObserver;
import com.sonicers.commonlib.net.DataBean;
import com.sonicers.commonlib.rx.RxSchedulers;
import com.sonicers.commonlib.util.ToastUtil;
import com.sonicers.commonlib.view.SteerView;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

@SuppressLint("CheckResult")
public class CameraActivity extends BaseActivity implements SteerView.ISteerListener {

    public static final String TAG = "CameraActivity";

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
    @BindView(R.id.sv_car)
    SteerView svCar;
    @BindView(R.id.sv_ring)
    StateView svRing;
    @BindView(R.id.sv_cameraplatform)
    StateView svCameraPlateform;
    @BindView(R.id.sv_baseplate)
    StateView svBaseplate;
    @BindView(R.id.sv_emergencystop)
    StateView svEmergencyStop;
    @BindView(R.id.tv_baseplate_electric)
    TextView tvBaseplateElectric;

    HCSdk hotHCSdk;
    HCSdk normalHCSdk;
    ProductionManager productionManager;
    Handler handler = new Handler();

    ISkyNet skyNet;
    boolean isMonitoring;
    AtomicBoolean sendStop = new AtomicBoolean(false);

    public static void go2Camera(Activity activity) {
        Intent intent = new Intent(activity, CameraActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i("----------CameraActivity--------");
        setContentView(R.layout.activity_camera);

        ButterKnife.bind(this);
        hotHCSdk = HCSdkManager.getHotHCSdk(this);
        if (!hotHCSdk.isInit()) {
            ToastUtil.showToastShort(this, "摄像头未连接");
//            finish();
        }

        normalHCSdk = HCSdkManager.getNormalHCSdk(this);
        if (!normalHCSdk.isInit()) {
            ToastUtil.showToastShort(this, "摄像头未连接");
//            finish();
        }

        skyNet = RetrofitManager.getInstance().create(ISkyNet.class);

        resetHotCameraView(hotSurfaceView, productionView);
        resetNormalCameraView(normalSurfaceView);

        hotHCSdk.setSurfaceView(hotSurfaceView);
        normalHCSdk.setSurfaceView(normalSurfaceView);
        svCar.setSteerListener(this);

        checkState();
        isMonitoring();
    }

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

        float centerWidth = getResources().getDimension(R.dimen.fr_center_width);
        productionManager = new ProductionManager(width, height, centerWidth);

        productionView.setParentSize(width, height);
        productionView.setProductionManager(productionManager);
    }

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

    @Override
    protected void onPause() {
        super.onPause();
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
                        ivStartMonitor.setImageResource(R.drawable.btn_start_active);
                        Logger.e("开始监火失败：" + msg);
                        ToastUtil.showToastShort(CameraActivity.this, "开始监火失败：" + msg);
                    }
                });
    }

    private void stopMonitor() {
        ivStartMonitor.setImageResource(R.drawable.btn_wait_dis);
        skyNet.stopMonitor()
                .compose(RxSchedulers.io_main())
                .subscribeWith(new BaseObserver<DataBean>() {
                    @Override
                    public void onSuccess(DataBean model) {
//                        ToastUtil.showToastShort(CameraActivity.this, "停止监控成功");
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

    private void isMonitoring() {
        showLoadingDialog();
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
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onFailure(String msg) {
//                        ToastUtil.showToastShort(CameraActivity.this, "获取监控失败");
                        Logger.e("获取监火状态失败:" + msg);
                        dismissLoadingDialog();
                    }
                });
    }

    private void resetAlarmLevel() {
        skyNet.resetAlarmLevel()
                .compose(RxSchedulers.io_main())
                .subscribeWith(new BaseObserver<DataBean>() {
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

    public void cameraPressed(View view) {
        SteerView steerView = (SteerView) view;
        if (steerView.getPressDirection() == SteerView.LEFT_PRESS) {
            Toast.makeText(this, "LEFT_PRESS", Toast.LENGTH_SHORT).show();
        } else if (steerView.getPressDirection() == SteerView.TOP_PRESS) {
            Toast.makeText(this, "TOP_PRESS", Toast.LENGTH_SHORT).show();
        } else if (steerView.getPressDirection() == SteerView.RIGHT_PRESS) {
            Toast.makeText(this, "RIGHT_PRESS", Toast.LENGTH_SHORT).show();
        } else if (steerView.getPressDirection() == SteerView.BOTTOM_PRESS) {
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

    @SuppressLint("CheckResult")
    private void plateTurn(int direction) {
        sendStop.set(false);
        Logger.t("interval").d("sendStop false");
        skyNet.setBaseplateDirection(direction)
                .repeatWhen(new Function<Observable<Object>, ObservableSource<?>>() {
                    @Override
                    // 在Function函数中，必须对输入的 Observable<Object>进行处理，此处使用flatMap操作符接收上游的数据
                    public ObservableSource<?> apply(@NonNull Observable<Object> objectObservable) throws Exception {
                        // 将原始 Observable 停止发送事件的标识（Complete（） /  Error（））转换成1个 Object 类型数据传递给1个新被观察者（Observable）
                        // 以此决定是否重新订阅 & 发送原来的 Observable，即轮询
                        // 此处有2种情况：
                        // 1. 若返回1个Complete（） /  Error（）事件，则不重新订阅 & 发送原来的 Observable，即轮询结束
                        // 2. 若返回其余事件，则重新订阅 & 发送原来的 Observable，即继续轮询
                        return objectObservable.flatMap(new Function<Object, ObservableSource<?>>() {
                            @Override
                            public ObservableSource<?> apply(@NonNull Object throwable) throws Exception {

                                // 加入判断条件：但发送停止后停止轮询
                                if (sendStop.get()) {
                                    // 此处选择发送onError事件以结束轮询，因为可触发下游观察者的onError（）方法回调
                                    return Observable.error(new Throwable(Constants.POLL_END));
                                }
                                // 若轮询次数＜4次，则发送1Next事件以继续轮询
                                // 注：此处加入了delay操作符，作用 = 延迟一段时间发送（此处设置 = 2s），以实现轮询间间隔设置
                                return Observable.just(1).delay(200, TimeUnit.MILLISECONDS);
                            }
                        });
                    }
                })
                .compose(RxSchedulers.io_main())
                .subscribeWith(new BaseObserver<DataBean>() {
                    @Override
                    public void onSuccess(DataBean model) {
                        Logger.t(TAG).d("plateTurn" + direction + "成功");
                    }

                    @Override
                    public void onFailure(String msg) {
                        if (Constants.POLL_END.equals(msg)) {
                            Logger.t(TAG).i("plateTurn" + direction + "轮询停止");
                        }
                        {
                            Logger.t(TAG).e("plateTurn" + direction + "失败：" + msg);
                        }
                    }
                });
    }

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

    @Override
    public void onPressDirection(int direction) {
        plateTurn(direction);
    }

    @Override
    public void onRelease() {
        sendStop.set(true);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                plateStop();
            }
        }, 200);
    }

    private void checkState() {
        skyNet.getRingState()
                .compose(RxSchedulers.io_main())
                .subscribeWith(new BaseObserver<Boolean>() {
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
                .subscribeWith(new BaseObserver<Boolean>() {
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
                .subscribeWith(new BaseObserver<Boolean>() {
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
                .subscribeWith(new BaseObserver<Boolean>() {
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
        skyNet.getBaseplateElectric()
                .compose(RxSchedulers.io_main())
                .subscribeWith(new BaseObserver<Integer>() {
                    @Override
                    public void onSuccess(Integer model) {
                        Logger.t(TAG).i("getBaseplateElectric success:" + model);
                        tvBaseplateElectric.setText(String.valueOf(model));
                    }

                    @Override
                    public void onFailure(String msg) {
                        Logger.t(TAG).e("getBaseplateElectric fail：" + msg);
                    }
                });
    }
}
