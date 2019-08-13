package com.hitqz.robot.watchtower.widget;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.blankj.utilcode.util.SizeUtils;
import com.hitqz.robot.watchtower.R;
import com.sonicers.commonlib.util.SoundPoolHelper;

public class AlertDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String ALERT = "ALERT";
    ImageView ivOk;
    TextView tvAlert;
    String alertString;
    Handler handler = new Handler();
    SoundPoolHelper soundPoolHelper;

    // fix crash
    public AlertDialogFragment() {
        // doesn't do anything special
    }

    public AlertDialogFragment(String alertText) {
        super();
        alertString = alertText;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.MyDialog);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (soundPoolHelper != null) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    soundPoolHelper.play(ALERT);
                }
            }, 500);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (soundPoolHelper != null) {
            soundPoolHelper.release();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().setCanceledOnTouchOutside(true);
        View rootView = inflater.inflate(R.layout.layout_alert_dhp, container, false);
        ivOk = rootView.findViewById(R.id.iv_pop_confirm);
        tvAlert = rootView.findViewById(R.id.tv_alert_text);
        tvAlert.setText(alertString);
        ivOk.setOnClickListener(this);
        //Do something
        // 设置宽度为屏宽、靠近屏幕底部。
        final Window window = getDialog().getWindow();
        window.setBackgroundDrawableResource(R.color.transparent);
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.width = SizeUtils.dp2px(300);
        wlp.height = SizeUtils.dp2px(250);
        window.setAttributes(wlp);
        setCancelable(false);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        if (ivOk == v) {
            this.dismiss();
        }
    }

    public void setSoundId(Context context, int soundId) {
        if (soundPoolHelper == null) {
            soundPoolHelper = new SoundPoolHelper(context);
        }
        soundPoolHelper.load(ALERT, R.raw.alert);
    }

    public static void showDialog(AppCompatActivity activity, String alertText) {
        AlertDialogFragment dialog = new AlertDialogFragment(alertText);
        dialog.show(activity.getSupportFragmentManager(), "dhalert");
    }

    /**
     * 显示dialog并播放提示音
     *
     * @param resId 声音id
     */
    public static void showSoundDialog(AppCompatActivity activity, String alertText, int resId) {
        AlertDialogFragment dialog = new AlertDialogFragment(alertText);
        dialog.setSoundId(activity, resId);
        dialog.show(activity.getSupportFragmentManager(), "dhalert");
    }
}
