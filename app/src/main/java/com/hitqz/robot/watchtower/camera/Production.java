package com.hitqz.robot.watchtower.camera;

public class Production {
    public float mX;
    public float mY;
    public float mRatio;

    public Production(float x, float y, float ratio) {
        mX = x;
        mY = y;
        mRatio = ratio;
    }

    public float mRawX;
    public float mRawY;

    public void setRawXy(float centerX, float centerY) {
        mRawX = centerX;
        mRawY = centerY;
    }
}
