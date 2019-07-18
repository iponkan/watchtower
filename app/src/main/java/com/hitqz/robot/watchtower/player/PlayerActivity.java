package com.hitqz.robot.watchtower.player;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.blankj.utilcode.util.ScreenUtils;
import com.hitqz.robot.commonlib.util.FullScreenUtil;
import com.hitqz.robot.commonlib.util.ToastUtils;
import com.hitqz.robot.watchtower.HCSdkManager;
import com.hitqz.robot.watchtower.R;
import com.hitqz.robot.watchtower.bean.FileInfo;
import com.hitqz.robot.watchtower.widget.CommonTitleBar;

import static com.hitqz.robot.watchtower.util.TimeUtil.formatTimeS;

public class PlayerActivity extends AppCompatActivity implements PlayerCallback, View.OnClickListener
        , SeekBar.OnSeekBarChangeListener {

    public static final String TAG = "PlayerActivity";

    public static final String EXTRA_PATH = "EXTRA_PATH";

    private SurfaceView surfaceView;
    private HCSdkManager hcSdkManager;
    private ImageView ivPlay;
    private SeekBar sbTime;
    private TextView tvCurrent;
    private TextView tvDuration;

    private FileInfo fileInfo;
    private int duration;

    CommonTitleBar commonTitleBar;

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
        commonTitleBar = findViewById(R.id.player_ctb);

        fileInfo = getIntent().getParcelableExtra(EXTRA_PATH);
        Log.i(TAG, "播放文件名：" + fileInfo);
        commonTitleBar.setTitle(fileInfo.startTime.toString() + "～" + fileInfo.stopTime.toString());
        surfaceView = findViewById(R.id.sv_player);

        ViewGroup playLayout = findViewById(R.id.rl_player);
        int width = ScreenUtils.getScreenWidth();
        int height = (int) ((width / 16f) * 9);
        ViewGroup.LayoutParams layoutParams = playLayout.getLayoutParams();
        layoutParams.height = height;
        playLayout.setLayoutParams(layoutParams);
        hcSdkManager = HCSdkManager.getNormalHCSdkManager(this);
        hcSdkManager.setSurfaceView(surfaceView);
        duration = hcSdkManager.getPlaybackDuration(fileInfo);
        ivPlay = findViewById(R.id.iv_play);

        ivPlay.setOnClickListener(this);

        surfaceView.setOnClickListener(this);

        tvCurrent = findViewById(R.id.tv_current_time);
        tvDuration = findViewById(R.id.tv_duration_time);
        tvDuration.setText(formatTimeS(duration));
        sbTime = findViewById(R.id.sb_time);
        sbTime.setOnSeekBarChangeListener(this);
        sbTime.setMax(duration);

        progressHandler = new ProgressHandler();
    }

    ProgressHandler progressHandler;

    @Override
    protected void onResume() {
        super.onResume();
        if (hcSdkManager != null) {
            hcSdkManager.playBack(fileInfo);
            hcSdkManager.addPlayerCallBack(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (hcSdkManager != null) {
            hcSdkManager.stopPlayback();
            hcSdkManager.removePlayerCallBack(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, TAG + " onDestroy");
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
                ToastUtils.showToastShort(PlayerActivity.this, "有画面拉");
            }
        });

    }

    @Override
    public void onPlaying(int progress) {

    }

    @Override
    public void onClick(View v) {
        if (ivPlay == v) {
            if (hcSdkManager.isPlaying()) {
                hcSdkManager.pausePlayBack();
            } else {
                hcSdkManager.resumePlayBack();
            }
        } else if (surfaceView == v) {
            if (hcSdkManager.isPlaying()) {
                ivPlay.setImageResource(R.drawable.icon_pause);
                ivPlay.setVisibility(View.VISIBLE);
                fadeOutPlayButton();
            } else if (hcSdkManager.isPause()) {
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

    private int seekProgress = -1;

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        hcSdkManager.playbackSeekTo(seekBar.getProgress() / (duration * 1.0f));
        seekProgress = seekBar.getProgress();
        progressHandler.removeMessages(UPDATE);
        progressHandler.sendEmptyMessage(SEEKING);
    }

    public static final int UPDATE = 0x01;
    public static final int SEEKING = 0x02;
    public static final int FADE_OUT_BUTTON = 0x03;

    class ProgressHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE:
                    int current = hcSdkManager.getPlayBackTime();
                    Log.d(TAG, "seekProgress;" + seekProgress);
                    Log.d(TAG, "current;" + current);
                    if (seekProgress != -1) {
                        if (Math.abs(current - seekProgress) < 100) {
                            ToastUtils.showToastShort(PlayerActivity.this, "有画面拉");
                            seekProgress = -1;
                        }
                    }
                    sbTime.setProgress(current);
                    tvCurrent.setText(formatTimeS(current));
                    progressHandler.sendEmptyMessageDelayed(UPDATE, 1000);
                    break;
                case SEEKING:
                    int playBackTime = hcSdkManager.getPlayBackTime();
                    Log.d(TAG, "seekProgress;" + seekProgress);
                    Log.d(TAG, "current;" + playBackTime);
                    if (seekProgress != -1) {
                        if (Math.abs(playBackTime - seekProgress) < 100) {
                            ToastUtils.showToastShort(PlayerActivity.this, "有画面拉");
                            seekProgress = -1;
                            progressHandler.sendEmptyMessageDelayed(UPDATE, 1000);
                        } else {
                            progressHandler.sendEmptyMessageDelayed(SEEKING, 1000);
                        }
                    }
                    break;
                case FADE_OUT_BUTTON:
                    ivPlay.setVisibility(View.GONE);
                    break;
            }
        }
    }

}
