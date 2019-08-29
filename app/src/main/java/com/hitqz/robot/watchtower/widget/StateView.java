package com.hitqz.robot.watchtower.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hitqz.robot.watchtower.R;

public class StateView extends LinearLayout {

    ViewGroup vpBack;
    Context context;
    TextView tvDes;
    TextView tvState;
    boolean state;
    String onString;
    String offString;

    public StateView(@NonNull Context context) {
        this(context, null);
    }

    public StateView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StateView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        this.context = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.StateView);
        String des = ta.getString(R.styleable.StateView_stateDes);
        state = ta.getBoolean(R.styleable.StateView_stateValue, false);
        onString = ta.getString(R.styleable.StateView_stateOn);
        offString = ta.getString(R.styleable.StateView_stateOff);
        ta.recycle();
        LayoutInflater.from(context).inflate(R.layout.layout_state_view, this, true);
        vpBack = findViewById(R.id.fl_back);
        tvDes = findViewById(R.id.tv_des);
        tvDes.setText(des);
        tvState = findViewById(R.id.tv_state);
        setState(state);
        setOrientation(HORIZONTAL);
    }

    public void setState(boolean state) {
        this.state = state;
        tvState.setText(state ? onString : offString);
        tvState.setTextColor(state ? Color.parseColor("#00de72") : Color.RED);
    }
}
