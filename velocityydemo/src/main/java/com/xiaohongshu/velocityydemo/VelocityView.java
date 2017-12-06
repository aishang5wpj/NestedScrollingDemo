package com.xiaohongshu.velocityydemo;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * Created by wupengjian on 17/11/23.
 */

public class VelocityView extends FrameLayout {

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    public VelocityView(@NonNull Context context) {
        this(context, null);
    }

    public VelocityView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VelocityView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(context, new AccelerateDecelerateInterpolator());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    mVelocityTracker.clear();
                }
                mVelocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mVelocityTracker.computeCurrentVelocity(1000);
                int velocity = (int) mVelocityTracker.getYVelocity();
                if (Math.abs(velocity) > 50) {
                    fling(velocity);
                }
                break;
        }
        return true;
    }

    /**
     * 1、{@link VelocityTracker#getYVelocity()} 返回值的正负表示速度的方向
     * 2、{@link Scroller#fling(int, int, int, int, int, int, int, int)} 中的velocity会
     * 区分正负，而且maxX-minX和maxY-minY值的正负要保持一致(maxY表示最大滚动距离)，即：
     * ①，当velocityY<0时，此时滚动效果等同于手指从下到上时的滚动，相机镜头从上往下，mScrollY由小变大
     * ②，当velocityY>0时，则相反，此时滚动效果等同于手指从上到下时的滚动
     *
     * @param velocityY
     */
    public void fling(int velocityY) {
//        mScroller.fling(getScrollX(), getScrollY(), 0, -velocityY, 0, 0, 0, 0); // n
//        mScroller.fling(0, 0, 0, -velocityY, 0, 0, 0, 300); // n
//        mScroller.fling(0, 0, 0, -velocityY, 0, 0, -300, 0); // y
//        mScroller.fling(0, 0, 0, -velocityY, 0, 0, -300, -200); // y
//        mScroller.fling(0, 0, 0, -velocityY, 0, 0, -300, 300); // y
//        mScroller.fling(getScrollX(), getScrollY(), 0, -velocityY, 0, 0, getScrollY() - 300, getScrollY() + 300); // y
//        mScroller.fling(getScrollX(), getScrollY(), 0, -velocityY, 0, 0, -900, 0); // y
//        mScroller.fling(getScrollX(), getScrollY(), 0, velocityY, 0, 0, 0, 900); // y
//        mScroller.fling(0, 0, 0, -velocityY, 0, 0, -900, 0); // y
//        mScroller.fling(0, 0, 0, velocityY, 0, 0, 0, 900); // y
//        mScroller.fling(0, 0, 0, velocityY, 0, 0, 0, 900); // y
//        mScroller.fling(0, 0, 0, velocityY, 0, 0, 300, 900); // n
//        mScroller.fling(0, 0, 0, velocityY, 0, 0, -100, 100); // y
//        mScroller.fling(0, 0, 0, -velocityY, 0, 0, -100, 100); // y
        mScroller.fling(0, 0, 0, -velocityY, 0, 0, -100, 0); // y
        invalidate();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }
}
