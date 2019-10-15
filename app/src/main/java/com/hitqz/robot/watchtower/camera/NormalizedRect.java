package com.hitqz.robot.watchtower.camera;

import android.graphics.Point;
import android.graphics.RectF;

public class NormalizedRect {

    float totalWidth;
    float totalHeight;
    float widthR;
    float heightR;

    public RectF mRectF;

    public Production mProduction;
    public float mRatio = 1.0f;// 默认初始态，这个状态不会去绘制，只有设置了框选区域(此时ratio不为1)才会绘制

    public NormalizedRect(float width, float height) {
        totalWidth = width;
        totalHeight = height;
        mRectF = new RectF();

        widthR = totalWidth / 1000f;
        heightR = totalHeight / 1000f;
        set(0, 0, width, height, 1.0f);
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

        float width = totalWidth * ratio;
        float height = totalHeight * ratio;

        centerX = Math.max(centerX, width / 2);
        centerX = Math.min(centerX, totalWidth - width / 2);
        centerY = Math.max(centerY, height / 2);
        centerY = Math.min(centerY, totalHeight - height / 2);

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
        return (int) ((totalHeight - mRectF.top) / heightR);
    }

    public int commonBottom() {
        return (int) ((totalHeight - mRectF.bottom) / heightR);
    }

    public void setPoints(Point[] points) {
        if (points == null || points.length < 2) {
            return;
        }

        float left = points[0].x * widthR;
        float right = points[1].x * widthR;
        float top = totalHeight - points[1].y * heightR;
        float bottom = totalHeight - points[0].y * heightR;

        float ratio = (right - left) / totalWidth;
        set(left, top, right, bottom, ratio);
    }
}