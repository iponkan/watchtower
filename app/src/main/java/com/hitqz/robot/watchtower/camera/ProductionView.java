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

    public static final String TAG = ProductionView.class.getSimpleName();

    private int drawRectState = STATE_NONE;

    public static final int STATE_NONE = 0x00;
    public static final int STATE_ONE = 0x01;
    public static final int STATE_TWO = 0x02;

    public static final float DEFAULT_RATIO = 1f / 3;
    private static final float MIN_SCALE = 0.1f;
    private static final float MAX_SCALE = 0.6f;

    private NormalizedRect normalizedRect;
    private ArrayList<NormalizedRect> normalizedRects = new ArrayList<>();
    private Paint paint;
    private Paint textPaint;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private float borderWidth;
    private float borderHeight;
    private boolean isScale;
    boolean drawFocusText = false;

    RectF rect1 = new RectF();
    RectF rect2 = new RectF();
    RectF rect3 = new RectF();
    RectF rect4 = new RectF();

    RectF rect6 = new RectF();
    RectF rect7 = new RectF();
    RectF rect8 = new RectF();
    RectF rect9 = new RectF();

    RectF wholeRectF = new RectF();

    List<RectF> textRectFS = new ArrayList<>();

    private int operateState = STATUS_IDLE;
    private static int STATUS_IDLE = 1;// 空闲状态
    private static int STATUS_READY = 2;// 就绪状态
    private static int STATUS_MOVE = 3;// 移动状态
    private static int STATUS_SCALE = 4;// 缩放状态

    //四个点触摸区域
    private RectF mLeftTopTouchRect = new RectF();
    private RectF mRightTopTouchRect = new RectF();
    private RectF mLeftBottomTouchRect = new RectF();
    private RectF mRightBottomTouchRect = new RectF();

    private int selectedControllerCicle;

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

    public void drawText(boolean drawText) {
        this.drawFocusText = drawText;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (normalizedRect.mRatio < 1.0f) {
            canvas.drawRect(normalizedRect.mRectF, paint);
        }

        if (drawFocusText) {
            canvas.drawText("聚焦中", getWidth() / 2f - 55, getHeight() / 2f + 10, textPaint);
        }

        if (drawText) {
            if (textRectFS.size() == 0 || texts.size() == 0) {
                //
            } else {
                int size = Math.min(textRectFS.size(), texts.size());
                Log.d(TAG, "draw min size:" + size);
                for (int i = 0; i < size; i++) {
                    RectF rectF = textRectFS.get(i);
                    String temperature = texts.get(i);
                    Log.d(TAG, "draw temperature:" + temperature);
                    canvas.drawText(temperature, rectF.centerX(), rectF.centerY(), textPaint);
                }
            }
        }
    }

    public Point[] getPoints() {
        if (normalizedRect.mRatio == 1.0f) {
            return null;
        }

        Point[] points = new Point[2];
        points[0] = new Point(normalizedRect.commonLeft(), normalizedRect.commonBottom());
        points[1] = new Point(normalizedRect.commonRight(), normalizedRect.commonTop());
        return points;
    }

    public void setPoints(Point[] points) {
        normalizedRect.setPoints(points);
        postInvalidate();
    }

    private void init(Context context) {
        paint = new Paint();
        textPaint = new Paint();
        Resources resources = context.getResources();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(resources.getDimension(R.dimen.production_lw));
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeWidth(resources.getDimension(R.dimen.center_lw));
        textPaint.setTextSize(30);
        textPaint.setAntiAlias(true);

        gestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onSingleTapUp(MotionEvent event) {
                        final float x = event.getX();
                        final float y = event.getY();
                        Production production = mProductionManager.contains(x, y);
                        normalizedRect.setProduction(production);
                        if (production == null) {
                            changeDrawRect(x, y, DEFAULT_RATIO);
                            if (mProductionListener != null) {
                                mProductionListener.onChangeRect(x, y, false, DEFAULT_RATIO);
                            }
                            operateState = STATUS_READY;
                        } else {
                            changeDrawRect(x, y, production.mRatio);
                        }
                        return true;
                    }

                    @Override
                    public void onLongPress(MotionEvent event) {
                        if (isScale) {
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
                            normalizedRect.setProduction(null);
                        } else {
                            if (!mProductionManager.isFull()) {
                                changeDrawRect(x, y, DEFAULT_RATIO);
                                Production newP = mProductionManager.addProduction(x, y,
                                        normalizedRect.mRatio);
                                normalizedRect.setProduction(newP);
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

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = (float) (normalizedRect.mRatio * Math.pow(detector.getScaleFactor(), 3));
                scaleFactor = Math.max(MIN_SCALE, Math.min(scaleFactor, MAX_SCALE));
                normalizedRect.setRatio(scaleFactor);
                Log.d(TAG, "onScale: mScale:" + normalizedRect.mRatio);
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
                    mProductionListener.onScale(normalizedRect.centerX(),
                            normalizedRect.centerY(), normalizedRect.mRatio);
                }
            }
        });
    }

    public void setParentSize(float width, float height) {
        borderWidth = width;
        borderHeight = height;
        normalizedRect = new NormalizedRect(borderWidth, borderHeight);
        normalizedRect.set(0, 0, borderWidth, borderHeight, 1.0f);
    }

    private ProductionManager mProductionManager;

    public void setProductionManager(ProductionManager productionManager) {
        mProductionManager = productionManager;
    }

    public void notifyItemRemove(int index) {
        NormalizedRect rectF = normalizedRects.get(index);
    }

    public void notifyItemSetChanged() {
//        ArrayList<Production> mProductions = mProductionManager.getProductions();
//        normalizedRects.clear();
//        for (int i = 0; i < mProductions.size(); i++) {
//            Production production = mProductions.get(i);
//            RectF rect = createCenterRect(production);
//            normalizedRects.add(rect);
//        }
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (antiTouch) {
            ToastUtil.showToastShort(getContext(), "请停止监火方能移动选框");
            return true;
        }

        Log.d(TAG, "onTouchEvent: getPointerCount:" + event.getPointerCount());

        if (event.getPointerCount() > 1) {
            isScale = true;
            scaleGestureDetector.onTouchEvent(event);
            if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN) {
                gestureDetector.onTouchEvent(event);
            }
        } else if (event.getPointerCount() == 1 && !isScale) {
            float x = event.getX();
            float y = event.getY();
            Log.d(TAG, "gestureDetector.onTouchEvent");

            if (operateState == STATUS_READY) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    int selectCircle = isSeletedControllerCircle(x, y);
                    switch (selectCircle) {
                        case 1:
                            x = normalizedRect.mRectF.left;
                            y = normalizedRect.mRectF.top;
                            break;
                        case 2:
                            x = normalizedRect.mRectF.right;
                            y = normalizedRect.mRectF.top;
                            break;
                        case 3:
                            x = normalizedRect.mRectF.left;
                            y = normalizedRect.mRectF.bottom;
                            break;
                        case 4:
                            x = normalizedRect.mRectF.right;
                            y = normalizedRect.mRectF.bottom;
                            break;
                    }
                    Log.d(TAG, " 选择控制点;" + selectCircle);
                    if (selectCircle > 0) {// 选择控制点
                        selectedControllerCicle = selectCircle;// 记录选中控制点编号
                        operateState = STATUS_SCALE;// 进入缩放状态
                        paint.setColor(Color.BLUE);
                    } else if (normalizedRect.mRectF.contains(x, y)) {// 选择缩放框内部
                        operateState = STATUS_MOVE;// 进入移动状态
                        paint.setColor(Color.BLUE);
                    }
                }
//                else if (event.getAction() == MotionEvent.ACTION_MOVE) {
//                    if (operateState == STATUS_SCALE) {// 缩放控制
//                        Log.d(TAG, "缩放控制");
//                        scaleCropController(x - oldx, y - oldy);
//                    } else if (operateState == STATUS_MOVE) {// 移动控制
//                        Log.d(TAG, "移动控制");
//                        translateCrop(x - oldx, y - oldy);
//                    }
//                }
            } else if (operateState == STATUS_IDLE) {
                gestureDetector.onTouchEvent(event);
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    onUp(x, y);
                }
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (operateState == STATUS_SCALE) {// 缩放控制
                    Log.d(TAG, "缩放控制");
                    scaleCropController(x - oldx, y - oldy);
                } else if (operateState == STATUS_MOVE) {// 移动控制
                    Log.d(TAG, "移动控制");
                    translateCrop(x - oldx, y - oldy);
                }
            }

            // 记录上一次动作点
            oldx = x;
            oldy = y;
        }
        if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
            if (operateState == STATUS_SCALE || operateState == STATUS_MOVE) {
                operateState = STATUS_READY;
                paint.setColor(Color.WHITE);
                invalidate();
            }
            Log.d(TAG, "onTouchEvent: ACTION_UP");
            isScale = false;
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
                    realDx = Math.min(realDx, normalizedRect.mRectF.width() - 50);
                } else {
                    realDx = Math.max(realDx, -normalizedRect.mRectF.left);
                }

                if (realDy > 0) {
                    realDy = Math.min(realDy, normalizedRect.mRectF.height() - 50);
                } else {
                    realDy = Math.max(realDy, -normalizedRect.mRectF.top);
                }

                normalizedRect.mRectF.left += realDx;
                normalizedRect.mRectF.top += realDy;
                break;
            case 2:// 右上角控制点
                if (realDx < 0) {
                    realDx = Math.max(realDx, 50 - normalizedRect.mRectF.width());
                } else {
                    realDx = Math.min(realDx, normalizedRect.totalWidth - normalizedRect.mRectF.right);
                }
                if (realDy > 0) {
                    realDy = Math.min(realDy, normalizedRect.mRectF.height() - 50);
                } else {
                    realDy = Math.max(realDy, -normalizedRect.mRectF.top);
                }

                normalizedRect.mRectF.right += realDx;
                normalizedRect.mRectF.top += realDy;
                break;
            case 3:// 左下角控制点
                if (realDx > 0) {
                    realDx = Math.min(realDx, normalizedRect.mRectF.width() - 50);
                } else {
                    realDx = Math.max(realDx, -normalizedRect.mRectF.left);
                }

                if (realDy < 0) {
                    realDy = Math.max(realDy, 50 - normalizedRect.mRectF.height());
                } else {
                    realDy = Math.min(realDy, normalizedRect.totalHeight - normalizedRect.mRectF.bottom);
                }

                normalizedRect.mRectF.left += realDx;
                normalizedRect.mRectF.bottom += realDy;
                break;
            case 4:// 右下角控制点
                if (realDx < 0) {
                    realDx = Math.max(realDx, 50 - normalizedRect.mRectF.width());
                } else {
                    realDx = Math.min(realDx, normalizedRect.totalWidth - normalizedRect.mRectF.right);
                }
                if (realDy < 0) {
                    realDy = Math.max(realDy, 50 - normalizedRect.mRectF.height());
                } else {
                    realDy = Math.min(realDy, normalizedRect.totalHeight - normalizedRect.mRectF.bottom);
                }
                normalizedRect.mRectF.right += realDx;
                normalizedRect.mRectF.bottom += realDy;
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
            realDx = Math.max(realDx, -normalizedRect.mRectF.left);
        } else {
            realDx = Math.min(realDx, normalizedRect.totalWidth - normalizedRect.mRectF.right);
        }

        if (dy < 0) {
            realDy = Math.max(realDy, -normalizedRect.mRectF.top);
        } else {
            realDy = Math.min(realDy, normalizedRect.totalHeight - normalizedRect.mRectF.bottom);
        }

        normalizedRect.mRectF.left += realDx;
        normalizedRect.mRectF.right += realDx;
        normalizedRect.mRectF.top += realDy;
        normalizedRect.mRectF.bottom += realDy;
        resetCorner();
        invalidate();
    }

    private float oldx, oldy;

    private void onUp(float x, float y) {
        if (mProductionListener != null) {
            mProductionListener.onChangeRect(x, y, true, normalizedRect.mRatio);
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

        float width = borderWidth * scale;
        float height = borderHeight * scale;

        centerX = Math.max(centerX, width / 2);
        centerX = Math.min(centerX, borderWidth - width / 2);
        centerY = Math.max(centerY, height / 2);
        centerY = Math.min(centerY, borderHeight - height / 2);

        float left = centerX - width / 2;
        float top = centerY - height / 2;
        float right = centerX + width / 2;
        float bottom = centerY + height / 2;

        normalizedRect.set(left, top, right, bottom, scale);
        resetCorner();
        invalidate();
    }

    public void moveDrawRect(float centerX, float centerY) {

        float width = normalizedRect.width();
        float height = normalizedRect.height();

        centerX = Math.max(centerX, width / 2);
        centerX = Math.min(centerX, borderWidth - width / 2);
        centerY = Math.max(centerY, height / 2);
        centerY = Math.min(centerY, borderHeight - height / 2);

        normalizedRect.set(centerX - width / 2, centerY - height / 2,
                centerX + width / 2, centerY + height / 2, normalizedRect.mRatio);
        invalidate();
    }

    public void zoomIn() {
        if (normalizedRect.mRatio == 1.0f) {
            return;
        }
        float finalRatio = Math.min(MAX_SCALE, normalizedRect.mRatio * 1.1f);
        normalizedRect.setRatio(finalRatio);
        invalidate();
    }

    public void zoomOut() {
        if (normalizedRect.mRatio == 1.0f) {
            return;
        }
        float finalRatio = Math.max(MIN_SCALE, normalizedRect.mRatio * 0.9f);
        normalizedRect.setRatio(finalRatio);
        invalidate();
    }

    boolean drawText;
    List<String> texts = new ArrayList<>();

    private void notDrawExtra() {
        drawText = false;
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
        textRectFS.clear();
        texts.clear();
        if (normalizedRect.mRatio < 1.0f) {
            for (int i = 0; i < floats.size(); i++) {
                DecimalFormat decimalFormat = new DecimalFormat(".000");
                String p = decimalFormat.format(floats.get(i));
                texts.add(p);
            }

            rect7.set(0, normalizedRect.mRectF.bottom, normalizedRect.mRectF.left, normalizedRect.totalHeight);
            if (rect7.width() > 0 && rect7.height() > 0) {
                textRectFS.add(rect7);
            }

            rect8.set(normalizedRect.mRectF.left, normalizedRect.mRectF.bottom, normalizedRect.mRectF.right, normalizedRect.totalHeight);
            if (rect8.width() > 0 && rect8.height() > 0) {
                textRectFS.add(rect8);
            }

            rect9.set(normalizedRect.mRectF.right, normalizedRect.mRectF.bottom, normalizedRect.totalWidth, normalizedRect.totalHeight);
            if (rect9.width() > 0 && rect9.height() > 0) {
                textRectFS.add(rect9);
            }

            rect4.set(0, normalizedRect.mRectF.top, normalizedRect.mRectF.left, normalizedRect.mRectF.bottom);
            if (rect4.width() > 0 && rect4.height() > 0) {
                textRectFS.add(rect4);
            }

//            textRectFS.add(normalizedRect.mRectF);

            rect6.set(normalizedRect.mRectF.right, normalizedRect.mRectF.top, normalizedRect.totalWidth, normalizedRect.mRectF.bottom);
            if (rect6.width() > 0 && rect6.height() > 0) {
                textRectFS.add(rect6);
            }

            rect1.set(0, 0, normalizedRect.mRectF.left, normalizedRect.mRectF.top);
            if (rect1.width() > 0 && rect1.height() > 0) {
                textRectFS.add(rect1);
            }

            rect2.set(normalizedRect.mRectF.left, 0, normalizedRect.mRectF.right, normalizedRect.mRectF.top);
            if (rect2.width() > 0 && rect2.height() > 0) {
                textRectFS.add(rect2);
            }

            rect3.set(normalizedRect.mRectF.right, 0, normalizedRect.totalWidth, normalizedRect.mRectF.top);
            if (rect3.width() > 0 && rect3.height() > 0) {
                textRectFS.add(rect3);
            }
        } else {
            float sum = 0;
            for (int i = 0; i < floats.size(); i++) {
                sum += floats.get(i);
            }
            DecimalFormat decimalFormat = new DecimalFormat(".000");
            String p = decimalFormat.format(sum / floats.size());
            texts.add(p);
            wholeRectF.set(0, 0, normalizedRect.totalWidth, normalizedRect.totalHeight);
            textRectFS.add(wholeRectF);
        }

        Log.d(TAG, "size 是否相等：" + (textRectFS.size() == texts.size()));
        drawText = true;
        postInvalidate();
    }

    private void resetCorner() {

        //设置触摸区域
        int touchRadius = 25;//触摸半径
        mLeftTopTouchRect.set(normalizedRect.mRectF.left - touchRadius, normalizedRect.mRectF.top - touchRadius,
                normalizedRect.mRectF.left + touchRadius, normalizedRect.mRectF.top + touchRadius);
        mRightTopTouchRect.set(normalizedRect.mRectF.right - touchRadius, normalizedRect.mRectF.top - touchRadius,
                normalizedRect.mRectF.right + touchRadius, normalizedRect.mRectF.top + touchRadius);
        mLeftBottomTouchRect.set(normalizedRect.mRectF.left - touchRadius, normalizedRect.mRectF.bottom - touchRadius,
                normalizedRect.mRectF.left + touchRadius, normalizedRect.mRectF.bottom + touchRadius);
        mRightBottomTouchRect.set(normalizedRect.mRectF.right - touchRadius, normalizedRect.mRectF.bottom - touchRadius,
                normalizedRect.mRectF.right + touchRadius, normalizedRect.mRectF.bottom + touchRadius);
    }

    public void reset() {
        normalizedRect.set(0, 0, borderWidth, borderHeight, 1.0f);
        mLeftTopTouchRect.set(0, 0, 0, 0);
        mRightTopTouchRect.set(0, 0, 0, 0);
        mLeftBottomTouchRect.set(0, 0, 0, 0);
        mRightBottomTouchRect.set(0, 0, 0, 0);
        operateState = STATUS_IDLE;
        postInvalidate();
    }
}
