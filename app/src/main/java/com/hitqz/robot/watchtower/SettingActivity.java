package com.hitqz.robot.watchtower;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;

import com.hitqz.robot.commonlib.util.FullScreenUtil;
import com.hitqz.robot.commonlib.util.ToastUtils;
import com.hitqz.robot.watchtower.net.AlarmLevelSettingEntity;
import com.hitqz.robot.watchtower.net.BaseObserver;
import com.hitqz.robot.watchtower.net.DataBean;
import com.hitqz.robot.watchtower.net.ISkyNet;
import com.hitqz.robot.watchtower.net.MonitorEntity;
import com.hitqz.robot.watchtower.net.RetrofitManager;
import com.hitqz.robot.watchtower.widget.DhpDialog;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SettingActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.i("----------SettingActivity--------");

        FullScreenUtil.initFullScreen(this);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        ivConfirm.setClickable(false);

        etLevel1.setText(String.valueOf(levle1.getAlarmTemperature()));
        etLevel2.setText(String.valueOf(levle2.getAlarmTemperature()));
        etLevel3.setText(String.valueOf(levle3.getAlarmTemperature()));

        skyNet = RetrofitManager.getInstance().create(ISkyNet.class);
        getAlarmLevelConfig();
        isMonitoring();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @SuppressLint("CheckResult")
    @OnClick(R.id.iv_confirm)
    void submit() {
        if (!checkInput()) {
            return;
        }
        if (isMonitoring) {
            DhpDialog.showDhpDialog(SettingActivity.this, "请先停止监火！");
            return;
        }
        List<AlarmLevelSettingEntity> alarmLevelSettingEntities = new ArrayList<>();
        levle1.setAlarmTemperature(Integer.parseInt(etLevel1.getText().toString()));
        levle2.setAlarmTemperature(Integer.parseInt(etLevel2.getText().toString()));
        levle3.setAlarmTemperature(Integer.parseInt(etLevel3.getText().toString()));
        alarmLevelSettingEntities.add(levle1);
        alarmLevelSettingEntities.add(levle2);
        alarmLevelSettingEntities.add(levle3);
        skyNet.setAlarmLevelConfig(alarmLevelSettingEntities).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new BaseObserver<DataBean>() {
                    @Override
                    public void onSuccess(DataBean model) {
                        DhpDialog.showDhpDialog(SettingActivity.this, "设置成功");
                        Logger.i("设置报警温度成功");
                    }

                    @Override
                    public void onFailure(String msg) {
                        ToastUtils.showToastShort(SettingActivity.this, "设置失败");
                        Logger.i("设置报警温度失败：" + msg);

                    }

                });

    }

    @SuppressLint("CheckResult")
    private void getAlarmLevelConfig() {
        skyNet.getAlarmLevelConfig().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new BaseObserver<List<AlarmLevelSettingEntity>>() {
                    @Override
                    public void onSuccess(List<AlarmLevelSettingEntity> model) {
                        if (model != null && model.size() > 0) {
                            for (AlarmLevelSettingEntity entity : model) {
                                int temperature = entity.getAlarmTemperature();
                                if (entity.getAlarmLevel() == 1) {
                                    levle1.setAlarmTemperature(temperature);
                                    etLevel1.setText(String.valueOf(temperature));
                                } else if (entity.getAlarmLevel() == 2) {
                                    levle2.setAlarmTemperature(temperature);
                                    etLevel2.setText(String.valueOf(entity.getAlarmTemperature()));
                                } else if (entity.getAlarmLevel() == 3) {
                                    levle3.setAlarmTemperature(temperature);
                                    etLevel3.setText(String.valueOf(entity.getAlarmTemperature()));
                                }
                            }
                        }
//                        ToastUtils.showToastShort(SettingActivity.this, "成功");
                    }

                    @Override
                    public void onFailure(String msg) {
                        ToastUtils.showToastShort(SettingActivity.this, "失败:" + msg);
                    }

                });

    }

    @SuppressLint("CheckResult")
    private void isMonitoring() {
        skyNet.isMonitoring().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new BaseObserver<MonitorEntity>() {
                    @Override
                    public void onSuccess(MonitorEntity model) {
                        if (model == null) {
                            return;
                        }
                        if (model.isMonitor()) {
//                            ToastUtils.showToastShort(SettingActivity.this, "正在监控");
                            Logger.i("正在监火");
                        } else {
                            Logger.i("不在监火");
//                            ToastUtils.showToastShort(SettingActivity.this, "不在监控");
                        }
                        isMonitoring = model.isMonitor();
                        ivConfirm.setClickable(true);
                    }

                    @Override
                    public void onFailure(String msg) {
                        isMonitoring = false;
//                        ToastUtils.showToastShort(SettingActivity.this, "获取监控失败");
                        Logger.e("获取监火状态失败:" + msg);
                        ivConfirm.setClickable(true);
                    }

                });

    }

    private boolean checkInput() {
        String s1 = etLevel1.getText().toString();
        if (TextUtils.isEmpty(s1)) {
            DhpDialog.showDhpDialog(SettingActivity.this, "一级报警输入为空");
            return false;
        }

        int t1 = Integer.parseInt(s1);

        String s2 = etLevel2.getText().toString();
        if (TextUtils.isEmpty(s1)) {
            DhpDialog.showDhpDialog(SettingActivity.this, "二级报警输入为空");
            return false;
        }
        int t2 = Integer.parseInt(s2);

        String s3 = etLevel3.getText().toString();
        if (TextUtils.isEmpty(s1)) {
            DhpDialog.showDhpDialog(SettingActivity.this, "三级报警输入为空");
            return false;
        }
        int t3 = Integer.parseInt(s3);

        boolean right = t2 > t1 && t2 < t3;
        if (!right) {
            DhpDialog.showDhpDialog(SettingActivity.this, "报警温度必须依次增加!");
        }
        return right;
    }
}
