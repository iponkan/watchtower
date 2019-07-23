package com.hitqz.robot.watchtower;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.hitqz.robot.commonlib.util.FullScreenUtil;

import butterknife.ButterKnife;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FullScreenUtil.initFullScreen(this);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

}
