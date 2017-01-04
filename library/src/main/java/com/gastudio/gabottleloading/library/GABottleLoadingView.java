/**
 * Copyright 2017 GAStudio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gastudio.gabottleloading.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * author: GAStudio
 * qq:1935400187
 * 技术交流qq群:277582728
 */

public class GABottleLoadingView extends View {

    private class PointWithAngle {
        public int x;
        public int y;
        public float tan;
    }

    private class WaterDrop {
        public static final int STATE_LEAVE_WATER = 0;
        public static final int STATE_MOVE = 1;

        public Path mDropPath;
        public PointWithAngle mLeftWaterPoint;
        public PointWithAngle mLeftControlPoint;
        public PointWithAngle mLeftDropPoint;
        public PointWithAngle mRightWaterPoint;
        public PointWithAngle mRightControlPoint;
        public PointWithAngle mRightDropPoint;
        public PointWithAngle mInterDropAndWater;
        public PointWithAngle mMiddleOfWaterPoints;
        public RectF mDropCircleRectF;
        public int dropRadius;
        public int dropX;
        public float mNormalizedTime;
        public int mState;

        public WaterDrop() {
            init();
        }

        private void init() {
            mDropPath = new Path();
            mDropCircleRectF = new RectF();
            initPoint();
        }

        private void initPoint() {
            if (mLeftWaterPoint == null) {
                mLeftWaterPoint = new PointWithAngle();
            }

            if (mLeftControlPoint == null) {
                mLeftControlPoint = new PointWithAngle();
            }

            if (mLeftDropPoint == null) {
                mLeftDropPoint = new PointWithAngle();
            }

            if (mRightWaterPoint == null) {
                mRightWaterPoint = new PointWithAngle();
            }

            if (mRightControlPoint == null) {
                mRightControlPoint = new PointWithAngle();
            }

            if (mRightDropPoint == null) {
                mRightDropPoint = new PointWithAngle();
            }

            if (mInterDropAndWater == null) {
                mInterDropAndWater = new PointWithAngle();
            }

            if (mMiddleOfWaterPoints == null) {
                mMiddleOfWaterPoints = new PointWithAngle();
            }
        }
    }

    // general
    private static final int FULL_ANGLE = 360;
    private static final int ZERO_ANGLE = 0;
    private static final int HALF_FULL_ANGLE = FULL_ANGLE / 2;
    private static final int QUAR_FULL_ANGLE = FULL_ANGLE / 4;
    private static final int ONE_ENGHTH_FULL_ANGLE = FULL_ANGLE / 8;

    private static final float FULL_NORMALIZED_TIME = 1F;
    private static final float ZERO_NORMALIZED_TIME = 0F;
    private static final int ERROR_OF_FIT_WATER_FUNC = 1;
    private static final int DEFAULT_ADJUST_OFFSET_TO_WATER_SUR = 2;

    private static final int WATER_ANIMATION_DURATION = 2000;
    private static final int WATER_ROUND_COUNT = 10;
    private static final int TOTAL_ANIMATION_DURATION = WATER_ANIMATION_DURATION * WATER_ROUND_COUNT;
    private static final int MIN_DRAW_ANGLE = 1;
    private static final float MAX_DIS_TO_CIRCLE_TOP_RATIO = 2f;

    private static final int DEFAULT_FLOW_WATER_OFFSET_FACTOR = 8;

    // debug
    private static final int DEFAULT_DEBUG_POINT_RADIUS = 2;
    private static final int DEFAULT_DEBUG_PAINT_STROKE_WIDTH = 2;
    private static final int DEFAULT_DEBUG_PAINT_TEXT_SIZE = 15;

    // water control
    private static final float[] WATER_FIRST_CON_X_CHANGE_ARR = {0.24f, 0.40f};
    private static final float[] WATER_FIRST_CON_Y_CHANGE_ARR = {0.44f, -0.08f};
    private static final float[] WATER_SECOND_CON_X_CHANGE_ARR = {0.60f, 0.76f};
    private static final float[] WATER_SECOND_CON_Y_CHANGE_ARR = {-0.08f, 0.44f};

    // view
    private static final float BOTTLE_WIDTH_TO_HEIGHT_RATIO = 120F / 170F;
    private static final int SUGGEST_BOTTLE_HEIGHT = 170;
    private static final int SUGGEST_BOTTLE_WIDTH = (int) (SUGGEST_BOTTLE_HEIGHT * BOTTLE_WIDTH_TO_HEIGHT_RATIO);

    // color
    private static final int DEFAULT_BOTTLE_COLOR = 0XFFCEFCFF;
    private static final int DEFAULT_WATER_COLOR = 0XFF41EDFA;

    // water drop
    private static final float WATER_DROP_LEAVE_WATER_TOTAL_DISTANCE_TO_DROP_RADIUS = 3.4F;
    private static final float WATER_DROP_UP_WATER_DISTANCE_TO_DROP_RADIUS = 2F;
    private static final float WATER_DROP_BROKEN_UP_TO_WATER_DISTANCE_TO_DROP_RADIUS = 1F;
    private static final float WATER_DROP_BROKEN_REMOVE_TAIL_TO_WATER_DISTANCE_TO_DROP_RADIUS = 0.5F;
    private static final float WATER_DROP_UNDER_WATER_DISTANCE_TO_DROP_RADIUS =
            WATER_DROP_LEAVE_WATER_TOTAL_DISTANCE_TO_DROP_RADIUS
                    - WATER_DROP_UP_WATER_DISTANCE_TO_DROP_RADIUS;

    private static final float WATER_POINTS_MAX_INTERVAL_TO_DROP_RADIUS = 2.2F;
    private static final float WATER_POINTS_MIN_INTERVAL_TO_DROP_RADIUS = 1.1F;

    private static final float WATER_MOVE_SEASON_1 =
            (WATER_DROP_LEAVE_WATER_TOTAL_DISTANCE_TO_DROP_RADIUS
                    - WATER_DROP_BROKEN_UP_TO_WATER_DISTANCE_TO_DROP_RADIUS)
                    / WATER_DROP_LEAVE_WATER_TOTAL_DISTANCE_TO_DROP_RADIUS;
    private static final float WATER_MOVE_SEASON_2 =
            (WATER_DROP_LEAVE_WATER_TOTAL_DISTANCE_TO_DROP_RADIUS
                    - WATER_DROP_BROKEN_REMOVE_TAIL_TO_WATER_DISTANCE_TO_DROP_RADIUS)
                    / WATER_DROP_LEAVE_WATER_TOTAL_DISTANCE_TO_DROP_RADIUS;
    private static final float WATER_MOVE_SEASON_3 = FULL_NORMALIZED_TIME;

    // bottle
    private static final float BOTTLE_STROKE_WIDTH_TO_VIEW_HEIGHT =
            5f / SUGGEST_BOTTLE_HEIGHT;
    private static final float BOTTLE_HALF_STROKE_WIDTH_TO_VIEW_HEIGHT =
            BOTTLE_STROKE_WIDTH_TO_VIEW_HEIGHT / 2;
    private static final float BOTTLE_LEFT_TOP_PADDING_LEFT_TO_VIEW_HEIGHT =
            40F / SUGGEST_BOTTLE_HEIGHT + BOTTLE_HALF_STROKE_WIDTH_TO_VIEW_HEIGHT;
    private static final float BOTTLE_LEFT_TOP_PADDING_TOP_TO_VIEW_HEIGHT =
            0F + BOTTLE_HALF_STROKE_WIDTH_TO_VIEW_HEIGHT;
    private static final float BOTTLE_LEFT_TOP_CIRCLE_RADIUS_TO_VIEW_HEIGHT =
            ((10f / SUGGEST_BOTTLE_HEIGHT) - BOTTLE_STROKE_WIDTH_TO_VIEW_HEIGHT) / 2;
    private static final float BOTTLE_LEFT_TOP_STRAIGHT_LINE_TO_VIEW_HEIGHT =
            60F / SUGGEST_BOTTLE_HEIGHT;

    private static final float BOTTLE_LEFT_BODY_CIRCLE_RADIUS_TO_VIEW_HEIGHT =
            50F / SUGGEST_BOTTLE_HEIGHT;
    private static final float BOTTLE_LEFT_BODY_CIRCLE_START_ANGLE = 14.5F;
    private static final float BOTTLE_LEFT_BODY_CIRCLE_TOTAL_ANGLE = 147F;
    private static final float BOTTLE_LEFT_BODY_CIRCLE_CENTER_X_VIEW_HEIGHT =
            BOTTLE_STROKE_WIDTH_TO_VIEW_HEIGHT
                    + BOTTLE_LEFT_BODY_CIRCLE_RADIUS_TO_VIEW_HEIGHT;
    private static final float BOTTLE_LEFT_BODY_CIRCLE_CENTER_Y_VIEW_HEIGHT =
            (float) (BOTTLE_LEFT_TOP_CIRCLE_RADIUS_TO_VIEW_HEIGHT * 2
                    + BOTTLE_LEFT_TOP_STRAIGHT_LINE_TO_VIEW_HEIGHT +
                    Math.cos(Math.toRadians(BOTTLE_LEFT_BODY_CIRCLE_START_ANGLE))
                            * BOTTLE_LEFT_BODY_CIRCLE_RADIUS_TO_VIEW_HEIGHT)
                    + BOTTLE_STROKE_WIDTH_TO_VIEW_HEIGHT;
    private static final float BOTTLE_LEFT_BOTTOM_TO_VIEW_HEIGHT =
            25f / SUGGEST_BOTTLE_HEIGHT;

    // view
    private RectF mViewRectF;

    // water
    private Paint mWaterPaint;
    private int mWaterColor;
    private Path mWaterPath;
    private ValueAnimator mFlowWaterUpAnimator;
    private ValueAnimator mFlowWaterDownAnimator;
    private float mFlowWaterValue;

    private int mFlowWaterHeight;
    private int mFlowWaterWidth;
    private int mFlowWaterStartX;
    private int mFlowWaterStartY;
    private int mFlowWaterEndX;
    private int mFlowWaterEndY;

    private int mFlowWaterFirstConX;
    private int mFlowWaterFirstConY;
    private int mFlowWaterSecondConX;
    private int mFlowWaterSecondConY;

    private RectF mStaticWaterRectF;
    private RectF mStaticWaterLeftCircleRectF;
    private RectF mStaticWaterRightCircleRectF;
    private float mStaticWaterBottomLineLen;
    private float mStaticWaterRotateAngle;

    // water drop
    private List<WaterDrop> mWaterDropList;
    private List<WaterDrop> mWaterDropWaitList;
    private int mAdjustOffsetToWaterSur;

    // bottle
    private Paint mBottlePaint;
    private int mBottleColor;
    private Path mBottlePath;
    private RectF mBottleRectF;
    private int mBottleStrokeWidth;
    private CornerPathEffect mBottleCornerPathEffect;

    // control
    private List<Animator> mAnimatorList;
    private List<Runnable> mRunnableList;
    private Handler mMainHandler;
    private int mWaterRoundCount;

    // debug
    private long mRecordCurrentMill;
    private boolean mIsDebug;
    private int mDebugPointRadius;
    private Paint mDebugPaint;

    public GABottleLoadingView(Context context) {
        this(context, null);
    }

    public GABottleLoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GABottleLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.GABottleLoadingView, defStyleAttr, 0);
        mBottleColor = typedArray.getColor(R.styleable.GABottleLoadingView_bottle_color, DEFAULT_BOTTLE_COLOR);
        mWaterColor = typedArray.getColor(R.styleable.GABottleLoadingView_water_color, DEFAULT_WATER_COLOR);
        init();
    }

    private void init() {
        mViewRectF = new RectF();

        mWaterPaint = new Paint();
        mWaterPaint.setColor(mWaterColor);
        mWaterPaint.setAntiAlias(true);
        mWaterPaint.setStyle(Paint.Style.FILL);

        mWaterDropList = new LinkedList<WaterDrop>();
        mWaterDropWaitList = new LinkedList<WaterDrop>();
        mAdjustOffsetToWaterSur = dipToPx(getContext(), DEFAULT_ADJUST_OFFSET_TO_WATER_SUR);

        mBottlePaint = new Paint();
        mBottlePaint.setAntiAlias(true);
        mBottlePaint.setStyle(Paint.Style.STROKE);
        mBottlePaint.setColor(mBottleColor);
        mBottlePaint.setStrokeCap(Paint.Cap.ROUND);

        mAnimatorList = new LinkedList<Animator>();
        mRunnableList = new LinkedList<Runnable>();
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthSpecMode != MeasureSpec.EXACTLY) {
            widthMeasureSpec =
                    MeasureSpec.makeMeasureSpec(dipToPx(getContext(),
                            SUGGEST_BOTTLE_WIDTH), MeasureSpec.EXACTLY);
        }
        if (heightSpecMode != MeasureSpec.EXACTLY) {
            heightMeasureSpec =
                    MeasureSpec.makeMeasureSpec(dipToPx(getContext(),
                            SUGGEST_BOTTLE_HEIGHT), MeasureSpec.EXACTLY);
        }
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // calculate the actual width and height
        int adjustWidth = (int) (h * BOTTLE_WIDTH_TO_HEIGHT_RATIO);
        adjustWidth = Math.min(adjustWidth, w);
        int adjustHeight = (int) (adjustWidth / BOTTLE_WIDTH_TO_HEIGHT_RATIO);
        mViewRectF.set(0, 0, adjustWidth, adjustHeight);

        // move bottle rect to the center
        mViewRectF.offset((w - adjustWidth) / 2, (h - adjustHeight) / 2);

        initBottleAndStaticWaterPath();
        updateWaterPath();
    }

    private void initBottleAndStaticWaterPath() {
        if (mBottlePath == null) {
            mBottlePath = new Path();
        } else {
            mBottlePath.reset();
        }
        mBottleStrokeWidth = (int) (BOTTLE_STROKE_WIDTH_TO_VIEW_HEIGHT * mViewRectF.height());
        mBottleCornerPathEffect = new CornerPathEffect(mBottleStrokeWidth);

        mBottlePaint.setStrokeWidth(mBottleStrokeWidth);
        mBottlePaint.setPathEffect(mBottleCornerPathEffect);

        // Generate left path
        int bottleHeight = (int) mViewRectF.height();
        int initDrawX = (int) (mViewRectF.left
                + bottleHeight * BOTTLE_LEFT_TOP_PADDING_LEFT_TO_VIEW_HEIGHT);
        int initDrawY = (int) (mViewRectF.top
                + bottleHeight * BOTTLE_LEFT_TOP_PADDING_TOP_TO_VIEW_HEIGHT);
        mBottlePath.moveTo(initDrawX, initDrawY);

        RectF bottleTopRectF = new RectF();
        int topCircleRadius = (int) (bottleHeight * BOTTLE_LEFT_TOP_CIRCLE_RADIUS_TO_VIEW_HEIGHT);
        bottleTopRectF.set(initDrawX - topCircleRadius, initDrawY,
                initDrawX + topCircleRadius, initDrawY + topCircleRadius * 2);
        mBottlePath.addArc(bottleTopRectF, -QUAR_FULL_ANGLE,
                -QUAR_FULL_ANGLE - ONE_ENGHTH_FULL_ANGLE);
        mBottlePath.rLineTo((float) Math.cos(Math.toRadians(ONE_ENGHTH_FULL_ANGLE)) * topCircleRadius,
                (float) Math.sin(Math.toRadians(ONE_ENGHTH_FULL_ANGLE)) * topCircleRadius);

        int lineLen = (int) (bottleHeight * BOTTLE_LEFT_TOP_STRAIGHT_LINE_TO_VIEW_HEIGHT);
        mBottlePath.rLineTo(0, lineLen);

        RectF bottleBottomRectF = new RectF();
        int bodyRectWidth = (int) (BOTTLE_LEFT_BODY_CIRCLE_RADIUS_TO_VIEW_HEIGHT * bottleHeight * 2);
        int bodyRectFLeft = (int) (BOTTLE_LEFT_BODY_CIRCLE_CENTER_X_VIEW_HEIGHT * bottleHeight
                + mViewRectF.left) - bodyRectWidth / 2;
        int bodyRectFTop = (int) (BOTTLE_LEFT_BODY_CIRCLE_CENTER_Y_VIEW_HEIGHT * bottleHeight
                + mViewRectF.top) - bodyRectWidth / 2;
        bottleBottomRectF.set(0, 0, bodyRectWidth, bodyRectWidth);
        bottleBottomRectF.offsetTo(bodyRectFLeft, bodyRectFTop);
        mBottlePath.arcTo(bottleBottomRectF,
                -BOTTLE_LEFT_BODY_CIRCLE_START_ANGLE - QUAR_FULL_ANGLE, -BOTTLE_LEFT_BODY_CIRCLE_TOTAL_ANGLE);

        int bottomLineLen = (int) (bottleHeight * BOTTLE_LEFT_BOTTOM_TO_VIEW_HEIGHT);
        mBottlePath.rLineTo(bottomLineLen, 0);

        // Generate the right path
        Camera camera = new Camera();
        Matrix matrix = new Matrix();
        camera.save();
        camera.rotateY(HALF_FULL_ANGLE);
        camera.getMatrix(matrix);
        camera.restore();
        matrix.preTranslate(-mViewRectF.centerX(), -mViewRectF.centerY());
        matrix.postTranslate(mViewRectF.centerX(), mViewRectF.centerY());

        Path rightBottlePath = new Path();
        rightBottlePath.addPath(mBottlePath);
        mBottlePath.addPath(rightBottlePath, matrix);

        // Compute the bounds
        if (mBottleRectF == null) {
            mBottleRectF = new RectF();
        }
        mBottlePath.computeBounds(mBottleRectF, false);

        // Move to the center of view
        float offsetX = mViewRectF.centerX() - mBottleRectF.centerX();
        float offsetY = mViewRectF.centerY() - mBottleRectF.centerY();
        mBottlePath.offset(offsetX, offsetY);
        mBottleRectF.offset(offsetX, offsetY);

        // Init static water
        bottleBottomRectF.inset(mBottleStrokeWidth, mBottleStrokeWidth);

        bottleBottomRectF.offset(offsetX, offsetY);
        if (mStaticWaterLeftCircleRectF == null) {
            mStaticWaterLeftCircleRectF = new RectF();
        }
        mStaticWaterLeftCircleRectF.set(bottleBottomRectF);

        bottleBottomRectF.offset((mBottleRectF.centerX() - bottleBottomRectF.centerX()) * 2, 0);
        if (mStaticWaterRightCircleRectF == null) {
            mStaticWaterRightCircleRectF = new RectF();
        }
        mStaticWaterRightCircleRectF.set(bottleBottomRectF);

        if (mStaticWaterRectF == null) {
            mStaticWaterRectF = new RectF();
        }
        mStaticWaterRectF.set(mStaticWaterLeftCircleRectF.left, mStaticWaterLeftCircleRectF.centerY(),
                mStaticWaterRightCircleRectF.right, mStaticWaterRightCircleRectF.bottom);

        mStaticWaterRotateAngle = BOTTLE_LEFT_BODY_CIRCLE_TOTAL_ANGLE
                + BOTTLE_LEFT_BODY_CIRCLE_START_ANGLE - QUAR_FULL_ANGLE;
        mStaticWaterBottomLineLen = (int) (bottleBottomRectF.width() / 2
                * Math.sin(Math.toRadians(QUAR_FULL_ANGLE - mStaticWaterRotateAngle))
                + mBottleRectF.centerX() - bottleBottomRectF.centerX() + 0.5) * 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        updateWaterPath();
        updateWaterDrop();

        // draw water drop
        Iterator<WaterDrop> iterator = mWaterDropList.iterator();
        while (iterator.hasNext()) {
            WaterDrop waterDrop = iterator.next();
            canvas.drawPath(waterDrop.mDropPath, mWaterPaint);
        }
        // draw water
        canvas.drawPath(mWaterPath, mWaterPaint);

        // draw bottle
        canvas.drawPath(mBottlePath, mBottlePaint);

        // draw assemble point
        drawDebugPoints(canvas);
    }

    private void updateWaterPath() {
        mFlowWaterHeight = (int) (mStaticWaterRectF.height() / 2);
        mFlowWaterWidth = (int) mStaticWaterRectF.width();
        int xOffset = mFlowWaterHeight / DEFAULT_FLOW_WATER_OFFSET_FACTOR;
        int yOffset = xOffset;
        mFlowWaterStartX = (int) mStaticWaterRectF.left + xOffset;
        mFlowWaterStartY = (int) mStaticWaterRectF.top - yOffset;
        mFlowWaterEndX = mFlowWaterStartX + mFlowWaterWidth - xOffset * 2;
        mFlowWaterEndY = mFlowWaterStartY;

        mFlowWaterFirstConX = (int) (mFlowWaterStartX +
                ((WATER_FIRST_CON_X_CHANGE_ARR[1] - WATER_FIRST_CON_X_CHANGE_ARR[0])
                        * mFlowWaterValue + WATER_FIRST_CON_X_CHANGE_ARR[0]) * mFlowWaterWidth);
        mFlowWaterFirstConY = (int) (mFlowWaterStartY -
                ((WATER_FIRST_CON_Y_CHANGE_ARR[1] - WATER_FIRST_CON_Y_CHANGE_ARR[0])
                        * mFlowWaterValue + WATER_FIRST_CON_Y_CHANGE_ARR[0]) * mFlowWaterWidth);
        mFlowWaterSecondConX = (int) (mFlowWaterStartX +
                ((WATER_SECOND_CON_X_CHANGE_ARR[1] - WATER_SECOND_CON_X_CHANGE_ARR[0])
                        * mFlowWaterValue + WATER_SECOND_CON_X_CHANGE_ARR[0]) * mFlowWaterWidth);
        mFlowWaterSecondConY = (int) (mFlowWaterStartY -
                ((WATER_SECOND_CON_Y_CHANGE_ARR[1] - WATER_SECOND_CON_Y_CHANGE_ARR[0])
                        * mFlowWaterValue + WATER_SECOND_CON_Y_CHANGE_ARR[0]) * mFlowWaterWidth);

        if (mWaterPath == null) {
            mWaterPath = new Path();
        } else {
            mWaterPath.reset();
        }
        mWaterPath.moveTo(mFlowWaterStartX, mFlowWaterStartY);
        mWaterPath.cubicTo(mFlowWaterFirstConX, mFlowWaterFirstConY,
                mFlowWaterSecondConX, mFlowWaterSecondConY, mFlowWaterEndX, mFlowWaterEndY);
        // SW is static Water, FW is flow water
        float rightFWTan = getPointAngleOnWater(mFlowWaterStartY, mFlowWaterFirstConY,
                mFlowWaterSecondConY, mFlowWaterEndY, FULL_NORMALIZED_TIME)
                / getPointAngleOnWater(mFlowWaterStartX, mFlowWaterFirstConX,
                mFlowWaterSecondConX, mFlowWaterEndX, FULL_NORMALIZED_TIME);

        int rightSWCircleRadius = (int) (mStaticWaterRightCircleRectF.width() / 2);
        float rightSWCircleAngle = (float) Math.toDegrees(Math.acos(
                (float) (rightSWCircleRadius - xOffset) / rightSWCircleRadius));
        float rightSWTan = (float) Math.tan(Math.toRadians(-QUAR_FULL_ANGLE + rightSWCircleAngle));
        int rightFWX = mFlowWaterEndX;
        int rightFWY = (int) (mStaticWaterRectF.top + Math.sin(Math.toRadians(
                rightSWCircleAngle)) * rightSWCircleRadius);
        int rightConX = (int) ((mFlowWaterEndY - rightFWY - rightFWTan * mFlowWaterEndX + rightSWTan * rightFWX)
                / (rightSWTan - rightFWTan));
        rightConX = (int) Math.min(mStaticWaterRectF.right + mBottleStrokeWidth * 3 / 4, rightConX);
        int rightConY = (int) ((rightConX - mFlowWaterEndX) * rightFWTan + mFlowWaterEndY);
        mWaterPath.quadTo(rightConX, rightConY, rightFWX, rightFWY);
        mWaterPath.arcTo(mStaticWaterRightCircleRectF, ZERO_ANGLE + rightSWCircleAngle,
                mStaticWaterRotateAngle - rightSWCircleAngle);
        mWaterPath.rLineTo(-mStaticWaterBottomLineLen, 0);
        mWaterPath.arcTo(mStaticWaterLeftCircleRectF, HALF_FULL_ANGLE - mStaticWaterRotateAngle,
                mStaticWaterRotateAngle - rightSWCircleAngle);

        float leftFWTan = getPointAngleOnWater(mFlowWaterStartY, mFlowWaterFirstConY,
                mFlowWaterSecondConY, mFlowWaterEndY, ZERO_NORMALIZED_TIME)
                / getPointAngleOnWater(mFlowWaterStartX, mFlowWaterFirstConX,
                mFlowWaterSecondConX, mFlowWaterEndX, ZERO_NORMALIZED_TIME);

        int leftSWX = mFlowWaterStartX;
        int leftSWY = rightFWY;
        float leftSWTan = -rightSWTan;
        int leftConX = (int) ((mFlowWaterStartY - leftSWY - leftFWTan
                * mFlowWaterStartX + leftSWTan * leftSWX)
                / (leftSWTan - leftFWTan));
        leftConX = (int) Math.max(mStaticWaterRectF.left - mBottleStrokeWidth * 3 / 4, leftConX);
        int leftConY = (int) ((leftConX - mFlowWaterStartX) * leftFWTan + mFlowWaterStartY);
        mWaterPath.quadTo(leftConX, leftConY, mFlowWaterStartX, mFlowWaterStartY);
    }

    private void drawDebugPoints(Canvas canvas) {
        if (mIsDebug) {
            Iterator<WaterDrop> iterator = mWaterDropList.iterator();
            while (iterator.hasNext()) {
                WaterDrop waterDrop = iterator.next();
                mDebugPaint.setColor(Color.RED);
                drawAssemblePoint(canvas, waterDrop.mInterDropAndWater.x, waterDrop.mInterDropAndWater.y);
                canvas.drawLine(waterDrop.mLeftWaterPoint.x, waterDrop.mLeftWaterPoint.y,
                        waterDrop.mRightWaterPoint.x, waterDrop.mRightWaterPoint.y, mWaterPaint);
                drawAssemblePoint(canvas, waterDrop.mLeftWaterPoint.x, waterDrop.mLeftWaterPoint.y);
                drawAssemblePoint(canvas, waterDrop.mRightWaterPoint.x, waterDrop.mRightWaterPoint.y);

                mDebugPaint.setColor(Color.WHITE);
                drawAssemblePoint(canvas, waterDrop.mRightDropPoint.x, waterDrop.mRightDropPoint.y);
                drawAssemblePoint(canvas, waterDrop.mLeftDropPoint.x, waterDrop.mLeftDropPoint.y);

                mDebugPaint.setStyle(Paint.Style.STROKE);
                canvas.drawArc(waterDrop.mDropCircleRectF, ZERO_ANGLE, FULL_ANGLE, false, mDebugPaint);
                mDebugPaint.setStyle(Paint.Style.FILL_AND_STROKE);

                mDebugPaint.setColor(Color.GREEN);
                drawAssemblePoint(canvas, waterDrop.mLeftControlPoint.x, waterDrop.mLeftControlPoint.y);
                drawAssemblePoint(canvas, waterDrop.mRightControlPoint.x, waterDrop.mRightControlPoint.y);

                String timeStr = "" + (System.currentTimeMillis() - mRecordCurrentMill);
                mDebugPaint.setColor(Color.RED);
                canvas.drawText(timeStr, mBottleRectF.centerX(), mBottleRectF.centerY(), mDebugPaint);
            }
        }
    }

    private void updateWaterDrop() {
        if (mWaterDropList == null || mWaterDropList.size() == 0) {
            return;
        }

        Iterator<WaterDrop> iterator = mWaterDropList.iterator();
        while (iterator.hasNext()) {
            WaterDrop waterDrop = iterator.next();
            float dropNormalizeTime = waterDrop.mNormalizedTime;

            if (waterDrop.mDropCircleRectF == null) {
                waterDrop.mDropCircleRectF = new RectF();
            }

            int halfWaterPointInterval = 0;
            // calculate interval distance between two water points
            if (dropNormalizeTime <= WATER_MOVE_SEASON_1) {
                float adjustNormalizeTime = dropNormalizeTime / WATER_MOVE_SEASON_1;
                halfWaterPointInterval = (int) (waterDrop.dropRadius
                        * (adjustNormalizeTime * (WATER_POINTS_MIN_INTERVAL_TO_DROP_RADIUS
                        - WATER_POINTS_MAX_INTERVAL_TO_DROP_RADIUS)
                        + WATER_POINTS_MAX_INTERVAL_TO_DROP_RADIUS));
            } else {
                halfWaterPointInterval = (int) (waterDrop.dropRadius
                        * WATER_POINTS_MIN_INTERVAL_TO_DROP_RADIUS);
            }

            waterDrop.mLeftWaterPoint.x = waterDrop.dropX - halfWaterPointInterval;
            waterDrop.mRightWaterPoint.x = waterDrop.dropX + halfWaterPointInterval;

            // Calculate waterPoints and angles
            float leftTime = calculateTbyX(mFlowWaterStartX, mFlowWaterFirstConX,
                    mFlowWaterSecondConX, mFlowWaterEndX, waterDrop.mLeftWaterPoint.x);
            waterDrop.mLeftWaterPoint.y = (int) getPointOnWater(mFlowWaterStartY,
                    mFlowWaterFirstConY, mFlowWaterSecondConY, mFlowWaterEndY, leftTime)
                    + mAdjustOffsetToWaterSur;
            float rightTime = calculateTbyX(mFlowWaterStartX, mFlowWaterFirstConX,
                    mFlowWaterSecondConX, mFlowWaterEndX, waterDrop.mRightWaterPoint.x);
            waterDrop.mRightWaterPoint.y =
                    (int) getPointOnWater(mFlowWaterStartY, mFlowWaterFirstConY,
                            mFlowWaterSecondConY, mFlowWaterEndY, rightTime) + mAdjustOffsetToWaterSur;

            float detLeftWaterPointX = getPointAngleOnWater(mFlowWaterStartX,
                    mFlowWaterFirstConX, mFlowWaterSecondConX, mFlowWaterEndX, leftTime);
            float detLeftWaterPointY = getPointAngleOnWater(mFlowWaterStartY,
                    mFlowWaterFirstConY, mFlowWaterSecondConY, mFlowWaterEndY, leftTime);
            float detLeftWaterPointTan = detLeftWaterPointY / detLeftWaterPointX;
            waterDrop.mLeftWaterPoint.tan = detLeftWaterPointTan;

            float detRightWaterPointX =
                    getPointAngleOnWater(mFlowWaterStartX, mFlowWaterFirstConX,
                            mFlowWaterSecondConX, mFlowWaterEndX, rightTime);
            float detRightWaterPointY =
                    getPointAngleOnWater(mFlowWaterStartY, mFlowWaterFirstConY,
                            mFlowWaterSecondConY, mFlowWaterEndY, rightTime);
            float detRightWaterPointTan = detRightWaterPointY / detRightWaterPointX;
            waterDrop.mRightWaterPoint.tan = detRightWaterPointTan;

            // Calculate the intersection of the vertical line of the circle and the water surface
            float middleTime = calculateTbyX(mFlowWaterStartX, mFlowWaterFirstConX,
                    mFlowWaterSecondConX, mFlowWaterEndX, waterDrop.mInterDropAndWater.x);
            waterDrop.mInterDropAndWater.x = waterDrop.dropX;
            waterDrop.mInterDropAndWater.y = (int) getPointOnWater(mFlowWaterStartY,
                    mFlowWaterFirstConY, mFlowWaterSecondConY, mFlowWaterEndY, middleTime);

            int detDisOfCircleCenter = (int) (dropNormalizeTime
                    * waterDrop.dropRadius * WATER_DROP_LEAVE_WATER_TOTAL_DISTANCE_TO_DROP_RADIUS);

            int circleCenY = (int) (waterDrop.mInterDropAndWater.y - (detDisOfCircleCenter -
                    waterDrop.dropRadius * WATER_DROP_UNDER_WATER_DISTANCE_TO_DROP_RADIUS));
            int circleCenX = waterDrop.dropX;

            waterDrop.mDropCircleRectF.set(circleCenX - waterDrop.dropRadius, circleCenY - waterDrop.dropRadius,
                    circleCenX + waterDrop.dropRadius, circleCenY + waterDrop.dropRadius);

            // State is move up and down beyond water, just draw the water drop
            if (waterDrop.mState == WaterDrop.STATE_MOVE) {
                waterDrop.mDropPath.reset();
                waterDrop.mDropPath.addArc(waterDrop.mDropCircleRectF, ZERO_ANGLE, FULL_ANGLE);
                continue;
            }

            // Calculate the middle point of water points
            waterDrop.mMiddleOfWaterPoints.x = (waterDrop.mLeftWaterPoint.x + waterDrop.mRightWaterPoint.x) / 2;
            waterDrop.mMiddleOfWaterPoints.y = (waterDrop.mLeftWaterPoint.y + waterDrop.mRightWaterPoint.y) / 2;
            waterDrop.mMiddleOfWaterPoints.tan =
                    (float) (waterDrop.mLeftWaterPoint.y - waterDrop.mRightWaterPoint.y)
                            / (waterDrop.mLeftWaterPoint.x - waterDrop.mRightWaterPoint.x);

            // Calculate cycle points
            int disToCircleTop = (int) Math.min(Math.max((waterDrop.mInterDropAndWater.y
                            - waterDrop.mDropCircleRectF.top) / 2 * 1.5f, 0),
                    waterDrop.dropRadius * MAX_DIS_TO_CIRCLE_TOP_RATIO);

            int angleTmp = (int) (Math.toDegrees(Math.acos(
                    (float) (waterDrop.dropRadius - disToCircleTop) / waterDrop.dropRadius)));
            int angleOfRightRadiusToHor =
                    (int) (QUAR_FULL_ANGLE - Math.toDegrees(Math.atan(waterDrop.mMiddleOfWaterPoints.tan)) - angleTmp);
            int angleOfLeftRadiusToHor = HALF_FULL_ANGLE - angleTmp * 2 - angleOfRightRadiusToHor;

            waterDrop.mRightDropPoint.x =
                    (int) (circleCenX + Math.cos(Math.toRadians(angleOfRightRadiusToHor)) * waterDrop.dropRadius);
            waterDrop.mRightDropPoint.y =
                    (int) (circleCenY - Math.sin(Math.toRadians(angleOfRightRadiusToHor)) * waterDrop.dropRadius);
            waterDrop.mRightDropPoint.tan =
                    (float) Math.tan(Math.toRadians(QUAR_FULL_ANGLE - angleOfRightRadiusToHor));

            waterDrop.mLeftDropPoint.x =
                    (int) (circleCenX - Math.cos(Math.toRadians(angleOfLeftRadiusToHor)) * waterDrop.dropRadius);
            waterDrop.mLeftDropPoint.y =
                    (int) (circleCenY - Math.sin(Math.toRadians(angleOfLeftRadiusToHor)) * waterDrop.dropRadius);
            waterDrop.mLeftDropPoint.tan =
                    (float) Math.tan(Math.toRadians(HALF_FULL_ANGLE - (QUAR_FULL_ANGLE - angleOfLeftRadiusToHor)));

            // Calculate control points
            // control point is the intersection of the tangent to the point on water point and the tangent to the point on drop
            // l_w: y - y_w = k_w * (x - x_w) -> y = y_w + k_w * (x - x_w)
            // l_d: y - y_d = k_d * (x - x_d) -> y = y_d + k_d * (x - x_d)
            // so, y_w + k_w * (x - x_w) = y_d + k_d * (x - x_d)
            // x = (y_w - y_d - k_w * x_w + k_d * x_d) / (k_d - k_w)
            float k_w1 = waterDrop.mLeftWaterPoint.tan;
            float k_d1 = waterDrop.mLeftDropPoint.tan;
            waterDrop.mLeftControlPoint.x =
                    (int) ((waterDrop.mLeftWaterPoint.y - waterDrop.mLeftDropPoint.y
                            - k_w1 * waterDrop.mLeftWaterPoint.x + k_d1 * waterDrop.mLeftDropPoint.x) / (k_d1 - k_w1));
            adjustPointBByPointA(waterDrop.mLeftWaterPoint, waterDrop.mLeftControlPoint, false, 0);
            waterDrop.mLeftControlPoint.y =
                    (int) (waterDrop.mLeftWaterPoint.y + k_w1
                            * (waterDrop.mLeftControlPoint.x - waterDrop.mLeftWaterPoint.x));

            float k_w2 = waterDrop.mRightWaterPoint.tan;
            float k_d2 = waterDrop.mRightDropPoint.tan;
            waterDrop.mRightControlPoint.x =
                    (int) ((waterDrop.mRightWaterPoint.y - waterDrop.mRightDropPoint.y
                            - k_w2 * waterDrop.mRightWaterPoint.x + k_d2 * waterDrop.mRightDropPoint.x) / (k_d2 - k_w2));
            adjustPointBByPointA(waterDrop.mRightWaterPoint, waterDrop.mRightControlPoint, true, 0);
            waterDrop.mRightControlPoint.y =
                    (int) (waterDrop.mRightWaterPoint.y
                            + k_w2 * (waterDrop.mRightControlPoint.x - waterDrop.mRightWaterPoint.x));

            // Generate water drop path
            if (waterDrop.mDropPath == null) {
                waterDrop.mDropPath = new Path();
            } else {
                waterDrop.mDropPath.reset();
            }

            if (!mViewRectF.contains(waterDrop.mLeftControlPoint.x, waterDrop.mLeftControlPoint.y)) {
                waterDrop.mLeftControlPoint.x = waterDrop.mLeftWaterPoint.x;
                waterDrop.mLeftControlPoint.y = waterDrop.mLeftWaterPoint.y;
            }

            if (!mViewRectF.contains(waterDrop.mRightControlPoint.x, waterDrop.mRightControlPoint.y)) {
                waterDrop.mRightControlPoint.x = waterDrop.mRightWaterPoint.x;
                waterDrop.mRightControlPoint.y = waterDrop.mRightWaterPoint.y;
            }

            // adjust control point
            if (waterDrop.mRightControlPoint.x - waterDrop.mLeftControlPoint.x <= 0) {
                waterDrop.mRightControlPoint.x =
                        (waterDrop.mRightControlPoint.x + waterDrop.mLeftControlPoint.x) / 2;
                waterDrop.mLeftControlPoint.x =
                        waterDrop.mRightControlPoint.x;
                waterDrop.mRightControlPoint.y =
                        (waterDrop.mRightControlPoint.y + waterDrop.mLeftControlPoint.y) / 2;
                waterDrop.mLeftControlPoint.y =
                        waterDrop.mRightControlPoint.y;
            }

            if (dropNormalizeTime >= WATER_MOVE_SEASON_2) {
                float adjustSeason2Time =
                        (dropNormalizeTime - WATER_MOVE_SEASON_2) / (WATER_MOVE_SEASON_3 - WATER_MOVE_SEASON_2);
                int topX = (waterDrop.mLeftDropPoint.x + waterDrop.mRightDropPoint.x) / 2;
                int detDis = (int) (-WATER_DROP_BROKEN_REMOVE_TAIL_TO_WATER_DISTANCE_TO_DROP_RADIUS
                        * waterDrop.dropRadius * (1f - adjustSeason2Time));
                int topY = Math.max(waterDrop.mInterDropAndWater.y + detDis, 0);


                int topToWaterLeftLen = (int) Math.sqrt(Math.pow(topX - waterDrop.mLeftWaterPoint.x, 2)
                        + Math.pow(topY - waterDrop.mLeftWaterPoint.y, 2));
                int topToWaterRightLen = (int) Math.sqrt(Math.pow(topX - waterDrop.mRightWaterPoint.x, 2)
                        + Math.pow(topY - waterDrop.mRightWaterPoint.y, 2));
                int totalLen = topToWaterLeftLen + topToWaterRightLen;
                float leftFactor = (float) topToWaterLeftLen / totalLen;
                float rightFactor = (float) topToWaterRightLen / totalLen;

                int totalTopLen = (int) (halfWaterPointInterval * 0.5f);
                int topLeftDetX = (int) (totalTopLen * leftFactor);
                int topRightDetX = (int) (totalTopLen * rightFactor);
                int topLeftDetY = (int) (topLeftDetX * waterDrop.mMiddleOfWaterPoints.tan);
                int topRightDetY = (int) (topRightDetX * waterDrop.mMiddleOfWaterPoints.tan);
                int leftTopConX = topX - topLeftDetX;
                int leftTopConY = topY - topLeftDetY;
                int rightTopConX = topX + topRightDetX;
                int rightTopConY = topY + topRightDetY;

                int totalAdjustLen = (int) (halfWaterPointInterval * 1.5f);
                int LeftBottomDetX = (int) (totalAdjustLen * leftFactor);
                int LeftBottomDetY = (int) (LeftBottomDetX * waterDrop.mMiddleOfWaterPoints.tan);
                int RightBottomDetX = (int) (totalAdjustLen * rightFactor);
                int RightBottomDetY = (int) (RightBottomDetX * waterDrop.mMiddleOfWaterPoints.tan);

                int leftBottomConX = waterDrop.mLeftWaterPoint.x + LeftBottomDetX;
                int leftBottomConY = waterDrop.mLeftWaterPoint.y + LeftBottomDetY;
                int rightBottomConX = waterDrop.mRightWaterPoint.x - RightBottomDetX;
                int rightBottomConY = waterDrop.mRightWaterPoint.y - RightBottomDetY;

                waterDrop.mDropPath.moveTo(waterDrop.mLeftWaterPoint.x, waterDrop.mLeftWaterPoint.y);
                waterDrop.mDropPath.cubicTo(leftBottomConX, leftBottomConY,
                        leftTopConX, leftTopConY, topX, topY);
                waterDrop.mDropPath.cubicTo(rightTopConX, rightTopConY,
                        rightBottomConX, rightBottomConY, waterDrop.mRightWaterPoint.x,
                        waterDrop.mRightWaterPoint.y);
                waterDrop.mDropPath.addArc(waterDrop.mDropCircleRectF, ZERO_ANGLE, FULL_ANGLE);
            } else {
                waterDrop.mDropPath.moveTo(waterDrop.mLeftWaterPoint.x, waterDrop.mLeftWaterPoint.y);
                waterDrop.mDropPath.quadTo(waterDrop.mLeftControlPoint.x, waterDrop.mLeftControlPoint.y,
                        waterDrop.mLeftDropPoint.x, waterDrop.mLeftDropPoint.y);
                waterDrop.mDropPath.arcTo(waterDrop.mDropCircleRectF, angleOfLeftRadiusToHor + HALF_FULL_ANGLE,
                        Math.max(angleTmp * 2, MIN_DRAW_ANGLE), false);
                waterDrop.mDropPath.quadTo(waterDrop.mRightControlPoint.x, waterDrop.mRightControlPoint.y,
                        waterDrop.mRightWaterPoint.x, waterDrop.mRightWaterPoint.y);
            }
        }
    }

    private int dipToPx(Context context, int dip) {
        return (int) (dip * getScreenDensity(context) + 0.5f);
    }

    private float getScreenDensity(Context context) {
        try {
            DisplayMetrics dm = new DisplayMetrics();
            ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                    .getMetrics(dm);
            return dm.density;
        } catch (Exception e) {
            return DisplayMetrics.DENSITY_DEFAULT;
        }
    }

    private void drawAssemblePoint(Canvas canvas, int centerX, int centerY) {
        if (mIsDebug) {
            canvas.drawCircle(centerX, centerY, mDebugPointRadius, mDebugPaint);
        }
    }


    public void performAnimation() {
        cancel();
        mWaterRoundCount = 0;
        mFlowWaterUpAnimator = ValueAnimator.ofFloat(0, 1);
        mFlowWaterUpAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mFlowWaterValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mFlowWaterUpAnimator.setDuration(WATER_ANIMATION_DURATION / 2);
        mFlowWaterUpAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimatorList.add(mFlowWaterUpAnimator);

        mFlowWaterDownAnimator = ValueAnimator.ofFloat(1, 0);
        mFlowWaterDownAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mFlowWaterValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mFlowWaterDownAnimator.setDuration(WATER_ANIMATION_DURATION / 2);
        mFlowWaterDownAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimatorList.add(mFlowWaterDownAnimator);

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(mFlowWaterUpAnimator, mFlowWaterDownAnimator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animatorSet.start();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (mWaterRoundCount == 0) {
                    performWaterDropAnimations();
                    mRecordCurrentMill = System.currentTimeMillis();
                }
                mWaterRoundCount++;
                mWaterRoundCount %= WATER_ROUND_COUNT;
            }
        });
        mAnimatorList.add(animatorSet);
        animatorSet.start();
    }

    private void performWaterDropAnimations() {
        performOneWaterDropAnimation(0.5f, 0.06f, 0.5f,
                WATER_ANIMATION_DURATION, (int) (WATER_ANIMATION_DURATION * 0.75f));
        performOneWaterDropAnimation(0.5f, 0.025f, 0.1f,
                WATER_ANIMATION_DURATION / 2, (int) (WATER_ANIMATION_DURATION * 0.8f));

        performOneWaterDropAnimation(0.25f, 0.035f, 0.2f,
                WATER_ANIMATION_DURATION / 2, (int) (WATER_ANIMATION_DURATION * 0.875f));

        performOneWaterDropAnimation(0.7f, 0.025f, 0.2f,
                WATER_ANIMATION_DURATION / 2, (int) (WATER_ANIMATION_DURATION * 1.5f));
        performOneWaterDropAnimation(0.85f, 0.03f, 0.2f,
                WATER_ANIMATION_DURATION / 2, (int) (WATER_ANIMATION_DURATION * 1.7f));

        performOneWaterDropAnimation(0.25f, 0.035f, 0.2f,
                WATER_ANIMATION_DURATION / 2, (int) (WATER_ANIMATION_DURATION * 1.85f));

        performOneWaterDropAnimation(0.5f, 0.02f, 0.5f,
                WATER_ANIMATION_DURATION, (int) (WATER_ANIMATION_DURATION * 1.85f));
        performOneWaterDropAnimation(0.5f, 0.01f, 0.2f,
                WATER_ANIMATION_DURATION / 2, (int) (WATER_ANIMATION_DURATION * 1.85f));

        performOneWaterDropAnimation(0.5f, 0.05f, 0.05f,
                WATER_ANIMATION_DURATION / 2, (int) (WATER_ANIMATION_DURATION * 3f));
        performOneWaterDropAnimation(0.5f, 0.03f, 0.2f,
                WATER_ANIMATION_DURATION / 2, (int) (WATER_ANIMATION_DURATION * 3f));

        performOneWaterDropAnimation(0.55f, 0.04f, 0.75f,
                (int) (WATER_ANIMATION_DURATION * 1.3f), (int) (WATER_ANIMATION_DURATION * 4.5f));
        performOneWaterDropAnimation(0.45f, 0.03f, 0.375f,
                WATER_ANIMATION_DURATION / 2, (int) (WATER_ANIMATION_DURATION * 4f));
        performOneWaterDropAnimation(0.45f, 0.03f, 0.06f,
                WATER_ANIMATION_DURATION / 4, (int) (WATER_ANIMATION_DURATION * 4.5f));

        performOneWaterDropAnimation(0.5f, 0.02f, 1f,
                WATER_ANIMATION_DURATION * 3 / 2, (int) (WATER_ANIMATION_DURATION * 5.5f));
        performOneWaterDropAnimation(0.5f, 0.05f, 0.075f,
                WATER_ANIMATION_DURATION / 2, (int) (WATER_ANIMATION_DURATION * 5.5f));
        performOneWaterDropAnimation(0.5f, 0.05f, 0.075f,
                WATER_ANIMATION_DURATION / 4, (int) (WATER_ANIMATION_DURATION * 7f));

        performOneWaterDropAnimation(0.45f, 0.07f, 0.01f,
                WATER_ANIMATION_DURATION / 2, (int) (WATER_ANIMATION_DURATION * 6f));
        performOneWaterDropAnimation(0.55f, 0.07f, 0.01f,
                WATER_ANIMATION_DURATION / 2, (int) (WATER_ANIMATION_DURATION * 6f));
        performOneWaterDropAnimation(0.55f, 0.03f, 0.2f,
                WATER_ANIMATION_DURATION / 2, (int) (WATER_ANIMATION_DURATION * 6f));

        performOneWaterDropAnimation(0.25f, 0.04f, 0.05f,
                WATER_ANIMATION_DURATION / 2, (int) (WATER_ANIMATION_DURATION * 6f));
        performOneWaterDropAnimation(0.6f, 0.04f, 0.075f,
                WATER_ANIMATION_DURATION / 2, (int) (WATER_ANIMATION_DURATION * 6.5f));
        performOneWaterDropAnimation(0.25f, 0.03f, 0.3f,
                WATER_ANIMATION_DURATION / 2, (int) (WATER_ANIMATION_DURATION * 7.2f));
        performOneWaterDropAnimation(0.5f, 0.04f, 0.05f,
                WATER_ANIMATION_DURATION / 2, (int) (WATER_ANIMATION_DURATION * 7.5f));
        performOneWaterDropAnimation(0.45f, 0.02f, 0.3f,
                WATER_ANIMATION_DURATION / 2, (int) (WATER_ANIMATION_DURATION * 7.7f));

        performOneWaterDropAnimation(0.5f, 0.05f, 1f,
                (int) (WATER_ANIMATION_DURATION * 1.5f), (int) (WATER_ANIMATION_DURATION * 8f));
        performOneWaterDropAnimation(0.5f, 0.03f, 0.5f,
                (int) (WATER_ANIMATION_DURATION * 1f), (int) (WATER_ANIMATION_DURATION * 8f));
        performOneWaterDropAnimation(0.5f, 0.01f, 0.2f,
                WATER_ANIMATION_DURATION / 2, (int) (WATER_ANIMATION_DURATION * 8f));
        performOneWaterDropAnimation(0.4f, 0.025f, 0.3f,
                WATER_ANIMATION_DURATION / 2, (int) (WATER_ANIMATION_DURATION * 8.5f));
        performOneWaterDropAnimation(0.35f, 0.025f, 0.2f,
                WATER_ANIMATION_DURATION / 2, (int) (WATER_ANIMATION_DURATION * 9.2f));
    }

    /**
     * perfrom one water drop animation
     * @param dropCenterXRatio
     * @param dropRadiusRatio
     * @param dropTotalDisRatio
     * @param totalTime
     * @param delayTime
     */
    private void performOneWaterDropAnimation(
            final float dropCenterXRatio, final float dropRadiusRatio,
            final float dropTotalDisRatio, final int totalTime, int delayTime) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mRunnableList != null) {
                    mRunnableList.remove(this);
                }

                final WaterDrop waterDrop;
                if (!mWaterDropWaitList.isEmpty()) {
                    waterDrop = mWaterDropWaitList.remove(0);
                } else {
                    waterDrop = new WaterDrop();
                }
                waterDrop.dropRadius = (int) (mFlowWaterWidth * dropRadiusRatio);
                waterDrop.dropX = (int) (dropCenterXRatio * mFlowWaterWidth + mFlowWaterStartX);
                mWaterDropList.add(waterDrop);

                float endValue = dropTotalDisRatio /
                        (dropRadiusRatio * WATER_DROP_LEAVE_WATER_TOTAL_DISTANCE_TO_DROP_RADIUS);

                final ValueAnimator dropUp = ValueAnimator.ofFloat(0, endValue);
                dropUp.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        waterDrop.mNormalizedTime = (float) animation.getAnimatedValue();
                        if (waterDrop.mNormalizedTime > FULL_NORMALIZED_TIME) {
                            waterDrop.mState = WaterDrop.STATE_MOVE;
                        } else {
                            waterDrop.mState = WaterDrop.STATE_LEAVE_WATER;
                        }
                    }
                });
                dropUp.setInterpolator(new DecelerateInterpolator());
                dropUp.setDuration(totalTime / 2);
                mAnimatorList.add(dropUp);

                final ValueAnimator dropDown = ValueAnimator.ofFloat(endValue, 0);
                dropDown.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        waterDrop.mNormalizedTime = (float) animation.getAnimatedValue();
                        if (waterDrop.mNormalizedTime > FULL_NORMALIZED_TIME) {
                            waterDrop.mState = WaterDrop.STATE_MOVE;
                        } else {
                            waterDrop.mState = WaterDrop.STATE_LEAVE_WATER;
                        }
                    }
                });
                dropDown.setInterpolator(new AccelerateInterpolator());
                dropDown.setDuration(totalTime / 2);
                mAnimatorList.add(dropDown);

                final AnimatorSet dropAnimatorSet = new AnimatorSet();
                dropAnimatorSet.playSequentially(dropUp, dropDown);
                dropAnimatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mWaterDropList.remove(waterDrop);
                        mWaterDropWaitList.add(waterDrop);
                    }
                });
                mAnimatorList.add(dropAnimatorSet);
                dropAnimatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (mAnimatorList != null) {
                            mAnimatorList.remove(dropUp);
                            mAnimatorList.remove(dropDown);
                            mAnimatorList.remove(dropAnimatorSet);
                        }
                    }
                });
                dropAnimatorSet.start();
            }
        };

        mRunnableList.add(runnable);
        mMainHandler.postDelayed(runnable, delayTime);
    }

    public void cancel() {
        if (mAnimatorList != null && !mAnimatorList.isEmpty()) {
            for (Animator animator : mAnimatorList) {
                if (animator == null) {
                    continue;
                }
                animator.removeAllListeners();
                if (animator instanceof ValueAnimator) {
                    ((ValueAnimator) animator).removeAllUpdateListeners();
                }
                animator.cancel();
            }
        }
        mAnimatorList.clear();

        if (mRunnableList != null && !mRunnableList.isEmpty()) {
            for (Runnable runnable : mRunnableList) {
                mMainHandler.removeCallbacks(runnable);
            }
        }
        mRunnableList.clear();
        mWaterDropList.clear();
        mWaterDropWaitList.clear();
    }

    public void setDebug(boolean state) {
        if (state && !mIsDebug) {
            mDebugPaint = new Paint();
            mDebugPaint.setColor(Color.RED);
            mDebugPaint.setAntiAlias(true);
            mDebugPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mDebugPaint.setStrokeWidth(dipToPx(getContext(), DEFAULT_DEBUG_PAINT_STROKE_WIDTH));
            mDebugPointRadius = dipToPx(getContext(), DEFAULT_DEBUG_POINT_RADIUS);
            mWaterPaint.setStyle(Paint.Style.STROKE);
            mDebugPaint.setTextSize(dipToPx(getContext(), DEFAULT_DEBUG_PAINT_TEXT_SIZE));
        } else if (!state && mIsDebug) {
            mDebugPaint = null;
            mWaterPaint.setStyle(Paint.Style.FILL);
        }
        mIsDebug = state;
    }

    private void adjustPointBByPointA(PointWithAngle pointA, PointWithAngle pointB, boolean onLeft, int minDet) {
        if (onLeft) {
            int det = pointA.x - pointB.x;
            pointB.x = pointA.x - Math.max(det, minDet);
        } else {
            int det = pointB.x - pointA.x;
            pointB.x = pointA.x + Math.max(det, minDet);
        }
    }

    /**
     * Get point (x or y) of water path
     * @param p0
     * @param p1
     * @param p2
     * @param p3
     * @param t
     * @return
     */
    private float getPointOnWater(float p0, float p1, float p2, float p3, float t) {
        float one_t = 1 - t;
        return p0 * one_t * one_t * one_t + 3 * p1 * t * one_t * one_t + 3 * p2 * t * t * one_t + p3 * t * t * t;
    }

    /**
     * Binary search method
     * @param p0
     * @param p1
     * @param p2
     * @param p3
     * @param x
     * @return
     */
    private float calculateTbyX(float p0, float p1, float p2, float p3, float x) {
        float t = 0;
        float t_pre = 0;
        float t_next = 1;

        int maxTimes = 10;
        int times = 0;
        float tmpX = x;
        t = (tmpX - p0) / (p3 - p0);

        while (times < maxTimes) {
            tmpX = getPointOnWater(p0, p1, p2, p3, t);
            float det = x - tmpX;
            if (Math.abs(det) < ERROR_OF_FIT_WATER_FUNC) {
                return t;
            }
            times++;

            if (det > 0) {
                t_pre = t;
                t = (t + t_next) / 2;
            } else {
                t_next = t;
                t = (t_pre + t) / 2;
            }
        }
        return t;
    }

    /**
     * Get tan of water path
     * @param p0
     * @param p1
     * @param p2
     * @param p3
     * @param t
     * @return
     */
    private float getPointAngleOnWater(float p0, float p1, float p2, float p3, float t) {
        float one_t = 1 - t;
        return 3 * (p1 - p0) * one_t * one_t + 6 * (p2 - p1) * t * one_t + 3 * (p3 - p2) * t * t;
    }
}
