package com.hitqz.robot.watchtower.gallery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ListView;

import com.hitqz.robot.commonlib.util.FullScreenUtil;
import com.hitqz.robot.commonlib.util.ToastUtils;
import com.hitqz.robot.watchtower.HCSdkManager;
import com.hitqz.robot.watchtower.R;
import com.hitqz.robot.watchtower.bean.FileInfo;
import com.hitqz.robot.watchtower.widget.CommonTitleBar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class VideoListActivity extends AppCompatActivity implements CalendarView.OnDateChangeListener {

    private CommonTitleBar commonTitleBar;
    private ListView listView;
    VideoAdapter videoAdapter;
    HCSdkManager hcSdkManager;
    CalendarPopWindow calendarPopWindow;
    View serchView;

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
        serchView = findViewById(R.id.rl_select_date);
        serchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (calendarPopWindow == null) {
                    calendarPopWindow = new CalendarPopWindow(VideoListActivity.this, VideoListActivity.this);
                    calendarPopWindow.showPopupWindow(serchView);
                } else {
                    calendarPopWindow.showPopupWindow(serchView);
                }
            }
        });
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

    private static final DateFormat FORMATTER = SimpleDateFormat.getDateInstance();

    @Override
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
        ToastUtils.showToastShort(this, FORMATTER.format(view.getDate()));
    }
}
