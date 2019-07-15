package com.hitqz.robot.watchtower.gallery;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.hitqz.robot.commonlib.util.FullScreenUtil;
import com.hitqz.robot.watchtower.HCSdkManager;
import com.hitqz.robot.watchtower.R;
import com.hitqz.robot.watchtower.bean.FileInfo;
import com.hitqz.robot.watchtower.util.CameraUtil;
import com.hitqz.robot.watchtower.widget.CommonTitleBar;

import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends AppCompatActivity {

    private CommonTitleBar commonTitleBar;
    private ListView listView;
    VideoAdapter videoAdapter;
    HCSdkManager hcSdkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullScreenUtil.initFullScreen(this);
        setContentView(R.layout.activity_video_list);
        commonTitleBar = findViewById(R.id.common_title_bar);
        commonTitleBar.setBackText("视频库");
        listView = findViewById(R.id.lv_videolist);
        hcSdkManager = HCSdkManager.getNormalHCSdkManager(this);
        ArrayList<FileInfo> videoList = (ArrayList<FileInfo>) hcSdkManager.findFile();
        videoAdapter = new VideoAdapter(videoList, this);
        listView.setAdapter(videoAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    public static void go2VideoList(Activity activity) {
        Intent intent = new Intent(activity, VideoListActivity.class);
        activity.startActivity(intent);
    }
}
