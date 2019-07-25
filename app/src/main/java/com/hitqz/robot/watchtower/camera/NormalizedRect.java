package com.hitqz.robot.watchtower.camera;

import android.graphics.Point;
import android.graphics.RectF;


public class NormalizedRect {

    private float mScreenWidth;
    private float mScreenHeight;

    public RectF mRectF;

    public Production mProduction;
    public float mRatio = 1.0f;// 默认初始态，这个状态不会去绘制，只有设置了框选区域(此时ratio不为1)才会绘制


    float widthR;
    float heightR;

    public NormalizedRect(float screenWidth, float screenheight) {
        mScreenWidth = screenWidth;
        mScreenHeight = screenheight;
        mRectF = new RectF();

        widthR = mScreenWidth / 1000f;
        heightR = mScreenHeight / 1000f;
    }

    public void set(float left, float top, float right, float bottom, float ratio) {
        mRectF.set(left, top, right, bottom);
        mRatio = ratio;
    }

    public float centerX() {
        return mRectF.centerX();
    }

    public float centerY() {
        return mRectF.centerY();
    }

    public float width() {
        return mRectF.width();
    }

    public float height() {
        return mRectF.height();
    }

    public void setRatio(float ratio) {

        float centerX;
        float centerY;
        if (mProduction != null) {
            mProduction.mRatio = ratio;
            centerX = mProduction.mRawX;
            centerY = mProduction.mRawY;
        } else {
            centerX = centerX();
            centerY = centerY();
        }
        mRatio = ratio;

        float width = mScreenWidth * ratio;
        float height = mScreenHeight * ratio;

        centerX = Math.max(centerX, width / 2);
        centerX = Math.min(centerX, mScreenWidth - width / 2);
        centerY = Math.max(centerY, height / 2);
        centerY = Math.min(centerY, mScreenHeight - height / 2);

        float left = centerX - width / 2;
        float top = centerY - height / 2;
        float right = centerX + width / 2;
        float bottom = centerY + height / 2;

        set(left, top, right, bottom, ratio);
    }

    public void setProduction(Production production) {
        mProduction = production;
    }

    public int commonLeft() {
        return (int) (mRectF.left / widthR);
    }

    public int commonRight() {
        return (int) (mRectF.right / widthR);
    }

    public int commonTop() {
        return (int) ((mScreenHeight - mRectF.top) / heightR);
    }

    public int commonBottom() {
        return (int) ((mScreenHeight - mRectF.bottom) / heightR);
    }

    public void setPoints(Point[] points) {
        if (points == null || points.length < 2) {
            return;
        }

        float left = points[0].x * widthR;
        float right = points[1].x * widthR;
        float top = mScreenHeight - points[1].y * heightR;
        float bottom = mScreenHeight - points[0].y * heightR;

        float ratio = (right - left) / mScreenWidth;
        set(left, top, right, bottom, ratio);

    }
}