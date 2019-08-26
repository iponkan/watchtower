package com.hitqz.robot.watchtower.gallery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.hitqz.robot.watchtower.DonghuoRecordManager;
import com.hitqz.robot.watchtower.HCSdk;
import com.hitqz.robot.watchtower.HCSdkManager;
import com.hitqz.robot.watchtower.R;
import com.hitqz.robot.watchtower.bean.DonghuoRecord;
import com.hitqz.robot.watchtower.bean.FileInfo;
import com.hitqz.robot.watchtower.bean.TimeRange;
import com.hitqz.robot.watchtower.bean.TimeStruct;
import com.orhanobut.logger.Logger;
import com.sonicers.commonlib.component.BaseActivity;
import com.sonicers.commonlib.rx.RxSchedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class GalleryActivity extends BaseActivity implements CalendarView.OnDateChangeListener
        , View.OnClickListener {

    @BindView(R.id.lv_records)
    ListView listView;
    @BindView(R.id.tv_empty_record)
    TextView tvEmpty;
    @BindView(R.id.rl_select_date)
    View searchView;
    @BindView(R.id.tv_select_date)
    TextView tvSelectDate;
    HCSdk hcSdk;
    List<DonghuoRecord> donghuoRecords = new ArrayList<>();
    List<DonghuoRecord> showList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i("----------GalleryActivity--------");
        setContentView(R.layout.activity_gallery);
        ButterKnife.bind(this);
        hcSdk = HCSdkManager.getNormalHCSdk(this);
        initDonghuaRecords();
        searchView.setOnClickListener(this);
    }

    private String getSelectDate(TimeStruct timeStruct) {
        return timeStruct.dwYear + "." + timeStruct.dwMonth + "." + timeStruct.dwDay;
    }

    @Override
    public void onClick(View v) {
        if (v == searchView) {
            if (calendarPopWindow == null) {
                calendarPopWindow = new CalendarPopWindow(GalleryActivity.this, GalleryActivity.this);
                if (!donghuoRecords.isEmpty()) {
                    long start = donghuoRecords.get(0).struStartTime.toMillSeconds();
                    long stop = donghuoRecords.get(donghuoRecords.size() - 1).struStartTime.toMillSeconds();
                    calendarPopWindow.setRange(start, stop);
                }
            }
            calendarPopWindow.showPopupWindow(searchView, timeRange.struStartTime.toMillSeconds());
        }
    }

    private void initDonghuaRecords() {
        Observable.create((ObservableOnSubscribe<List<DonghuoRecord>>) emitter -> {
            ArrayList<FileInfo> fileInfos = (ArrayList<FileInfo>) hcSdk.findFile(TimeStruct.farPast(), TimeStruct.today());
            if (fileInfos != null && fileInfos.size() > 0) {
                FileInfo fileInfo = fileInfos.get(0);
                long time = fileInfo.startTime.toMillSeconds();// SD卡存储的视频最早时间
//                    DonghuoRecordManager.getInstance().removeTimePointBefore(time);
                donghuoRecords = DonghuoRecordManager.getInstance().getDonghuoRecords();
                emitter.onNext(donghuoRecords);
            }
        }).compose(RxSchedulers.io_main()).subscribe(new Observer<List<DonghuoRecord>>() {

            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(List<DonghuoRecord> donghuoRecords) {
                if (donghuoRecords == null || donghuoRecords.size() == 0) {
                    showEmpty("暂时还没有动火记录哦");
                    tvSelectDate.setText(getSelectDate(TimeStruct.today()));
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    DonghuoRecord newest = donghuoRecords.get(donghuoRecords.size() - 1);

                    timeRange = TimeRange.getDayTimeRange(newest.struStartTime.dwYear, newest.struStartTime.dwMonth, newest.struStartTime.dwDay, 0, 0, 0);
                    filterDonghuoRecord();
                    tvSelectDate.setText(getSelectDate(newest.struStartTime));
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    CalendarPopWindow calendarPopWindow;

    Handler handler = new Handler();

    TimeRange timeRange = new TimeRange();

    @Override
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {

        handler.postDelayed(() -> calendarPopWindow.dismiss(), 300);
//        ToastUtil.showToastShort(this, "" + year + " " + (month + 1) + " " + dayOfMonth);

        timeRange = TimeRange.getDayTimeRange(year, month + 1, dayOfMonth, 0, 0, 0);

        tvSelectDate.setText(year + "." + (month + 1) + "." + dayOfMonth);

        filterDonghuoRecord();
    }

    private void filterDonghuoRecord() {
        showList.clear();
        for (int i = 0; i < donghuoRecords.size(); i++) {
            DonghuoRecord donghuoRecord = donghuoRecords.get(i);
            if (donghuoRecord.struStartTime.toMillSeconds() >= timeRange.struStartTime.toMillSeconds()
                    && donghuoRecord.struStartTime.toMillSeconds() < timeRange.struStopTime.toMillSeconds()) {
                showList.add(donghuoRecord);
            }
            Collections.reverse(showList);
        }
        if (showList.isEmpty()) {
            showEmpty("该日期下没有动火记录哦");
        }
        DonghuoRecordAdapter donghuoRecordAdapter = new DonghuoRecordAdapter(showList, GalleryActivity.this);
        listView.setAdapter(donghuoRecordAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> VideoListActivity.go2VideoList(GalleryActivity.this, showList.get(position)));
    }

    public static void go2Gallery(Activity activity) {
        Intent intent = new Intent(activity, GalleryActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    private void showEmpty(String msg) {
        tvEmpty.setText(msg);
        tvEmpty.setVisibility(View.VISIBLE);
    }
}
