package com.hitqz.robot.watchtower.gallery;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hitqz.robot.commonlib.util.FullScreenUtil;
import com.hitqz.robot.watchtower.DonghuoRecordManager;
import com.hitqz.robot.watchtower.R;
import com.hitqz.robot.watchtower.bean.DonghuoRecord;
import com.hitqz.robot.watchtower.widget.CommonTitleBar;

import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private CommonTitleBar commonTitleBar;

    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullScreenUtil.initFullScreen(this);
        setContentView(R.layout.activity_gallery);
        commonTitleBar = findViewById(R.id.common_title_bar);
        commonTitleBar.setBackText("视频库");

        DonghuoRecordManager.getInstance().initRecords();
        List<DonghuoRecord> donghuoRecords = DonghuoRecordManager.getInstance().getDonghuoRecords();


        listView = findViewById(R.id.lv_records);
        DonghuoRecordAdapter donghuoRecordAdapter = new DonghuoRecordAdapter(donghuoRecords, this);
        listView.setAdapter(donghuoRecordAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                VideoListActivity.go2VideoList(GalleryActivity.this);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }


    public static void go2Gallery(Activity activity) {
        Intent intent = new Intent(activity, GalleryActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

}
