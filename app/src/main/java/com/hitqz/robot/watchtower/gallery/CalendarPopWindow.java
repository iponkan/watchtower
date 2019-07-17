package com.hitqz.robot.watchtower.gallery;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.PopupWindow;

import com.hitqz.robot.watchtower.R;

public class CalendarPopWindow extends PopupWindow {

    private View conentView;
    private Activity context;
    private CalendarView calendarView;
    private CalendarView.OnDateChangeListener dateChangeListener;

    public CalendarPopWindow(final Activity context, CalendarView.OnDateChangeListener listener) {
        super(context);
        this.context = context;
        this.dateChangeListener = listener;
        this.initPopupWindow();
    }

    private void initPopupWindow() {

        View decorView = context.getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        //使用view来引入布局
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        conentView = inflater.inflate(R.layout.popuo_dialog, null);
        // 设置SelectPicPopupWindow的View
        this.setContentView(conentView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(false);
        this.setOutsideTouchable(true);
        // 刷新状态
        this.update();
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(Color.TRANSPARENT);
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        this.setBackgroundDrawable(dw);
        // mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        // 设置SelectPicPopupWindow弹出窗体动画效果，设置动画，一会会讲解
//        this.setAnimationStyle(R.style.AnimationPreview);
        //布局控件初始化与监听设置
        calendarView = conentView
                .findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener(dateChangeListener);
    }

    /**
     * 显示popupWindow的方式设置，当然可以有别的方式。
     * 一会会列出其他方法
     *
     * @param parent
     */
    public void showPopupWindow(View parent, long time) {
        if (!this.isShowing()) {
            // 以下拉方式显示popupwindow
            this.showAsDropDown(parent);
            calendarView.setDate(time);
        } else {
            this.dismiss();
        }
    }


}
