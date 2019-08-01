package com.hitqz.robot.watchtower;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.hitqz.robot.commonlib.widget.LoadingDialog;

public class BaseActivity extends AppCompatActivity {

    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        FullScreenUtil.initFullScreen(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    public void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog.Companion.get(this);
        }
        loadingDialog.show();
    }

    public void dismissLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog.hide();
        }
    }
}
