package com.hitqz.robot.watchtower;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageView;

import com.hitqz.robot.commonlib.util.FullScreenUtil;
import com.hitqz.robot.commonlib.util.ToastUtils;
import com.hitqz.robot.watchtower.net.AlarmLevelSettingEntity;
import com.hitqz.robot.watchtower.net.BaseObserver;
import com.hitqz.robot.watchtower.net.DataBean;
import com.hitqz.robot.watchtower.net.ISkyNet;
import com.hitqz.robot.watchtower.net.RetrofitManager;

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

    ISkyNet skyNet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FullScreenUtil.initFullScreen(this);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        skyNet = RetrofitManager.getInstance().create(ISkyNet.class);

    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @SuppressLint("CheckResult")
    @OnClick(R.id.iv_confirm)
    void submit() {
        List<AlarmLevelSettingEntity> alarmLevelSettingEntities = new ArrayList<>();
        AlarmLevelSettingEntity levle1 = new AlarmLevelSettingEntity(1, 100);
        AlarmLevelSettingEntity levle2 = new AlarmLevelSettingEntity(1, 100);
        AlarmLevelSettingEntity levle3 = new AlarmLevelSettingEntity(1, 100);
        alarmLevelSettingEntities.add(levle1);
        alarmLevelSettingEntities.add(levle2);
        alarmLevelSettingEntities.add(levle3);
        skyNet.setAlarmLevelConfig(alarmLevelSettingEntities).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new BaseObserver<DataBean>() {
                    @Override
                    public void onSuccess(DataBean model) {
                        ToastUtils.showToastShort(SettingActivity.this, "成功");
                    }

                    @Override
                    public void onFailure(String msg) {
                        ToastUtils.showToastShort(SettingActivity.this, "失败");
                    }

                });

    }

    @SuppressLint("CheckResult")
    private void getAlarmLevelConfig() {
        skyNet.getAlarmLevelConfig().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new BaseObserver<DataBean>() {
                    @Override
                    public void onSuccess(DataBean model) {
                        ToastUtils.showToastShort(SettingActivity.this, "成功");
                    }

                    @Override
                    public void onFailure(String msg) {
                        ToastUtils.showToastShort(SettingActivity.this, "失败");
                    }

                });

    }

    @SuppressLint("CheckResult")
    private void getAlarmLevel() {
        skyNet.getAlarmLevel().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new BaseObserver<DataBean>() {
                    @Override
                    public void onSuccess(DataBean model) {
                        ToastUtils.showToastShort(SettingActivity.this, "成功");
                    }

                    @Override
                    public void onFailure(String msg) {
                        ToastUtils.showToastShort(SettingActivity.this, "失败");
                    }

                });

    }

    @SuppressLint("CheckResult")
    private void resetAlarmLevel() {
        skyNet.resetAlarmLevel().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new BaseObserver<DataBean>() {
                    @Override
                    public void onSuccess(DataBean model) {
                        ToastUtils.showToastShort(SettingActivity.this, "成功");
                    }

                    @Override
                    public void onFailure(String msg) {
                        ToastUtils.showToastShort(SettingActivity.this, "失败");
                    }

                });

    }

}
