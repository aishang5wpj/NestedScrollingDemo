package com.xiaohongshu.nestedscrollingrecyclerview;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.webkit.WebView;
import android.widget.Scroller;

/**
 * Created by wupengjian on 17/11/23.
 */
public class NestedScrollingWebView extends WebView implements NestedScrollingChild, IScrollingHelper {

    private NestedScrollingChildHelper mChildHelper;
    private int[] mConsume = new int[2];
    private int[] mOffsetInWindow = new int[2];
    private float mLastY;

    private boolean mIsFling;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    public NestedScrollingWebView(Context context) {
        this(context, null);
    }

    public NestedScrollingWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestedScrollingWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(context, new LinearInterpolator());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mLastY = event.getRawY();
                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    mVelocityTracker.clear();
                }
                mVelocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(event);

                int dy = (int) (mLastY - event.getRawY());
                mLastY = event.getRawY();
                if (startNestedScroll(SCROLL_AXIS_VERTICAL) && dispatchNestedPreScroll(0, dy, mConsume, mOffsetInWindow)) {
                    dy -= mConsume[1];
                }
                int deltaY = calculateAndScrollY(dy);
                if (dy != deltaY) {
                    dispatchNestedScroll(0, deltaY, 0, dy - deltaY, mOffsetInWindow);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                //手指抬起时：1、若子view没有滚完，则子view进行fling；
                //2、若子view已经滚完，则直接让父view进行fling
                mVelocityTracker.computeCurrentVelocity(1000);
                int velocity = (int) mVelocityTracker.getYVelocity();
//                dispatchNestedPreFling(0, -velocity);
                fling(-velocity);
                stopNestedScroll();
                break;
        }
        return true;
    }

    private void fling(int velocity) {
        if (Math.abs(velocity) < 3) {
            return;
        }
        mIsFling = true;
        mScroller.fling(0, getCurrentScrollY(), 0, velocity, 0, 0, getMinScrollY(), getMaxScrollY());
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

    @Override
    public int calculateAndScrollY(int dy) {
        int deltaY = calculateScrollY(dy);
        scrollBy(0, deltaY);
        return deltaY;
    }

    @Override
    public int getNestedScrollingTop() {
        ViewGroup group = (ViewGroup) getParent();
        return group.getTop();
    }

    private int calculateScrollY(int dy) {
        int deltaY = 0;
        //手指从下到上，屏幕相机从上到下，scrollY的增值为正数
        if (dy > 0) {
            if (getCurrentScrollY() < getMaxScrollY()) {
                deltaY = dy;
                if (getCurrentScrollY() + deltaY >= getMaxScrollY()) {
                    deltaY = getMaxScrollY() - getCurrentScrollY();
                }
            }
        }
        //手指从上到下，屏幕相机从下到上，scrollY的增值为负数
        else {
            if (getCurrentScrollY() > getMinScrollY()) {
                deltaY = dy;
                if (getCurrentScrollY() + deltaY <= getMinScrollY()) {
                    deltaY = getMinScrollY() - getCurrentScrollY();
                }
            }
        }
        return deltaY;
    }

    @Override
    public int getCurrentScrollY() {
        return getScrollY();
    }

    @Override
    public int getMinScrollY() {
        return 0;
    }

    @Override
    public int getMaxScrollY() {
        return (int) (getContentHeight() * getScale()) - getMeasuredHeight();
    }

    @Override
    public boolean canScrollUp() {
        return getCurrentScrollY() > getMinScrollY();
    }

    @Override
    public boolean canScrollDown() {
        return getCurrentScrollY() < getMaxScrollY();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        //parent fling
        if (mIsFling && mScroller.isFinished()) {
            mIsFling = false;
            float velocity = 0;
            if (mScroller.getFinalY() == getMaxScrollY()) {

                velocity = mScroller.getCurrVelocity() * 2;
            } else if (mScroller.getFinalY() == getMinScrollY()) {
                //手指从上到下
                //getCurrVelocity得到的值为何始终为正数？？VelocityTracker可以得到正数和负数的值
                velocity = mScroller.getCurrVelocity() * -2;
            }
            if (velocity != 0) {
                getChildHelper().dispatchNestedFling(0, velocity, false);
            }
        }
    }

    private NestedScrollingChildHelper getChildHelper() {
        if (mChildHelper == null) {
            mChildHelper = new NestedScrollingChildHelper(this);
            mChildHelper.setNestedScrollingEnabled(true);
        }
        return mChildHelper;
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        getChildHelper().setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return getChildHelper().isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return getChildHelper().startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        getChildHelper().stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return getChildHelper().hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return getChildHelper().dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return getChildHelper().dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return getChildHelper().dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return getChildHelper().dispatchNestedFling(velocityX, velocityY, consumed);
    }
}
