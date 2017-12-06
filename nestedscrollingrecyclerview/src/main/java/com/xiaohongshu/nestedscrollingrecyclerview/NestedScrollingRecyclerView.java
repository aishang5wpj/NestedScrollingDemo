package com.xiaohongshu.nestedscrollingrecyclerview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by wupengjian on 17/11/17.
 */
public class NestedScrollingRecyclerView extends RecyclerView implements NestedScrollingParent {

    private NestedScrollingParentHelper mParentHelper;

    public NestedScrollingRecyclerView(Context context) {
        this(context, null);
    }

    public NestedScrollingRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestedScrollingRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        return false;
    }

    @Override
    public void onChildAttachedToWindow(View child) {
        super.onChildAttachedToWindow(child);
        View view = findNestedScrollingView(child);
        if (view != null && view instanceof NestedScrollingWebView) {
            NestedScrollingLinearLayoutManager manager = (NestedScrollingLinearLayoutManager) getLayoutManager();
            manager.setScrollingHelper((IScrollingHelper) view);
        }
    }

    @Override
    public void onChildDetachedFromWindow(View child) {
        super.onChildDetachedFromWindow(child);
        View view = findNestedScrollingView(child);
        if (view != null && view instanceof NestedScrollingWebView) {
            NestedScrollingLinearLayoutManager manager = (NestedScrollingLinearLayoutManager) getLayoutManager();
            manager.setScrollingHelper(null);
        }
    }

    /**
     * 查找NestedScrollingView
     *
     * @param view
     * @return
     */
    private View findNestedScrollingView(View view) {
        if (view instanceof NestedScrollingChild) {
            return view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = viewGroup.getChildAt(i);
                View nestedChild = findNestedScrollingView(child);
                if (nestedChild != null) {
                    return nestedChild;
                }
            }
        }
        return null;
    }

    /*********************************************** Nested Child start *********************************************/

    private NestedScrollingParentHelper getParentHelper() {
        if (mParentHelper == null) {
            mParentHelper = new NestedScrollingParentHelper(this);
        }
        return mParentHelper;
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return nestedScrollAxes == SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        getParentHelper().onNestedScrollAccepted(child, target, nestedScrollAxes);
    }

    @Override
    public void onStopNestedScroll(View target) {
        getParentHelper().onStopNestedScroll(target);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        ViewGroup viewGroup = (ViewGroup) target.getParent();
        int top = viewGroup.getTop();
        int deltaY = 0;
        //手指从下到上，屏幕相机从上到下，scrollY的增值为正数
        if (dy > 0) {
            if (top > 0) {
                if (dy > top) {
                    deltaY = top;
                } else {
                    deltaY = dy;
                }
            }
        } else {
            //手指从上到下，屏幕相机从下到上，scrollY的增值为负数
            if (top < 0) {
                if (dy < top) {
                    deltaY = top;
                } else {
                    deltaY = dy;
                }
            }
        }
        consumed[1] = deltaY;
        scrollBy(0, deltaY);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        scrollBy(0, dyUnconsumed);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return fling((int) velocityX, (int) velocityY);
    }

    @Override
    public int getNestedScrollAxes() {
        return getParentHelper().getNestedScrollAxes();
    }
    /*********************************************** Nested Child end *********************************************/
}
