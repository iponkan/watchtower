package com.hitqz.robot.watchtower;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.hitqz.robot.commonlib.util.FullScreenUtil;
import com.hitqz.robot.watchtower.widget.CommonTitleBar;

public class GalleryActivity extends AppCompatActivity {

    private CommonTitleBar commonTitleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        commonTitleBar = findViewById(R.id.common_title_bar);
        commonTitleBar.setBackText("视频库");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            FullScreenUtil.initFullScreen(this);
        }
    }
}
