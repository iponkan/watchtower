package com.hitqz.robot.watchtower.player;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hitqz.robot.commonlib.util.FullScreenUtil;
import com.hitqz.robot.watchtower.HCSdkManager;
import com.hitqz.robot.watchtower.R;

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

    private String filePath;

    public static void go2Player(Activity context, String filePath) {
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra(EXTRA_PATH, filePath);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palyer);

        filePath = getIntent().getStringExtra(EXTRA_PATH);
        Log.i(TAG, "播放文件名：" + filePath);
        surfaceView = findViewById(R.id.sv_player);
        hcSdkManager = HCSdkManager.getInstance(this);
        hcSdkManager.setSurfaceView(surfaceView);
        hcSdkManager.playBack(filePath);
        hcSdkManager.addPlayerCallBack(this);

        ivPlay = findViewById(R.id.iv_play);

        ivPlay.setOnClickListener(this);

        surfaceView.setOnClickListener(this);

        tvCurrent = findViewById(R.id.tv_current_time);
        tvDuration = findViewById(R.id.tv_duration_time);
        sbTime = findViewById(R.id.sb_time);
        sbTime.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            FullScreenUtil.initFullScreen(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, TAG + " onDestroy");
        if (hcSdkManager != null) {
            hcSdkManager.stopPlayback();
            hcSdkManager.removePlayerCallBack(this);
        }
    }

    @Override
    public void onPlayStart() {
        ivPlay.setVisibility(View.GONE);
    }

    @Override
    public void onPlayPause() {
        ivPlay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPlayStop() {
        ivPlay.setVisibility(View.VISIBLE);
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
                ivPlay.setImageResource(R.drawable.bt_pause);
                ivPlay.setVisibility(View.VISIBLE);
                fadeOutPlayButton();
            } else if (hcSdkManager.isPause()) {
                ivPlay.setImageResource(R.drawable.bt_play);
                ivPlay.setVisibility(View.VISIBLE);
                fadeOutPlayButton();
            }
        }
    }

    private Handler handler = new Handler();

    private void fadeOutPlayButton() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ivPlay.setVisibility(View.GONE);
            }
        }, 1000);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
