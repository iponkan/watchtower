package com.hitqz.robot.watchtower.camera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

import com.hitqz.robot.watchtower.R;

import java.util.ArrayList;


public class ProductionView extends View {

    public static final String TAG = ProductionView.class.getSimpleName();

    public static final float DEFAULT_RATIO = 1f / 3;
    private static final float MIN_SCALE = 0.1f;
    private static final float MAX_SCALE = 0.6f;

    private NormalizedRect mBorderRect;
    private ArrayList<RectF> mCenterRectFs = new ArrayList<>();
    private Paint mPaint;
    private Paint mCenterPaint;
    private float mCenterWidth;
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private float mCenterRoundRadius;
    private float mScreenWidth;
    private float mScreenHeight;
    private boolean mIsScale;

    public ProductionView(Context context) {
        this(context, null);
    }

    public ProductionView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProductionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) return;
        init(context);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBorderRect.mRatio < 1.0f) {
            canvas.drawRect(mBorderRect.mRectF, mPaint);
        }
//        for (int i = 0; i < mCenterRectFs.size(); i++) {
//            canvas.drawRoundRect(mCenterRectFs.get(i), mCenterRoundRadius, mCenterRoundRadius, mCenterPaint);
//        }

    }

    public Point[] getPoints() {
        if (mBorderRect.mRatio == 1.0f) {
            return null;
        }

        Point[] points = new Point[2];
        points[0] = new Point(mBorderRect.commonLeft(), mBorderRect.commonBottom());
        points[1] = new Point(mBorderRect.commonRight(), mBorderRect.commonTop());
        return points;
    }

    public void setPoints(Point[] points) {
        mBorderRect.setPoints(points);
        postInvalidate();
    }

    private void init(Context context) {
        mPaint = new Paint();
        mCenterPaint = new Paint();
        Resources resources = context.getResources();
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(resources.getDimension(R.dimen.fr_director_production_lw));
        mCenterPaint.setColor(resources.getColor(R.color.fr_storage_photo));
        mCenterPaint.setStyle(Paint.Style.STROKE);
        mCenterPaint.setStrokeWidth(resources.getDimension(R.dimen.fr_director_center_lw));
        mCenterPaint.setAntiAlias(true);
        mCenterWidth = getResources().getDimension(R.dimen.fr_center_width);
        mCenterRoundRadius = getResources().getDimension(R.dimen.fr_director_center_radius);

        mGestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onSingleTapUp(MotionEvent event) {
                        final float x = event.getX();
                        final float y = event.getY();
                        Production production = mProductionManager.contains(x, y);
                        mBorderRect.setProduction(production);
                        if (production == null) {
                            changeDrawRect(x, y, DEFAULT_RATIO);
                            if (mProductionListener != null) {
                                mProductionListener.onChangeRect(x, y, false, DEFAULT_RATIO);
                            }
                        } else {
                            changeDrawRect(x, y, production.mRatio);
                        }
                        return true;
                    }

                    @Override
                    public void onLongPress(MotionEvent event) {
                        if (mIsScale) {
                            return;
                        }
                        Log.d(TAG, "onLongPress: ");
                        final float x = event.getX();
                        final float y = event.getY();
                        Production production = mProductionManager.contains(x, y);
                        if (production != null) {
                            int index = mProductionManager.indexOfProduction(production);
                            notifyItemRemove(index);
                            mProductionManager.remove(production);
                            mBorderRect.setProduction(null);
                        } else {
                            if (!mProductionManager.isFull()) {
                                changeDrawRect(x, y, DEFAULT_RATIO);
                                Production newP = mProductionManager.addProduction(x, y,
                                        mBorderRect.mRatio);
                                mBorderRect.setProduction(newP);
                                notifyItemSetChanged();
                                if (mProductionListener != null) {
                                    mProductionListener.onChangeRect(x, y, false, DEFAULT_RATIO);
                                }
                            }
                        }

                    }

                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                        if (e2.getAction() == MotionEvent.ACTION_MOVE) {
                            final float x = e2.getX();
                            final float y = e2.getY();
                            moveDrawRect(x, y);
                        }

                        return true;
                    }


                });

        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = (float) (mBorderRect.mRatio * Math.pow(detector.getScaleFactor(), 3));
                scaleFactor = Math.max(MIN_SCALE, Math.min(scaleFactor, MAX_SCALE));
                mBorderRect.setRatio(scaleFactor);
                Log.d(TAG, "onScale: mScale:" + mBorderRect.mRatio);
                invalidate();
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                if (mProductionListener != null) {
                    mProductionListener.onScale(mBorderRect.centerX(),
                            mBorderRect.centerY(), mBorderRect.mRatio);
                }
            }
        });
    }

    public void setParentSize(float width, float height) {
        mScreenWidth = width;
        mScreenHeight = height;
        mBorderRect = new NormalizedRect(mScreenWidth, mScreenHeight);
        mBorderRect.set(0, 0, mScreenWidth, mScreenHeight, 1.0f);
    }

    private ProductionManager mProductionManager;

    public void setProductionManager(ProductionManager productionManager) {
        mProductionManager = productionManager;
    }

    public void notifyItemRemove(int index) {
        RectF rectF = mCenterRectFs.get(index);
        ValueAnimator animator = ValueAnimator.ofInt((int) mCenterWidth, 2 * (int) mCenterWidth);
        float centerX = rectF.centerX();
        float centerY = rectF.centerY();


        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                float left = centerX - value / 2;
                float top = centerY - value / 2;
                float right = centerX + value / 2;
                float bottom = centerY + value / 2;
                rectF.set(left, top, right, bottom);
                invalidate();
            }
        });
        animator.setDuration(500);
        animator.start();

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mCenterRectFs.remove(rectF);
                invalidate();
            }
        });
    }

    public void notifyItemSetChanged() {
        ArrayList<Production> mProductions = mProductionManager.getProductions();
        mCenterRectFs.clear();
        for (int i = 0; i < mProductions.size(); i++) {
            Production production = mProductions.get(i);
            RectF rect = createCenterRect(production);
            mCenterRectFs.add(rect);
        }
        invalidate();
    }


    private RectF createCenterRect(Production production) {
        float left = production.mRawX - mCenterWidth / 2;
        float top = production.mRawY - mCenterWidth / 2;
        float right = production.mRawX + mCenterWidth / 2;
        float bottom = production.mRawY + mCenterWidth / 2;

        RectF rect = new RectF(left, top, right, bottom);
        return rect;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (antiTouch) {
            return true;
        }

        Log.d(TAG, "onTouchEvent: getPointerCount:" + event.getPointerCount());

        if (event.getPointerCount() > 1) {
            mIsScale = true;
            mScaleGestureDetector.onTouchEvent(event);
            if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN) {
                mGestureDetector.onTouchEvent(event);
            }
        } else if (event.getPointerCount() == 1 && !mIsScale) {
            Log.d(TAG, "mGestureDetector.onTouchEvent");
            mGestureDetector.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_UP) {

                final float x = event.getX();
                final float y = event.getY();
                onUp(x, y);
            }
        }
        if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
            Log.d(TAG, "onTouchEvent: ACTION_UP");
            mIsScale = false;
        }

        return true;
    }

    private void onUp(float x, float y) {
        if (mProductionListener != null) {
            mProductionListener.onChangeRect(x, y, true, mBorderRect.mRatio);
        }
    }

    private ProductionListener mProductionListener;

    public void setProductionListener(ProductionListener l) {
        mProductionListener = l;
    }

    public void drawWide() {
        mBorderRect.setProduction(null);
        mBorderRect.set(0, 0, mScreenWidth, mScreenHeight, 1.0f);
        postInvalidate();
    }

    boolean antiTouch;

    public void antiTouch(boolean b) {
        antiTouch = b;
    }

    public interface ProductionListener {
        void onChangeRect(float x, float y, boolean animate, float ratio);

        void onScale(float x, float y, float scale);
    }

    public void changeSelectRect(Production production) {
        mBorderRect.setProduction(production);
        changeDrawRect(mScreenWidth * production.mX,
                mScreenHeight - mScreenHeight * production.mY, production.mRatio);
    }


    private void changeDrawRect(float centerX, float centerY, float scale) {

        float width = mScreenWidth * scale;
        float height = mScreenHeight * scale;

        centerX = Math.max(centerX, width / 2);
        centerX = Math.min(centerX, mScreenWidth - width / 2);
        centerY = Math.max(centerY, height / 2);
        centerY = Math.min(centerY, mScreenHeight - height / 2);

        float left = centerX - width / 2;
        float top = centerY - height / 2;
        float right = centerX + width / 2;
        float bottom = centerY + height / 2;

        mBorderRect.set(left, top, right, bottom, scale);
        invalidate();

    }


    public void moveDrawRect(float centerX, float centerY) {

        float width = mBorderRect.width();
        float height = mBorderRect.height();

        centerX = Math.max(centerX, width / 2);
        centerX = Math.min(centerX, mScreenWidth - width / 2);
        centerY = Math.max(centerY, height / 2);
        centerY = Math.min(centerY, mScreenHeight - height / 2);

        mBorderRect.set(centerX - width / 2, centerY - height / 2,
                centerX + width / 2, centerY + height / 2, mBorderRect.mRatio);
        invalidate();
    }

    public int getCurrentHighlight() {
        int i = -1;
        if (mBorderRect.mProduction != null) {
            i = mProductionManager.indexOfProduction(mBorderRect.mProduction) + 1;
        } else if (mBorderRect.mRatio == 1.0f) {
            i = 0;
        }
        return i;
    }

    public void zoomIn() {
        if (mBorderRect.mRatio == 1.0f) {
            return;
        }
        float finalRatio = Math.min(MAX_SCALE, mBorderRect.mRatio * 1.1f);
        mBorderRect.setRatio(finalRatio);
        invalidate();
    }

    public void zoomOut() {
        if (mBorderRect.mRatio == 1.0f) {
            return;
        }
        float finalRatio = Math.max(MIN_SCALE, mBorderRect.mRatio * 0.9f);
        mBorderRect.setRatio(finalRatio);
        invalidate();
    }
}
