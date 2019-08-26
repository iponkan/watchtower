package com.hitqz.robot.watchtower.gallery;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.hitqz.robot.watchtower.HCSdk;
import com.hitqz.robot.watchtower.HCSdkManager;
import com.hitqz.robot.watchtower.R;
import com.hitqz.robot.watchtower.bean.DonghuoRecord;
import com.hitqz.robot.watchtower.bean.FileInfo;
import com.hitqz.robot.watchtower.player.PlayerActivity;
import com.hitqz.robot.watchtower.widget.CommonTitleBar;
import com.sonicers.commonlib.component.BaseActivity;

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
public class VideoListActivity extends BaseActivity
        implements AdapterView.OnItemClickListener {

    public static final String EXTRA_NAME = "EXTRA_NAME";

    @BindView(R.id.common_title_bar)
    CommonTitleBar commonTitleBar;
    @BindView(R.id.lv_videolist)
    ListView listView;
    @BindView(R.id.tv_empty_video)
    TextView tvEmpty;

    VideoAdapter videoAdapter;
    HCSdk hcSdk;

    public static void go2VideoList(Activity activity, DonghuoRecord donghuoRecord) {
        Intent intent = new Intent(activity, VideoListActivity.class);
        intent.putExtra(EXTRA_NAME, donghuoRecord);
        activity.startActivity(intent);
    }

    DonghuoRecord donghuoRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        ButterKnife.bind(this);

        donghuoRecord = getIntent().getParcelableExtra(EXTRA_NAME);

        commonTitleBar.setTitle("动火记录:" + donghuoRecord.toString());
        listView.setOnItemClickListener(this);
        hcSdk = HCSdkManager.getNormalHCSdk(this);
        findFile();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PlayerActivity.go2Player(VideoListActivity.this, videoList.get(position));
    }

    ArrayList<FileInfo> videoList;

    private void findFile() {
        Observable.create(new ObservableOnSubscribe<ArrayList<FileInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<FileInfo>> emitter) throws Exception {
                videoList = (ArrayList<FileInfo>) hcSdk.findFile(donghuoRecord.struStartTime, donghuoRecord.struStopTime);
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
                        showLoadingDialog();
                    }

                    @Override
                    public void onNext(ArrayList<FileInfo> fileInfos) {
                        dismissLoadingDialog();

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
