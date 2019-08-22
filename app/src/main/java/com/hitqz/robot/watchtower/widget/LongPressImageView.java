package com.hitqz.robot.watchtower.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class LongPressImageView extends AppCompatImageView {

    public LongPressImageView(Context context) {
        super(context);
    }

    public LongPressImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LongPressImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (listener != null) {
                    listener.onPress();
                }
                invalidate();

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        unClick();
                    }
                }, 50);
                if (LongPressImageView.this.listener != null) {
                    listener.onRelease();
                }

                break;
        }
        return true;
    }

    private void unClick() {
        invalidate();
    }

    public interface ILongPressListener {

        void onPress();

        void onRelease();
    }

    ILongPressListener listener;

    public void setLongpressListener(ILongPressListener l) {
        this.listener = l;
    }
}
