package com.hitqz.robot.watchtower.player;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.blankj.utilcode.util.ScreenUtils;
import com.github.loadingview.LoadingView;
import com.hitqz.robot.commonlib.util.FullScreenUtil;
import com.hitqz.robot.watchtower.HCSdk;
import com.hitqz.robot.watchtower.HCSdkManager;
import com.hitqz.robot.watchtower.R;
import com.hitqz.robot.watchtower.bean.FileInfo;
import com.hitqz.robot.watchtower.widget.CommonTitleBar;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.hitqz.robot.commonlib.util.TimeUtil.formatTimeS;

public class PlayerActivity extends AppCompatActivity implements PlayerCallback, View.OnClickListener
        , SeekBar.OnSeekBarChangeListener {

    public static final String TAG = "PlayerActivity";

    public static final String EXTRA_PATH = "EXTRA_PATH";

    @BindView(R.id.sv_player)
    SurfaceView surfaceView;
    @BindView(R.id.iv_play)
    ImageView ivPlay;
    @BindView(R.id.sb_time)
    SeekBar sbTime;
    @BindView(R.id.tv_current_time)
    TextView tvCurrent;
    @BindView(R.id.tv_duration_time)
    TextView tvDuration;
    @BindView(R.id.loading_view)
    LoadingView loadingView;
    @BindView(R.id.player_mask)
    View playerMask;
    @BindView(R.id.common_title_bar)
    CommonTitleBar commonTitleBar;
    @BindView(R.id.rl_player)
    ViewGroup playLayout;

    HCSdk hcSdk;
    FileInfo fileInfo;
    int duration;


    public static void go2Player(Activity context, FileInfo fileInfo) {
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra(EXTRA_PATH, fileInfo);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullScreenUtil.initFullScreen(this);
        setContentView(R.layout.activity_palyer);
        ButterKnife.bind(this);
        fileInfo = getIntent().getParcelableExtra(EXTRA_PATH);
        Logger.i("播放文件名：" + fileInfo.startTime.toString() + "～" + fileInfo.stopTime.toString());
        commonTitleBar.setTitle(fileInfo.startTime.toString() + "～" + fileInfo.stopTime.toString());

        resetView(playLayout);

        hcSdk = HCSdkManager.getNormalHCSdk(this);
        hcSdk.setSurfaceView(surfaceView);
        duration = hcSdk.getPlaybackDuration(fileInfo);
        ivPlay.setOnClickListener(this);
        surfaceView.setOnClickListener(this);
        tvDuration.setText(formatTimeS(duration));
        sbTime.setOnSeekBarChangeListener(this);
        sbTime.setMax(duration);

        progressHandler = new ProgressHandler();
    }

    private void resetView(View... views) {


        int width = ScreenUtils.getScreenWidth();
        int height = (int) (width / 16f * 9);

        for (View view : views) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.width = width;
            layoutParams.height = height;

            view.setLayoutParams(layoutParams);
        }
    }

    ProgressHandler progressHandler;

    @Override
    protected void onResume() {
        super.onResume();
        if (hcSdk != null) {
            hcSdk.playBack(fileInfo);
            hcSdk.addPlayerCallBack(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (hcSdk != null) {
            hcSdk.stopPlayback();
            hcSdk.removePlayerCallBack(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressHandler != null) {
            progressHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onPlayStart() {
        ivPlay.setVisibility(View.GONE);
        progressHandler.sendEmptyMessage(UPDATE);
    }

    @Override
    public void onPlayPause() {
        ivPlay.setVisibility(View.VISIBLE);
        progressHandler.removeMessages(UPDATE);
    }

    @Override
    public void onPlayStop() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ivPlay.setImageResource(R.drawable.icon_play);
                ivPlay.setVisibility(View.VISIBLE);
                progressHandler.removeMessages(UPDATE);
            }
        });

    }

    @Override
    public void onPlaying(int progress) {

    }

    @Override
    public void onSeekComplete() {
        progressHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressHandler.sendEmptyMessage(UPDATE);
                dismissDialog();
            }
        }, 1500);
    }

    @Override
    public void onClick(View v) {
        if (ivPlay == v) {
            if (hcSdk.isPlaying()) {
                hcSdk.pausePlayBack();
            } else if (hcSdk.isPause()) {
                hcSdk.resumePlayBack();
            } else if (hcSdk.isStop()) {
                hcSdk.playBack(fileInfo);
            }
        } else if (surfaceView == v) {
            if (hcSdk.isPlaying()) {
                ivPlay.setImageResource(R.drawable.icon_pause);
                ivPlay.setVisibility(View.VISIBLE);
                fadeOutPlayButton();
            } else if (hcSdk.isPause()) {
                ivPlay.setImageResource(R.drawable.icon_play);
                ivPlay.setVisibility(View.VISIBLE);
                fadeOutPlayButton();
            }
        }
    }

    private void fadeOutPlayButton() {
        progressHandler.removeMessages(FADE_OUT_BUTTON);
        progressHandler.sendEmptyMessageDelayed(FADE_OUT_BUTTON, 2000);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        hcSdk.playbackSeekTo(progress / (duration * 1.0f));
        tvCurrent.setText(formatTimeS(progress));
        progressHandler.removeMessages(UPDATE);
        showDialog();
    }

    public static final int UPDATE = 0x01;
    public static final int FADE_OUT_BUTTON = 0x02;

    class ProgressHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE:
                    int current = hcSdk.getPlayBackTime();
                    if (hcSdk.isEnd()) {
                        hcSdk.stopPlayback();
                    }
                    sbTime.setProgress(current);
                    tvCurrent.setText(formatTimeS(current));
                    progressHandler.sendEmptyMessageDelayed(UPDATE, 1000);
                    break;
                case FADE_OUT_BUTTON:
                    ivPlay.setVisibility(View.GONE);
                    break;
            }
        }
    }

    private void showDialog() {
        playerMask.setVisibility(View.VISIBLE);
        loadingView.start();
    }

    private void dismissDialog() {
        playerMask.setVisibility(View.GONE);
        loadingView.stop();
    }

}
