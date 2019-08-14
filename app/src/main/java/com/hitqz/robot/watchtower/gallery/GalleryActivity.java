package com.hitqz.robot.watchtower.gallery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.sonicers.commonlib.component.BaseActivity;
import com.hitqz.robot.watchtower.DonghuoRecordManager;
import com.hitqz.robot.watchtower.R;
import com.hitqz.robot.watchtower.bean.DonghuoRecord;
import com.orhanobut.logger.Logger;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GalleryActivity extends BaseActivity {

    @BindView(R.id.lv_records)
    ListView listView;
    @BindView(R.id.tv_empty_record)
    TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i("----------GalleryActivity--------");
        setContentView(R.layout.activity_gallery);
        ButterKnife.bind(this);

        List<DonghuoRecord> donghuoRecords = DonghuoRecordManager.getInstance().getDonghuoRecords();
        if (donghuoRecords == null || donghuoRecords.size() == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }

        DonghuoRecordAdapter donghuoRecordAdapter = new DonghuoRecordAdapter(donghuoRecords, this);
        listView.setAdapter(donghuoRecordAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VideoListActivity.go2VideoList(GalleryActivity.this, donghuoRecords.get(position));
            }
        });
    }

    public static void go2Gallery(Activity activity) {
        Intent intent = new Intent(activity, GalleryActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

}
