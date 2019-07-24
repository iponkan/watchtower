package com.hitqz.robot.watchtower.gallery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.loadingview.LoadingView;
import com.hitqz.robot.commonlib.util.FullScreenUtil;
import com.hitqz.robot.watchtower.HCSdkManager;
import com.hitqz.robot.watchtower.R;
import com.hitqz.robot.watchtower.bean.DonghuoRecord;
import com.hitqz.robot.watchtower.bean.FileInfo;
import com.hitqz.robot.watchtower.bean.TimeRange;
import com.hitqz.robot.watchtower.bean.TimeStruct;
import com.hitqz.robot.watchtower.player.PlayerActivity;
import com.hitqz.robot.watchtower.widget.CommonTitleBar;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@SuppressLint("CheckResult")
public class VideoListActivity extends AppCompatActivity implements CalendarView.OnDateChangeListener
        , AdapterView.OnItemClickListener, View.OnClickListener {

    public static final String EXTRA_NAME = "EXTRA_NAME";

    @BindView(R.id.common_title_bar)
    CommonTitleBar commonTitleBar;
    @BindView(R.id.lv_videolist)
    ListView listView;
    @BindView(R.id.rl_select_date)
    View searchView;
    @BindView(R.id.tv_select_date)
    TextView tvSelectDate;
    @BindView(R.id.loading_view)
    LoadingView loadingView;
    @BindView(R.id.tv_empty_video)
    TextView tvEmpty;


    VideoAdapter videoAdapter;
    HCSdkManager hcSdkManager;
    CalendarPopWindow calendarPopWindow;

    ArrayList<FileInfo> videoList;

    DonghuoRecord donghuoRecord;
    Handler handler = new Handler();
    TimeRange dayTimeRange;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullScreenUtil.initFullScreen(this);
        setContentView(R.layout.activity_video_list);
        ButterKnife.bind(this);

        donghuoRecord = getIntent().getParcelableExtra(EXTRA_NAME);
        // debug
        donghuoRecord = DonghuoRecord.getHH();

        dayTimeRange = TimeRange.getDayTimeRange(donghuoRecord.struStartTime);
        commonTitleBar.setTitle(donghuoRecord.toString());
        listView.setOnItemClickListener(this);
        hcSdkManager = HCSdkManager.getNormalHCSdkManager(this);
        findFile();
        searchView.setOnClickListener(this);
        tvSelectDate.setText(getSelectDate(donghuoRecord.struStartTime));
    }

    @Override
    public void onClick(View v) {
        if (v == searchView) {
            if (calendarPopWindow == null) {
                calendarPopWindow = new CalendarPopWindow(VideoListActivity.this, VideoListActivity.this);
                calendarPopWindow.setRange(donghuoRecord.struStartTime.toMillSeconds(), donghuoRecord.struStopTime.toMillSeconds());
            }
            calendarPopWindow.showPopupWindow(searchView, dayTimeRange.struStartTime.toMillSeconds());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PlayerActivity.go2Player(VideoListActivity.this, videoList.get(position));
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }


    public static void go2VideoList(Activity activity, DonghuoRecord donghuoRecord) {
        Intent intent = new Intent(activity, VideoListActivity.class);
        intent.putExtra(EXTRA_NAME, donghuoRecord);
        activity.startActivity(intent);
    }

    @Override
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                calendarPopWindow.dismiss();

            }
        }, 300);
//        ToastUtils.showToastShort(this, "" + year + " " + (month + 1) + " " + dayOfMonth);

        dayTimeRange = TimeRange.getDayTimeRange(year, month + 1, dayOfMonth, 0, 0, 0);

        tvSelectDate.setText(year + "." + (month + 1) + "." + dayOfMonth);

        findFile();

    }

    private void showDialog() {
        loadingView.start();
    }

    private void dismissDialog() {
        loadingView.stop();
    }

    private String getSelectDate(TimeStruct timeStruct) {
        return timeStruct.dwYear + "." + timeStruct.dwMonth + "." + timeStruct.dwDay;
    }

    private void findFile() {
        Observable.create(new ObservableOnSubscribe<ArrayList<FileInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<FileInfo>> emitter) throws Exception {
                videoList = (ArrayList<FileInfo>) hcSdkManager.findFile(dayTimeRange.struStartTime, dayTimeRange.struStopTime);
                emitter.onNext(videoList);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArrayList<FileInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (videoList != null) {
                            videoList.clear();
                            videoAdapter.notifyDataSetChanged();
                            tvEmpty.setVisibility(View.GONE);
                        }
                        showDialog();
                    }

                    @Override
                    public void onNext(ArrayList<FileInfo> fileInfos) {
                        dismissDialog();

                        if (fileInfos == null || fileInfos.size() == 0) {
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
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
    }
}
