package com.hitqz.robot.watchtower.camera;

import android.graphics.RectF;

import java.util.ArrayList;


public class ProductionManager {

    public static final int CAPACITY = 1;

    private ArrayList<Production> mProductions = new ArrayList<>();

    public static Production mOriginal = new Production(0f, 0f, 1.0f);

    public Production getOriginal() {
        return mOriginal;
    }

    private float mWindowWidth;
    private float mWindowHeight;
    private float mCenterWidth;

    public ProductionManager(float windowWidth, float windowHeight, float centerWidth) {
        mWindowWidth = windowWidth;
        mWindowHeight = windowHeight;
        mCenterWidth = centerWidth;
    }

    public Production getProduction(int key) {
        Production production = null;
        if (key <= mProductions.size()) {
            production = mProductions.get(key - 1);
        }
        return production;
    }


    public Production addProduction(float rawX, float rawY, float ratio) {

        float x = rawX / mWindowWidth;
        float y = (mWindowHeight - rawY) / mWindowHeight;

        Production production = new Production(x, y, ratio);
        production.setRawXy(rawX, rawY);
        mProductions.add(production);
        return production;
    }

    public void clear() {
        mProductions.clear();
    }

    public ArrayList<Production> getProductions() {
        return mProductions;
    }


    public boolean isFull() {
        return mProductions.size() == CAPACITY;
    }

    public Production contains(float rawX, float rawY) {
        Production result = null;
        for (int i = 0; i < mProductions.size(); i++) {
            Production production = mProductions.get(i);

            RectF rect = new RectF(production.mRawX - mCenterWidth / 2, production.mRawY - mCenterWidth / 2,
                    production.mRawX + mCenterWidth / 2, production.mRawY + mCenterWidth / 2);
            if (rect.contains(rawX, rawY)) {
                result = production;
            }
        }
        return result;
    }

    public void remove(Production production) {
        mProductions.remove(production);
    }

    public int indexOfProduction(Production production) {
        return mProductions.indexOf(production);
    }
}
