package com.hitqz.robot.watchtower.gallery;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.hitqz.robot.commonlib.util.FullScreenUtil;
import com.hitqz.robot.watchtower.DonghuoRecordManager;
import com.hitqz.robot.watchtower.R;
import com.hitqz.robot.watchtower.bean.DonghuoRecord;

import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    ListView listView;
    TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullScreenUtil.initFullScreen(this);
        setContentView(R.layout.activity_gallery);
        tvEmpty = findViewById(R.id.tv_empty_record);

        DonghuoRecordManager.getInstance().initRecords();
        List<DonghuoRecord> donghuoRecords = DonghuoRecordManager.getInstance().getDonghuoRecords();
        if (donghuoRecords == null || donghuoRecords.size() == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }

        listView = findViewById(R.id.lv_records);
        DonghuoRecordAdapter donghuoRecordAdapter = new DonghuoRecordAdapter(donghuoRecords, this);
        listView.setAdapter(donghuoRecordAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                VideoListActivity.go2VideoList(GalleryActivity.this, donghuoRecords.get(position));
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
