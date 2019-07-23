package com.hitqz.robot.watchtower.camera;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.hitqz.robot.commonlib.util.FullScreenUtil;
import com.hitqz.robot.commonlib.util.ToastUtils;

import com.hitqz.robot.commonlib.view.SteerView;
import com.hitqz.robot.watchtower.DonghuoRecordManager;
import com.hitqz.robot.watchtower.HCSdkManager;
import com.hitqz.robot.watchtower.R;

import butterknife.BindView;


public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.iv_camera_minus)
    private ImageView ivMinus;
    @BindView(R.id.iv_camera_plus)
    private ImageView ivPlus;
    @BindView(R.id.iv_camera_focus)
    private ImageView ivFocus;
    @BindView(R.id.iv_camera_confirm)
    private ImageView ivConfirm;
    @BindView(R.id.sv_hot_camera)
    private SurfaceView hotSurfaceView;
    @BindView(R.id.sv_normal_camera)
    private SurfaceView normalSurfaceView;
    @BindView(R.id.pv_camera)
    private ProductionView productionView;

    private HCSdkManager hotHCSdkManager;
    private HCSdkManager normalHCSdkManager;
    ProductionManager productionManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullScreenUtil.initFullScreen(this);
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

        ivConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DonghuoRecordManager.getInstance().addTimePoint();
            }
        });

        resetHotCameraView(hotSurfaceView, productionView);
        resetNormalCameraView(normalSurfaceView);

        hotHCSdkManager.setSurfaceView(hotSurfaceView);
        normalHCSdkManager.setSurfaceView(normalSurfaceView);

        ivPlus.setOnClickListener(this);
        ivMinus.setOnClickListener(this);
        ivFocus.setOnClickListener(this);
    }

    private void resetHotCameraView(View... views) {

        int leftMargin = SizeUtils.dp2px(40);
        int rightMargin = SizeUtils.dp2px(40);
        int topMargin = SizeUtils.dp2px(10);

        int width = ScreenUtils.getScreenWidth() - leftMargin - rightMargin;
        int height = (int) (width / 16f * 9);

        for (View view : views) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.width = width;
            layoutParams.height = height;
            layoutParams.leftMargin = leftMargin;
            layoutParams.rightMargin = rightMargin;
            layoutParams.topMargin = topMargin;

            view.setLayoutParams(layoutParams);
        }


        float centerWidth = getResources().getDimension(R.dimen.fr_center_width);
        productionManager = new ProductionManager(width, height, centerWidth);

        productionView.setParentSize(width, height);
        productionView.setProductionManager(productionManager);
    }

    private void resetNormalCameraView(View normalCameraView) {

        int leftMargin = SizeUtils.dp2px(40);
        int rightMargin = SizeUtils.dp2px(40);
        int topMargin = SizeUtils.dp2px(10);

        int width = ScreenUtils.getScreenWidth() - leftMargin - rightMargin;
        int height = (int) (width / 16f * 9);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) normalCameraView.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        layoutParams.leftMargin = leftMargin;
        layoutParams.rightMargin = rightMargin;
        layoutParams.topMargin = topMargin;

        normalCameraView.setLayoutParams(layoutParams);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
        if (hotHCSdkManager != null) {
            hotHCSdkManager.stopSinglePreview();
        }

        if (normalHCSdkManager != null) {
            normalHCSdkManager.stopSinglePreview();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hotHCSdkManager != null) {
            hotHCSdkManager.startSinglePreview();
        }
        if (normalHCSdkManager != null) {
            normalHCSdkManager.startSinglePreview();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onClick(View v) {
        if (v == ivPlus) {
            productionView.zoomIn();
        } else if (v == ivMinus) {
            productionView.zoomOut();
        } else if (v == ivFocus) {
            //hotHCSdkManager.focusNear();
            // test
//                DonghuoRecordManager.getInstance().clearRecods();

//                normalHCSdkManager.testGetAbility();
            if (!normalHCSdkManager.recording) {
                normalHCSdkManager.startRecord();

            } else {
                normalHCSdkManager.stopRecord();

            }
        }
    }

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
}
