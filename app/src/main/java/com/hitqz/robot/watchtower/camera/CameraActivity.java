package com.hitqz.robot.watchtower.camera;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.blankj.utilcode.util.ScreenUtils;
import com.hitqz.robot.commonlib.util.FullScreenUtil;
import com.hitqz.robot.commonlib.util.ToastUtils;
import com.hitqz.robot.commonlib.view.AzimuthCircle;

import com.hitqz.robot.watchtower.HCSdkManager;
import com.hitqz.robot.watchtower.R;


public class CameraActivity extends AppCompatActivity {

    public void circlePressed(View view) {
        AzimuthCircle azimuthCircle = (AzimuthCircle) view;
        if (azimuthCircle.getPressDirection() == AzimuthCircle.LEFT_PRESS) {
            Toast.makeText(this, "LEFT_PRESS", Toast.LENGTH_SHORT).show();
        } else if (azimuthCircle.getPressDirection() == AzimuthCircle.TOP_PRESS) {
            Toast.makeText(this, "TOP_PRESS", Toast.LENGTH_SHORT).show();
        } else if (azimuthCircle.getPressDirection() == AzimuthCircle.RIGHT_PRESS) {
            Toast.makeText(this, "RIGHT_PRESS", Toast.LENGTH_SHORT).show();
        } else if (azimuthCircle.getPressDirection() == AzimuthCircle.BOTTOM_PRESS) {
            Toast.makeText(this, "BOTTOM_PRESS", Toast.LENGTH_SHORT).show();
        }
    }

    public void okClick(View view) {
        Toast.makeText(this, "OK_PRESS", Toast.LENGTH_SHORT).show();
    }

    private Button m_oTestBtn = null;
    private SurfaceView m_osurfaceView = null;


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            FullScreenUtil.initFullScreen(this);
        }
        super.onWindowFocusChanged(hasFocus);
    }

    private HCSdkManager hcSdkManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        hcSdkManager = HCSdkManager.getInstance(this);
        if (!hcSdkManager.isInit()) {
            ToastUtils.showToastShort(this, "摄像头Sdk未初始化");
            finish();
            return;
        }

        m_oTestBtn = findViewById(R.id.btn_Test);
        m_osurfaceView = findViewById(R.id.Sur_Player);

        if (!hcSdkManager.isLogin()) {
            boolean result = hcSdkManager.login();
            if (!result) {
                ToastUtils.showToastShort(this, "登录失败");
            }
        }

        hcSdkManager.setSurfaceView(m_osurfaceView);

        hcSdkManager.startSinglePreview();

        m_oTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        hcSdkManager.setSurfaceView(m_osurfaceView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hcSdkManager != null) {
            hcSdkManager.stopSinglePreview();
            hcSdkManager.stopPlayback();
        }
    }
}
