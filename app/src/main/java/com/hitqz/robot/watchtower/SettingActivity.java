package com.hitqz.robot.watchtower;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.hitqz.robot.commonlib.util.FullScreenUtil;
import com.hitqz.robot.watchtower.widget.CommonTitleBar;

public class SettingActivity extends AppCompatActivity {

    private CommonTitleBar commonTitleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FullScreenUtil.initFullScreen(this);
        setContentView(R.layout.activity_setting);
        commonTitleBar = findViewById(R.id.common_title_bar);
        commonTitleBar.setBackText("设置");
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

}
