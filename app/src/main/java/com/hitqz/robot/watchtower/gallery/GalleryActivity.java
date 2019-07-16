package com.hitqz.robot.watchtower.gallery;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.hitqz.robot.commonlib.util.FullScreenUtil;
import com.hitqz.robot.watchtower.DonghuoRecordManager;
import com.hitqz.robot.watchtower.R;
import com.hitqz.robot.watchtower.widget.CommonTitleBar;

public class GalleryActivity extends AppCompatActivity {

    private CommonTitleBar commonTitleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullScreenUtil.initFullScreen(this);
        setContentView(R.layout.activity_gallery);
        commonTitleBar = findViewById(R.id.common_title_bar);
        commonTitleBar.setBackText("视频库");

        DonghuoRecordManager.getInstance().initRecords();
        VideoListActivity.go2VideoList(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

}
