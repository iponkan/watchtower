package com.hitqz.robot.watchtower.widget;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.blankj.utilcode.util.SizeUtils;
import com.hitqz.robot.watchtower.R;

public class DhpDialog extends DialogFragment implements View.OnClickListener {

    private ImageView ivOk;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.MyDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().setCanceledOnTouchOutside(true);
        View rootView = inflater.inflate(R.layout.layout_alert_dhp, container, false);
        ivOk = rootView.findViewById(R.id.iv_pop_confirm);
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


    public static void showDhpDialog(AppCompatActivity activity) {
        DhpDialog dialog = new DhpDialog();
        dialog.show(activity.getSupportFragmentManager(), "dhalert");
    }
}
