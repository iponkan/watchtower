package com.hitqz.robot.watchtower.debug;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.FileUtils;
import com.hitqz.robot.watchtower.util.PathUtil;
import com.sonicers.commonlib.R;
import com.sonicers.commonlib.component.BaseActivity;
import com.sonicers.commonlib.rx.RxSchedulers;
import com.sonicers.commonlib.util.UploadUtil;
import com.sonicers.commonlib.util.ZipUtils;

import java.util.Calendar;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class DebugActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "DebugActivity";

    private Button mBtnUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        mBtnUpload = findViewById(R.id.btn_upload);
        mBtnUpload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mBtnUpload == v) {
            String folser = PathUtil.getLogFolderPath();
            upload(folser);
        }
    }

    public static final String URL = "http://47.92.118.121:4000/files/uploads?folder=WatchTower";

    private void upload(String folser) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                String tempZipPath = PathUtil.getZipPath();
                ZipUtils.zip(folser, tempZipPath);
                if (FileUtils.isFileExists(tempZipPath)) {
                    int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
                    int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                    String filename = month + "-" + day + ".zip";
                    String result = UploadUtil.upload(URL, tempZipPath, filename).toString();
                    Log.d(TAG, "result:" + result);
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
                Log.e(TAG, "onNext : value : ");
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.e(TAG, "onError : value : " + e.getMessage() + "\n");
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete" + "\n");
            }
        });
    }

    public static void go2Debug(Activity activity) {
        Intent intent = new Intent(activity, DebugActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }
}
