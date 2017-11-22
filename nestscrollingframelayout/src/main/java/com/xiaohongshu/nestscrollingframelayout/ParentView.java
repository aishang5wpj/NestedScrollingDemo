package com.xiaohongshu.nestscrollingframelayout;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * Created by wupengjian on 17/11/20.
 */
public class ParentView extends LinearLayout implements NestedScrollingParent, INestedView {

    private IPrint mPrint;
    private View mHeadView, mBodyView, mTailView;
    private int mHeadHeight, mBodyHeight, mTailHeight;
    private int mMaxScrollY = -1;
    private NestedScrollingParentHelper mParentHelper;

    private float mLastY;
    private Scroller mScroller;

    public ParentView(@NonNull Context context) {
        this(context, null);
    }

    public ParentView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ParentView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(context, new AccelerateDecelerateInterpolator());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mHeadHeight = mHeadView.getMeasuredHeight();
        mBodyHeight = mBodyView.getMeasuredHeight();
        mTailHeight = mTailView.getMeasuredHeight();

        mMaxScrollY = mHeadHeight + mBodyHeight + mTailHeight;
    }

    //处理其他子view的滚动
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                int dy = (int) (mLastY - event.getRawY());
                mLastY = event.getRawY();
                //doScroll中只会记录子View的滚动距离
                int deltaY = doNestedScroll(dy);
                print(String.format("parent cost %s , total offset is %d", deltaY, getCurrentScrollY()));
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHeadView = getChildAt(0);
        mBodyView = getChildAt(1);
        mTailView = getChildAt(2);
    }

    public void setPrint(IPrint print) {
        mPrint = print;
    }

    private void print(String text) {
        if (mPrint != null && text != null) {
            mPrint.printInfo(text);
        }
    }

    public NestedScrollingParentHelper getParentHelper() {
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
        int deltaY = doNestedPreScroll(dy);
        consumed[1] = deltaY;
        print(String.format("parent cost %s , total offset is %d", deltaY, getCurrentScrollY()));
    }

    /**
     * 嵌套滚动前的滚动，只处理子view嵌套滚动前的那段距离：
     * 1、手指从下往上时，最多只能滚动到mBodyView刚好要隐藏的位置
     * 2、手指从上往下时，最多只能滚动到mBodyView刚好要显示的位置
     *
     * @param dy
     * @return
     */
    private int doNestedPreScroll(int dy) {
        int deltaY = dy;
        //手指从上往下，屏幕相机从下到上，mScrollY由大变小
        if (dy < 0) {
            //滚动显示mBodyView
            if (getCurrentScrollY() > mHeadHeight) {
                deltaY = mHeadHeight - getCurrentScrollY();
            } else {
                deltaY = 0;
            }
        }
        //手指从下往上
        else if (dy > 0) {
            //滚动隐藏mHeadView
            if (getCurrentScrollY() < mHeadHeight) {
                if (deltaY + getCurrentScrollY() > mHeadHeight) {
                    deltaY = mHeadHeight - getCurrentScrollY();
                }
            } else {
                deltaY = 0;
            }
        }
        scrollBy(0, deltaY);
        return deltaY;
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        //这里的dy是父View第一次消费完和子View消费完之后剩下没有消费的距离
        int deltaY = doNestedScroll(dyUnconsumed);
        print(String.format("parent cost %s , total offset is %d", deltaY, getCurrentScrollY()));
    }

    /**
     * 不用考虑子View的嵌套滚动，只考虑自己的边界情况即可
     * <p>
     * 1、纯事件分发走进来
     * 2、子view没有消费完的滚动距离
     *
     * @param dy
     * @return
     */
    private int doNestedScroll(int dy) {
        int deltaY = dy;
        //手指从上往下，屏幕相机从下到上，mScrollY由大变小
        if (dy < 0) {
            //滚动显示mHeadView
            if (getCurrentScrollY() > getMinScrollY()) {
                if (deltaY + getCurrentScrollY() < getMinScrollY()) {
                    deltaY = getMinScrollY() - getCurrentScrollY();
                }
            } else {
                deltaY = 0;
            }
        }
        //手指从下往上
        else if (dy > 0) {
            //隐藏mBodyView
            if (getCurrentScrollY() < getMaxScrollY()) {
                if (deltaY + getCurrentScrollY() > getMaxScrollY()) {
                    deltaY = getMaxScrollY() - getCurrentScrollY();
                }
            } else {
                deltaY = 0;
            }
        }
        scrollBy(0, deltaY);
        return deltaY;
    }

    @Override
    public boolean canMove2Top() {
        return getCurrentScrollY() > getMinScrollY();
    }

    @Override
    public boolean canMove2Bottom() {
        return getCurrentScrollY() < getMaxScrollY();
    }

    @Override
    public int getMinScrollY() {
        return 0;
    }

    @Override
    public int getMaxScrollY() {
        return mMaxScrollY;
    }

    @Override
    public int getCurrentScrollY() {
        return getScrollY();
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        fling((int) velocityY);
        return false;
    }

    public void fling(int velocityY) {
        mScroller.fling(0, getScrollY(), 0, velocityY, 0, 0, 0, mHeadHeight + mBodyHeight);
        invalidate();
    }

    @Override
    public int getNestedScrollAxes() {
        return getParentHelper().getNestedScrollAxes();
    }
}
