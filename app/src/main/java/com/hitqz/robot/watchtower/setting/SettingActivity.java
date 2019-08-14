package com.hitqz.robot.watchtower.setting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;

import com.hitqz.robot.watchtower.R;
import com.hitqz.robot.watchtower.net.AlarmLevelSettingEntity;
import com.hitqz.robot.watchtower.net.ISkyNet;
import com.hitqz.robot.watchtower.net.MonitorEntity;
import com.hitqz.robot.watchtower.net.RetrofitManager;
import com.hitqz.robot.watchtower.net.base.BaseObserver;
import com.hitqz.robot.watchtower.widget.AlertDialogFragment;
import com.orhanobut.logger.Logger;
import com.sonicers.commonlib.component.BaseActivity;
import com.sonicers.commonlib.net.DataBean;
import com.sonicers.commonlib.rx.RxSchedulers;
import com.sonicers.commonlib.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends BaseActivity {

    @BindView(R.id.iv_confirm)
    ImageView ivConfirm;

    @BindView(R.id.et_level1)
    EditText etLevel1;

    @BindView(R.id.et_level2)
    EditText etLevel2;

    @BindView(R.id.et_level3)
    EditText etLevel3;

    ISkyNet skyNet;
    boolean isMonitoring;

    AlarmLevelSettingEntity levle1 = AlarmLevelSettingEntity.getDefaultLevel1();
    AlarmLevelSettingEntity levle2 = AlarmLevelSettingEntity.getDefaultLevel2();
    AlarmLevelSettingEntity levle3 = AlarmLevelSettingEntity.getDefaultLevel3();

    public static void go2Setting(Activity activity) {
        Intent intent = new Intent(activity, SettingActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.i("----------SettingActivity--------");

        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        ivConfirm.setClickable(false);

        etLevel1.setText(String.valueOf(levle1.getAlarmTemperature()));
        etLevel2.setText(String.valueOf(levle2.getAlarmTemperature()));
        etLevel3.setText(String.valueOf(levle3.getAlarmTemperature()));

        skyNet = RetrofitManager.getInstance().create(ISkyNet.class);
        isMonitoring();
        getAlarmLevelConfig();
    }

    @SuppressLint("CheckResult")
    @OnClick(R.id.iv_confirm)
    void submit() {
        if (isMonitoring) {
            AlertDialogFragment.showDialog(SettingActivity.this, "请先停止监火！");
            return;
        }
        if (!checkInput()) {
            return;
        }
        List<AlarmLevelSettingEntity> alarmLevelSettingEntities = new ArrayList<>();
        levle1.setAlarmTemperature(Integer.parseInt(etLevel1.getText().toString()));
        levle2.setAlarmTemperature(Integer.parseInt(etLevel2.getText().toString()));
        levle3.setAlarmTemperature(Integer.parseInt(etLevel3.getText().toString()));
        alarmLevelSettingEntities.add(levle1);
        alarmLevelSettingEntities.add(levle2);
        alarmLevelSettingEntities.add(levle3);
        skyNet.setAlarmLevelConfig(alarmLevelSettingEntities)
                .compose(RxSchedulers.io_main())
                .subscribeWith(new BaseObserver<DataBean>(loadingDialog) {
                    @Override
                    public void onSuccess(DataBean model) {
                        AlertDialogFragment.showDialog(SettingActivity.this, "设置成功");
                        Logger.i("设置报警温度成功");
                    }

                    @Override
                    public void onFailure(String msg) {
                        ToastUtil.showToastShort(SettingActivity.this, "设置失败");
                        Logger.i("设置报警温度失败：" + msg);
                    }
                });
    }

    @SuppressLint("CheckResult")
    private void getAlarmLevelConfig() {
        skyNet.getAlarmLevelConfig()
                .compose(RxSchedulers.io_main())
                .subscribeWith(new BaseObserver<List<AlarmLevelSettingEntity>>(loadingDialog) {
                    @Override
                    public void onSuccess(List<AlarmLevelSettingEntity> model) {
                        if (model != null && model.size() > 0) {
                            for (AlarmLevelSettingEntity entity : model) {
                                int level = entity.getAlarmLevel();
                                int temperature = entity.getAlarmTemperature();
                                switch (level) {
                                    case 1:
                                        levle1.setAlarmTemperature(temperature);
                                        break;
                                    case 2:
                                        levle2.setAlarmTemperature(temperature);
                                        break;
                                    case 3:
                                        levle3.setAlarmTemperature(temperature);
                                        break;
                                }
                            }
                        }
                        setEtText();
//                        ToastUtil.showToastShort(SettingActivity.this, "成功");
                    }

                    @Override
                    public void onFailure(String msg) {
                        ToastUtil.showToastShort(SettingActivity.this, "失败:" + msg);
                        setEtText();
                    }
                });
    }

    private void setEtText() {
        etLevel1.setText(String.valueOf(levle1.getAlarmTemperature()));
        etLevel2.setText(String.valueOf(levle2.getAlarmTemperature()));
        etLevel3.setText(String.valueOf(levle3.getAlarmTemperature()));
    }

    @SuppressLint("CheckResult")
    private void isMonitoring() {
        skyNet.isMonitoring()
                .compose(RxSchedulers.io_main())
                .subscribeWith(new BaseObserver<MonitorEntity>(loadingDialog) {
                    @Override
                    public void onSuccess(MonitorEntity model) {
                        if (model == null) {
                            return;
                        }
                        if (model.isMonitor()) {
//                            ToastUtil.showToastShort(SettingActivity.this, "正在监控");
                            Logger.i("正在监火");
                        } else {
                            Logger.i("不在监火");
//                            ToastUtil.showToastShort(SettingActivity.this, "不在监控");
                        }
                        isMonitoring = model.isMonitor();
                        ivConfirm.setClickable(true);
                    }

                    @Override
                    public void onFailure(String msg) {
                        isMonitoring = false;
//                        ToastUtil.showToastShort(SettingActivity.this, "获取监控失败");
                        Logger.e("获取监火状态失败:" + msg);
                        ivConfirm.setClickable(true);
                    }
                });
    }

    private boolean checkInput() {
        String s1 = etLevel1.getText().toString();
        if (TextUtils.isEmpty(s1)) {
            AlertDialogFragment.showDialog(SettingActivity.this, "一级报警输入为空");
            return false;
        }

        int t1 = Integer.parseInt(s1);

        String s2 = etLevel2.getText().toString();
        if (TextUtils.isEmpty(s1)) {
            AlertDialogFragment.showDialog(SettingActivity.this, "二级报警输入为空");
            return false;
        }
        int t2 = Integer.parseInt(s2);

        String s3 = etLevel3.getText().toString();
        if (TextUtils.isEmpty(s1)) {
            AlertDialogFragment.showDialog(SettingActivity.this, "三级报警输入为空");
            return false;
        }
        int t3 = Integer.parseInt(s3);

        boolean right = t2 > t1 && t2 < t3;
        if (!right) {
            AlertDialogFragment.showDialog(SettingActivity.this, "报警温度必须依次增加!");
        }
        return right;
    }
}
