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

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker = VelocityTracker.obtain();

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
                mVelocityTracker.clear();
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
                dispatchNestedPreFling(0, -velocity);
                stopNestedScroll();
                break;
        }
        return true;
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

    private int getCurrentScrollY() {
        return getScrollY();
    }

    private int getMinScrollY() {
        return 0;
    }

    private int getMaxScrollY() {
        return (int) (getContentHeight() * getScale()) - getMeasuredHeight();
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
