package com.hitqz.robot.watchtower.camera;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.hitqz.robot.commonlib.util.FullScreenUtil;
import com.hitqz.robot.commonlib.util.ToastUtils;

import com.hitqz.robot.commonlib.view.SteerView;
import com.hitqz.robot.watchtower.HCSdkManager;
import com.hitqz.robot.watchtower.R;
import com.hitqz.robot.watchtower.widget.CommonTitleBar;


public class CameraActivity extends AppCompatActivity {

    private CommonTitleBar commonTitleBar;

    public void circlePressed(View view) {
        SteerView azimuthCircle = (SteerView) view;
        if (azimuthCircle.getPressDirection() == SteerView.LEFT_PRESS) {
            Toast.makeText(this, "LEFT_PRESS", Toast.LENGTH_SHORT).show();
        } else if (azimuthCircle.getPressDirection() == SteerView.TOP_PRESS) {
            Toast.makeText(this, "TOP_PRESS", Toast.LENGTH_SHORT).show();
        } else if (azimuthCircle.getPressDirection() == SteerView.RIGHT_PRESS) {
            Toast.makeText(this, "RIGHT_PRESS", Toast.LENGTH_SHORT).show();
        } else if (azimuthCircle.getPressDirection() == SteerView.BOTTOM_PRESS) {
            Toast.makeText(this, "BOTTOM_PRESS", Toast.LENGTH_SHORT).show();
        }
    }

    private Button m_oTestBtn = null;
    private SurfaceView hotSurfaceView = null;
    private SurfaceView normalSurfaceView;
    private ProductionView productionView;


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            FullScreenUtil.initFullScreen(this);
        }
        super.onWindowFocusChanged(hasFocus);
    }

    private HCSdkManager hotHCSdkManager;
    private HCSdkManager normalHCSdkManager;
    ProductionManager productionManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        hotHCSdkManager = HCSdkManager.getHotHCSdkManager(this);
        if (!hotHCSdkManager.isInit()) {
            ToastUtils.showToastShort(this, "热成像摄像头Sdk未初始化");
            return;
        }

        normalHCSdkManager = HCSdkManager.getNormalHCSdkManager(this);
        if (!normalHCSdkManager.isInit()) {
            ToastUtils.showToastShort(this, "高清摄像头Sdk未初始化");
            return;
        }

        m_oTestBtn = findViewById(R.id.btn_Test);
        hotSurfaceView = findViewById(R.id.sv_hot_camera);
        productionView = findViewById(R.id.pv_camera);
        normalSurfaceView = findViewById(R.id.sv_normal_camera);

        commonTitleBar = findViewById(R.id.common_title_bar);
        commonTitleBar.setBackText("相机设置");

        resetSize(hotSurfaceView, productionView, normalSurfaceView);

        hotHCSdkManager.setSurfaceView(hotSurfaceView);
        hotHCSdkManager.startSinglePreview();
        normalHCSdkManager.setSurfaceView(normalSurfaceView);
        normalHCSdkManager.startSinglePreview();

        m_oTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                List<FileInfo> fileList = hotHCSdkManager.findFile();
//                if (fileList != null && fileList.size() > 0) {
//                    hotHCSdkManager.Test_GetFileByName(fileList.get(0).fileName);
//                    ToastUtils.showToastShort(CameraActivity.this, "下载完成");
//                }
            }
        });

    }

    private void resetSize(View... views) {

        int margin = SizeUtils.dp2px(20);

        int width = ScreenUtils.getScreenWidth() - 2 * margin;
        int height = (int) (width / 16f * 9);

        for (View view : views) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.width = width;
            layoutParams.height = height;
            layoutParams.leftMargin = margin;
            layoutParams.rightMargin = margin;
            layoutParams.topMargin = margin;

            view.setLayoutParams(layoutParams);
        }


        float centerWidth = getResources().getDimension(R.dimen.fr_center_width);
        productionManager = new ProductionManager(width, height, centerWidth);

        productionView.setParentSize(width, height);
        productionView.setProductionManager(productionManager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hotHCSdkManager.setSurfaceView(hotSurfaceView);
        normalHCSdkManager.setSurfaceView(normalSurfaceView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hotHCSdkManager != null) {
            hotHCSdkManager.stopSinglePreview();
            hotHCSdkManager.stopPlayback();
        }

        if (normalHCSdkManager != null) {
            normalHCSdkManager.stopSinglePreview();
            normalHCSdkManager.stopPlayback();
        }
    }
}
