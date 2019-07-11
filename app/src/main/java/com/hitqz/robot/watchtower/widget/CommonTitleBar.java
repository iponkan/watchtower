package com.hitqz.robot.watchtower.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hitqz.robot.watchtower.R;

public class CommonTitleBar extends FrameLayout implements View.OnClickListener {

    private ViewGroup vpBack;
    private Context context;
    private TextView tvTitle;
    private TextView tvBackText;

    public CommonTitleBar(@NonNull Context context) {
        this(context, null);
    }

    public CommonTitleBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonTitleBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        setBackground(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        LayoutInflater.from(context).inflate(R.layout.layout_title_bar, this, true);
        vpBack = findViewById(R.id.fl_back);
        vpBack.setOnClickListener(this);
        tvTitle = findViewById(R.id.tv_title_bar_title);
        tvBackText = findViewById(R.id.tv_title_bar_back_text);
    }

    @Override
    public void onClick(View v) {
        if (vpBack == v) {
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        }
    }

    public void setTitle(String title) {
        tvTitle.setText(title);
    }

    public void setBackText(String backText) {
        tvBackText.setText(backText);
    }
}
