package com.hitqz.robot.watchtower.gallery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CalendarView;
import android.widget.ListView;

import com.github.loadingview.LoadingView;
import com.hitqz.robot.commonlib.util.FullScreenUtil;
import com.hitqz.robot.commonlib.util.ToastUtils;
import com.hitqz.robot.watchtower.HCSdkManager;
import com.hitqz.robot.watchtower.R;
import com.hitqz.robot.watchtower.bean.DonghuoRecord;
import com.hitqz.robot.watchtower.bean.FileInfo;
import com.hitqz.robot.watchtower.player.PlayerActivity;
import com.hitqz.robot.watchtower.widget.CommonTitleBar;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class VideoListActivity extends AppCompatActivity implements CalendarView.OnDateChangeListener {

    private CommonTitleBar commonTitleBar;
    private ListView listView;
    VideoAdapter videoAdapter;
    HCSdkManager hcSdkManager;
    CalendarPopWindow calendarPopWindow;
    View serchView;
    LoadingView loadingView;
    ArrayList<FileInfo> videoList;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullScreenUtil.initFullScreen(this);
        setContentView(R.layout.activity_video_list);
        commonTitleBar = findViewById(R.id.common_title_bar);
        String name = getIntent().getStringExtra(EXTRA_NAME);
        commonTitleBar.setTitle(name);
        listView = findViewById(R.id.lv_videolist);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlayerActivity.go2Player(VideoListActivity.this, videoList.get(position));
            }
        });
        loadingView = findViewById(R.id.loadingView);
        hcSdkManager = HCSdkManager.getNormalHCSdkManager(this);
        Observable.create(new ObservableOnSubscribe<ArrayList<FileInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<FileInfo>> emitter) throws Exception {
                videoList = (ArrayList<FileInfo>) hcSdkManager.findFile();
                emitter.onNext(videoList);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArrayList<FileInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        showDialog();
                    }

                    @Override
                    public void onNext(ArrayList<FileInfo> fileInfos) {
                        dismissDialog();
                        videoAdapter = new VideoAdapter(fileInfos, VideoListActivity.this);
                        listView.setAdapter(videoAdapter);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });


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

    public static final String EXTRA_NAME = "EXTRA_NAME";

    public static void go2VideoList(Activity activity, String donghuoRecord) {
        Intent intent = new Intent(activity, VideoListActivity.class);
        intent.putExtra(EXTRA_NAME, donghuoRecord);
        activity.startActivity(intent);
    }

    @Override
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
        ToastUtils.showToastShort(this, "" + year + " " + month + " " + dayOfMonth);
    }

    private void showDialog() {
        loadingView.start();
    }

    private void dismissDialog() {
        loadingView.stop();
    }
}
