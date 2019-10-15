package com.hitqz.robot.watchtower.camera;

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

    public static final int STATE_NONE = 0x00;
    public static final int STATE_ONE = 0x01;
    public static final int STATE_TWO = 0x02;

    public static final String TAG = ProductionView.class.getSimpleName();

    public static final float DEFAULT_RATIO = 1f / 3;
    private static final float MIN_SCALE = 0.1f;
    private static final float MAX_SCALE = 0.6f;

    private NormalizedRect mNormalizedRect;
    private ArrayList<RectF> mCenterRectFs = new ArrayList<>();
    private Paint mPaint;
    private Paint mTextPaint;
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private float mScreenWidth;
    private float mScreenHeight;
    private boolean mIsScale;

    RectF rect1 = new RectF();
    RectF rect2 = new RectF();
    RectF rect3 = new RectF();
    RectF rect4 = new RectF();

    RectF rect6 = new RectF();
    RectF rect7 = new RectF();
    RectF rect8 = new RectF();
    RectF rect9 = new RectF();

    RectF wholeRectF = new RectF();

    List<RectF> mTextRectFS = new ArrayList<>();

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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mNormalizedRect.mRatio < 1.0f) {
            canvas.drawRect(mNormalizedRect.mRectF, mPaint);
        }

        if (drawText) {
            canvas.drawText("聚焦中", getWidth() / 2f - 55, getHeight() / 2f + 10, mTextPaint);
        }

        if (drawExtra) {
            if (mTextRectFS.size() == 0 || texts.size() == 0) {
                //
            } else {
                int size = Math.min(mTextRectFS.size(), texts.size());
                Log.d(TAG, "draw min size:" + size);
                for (int i = 0; i < size; i++) {
                    RectF rectF = mTextRectFS.get(i);
                    String temperature = texts.get(i);
                    Log.d(TAG, "draw temperature:" + temperature);
                    canvas.drawText(temperature, rectF.centerX(), rectF.centerY(), mTextPaint);
                }
            }
        }
    }

    public Point[] getPoints() {
        if (mNormalizedRect.mRatio == 1.0f) {
            return null;
        }

        Point[] points = new Point[2];
        points[0] = new Point(mNormalizedRect.commonLeft(), mNormalizedRect.commonBottom());
        points[1] = new Point(mNormalizedRect.commonRight(), mNormalizedRect.commonTop());
        return points;
    }

    public void setPoints(Point[] points) {
        mNormalizedRect.setPoints(points);
        postInvalidate();
    }

    private void init(Context context) {
        mPaint = new Paint();
        mTextPaint = new Paint();
        Resources resources = context.getResources();
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(resources.getDimension(R.dimen.production_lw));
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setStrokeWidth(resources.getDimension(R.dimen.center_lw));
        mTextPaint.setTextSize(30);
        mTextPaint.setAntiAlias(true);

        mGestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onSingleTapUp(MotionEvent event) {
                        final float x = event.getX();
                        final float y = event.getY();
                        Production production = mProductionManager.contains(x, y);
                        mNormalizedRect.setProduction(production);
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
                            mNormalizedRect.setProduction(null);
                        } else {
                            if (!mProductionManager.isFull()) {
                                changeDrawRect(x, y, DEFAULT_RATIO);
                                Production newP = mProductionManager.addProduction(x, y,
                                        mNormalizedRect.mRatio);
                                mNormalizedRect.setProduction(newP);
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
                float scaleFactor = (float) (mNormalizedRect.mRatio * Math.pow(detector.getScaleFactor(), 3));
                scaleFactor = Math.max(MIN_SCALE, Math.min(scaleFactor, MAX_SCALE));
                mNormalizedRect.setRatio(scaleFactor);
                Log.d(TAG, "onScale: mScale:" + mNormalizedRect.mRatio);
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
                    mProductionListener.onScale(mNormalizedRect.centerX(),
                            mNormalizedRect.centerY(), mNormalizedRect.mRatio);
                }
            }
        });
    }

    public void setParentSize(float width, float height) {
        mScreenWidth = width;
        mScreenHeight = height;
        mNormalizedRect = new NormalizedRect(mScreenWidth, mScreenHeight);
        mNormalizedRect.set(0, 0, mScreenWidth, mScreenHeight, 1.0f);
    }

    private ProductionManager mProductionManager;

    public void setProductionManager(ProductionManager productionManager) {
        mProductionManager = productionManager;
    }

    public void notifyItemRemove(int index) {
        RectF rectF = mCenterRectFs.get(index);
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
//        float left = production.mRawX - mCenterWidth / 2;
//        float top = production.mRawY - mCenterWidth / 2;
//        float right = production.mRawX + mCenterWidth / 2;
//        float bottom = production.mRawY + mCenterWidth / 2;
//
//        RectF rect = new RectF(left, top, right, bottom);
        return new RectF();
    }

    /**
     * 是否选中控制点
     * <p>
     * -1为没有
     */
    private int isSeletedControllerCircle(float x, float y) {
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
                            x = mNormalizedRect.mRectF.left;
                            y = mNormalizedRect.mRectF.top;
                            break;
                        case 2:
                            x = mNormalizedRect.mRectF.right;
                            y = mNormalizedRect.mRectF.top;
                            break;
                        case 3:
                            x = mNormalizedRect.mRectF.left;
                            y = mNormalizedRect.mRectF.bottom;
                            break;
                        case 4:
                            x = mNormalizedRect.mRectF.right;
                            y = mNormalizedRect.mRectF.bottom;
                            break;
                    }
                    Log.d(TAG, " 选择控制点;" + selectCircle);
                    if (selectCircle > 0) {// 选择控制点
                        selectedControllerCicle = selectCircle;// 记录选中控制点编号
                        status = STATUS_SCALE;// 进入缩放状态
                        mPaint.setColor(Color.BLUE);
                    } else if (mNormalizedRect.mRectF.contains(x, y)) {// 选择缩放框内部
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
        switch (selectedControllerCicle) {

            case 1:// 左上角控制点
                if (realDx > 0) {
                    realDx = Math.min(realDx, mNormalizedRect.mRectF.width() - 50);
                } else {
                    realDx = Math.max(realDx, -mNormalizedRect.mRectF.left);
                }

                if (realDy > 0) {
                    realDy = Math.min(realDy, mNormalizedRect.mRectF.height() - 50);
                } else {
                    realDy = Math.max(realDy, -mNormalizedRect.mRectF.top);
                }

                mNormalizedRect.mRectF.left += realDx;
                mNormalizedRect.mRectF.top += realDy;
                break;
            case 2:// 右上角控制点
                if (realDx < 0) {
                    realDx = Math.max(realDx, 50 - mNormalizedRect.mRectF.width());
                } else {
                    realDx = Math.min(realDx, mNormalizedRect.totalWidth - mNormalizedRect.mRectF.right);
                }
                if (realDy > 0) {
                    realDy = Math.min(realDy, mNormalizedRect.mRectF.height() - 50);
                } else {
                    realDy = Math.max(realDy, -mNormalizedRect.mRectF.top);
                }

                mNormalizedRect.mRectF.right += realDx;
                mNormalizedRect.mRectF.top += realDy;
                break;
            case 3:// 左下角控制点
                if (realDx > 0) {
                    realDx = Math.min(realDx, mNormalizedRect.mRectF.width() - 50);
                } else {
                    realDx = Math.max(realDx, -mNormalizedRect.mRectF.left);
                }

                if (realDy < 0) {
                    realDy = Math.max(realDy, 50 - mNormalizedRect.mRectF.height());
                } else {
                    realDy = Math.min(realDy, mNormalizedRect.totalHeight - mNormalizedRect.mRectF.bottom);
                }

                mNormalizedRect.mRectF.left += realDx;
                mNormalizedRect.mRectF.bottom += realDy;
                break;
            case 4:// 右下角控制点
                if (realDx < 0) {
                    realDx = Math.max(realDx, 50 - mNormalizedRect.mRectF.width());
                } else {
                    realDx = Math.min(realDx, mNormalizedRect.totalWidth - mNormalizedRect.mRectF.right);
                }
                if (realDy < 0) {
                    realDy = Math.max(realDy, 50 - mNormalizedRect.mRectF.height());
                } else {
                    realDy = Math.min(realDy, mNormalizedRect.totalHeight - mNormalizedRect.mRectF.bottom);
                }
                mNormalizedRect.mRectF.right += realDx;
                mNormalizedRect.mRectF.bottom += realDy;
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
            realDx = Math.max(realDx, -mNormalizedRect.mRectF.left);
        } else {
            realDx = Math.min(realDx, mNormalizedRect.totalWidth - mNormalizedRect.mRectF.right);
        }

        if (dy < 0) {
            realDy = Math.max(realDy, -mNormalizedRect.mRectF.top);
        } else {
            realDy = Math.min(realDy, mNormalizedRect.totalHeight - mNormalizedRect.mRectF.bottom);
        }

        mNormalizedRect.mRectF.left += realDx;
        mNormalizedRect.mRectF.right += realDx;
        mNormalizedRect.mRectF.top += realDy;
        mNormalizedRect.mRectF.bottom += realDy;
        resetCorner();
        invalidate();
    }

    private float oldx, oldy;

    private void onUp(float x, float y) {
        if (mProductionListener != null) {
            mProductionListener.onChangeRect(x, y, true, mNormalizedRect.mRatio);
        }
    }

    private ProductionListener mProductionListener;

    public void setProductionListener(ProductionListener l) {
        mProductionListener = l;
    }

    boolean antiTouch;

    public void antiTouch(boolean b) {
        antiTouch = b;
    }

    public interface ProductionListener {
        void onChangeRect(float x, float y, boolean animate, float ratio);

        void onScale(float x, float y, float scale);
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

        mNormalizedRect.set(left, top, right, bottom, scale);
        resetCorner();
        invalidate();
    }

    public void moveDrawRect(float centerX, float centerY) {

        float width = mNormalizedRect.width();
        float height = mNormalizedRect.height();

        centerX = Math.max(centerX, width / 2);
        centerX = Math.min(centerX, mScreenWidth - width / 2);
        centerY = Math.max(centerY, height / 2);
        centerY = Math.min(centerY, mScreenHeight - height / 2);

        mNormalizedRect.set(centerX - width / 2, centerY - height / 2,
                centerX + width / 2, centerY + height / 2, mNormalizedRect.mRatio);
        invalidate();
    }

    public void zoomIn() {
        if (mNormalizedRect.mRatio == 1.0f) {
            return;
        }
        float finalRatio = Math.min(MAX_SCALE, mNormalizedRect.mRatio * 1.1f);
        mNormalizedRect.setRatio(finalRatio);
        invalidate();
    }

    public void zoomOut() {
        if (mNormalizedRect.mRatio == 1.0f) {
            return;
        }
        float finalRatio = Math.max(MIN_SCALE, mNormalizedRect.mRatio * 0.9f);
        mNormalizedRect.setRatio(finalRatio);
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
        mTextRectFS.clear();
        texts.clear();
        if (mNormalizedRect.mRatio < 1.0f) {
            for (int i = 0; i < floats.size(); i++) {
                DecimalFormat decimalFormat = new DecimalFormat(".000");
                String p = decimalFormat.format(floats.get(i));
                texts.add(p);
            }

            rect7.set(0, mNormalizedRect.mRectF.bottom, mNormalizedRect.mRectF.left, mNormalizedRect.totalHeight);
            if (rect7.width() > 0 && rect7.height() > 0) {
                mTextRectFS.add(rect7);
            }

            rect8.set(mNormalizedRect.mRectF.left, mNormalizedRect.mRectF.bottom, mNormalizedRect.mRectF.right, mNormalizedRect.totalHeight);
            if (rect8.width() > 0 && rect8.height() > 0) {
                mTextRectFS.add(rect8);
            }

            rect9.set(mNormalizedRect.mRectF.right, mNormalizedRect.mRectF.bottom, mNormalizedRect.totalWidth, mNormalizedRect.totalHeight);
            if (rect9.width() > 0 && rect9.height() > 0) {
                mTextRectFS.add(rect9);
            }

            rect4.set(0, mNormalizedRect.mRectF.top, mNormalizedRect.mRectF.left, mNormalizedRect.mRectF.bottom);
            if (rect4.width() > 0 && rect4.height() > 0) {
                mTextRectFS.add(rect4);
            }

//            mTextRectFS.add(mNormalizedRect.mRectF);

            rect6.set(mNormalizedRect.mRectF.right, mNormalizedRect.mRectF.top, mNormalizedRect.totalWidth, mNormalizedRect.mRectF.bottom);
            if (rect6.width() > 0 && rect6.height() > 0) {
                mTextRectFS.add(rect6);
            }

            rect1.set(0, 0, mNormalizedRect.mRectF.left, mNormalizedRect.mRectF.top);
            if (rect1.width() > 0 && rect1.height() > 0) {
                mTextRectFS.add(rect1);
            }

            rect2.set(mNormalizedRect.mRectF.left, 0, mNormalizedRect.mRectF.right, mNormalizedRect.mRectF.top);
            if (rect2.width() > 0 && rect2.height() > 0) {
                mTextRectFS.add(rect2);
            }

            rect3.set(mNormalizedRect.mRectF.right, 0, mNormalizedRect.totalWidth, mNormalizedRect.mRectF.top);
            if (rect3.width() > 0 && rect3.height() > 0) {
                mTextRectFS.add(rect3);
            }
        } else {
            float sum = 0;
            for (int i = 0; i < floats.size(); i++) {
                sum += floats.get(i);
            }
            DecimalFormat decimalFormat = new DecimalFormat(".000");
            String p = decimalFormat.format(sum / floats.size());
            texts.add(p);
            wholeRectF.set(0, 0, mNormalizedRect.totalWidth, mNormalizedRect.totalHeight);
            mTextRectFS.add(wholeRectF);
        }

        Log.d(TAG, "size 是否相等：" + (mTextRectFS.size() == texts.size()));
        drawExtra = true;
        postInvalidate();
    }

    private void resetCorner() {

        //设置触摸区域
        int touchRadius = 25;//触摸半径
        mLeftTopTouchRect.set(mNormalizedRect.mRectF.left - touchRadius, mNormalizedRect.mRectF.top - touchRadius,
                mNormalizedRect.mRectF.left + touchRadius, mNormalizedRect.mRectF.top + touchRadius);
        mRightTopTouchRect.set(mNormalizedRect.mRectF.right - touchRadius, mNormalizedRect.mRectF.top - touchRadius,
                mNormalizedRect.mRectF.right + touchRadius, mNormalizedRect.mRectF.top + touchRadius);
        mLeftBottomTouchRect.set(mNormalizedRect.mRectF.left - touchRadius, mNormalizedRect.mRectF.bottom - touchRadius,
                mNormalizedRect.mRectF.left + touchRadius, mNormalizedRect.mRectF.bottom + touchRadius);
        mRightBottomTouchRect.set(mNormalizedRect.mRectF.right - touchRadius, mNormalizedRect.mRectF.bottom - touchRadius,
                mNormalizedRect.mRectF.right + touchRadius, mNormalizedRect.mRectF.bottom + touchRadius);
    }

    public void reset() {
        mNormalizedRect.set(0, 0, mScreenWidth, mScreenHeight, 1.0f);
        mLeftTopTouchRect.set(0, 0, 0, 0);
        mRightTopTouchRect.set(0, 0, 0, 0);
        mLeftBottomTouchRect.set(0, 0, 0, 0);
        mRightBottomTouchRect.set(0, 0, 0, 0);
        status = STATUS_IDLE;
        postInvalidate();
    }
}
