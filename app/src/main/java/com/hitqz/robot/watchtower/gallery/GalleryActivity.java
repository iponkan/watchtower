package com.hitqz.robot.watchtower.gallery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
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
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i("----------GalleryActivity--------");
        setContentView(R.layout.activity_gallery);
        ButterKnife.bind(this);
        hcSdk = HCSdkManager.getNormalHCSdk(this);
        initDonghuaRecords();
        searchView.setOnClickListener(this);
        tvSelectDate.setText(getSelectDate(donghuoRecord.struStartTime));
    }

    private String getSelectDate(TimeStruct timeStruct) {
        return timeStruct.dwYear + "." + timeStruct.dwMonth + "." + timeStruct.dwDay;
    }

    @Override
    public void onClick(View v) {
        if (v == searchView) {
            if (calendarPopWindow == null) {
                calendarPopWindow = new CalendarPopWindow(GalleryActivity.this, GalleryActivity.this);
                calendarPopWindow.setRange(donghuoRecord.struStartTime.toMillSeconds(), donghuoRecord.struStopTime.toMillSeconds());
            }
            calendarPopWindow.showPopupWindow(searchView, dayTimeRange.struStartTime.toMillSeconds());
        }
    }

    private void initDonghuaRecords() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                ArrayList<FileInfo> fileInfos = (ArrayList<FileInfo>) hcSdk.findFile(TimeStruct.farPast(), TimeStruct.today());
                if (fileInfos != null && fileInfos.size() > 0) {
                    FileInfo fileInfo = fileInfos.get(0);
                    long time = fileInfo.startTime.toMillSeconds();// SD卡存储的视频最早时间
//                    DonghuoRecordManager.getInstance().removeTimePointBefore(time);
                    List<DonghuoRecord> donghuoRecords = DonghuoRecordManager.getInstance().getDonghuoRecords();
                    if (donghuoRecords == null || donghuoRecords.size() == 0) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }
                    DonghuoRecordAdapter donghuoRecordAdapter = new DonghuoRecordAdapter(donghuoRecords, GalleryActivity.this);
                    listView.setAdapter(donghuoRecordAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            VideoListActivity.go2VideoList(GalleryActivity.this, donghuoRecords.get(position));
                        }
                    });
                }
            }
        }).compose(RxSchedulers.io_main()).subscribe(new Observer<Integer>() { // 第三步：订阅

            // 第二步：初始化Observer
            private int i;
            private Disposable mDisposable;

            @Override
            public void onSubscribe(@NonNull Disposable d) {
                mDisposable = d;
            }

            @Override
            public void onNext(@NonNull Integer integer) {

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

    DonghuoRecord donghuoRecord;
    Handler handler = new Handler();
    TimeRange dayTimeRange;

    @Override
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                calendarPopWindow.dismiss();
            }
        }, 300);
//        ToastUtil.showToastShort(this, "" + year + " " + (month + 1) + " " + dayOfMonth);

        dayTimeRange = TimeRange.getDayTimeRange(year, month + 1, dayOfMonth, 0, 0, 0);

        tvSelectDate.setText(year + "." + (month + 1) + "." + dayOfMonth);

        findFile();
    }

    private void findFile() {

    }

    public static void go2Gallery(Activity activity) {
        Intent intent = new Intent(activity, GalleryActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }
}
