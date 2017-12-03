/*
 * Copyright (C) 2017 Capricorn
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


package com.realmo.view;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;

import java.util.Arrays;


/**
 * @author Realmo
 * @version 1.0.3
 * @name BezierRoundView
 * @email momo.weiye@gmail.com
 * @time 2017/12/2 13:49
 * @describe ����ViewPagerʹ��
 *
 */
public class BezierRoundView extends View implements ViewPager.OnPageChangeListener {


    private final String TAG = "realmo";
    private int time_animator = 600;  //����ʱ��
    private Matrix matrix_bounceL;   //�����ҵ��Ķ�����Ϊ����
    private int color_select = 0xfffe626d;
    private int color_touch = 0xfffe626d;
    private int color_default = Color.GRAY;

    private int DEFAULT_WIDTH;
    private int DEFAULT_HEIGHT;
    private int default_round_count = 4;   //Ĭ��Բ�������

    private ViewPager mViewPage;

    //չʾ���Զ���
    private ValueAnimator animatorStart;
    private TimeInterpolator timeInterpolator = new DecelerateInterpolator();
    private float animatedValue;
    private boolean isAniming = false;

    private ValueAnimator animatorTouch;
    private boolean isTouchAniming = false;
    private float animatedTouchValue;
    private RectF rectF_touch = new RectF();  //����������Χ


    private Paint mBezPaint;
    private Paint mRoundStrokePaint;
    private Paint mTouchPaint;
    private int mWidth;
    private int mHeight;
    private int mRadius;
    private int mDistance;
    private final float bezFactor = 0.551915024494f;
    private Xfermode clearXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private Path mBezPath;

    private PointF p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11;


    private float rRadio = 1;  //P2,3,4 x�ᱶ��
    private float lRadio = 1;  //P8,9,10����
    private float tbRadio = 1;  //y�����ű���
    private float boundRadio = 0.55f;  //������һ��Բ�Ļص�Ч��

    /**
     * �뿪Բ����ֵ
     */
    private float disL = 0.5f;
    /**
     * ���ֵ����ֵ
     */
    private float disM = 0.8f;
    /**
     * �����¸�Բ����ֵ
     */
    private float disA = 0.9f;


    private float[] bezPos; //��¼ÿһ��Բ��x���λ��
    private float[] xPivotPos;  //����Բ��x��+mRadius�����ֳɲ�ͬ������ ,��ҪΪ���жϴ���x���λ��
    private int curPos = 0;  //��ǰԲ��λ��
    private int nextPos = 0; //ԲҪ�������һ��λ��


    public BezierRoundView(Context context) {
        this(context, null);
    }

    public BezierRoundView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BezierRoundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());  //Ĭ������15dp

        // ��ȡ�Զ�����ʽ����
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BezierRoundView, defStyleAttr, 0);
        color_select = array.getColor(R.styleable.BezierRoundView_color_select, color_select);
        color_touch = array.getColor(R.styleable.BezierRoundView_color_touch, color_touch);
        color_default = array.getColor(R.styleable.BezierRoundView_color_not_select, color_default);
        time_animator = array.getInteger(R.styleable.BezierRoundView_time_animator, time_animator);
        default_round_count = array.getInteger(R.styleable.BezierRoundView_round_count, default_round_count);
        mRadius = array.getDimensionPixelSize(R.styleable.BezierRoundView_radius, mRadius);
        mDistance = array.getDimensionPixelSize(R.styleable.BezierRoundView_distance, mDistance);
        array.recycle();

        init();
    }



    private void init() {

        DEFAULT_HEIGHT = mRadius * 3;
        mBezPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBezPaint.setColor(color_select);
        mBezPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mRoundStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRoundStrokePaint.setColor(color_default);
        mRoundStrokePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mTouchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTouchPaint.setColor(color_touch);
        mTouchPaint.setStyle(Paint.Style.FILL);
        mTouchPaint.setXfermode(clearXfermode);

        mBezPath = new Path();

        //y��һ��
        p5 = new PointF(mRadius * bezFactor, mRadius);
        p6 = new PointF(0, mRadius);
        p7 = new PointF(-mRadius * bezFactor, mRadius);
        //y��һ��
        p0 = new PointF(0, -mRadius);
        p1 = new PointF(mRadius * bezFactor, -mRadius);
        p11 = new PointF(-mRadius * bezFactor, -mRadius);
        //x��һ��
        p2 = new PointF(mRadius, -mRadius * bezFactor);
        p3 = new PointF(mRadius, 0);
        p4 = new PointF(mRadius, mRadius * bezFactor);
        //x��һ��
        p8 = new PointF(-mRadius, mRadius * bezFactor);
        p9 = new PointF(-mRadius, 0);
        p10 = new PointF(-mRadius, -mRadius * bezFactor);

        matrix_bounceL = new Matrix();
        matrix_bounceL.preScale(-1, 1);
    }



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (DEFAULT_WIDTH == 0) {
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            DEFAULT_WIDTH = wm.getDefaultDisplay().getWidth();

        }

        int width = measureSize(1, DEFAULT_WIDTH, widthMeasureSpec);
        int height = measureSize(1, DEFAULT_HEIGHT, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;

        initCountPos();
    }

    /**
     * ���measure
     *
     * @param specType    1Ϊ�� ����Ϊ��
     * @param contentSize Ĭ��ֵ
     */
    private int measureSize(int specType, int contentSize, int measureSpec) {
        int result;
        //��ȡ������ģʽ��Size
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = Math.min(contentSize, specSize);
        } else {
            result = contentSize;

            if (specType == 1) {
                // ���ݴ��뷽ʽ�����
                result += (getPaddingLeft() + getPaddingRight());
            } else {
                // ���ݴ��뷽ʽ�����
                result += (getPaddingTop() + getPaddingBottom());
            }
        }

        return result;
    }




    private void initCountPos() {
        bezPos = new float[default_round_count];
        xPivotPos = new float[default_round_count];

        if(mDistance == 0){
            mDistance = mWidth/(default_round_count + 1);
        }
        float startX = mWidth/2 - mDistance*(default_round_count-1)/2;
        for (int i = 0; i < default_round_count; i++) {

            bezPos[i] = startX + mDistance*i;
            xPivotPos[i] = startX + mDistance*i + mRadius;


        }

//        for (int i = 0; i < default_round_count; i++) {
//            bezPos[i] = mWidth / (default_round_count + 1) * (i + 1);
//            xPivotPos[i] = mWidth / (default_round_count + 1) * (i + 1) + mRadius;
//
//        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                float y = event.getY();

                if (y <= mHeight / 2 + mRadius && y >= mHeight / 2 - mRadius && !isAniming) {  //���ж�y�����y�������Բy��ķ�Χ
                    int pos = -Arrays.binarySearch(xPivotPos, x) - 1;
                    if (pos >= 0 && pos < default_round_count && x + mRadius >= bezPos[pos]) {
                        nextPos = pos;

//                        Log.e(TAG, "ontouch  curPos" + curPos);
//                        Log.e(TAG, "ontouch  nextPos" + nextPos);
//                        Log.e(TAG, "ontouch  isAniming" + isAniming);
                        if (mViewPage != null && curPos != nextPos) {

                            mViewPage.setCurrentItem(pos);
                            isAniming = true;
                            direction = (curPos < pos);

                            startAnimator();
                            startTouchAnimator();
                        }
                    }
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * ����ViewPager������scroll���иı�bezRound
     */
    public void attach2ViewPage(ViewPager vPage) {
        vPage.addOnPageChangeListener(this);
        this.mViewPage = vPage;
        if (PagerAdapter.class.isInstance(vPage.getAdapter())) {
            this.default_round_count = vPage.getAdapter().getCount();
            initCountPos();
        }
    }



    public void startAnimator() {
        if (animatorStart != null) {
            if (animatorStart.isRunning()) {
                return;
            }
            animatorStart.start();
        } else {
            animatorStart = ValueAnimator.ofFloat(0, 1f).setDuration(time_animator);
            animatorStart.setInterpolator(timeInterpolator);
            animatorStart.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    isAniming = true;
                    animatedValue = (float) animation.getAnimatedValue();
                    invalidate();

                }
            });
            animatorStart.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    isAniming = true;

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    isAniming = false;
                    curPos = nextPos;

                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    isAniming = false;
                    curPos = nextPos;

                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });

            animatorStart.start();
        }
    }

    private void startTouchAnimator() {
        //���ô�����Χ
        rectF_touch.set(bezPos[nextPos] - mRadius * 1.5f, -mRadius * 1.5f, bezPos[nextPos] + mRadius * 1.5f, mRadius * 1.5f);

        if (animatorTouch != null) {
            if (animatorTouch.isRunning()) {
                return;
            }
            isTouchAniming = true;
            animatorTouch.start();
        } else {
            animatorTouch = ValueAnimator.ofFloat(0, mRadius * 1.5f).setDuration(time_animator / 2);
            animatorTouch.setInterpolator(timeInterpolator);
            animatorTouch.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    animatedTouchValue = (float) animation.getAnimatedValue();
                    if (animatedTouchValue == mRadius * 1.5f) {
                        isTouchAniming = false;
                    }
                }
            });
            isTouchAniming = true;
            animatorTouch.start();
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(0, mHeight / 2);


        mBezPath.reset();
        for (int i = 0; i < default_round_count; i++) {
            if(i == curPos){
                canvas.drawCircle(bezPos[i], 0, mRadius, mBezPaint);   //���Ƶ�ǰԲ
            }else{
                canvas.drawCircle(bezPos[i], 0, mRadius, mRoundStrokePaint);   //��������Բ
            }

        }
        if (animatedValue == 1 || animatedValue == 0) {
            //canvas.drawCircle(bezPos[nextPos], 0, mRadius, mBezPaint);
            return;
        }


        if (isTouchAniming) {  //ʵ�� ��������
            int count = canvas.saveLayer(rectF_touch, mTouchPaint, Canvas.ALL_SAVE_FLAG);
            canvas.drawCircle(bezPos[nextPos], 0, animatedTouchValue, mTouchPaint);   //�Ȼ�һ����ɫ��Բ [0,mRadius*1.5]

            mTouchPaint.setXfermode(clearXfermode);

            canvas.drawCircle(bezPos[nextPos], 0, mRadius * 0.7f, mTouchPaint);  //�� 0.7-1.4   Ч������ÿ���

            if (animatedTouchValue >= mRadius) {             //�����ɫ��Բ�뾶>=mRadis ���Ϳ�ʼ����͸����Բ
                canvas.drawCircle(bezPos[nextPos], 0, (animatedTouchValue - mRadius) / 0.5f * 1.4f, mTouchPaint);
            }
            mTouchPaint.setXfermode(null);


            canvas.restoreToCount(count);

        }

        canvas.translate(bezPos[curPos], 0);

        if (0 < animatedValue && animatedValue <= disL) {
            rRadio = 1f + animatedValue * 2;                         //  [1,2]
            lRadio = 1f;
            tbRadio = 1f;
        }
        if (disL < animatedValue && animatedValue <= disM) {
            rRadio = 2 - range0Until1(disL, disM) * 0.5f;          //  [2,1.5]
            lRadio = 1 + range0Until1(disL, disM) * 0.5f;          // [1,1.5]
            tbRadio = 1 - range0Until1(disL, disM) / 3;           // [1 , 2/3]
        }
        if (disM < animatedValue && animatedValue <= disA) {
            rRadio = 1.5f - range0Until1(disM, disA) * 0.5f;     //  [1.5,1]
            lRadio = 1.5f - range0Until1(disM, disA) * (1.5f - boundRadio);      //����Ч�������� �ڵ�boundRadio
            tbRadio = (range0Until1(disM, disA) + 2) / 3;        // [ 2/3,1]
        }
        if (disA < animatedValue && animatedValue <= 1f) {
            rRadio = 1;
            tbRadio = 1;
            lRadio = boundRadio + range0Until1(disA, 1) * (1 - boundRadio);     //����Ч��������
        }
        if (animatedValue == 1 || animatedValue == 0) {  //��ֹ����ֱ��Ļ���
            rRadio = 1f;
            lRadio = 1f;
            tbRadio = 1f;
        }


        boolean isTrans = false;
       // float transX = (nextPos - curPos) * (mWidth / (default_round_count + 1));
        float transX = (nextPos - curPos) * mDistance;
        if (disL <= animatedValue && animatedValue <= disA) {
            isTrans = true;

            transX = transX * (animatedValue - disL) / (disA - disL);
        }
        if (disA < animatedValue && animatedValue <= 1) {
            isTrans = true;
        }
        if (isTrans) {
            canvas.translate(transX, 0);
        }

        bounce2RightRound();


        if (!direction) {
            mBezPath.transform(matrix_bounceL);
        }
        canvas.drawPath(mBezPath, mBezPaint);

        if (isTrans) {
            canvas.save();
        }

    }

    /**
     * ͨ�� path �����ҵ���Ķ������Ƴ���
     * ���Ҫ��������Ķ�����ֻҪ����path��transform(matrix)����
     */
    private void bounce2RightRound() {
        mBezPath.moveTo(p0.x, p0.y * tbRadio);
        mBezPath.cubicTo(p1.x, p1.y * tbRadio, p2.x * rRadio, p2.y, p3.x * rRadio, p3.y);
        mBezPath.cubicTo(p4.x * rRadio, p4.y, p5.x, p5.y * tbRadio, p6.x, p6.y * tbRadio);
        mBezPath.cubicTo(p7.x, p7.y * tbRadio, p8.x * lRadio, p8.y, p9.x * lRadio, p9.y);
        mBezPath.cubicTo(p10.x * lRadio, p10.y, p11.x, p11.y * tbRadio, p0.x, p0.y * tbRadio);
        mBezPath.close();
    }


    /**
     * ��animatedValueֵ��ת��Ϊ[0,1]
     *
     * @param minValue ���ڵ���
     * @param maxValue С�ڵ���
     * @return ���ݵ�ǰ animatedValue,���� [0,1] ��Ӧ����ֵ
     */
    private float range0Until1(float minValue, float maxValue) {
        return (animatedValue - minValue) / (maxValue - minValue);
    }


    private boolean direction; //���� , true��λ������(0->1)

    /**
     * @param position       ��ǰcurλ�ã������ǰ��1����ָ�һ���vPage���󻬶����Ǿ���0�� ������һ��λ�ò�Ϊ2
     * @param positionOffset [0,1) ,������һ��pos����Ϊ0
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (isAniming)     //������ֶ�ѡ��pos������animatorValueȥ���� 0-->3
            return;
        //Log.w(TAG, "onPageScrolled positionOffset==" + positionOffset);

        animatedValue = positionOffset;

        direction = ((position + positionOffset) - curPos > 0);  //�˶����� trueΪ�ұ�(�����󻬶�)
        nextPos = direction ? curPos + 1 : curPos - 1;  //�� +1   �� -1

        if (!direction)   //���������
            animatedValue = 1 - animatedValue;  //�� animatedValue �������󻬻����һ�������[0,1)��ʼ����

        if (positionOffset == 0) {
            curPos = position;
            nextPos = position;
        }

        //���ٻ�����ʱ��positionOffset�п��ܲ�������0
        if (direction && position + positionOffset > nextPos) {  //���ң�����
            curPos = position;
            nextPos = position + 1;
        } else if (!direction && position + positionOffset < nextPos) {
            curPos = position;
            nextPos = position - 1;
        }


//        Log.w(TAG, "onPageScrolled animatedValue==" + animatedValue);
//        Log.w(TAG, "onPageScrolled direction==" + direction);
//        Log.w(TAG, "onPageScrolled curPos==" + curPos);
//        Log.w(TAG, "onPageScrolled nextPos==" + nextPos);
//        Log.w(TAG, "onPageScrolled position==" + position);

        invalidate();
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public void setAnimDuration(int time) {
        time_animator = time;
    }

    /**
     * ����Բ�İ뾶
     */
    public void setRadius(int radius) {
        this.mRadius = radius;
        init();
    }

    /**
     *
     * ����bezԲ֮ǰ�ľ��룬Ĭ���� �ؼ����/��bezԲ��+1��
     */
    public void setDistance(int distance){
        this.mDistance = distance;
        initCountPos();
    }

    /**
     * ����bez Բ��������Ĭ��4��
     */
    public void setRoundCount(int count) {
        this.default_round_count = count;
        initCountPos();
    }

    /**
     * ����bez Բ����ɫ��Ĭ�Ϸۺ�ɫ
     */
    public void setBezRoundColor(int roundcolor) {
        color_select = roundcolor;
        mBezPaint.setColor(roundcolor);
    }

    /**
     * ����Ч����ɫ��Ĭ�Ϸۺ�ɫ
     */
    public void setTouchColor(int touchColor) {
        color_touch = touchColor;
        mTouchPaint.setColor(touchColor);
    }

    /**
     * Բ�����ɫ
     */
    public void setStrokeColor(int strokeColor) {
        color_default = strokeColor;
        mRoundStrokePaint.setColor(strokeColor);
    }

}