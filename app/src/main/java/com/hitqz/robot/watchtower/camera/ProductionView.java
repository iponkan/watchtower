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
import com.sonicers.commonlib.util.ToastUtil;

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
                            status = STATUS_READY;
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

    /**
     * 是否选中控制点
     * <p>
     * -1为没有
     *
     * @param x
     * @param y
     * @return
     */
    private int isSeletedControllerCircle(float x, float y) {
//        Log.d("yrclog", "mLeftTopTouchRect.left=============" + mLeftTopTouchRect.left);
//        Log.d("yrclog", "mRightTopTouchRect.top=============" + mRightTopTouchRect.top);
//        Log.d("yrclog", "mLeftBottomTouchRect.right=============" + mLeftBottomTouchRect.right);
//        Log.d("yrclog", "mRightBottomTouchRect.bottom=============" + mRightBottomTouchRect.bottom);
//        Log.d("yrclog", "x==============" + x + "y=================" + y);
        if (mLeftTopTouchRect.contains(x, y))// 选中左上角
            return 1;
        if (mRightTopTouchRect.contains(x, y))// 选中右上角
            return 2;
        if (mLeftBottomTouchRect.contains(x, y))// 选中左下角
            return 3;
        if (mRightBottomTouchRect.contains(x, y))// 选中右下角
            return 4;
        return -1;
    }

    private int selectedControllerCicle;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (antiTouch) {
            ToastUtil.showToastShort(getContext(), "请停止监火方能移动选框");
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
            float x = event.getX();
            float y = event.getY();
            Log.d(TAG, "mGestureDetector.onTouchEvent");

            if (status == STATUS_READY) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    int selectCircle = isSeletedControllerCircle(x, y);
                    switch (selectCircle) {
                        case 1:
                            x = mBorderRect.mRectF.left;
                            y = mBorderRect.mRectF.top;
                            break;
                        case 2:
                            x = mBorderRect.mRectF.right;
                            y = mBorderRect.mRectF.top;
                            break;
                        case 3:
                            x = mBorderRect.mRectF.left;
                            y = mBorderRect.mRectF.bottom;
                            break;
                        case 4:
                            x = mBorderRect.mRectF.right;
                            y = mBorderRect.mRectF.bottom;
                            break;
                    }
                    Log.d(TAG, " 选择控制点;" + selectCircle);
                    if (selectCircle > 0) {// 选择控制点
                        selectedControllerCicle = selectCircle;// 记录选中控制点编号
                        status = STATUS_SCALE;// 进入缩放状态
                        mPaint.setColor(Color.BLUE);
                    } else if (mBorderRect.mRectF.contains(x, y)) {// 选择缩放框内部
                        status = STATUS_MOVE;// 进入移动状态
                        mPaint.setColor(Color.BLUE);
                    }
                }
//                else if (event.getAction() == MotionEvent.ACTION_MOVE) {
//                    if (status == STATUS_SCALE) {// 缩放控制
//                        Log.d(TAG, "缩放控制");
//                        scaleCropController(x - oldx, y - oldy);
//                    } else if (status == STATUS_MOVE) {// 移动控制
//                        Log.d(TAG, "移动控制");
//                        translateCrop(x - oldx, y - oldy);
//                    }
//                }
            } else if (status == STATUS_IDLE) {
                mGestureDetector.onTouchEvent(event);
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    onUp(x, y);
                }
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (status == STATUS_SCALE) {// 缩放控制
                    Log.d(TAG, "缩放控制");
                    scaleCropController(x - oldx, y - oldy);
                } else if (status == STATUS_MOVE) {// 移动控制
                    Log.d(TAG, "移动控制");
                    translateCrop(x - oldx, y - oldy);
                }
            }

            // 记录上一次动作点
            oldx = x;
            oldy = y;
        }
        if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
            if (status == STATUS_SCALE || status == STATUS_MOVE) {
                status = STATUS_READY;
                mPaint.setColor(Color.WHITE);
                invalidate();
            }
            Log.d(TAG, "onTouchEvent: ACTION_UP");
            mIsScale = false;
        }

        return true;
    }

    /**
     * 操作控制点 控制缩放
     */
    private void scaleCropController(float dx, float dy) {
        float realDx = dx;
        float realDy = dy;

//        if (dx < 0) {
//            realDx = Math.max(realDx, -mBorderRect.mRectF.left);
//        } else {
//            realDx = Math.min(realDx, mBorderRect.totalWidth - mBorderRect.mRectF.right);
//        }
//
//        if (dy < 0) {
//            realDy = Math.max(realDy, -mBorderRect.mRectF.top);
//        } else {
//            realDy = Math.min(realDy, mBorderRect.totalHeight - mBorderRect.mRectF.bottom);
//        }

//        mBorderRect.mRectF.left += realDx;
//        mBorderRect.mRectF.right += realDx;
//        mBorderRect.mRectF.top += realDy;
//        mBorderRect.mRectF.bottom += realDy;
//        temp.set(mBorderRect.mRectF);
        switch (selectedControllerCicle) {

            case 1:// 左上角控制点
                if (realDx > 0) {
                    realDx = Math.min(realDx, mBorderRect.mRectF.width() - 50);
                } else {
                    realDx = Math.max(realDx, -mBorderRect.mRectF.left);
                }

                if (realDy > 0) {
                    realDy = Math.min(realDy, mBorderRect.mRectF.height() - 50);
                } else {
                    realDy = Math.max(realDy, -mBorderRect.mRectF.top);
                }

                mBorderRect.mRectF.left += realDx;
                mBorderRect.mRectF.top += realDy;
                break;
            case 2:// 右上角控制点
                if (realDx < 0) {
                    realDx = Math.max(realDx, 50 - mBorderRect.mRectF.width());
                } else {
                    realDx = Math.min(realDx, mBorderRect.totalWidth - mBorderRect.mRectF.right);
                }
                if (realDy > 0) {
                    realDy = Math.min(realDy, mBorderRect.mRectF.height() - 50);
                } else {
                    realDy = Math.max(realDy, -mBorderRect.mRectF.top);
                }

                mBorderRect.mRectF.right += realDx;
                mBorderRect.mRectF.top += realDy;
                break;
            case 3:// 左下角控制点
                if (realDx > 0) {
                    realDx = Math.min(realDx, mBorderRect.mRectF.width() - 50);
                } else {
                    realDx = Math.max(realDx, -mBorderRect.mRectF.left);
                }

                if (realDy < 0) {
                    realDy = Math.max(realDy, 50 - mBorderRect.mRectF.height());
                } else {
                    realDy = Math.min(realDy, mBorderRect.totalHeight - mBorderRect.mRectF.bottom);
                }

                mBorderRect.mRectF.left += realDx;
                mBorderRect.mRectF.bottom += realDy;
                break;
            case 4:// 右下角控制点
                if (realDx < 0) {
                    realDx = Math.max(realDx, 50 - mBorderRect.mRectF.width());
                } else {
                    realDx = Math.min(realDx, mBorderRect.totalWidth - mBorderRect.mRectF.right);
                }
                if (realDy < 0) {
                    realDy = Math.max(realDy, 50 - mBorderRect.mRectF.height());
                } else {
                    realDy = Math.min(realDy, mBorderRect.totalHeight - mBorderRect.mRectF.bottom);
                }
                mBorderRect.mRectF.right += realDx;
                mBorderRect.mRectF.bottom += realDy;
                break;
        }// end switch
        resetCorner();
        invalidate();
    }

    /**
     * 移动剪切框
     *
     * @param dx
     * @param dy
     */
    private void translateCrop(float dx, float dy) {
        float realDx = dx;
        float realDy = dy;

        if (dx < 0) {
            realDx = Math.max(realDx, -mBorderRect.mRectF.left);
        } else {
            realDx = Math.min(realDx, mBorderRect.totalWidth - mBorderRect.mRectF.right);
        }

        if (dy < 0) {
            realDy = Math.max(realDy, -mBorderRect.mRectF.top);
        } else {
            realDy = Math.min(realDy, mBorderRect.totalHeight - mBorderRect.mRectF.bottom);
        }

        mBorderRect.mRectF.left += realDx;
        mBorderRect.mRectF.right += realDx;
        mBorderRect.mRectF.top += realDy;
        mBorderRect.mRectF.bottom += realDy;
        resetCorner();
        invalidate();
    }

    private float oldx, oldy;

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
        resetCorner();
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
        postInvalidate();
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
        }
        rectFS.clear();
        texts.clear();
        if (mBorderRect.mRatio < 1.0f) {
            for (int i = 0; i < floats.size(); i++) {
                DecimalFormat decimalFormat = new DecimalFormat(".000");
                String p = decimalFormat.format(floats.get(i));
                texts.add(p);
            }
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
            float sum = 0;
            for (int i = 0; i < floats.size(); i++) {
                sum += floats.get(i);
            }
            DecimalFormat decimalFormat = new DecimalFormat(".000");
            String p = decimalFormat.format(sum / floats.size());
            texts.add(p);
            wholeRectF.set(0, 0, mBorderRect.totalWidth, mBorderRect.totalHeight);
            rectFS.add(wholeRectF);
        }

        Log.d(TAG, "size 是否相等：" + (rectFS.size() == texts.size()));
        drawExtra = true;
        postInvalidate();
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

    private static int STATUS_IDLE = 1;// 空闲状态
    private static int STATUS_READY = 2;// 就绪状态
    private static int STATUS_MOVE = 3;// 移动状态
    private static int STATUS_SCALE = 4;// 缩放状态
    private int status = STATUS_IDLE;

    //四个点触摸区域
    private RectF mLeftTopTouchRect = new RectF();
    private RectF mRightTopTouchRect = new RectF();
    private RectF mLeftBottomTouchRect = new RectF();
    private RectF mRightBottomTouchRect = new RectF();

    private void resetCorner() {

        //设置触摸区域
        int touchRadius = 25;//触摸半径
        mLeftTopTouchRect.set(mBorderRect.mRectF.left - touchRadius, mBorderRect.mRectF.top - touchRadius,
                mBorderRect.mRectF.left + touchRadius, mBorderRect.mRectF.top + touchRadius);
        mRightTopTouchRect.set(mBorderRect.mRectF.right - touchRadius, mBorderRect.mRectF.top - touchRadius,
                mBorderRect.mRectF.right + touchRadius, mBorderRect.mRectF.top + touchRadius);
        mLeftBottomTouchRect.set(mBorderRect.mRectF.left - touchRadius, mBorderRect.mRectF.bottom - touchRadius,
                mBorderRect.mRectF.left + touchRadius, mBorderRect.mRectF.bottom + touchRadius);
        mRightBottomTouchRect.set(mBorderRect.mRectF.right - touchRadius, mBorderRect.mRectF.bottom - touchRadius,
                mBorderRect.mRectF.right + touchRadius, mBorderRect.mRectF.bottom + touchRadius);
    }

    public void reset() {
        mBorderRect.set(0, 0, mScreenWidth, mScreenHeight, 1.0f);
        mLeftTopTouchRect.set(0, 0, 0, 0);
        mRightTopTouchRect.set(0, 0, 0, 0);
        mLeftBottomTouchRect.set(0, 0, 0, 0);
        mRightBottomTouchRect.set(0, 0, 0, 0);
        status = STATUS_IDLE;
        postInvalidate();
    }
}
