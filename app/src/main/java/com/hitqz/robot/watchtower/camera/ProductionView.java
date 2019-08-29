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
import com.hitqz.robot.watchtower.net.bean.TemperatureList;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ProductionView extends View {

    public static final String TAG = ProductionView.class.getSimpleName();

    public static final float DEFAULT_RATIO = 1f / 3;
    private static final float MIN_SCALE = 0.1f;
    private static final float MAX_SCALE = 0.6f;

    private NormalizedRect mBorderRect;
    private ArrayList<RectF> mCenterRectFs = new ArrayList<>();
    private Paint mPaint;
    private Paint mTextPaint;
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

    boolean drawText = false;

    public void drawText(boolean drawText) {
        this.drawText = drawText;
        invalidate();
    }

    List<RectF> rectFS = new ArrayList<>();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBorderRect.mRatio < 1.0f) {
            canvas.drawRect(mBorderRect.mRectF, mPaint);
        }

        if (drawText) {
            canvas.drawText("聚焦中", getWidth() / 2f - 55, getHeight() / 2f + 10, mTextPaint);
        }

        if (drawExtra) {
            if (rectFS.size() == 0 || texts.size() == 0) {
                //
            } else {
                int size = Math.min(rectFS.size(), texts.size());
                Log.d(TAG, "draw min size:" + size);
                for (int i = 0; i < size; i++) {
                    RectF rectF = rectFS.get(i);
                    String temperature = texts.get(i);
                    Log.d(TAG, "draw temperature:" + temperature);
                    canvas.drawText(temperature, rectF.centerX(), rectF.centerY(), mTextPaint);
                }
            }
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
        mTextPaint = new Paint();
        mCenterPaint = new Paint();
        Resources resources = context.getResources();
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(resources.getDimension(R.dimen.production_lw));
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setStrokeWidth(resources.getDimension(R.dimen.center_lw));
        mTextPaint.setTextSize(30);
        mTextPaint.setAntiAlias(true);
        mCenterPaint.setColor(resources.getColor(R.color.fr_storage_photo));
        mCenterPaint.setStyle(Paint.Style.STROKE);
        mCenterPaint.setStrokeWidth(resources.getDimension(R.dimen.center_lw));
        mCenterPaint.setAntiAlias(true);
        mCenterWidth = getResources().getDimension(R.dimen.center_width);
        mCenterRoundRadius = getResources().getDimension(R.dimen.center_radius);

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

    boolean drawExtra;
    List<String> texts = new ArrayList<>();

    private void notDrawExtra() {
        drawExtra = false;
        texts.clear();
        invalidate();
    }

    public void showTemperature(TemperatureList temperatureList) {
        if (temperatureList == null) {
            notDrawExtra();
            return;
        }
        List<Float> floats = temperatureList.toList();

        if (floats == null || floats.size() == 0) {
            notDrawExtra();
            return;
        } else {
            texts.clear();
            for (int i = 0; i < floats.size(); i++) {
                DecimalFormat decimalFormat = new DecimalFormat(".000");
                String p = decimalFormat.format(floats.get(i));
                texts.add(p);
            }
        }
        rectFS.clear();
        if (mBorderRect.mRatio < 1.0f) {
            rect1.set(0, 0, mBorderRect.mRectF.left, mBorderRect.mRectF.top);
            if (rect1.width() > 0 && rect1.height() > 0) {
                rectFS.add(rect1);
            }

            rect2.set(mBorderRect.mRectF.left, 0, mBorderRect.mRectF.right, mBorderRect.mRectF.top);
            if (rect2.width() > 0 && rect2.height() > 0) {
                rectFS.add(rect2);
            }

            rect3.set(mBorderRect.mRectF.right, 0, mBorderRect.totalWidth, mBorderRect.mRectF.top);
            if (rect3.width() > 0 && rect3.height() > 0) {
                rectFS.add(rect3);
            }

            rect4.set(0, mBorderRect.mRectF.top, mBorderRect.mRectF.left, mBorderRect.mRectF.bottom);
            if (rect4.width() > 0 && rect4.height() > 0) {
                rectFS.add(rect4);
            }

//            rectFS.add(mBorderRect.mRectF);

            rect6.set(mBorderRect.mRectF.right, mBorderRect.mRectF.top, mBorderRect.totalWidth, mBorderRect.mRectF.bottom);
            if (rect6.width() > 0 && rect6.height() > 0) {
                rectFS.add(rect6);
            }

            rect7.set(0, mBorderRect.mRectF.bottom, mBorderRect.mRectF.left, mBorderRect.totalHeight);
            if (rect7.width() > 0 && rect7.height() > 0) {
                rectFS.add(rect7);
            }

            rect8.set(mBorderRect.mRectF.left, mBorderRect.mRectF.bottom, mBorderRect.mRectF.right, mBorderRect.totalHeight);
            if (rect8.width() > 0 && rect8.height() > 0) {
                rectFS.add(rect8);
            }

            rect9.set(mBorderRect.mRectF.right, mBorderRect.mRectF.bottom, mBorderRect.totalWidth, mBorderRect.totalHeight);
            if (rect9.width() > 0 && rect9.height() > 0) {
                rectFS.add(rect9);
            }
        } else {
            wholeRectF.set(0, 0, mBorderRect.totalWidth, mBorderRect.totalHeight);
            rectFS.add(wholeRectF);
        }

        Log.d(TAG, "size 是否相等：" + (rectFS.size() == floats.size()));
        drawExtra = true;
        invalidate();
    }

    RectF rect1 = new RectF();
    RectF rect2 = new RectF();
    RectF rect3 = new RectF();
    RectF rect4 = new RectF();

    RectF rect6 = new RectF();
    RectF rect7 = new RectF();
    RectF rect8 = new RectF();
    RectF rect9 = new RectF();

    RectF wholeRectF = new RectF();
}
