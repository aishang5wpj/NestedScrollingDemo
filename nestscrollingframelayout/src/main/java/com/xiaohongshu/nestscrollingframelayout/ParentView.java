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
import android.widget.LinearLayout;

/**
 * Created by wupengjian on 17/11/20.
 */
public class ParentView extends LinearLayout implements NestedScrollingParent {

    private IPrint mPrint;
    private View mHeadView, mBodyView, mTailView;
    private int mHeadHeight, mBodyHeight, mTailHeight;
    private int mMinNestedOffset = -1, mMaxNestedOffset = -1;
    //总的位移，包含子View（特别重要）粘性滚动时的位移
    private int mOffsetIncludeChildren;
    private NestedScrollingParentHelper mParentHelper;

    private float mLastY;

    public ParentView(@NonNull Context context) {
        super(context);
    }

    public ParentView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ParentView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mHeadHeight = mHeadView.getMeasuredHeight();
        mBodyHeight = mBodyView.getMeasuredHeight();
        mTailHeight = mTailView.getMeasuredHeight();

        mMinNestedOffset = -mHeadHeight;
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
                int dy = (int) (event.getRawY() - mLastY);
                mLastY = event.getRawY();
                //doScroll中只会记录子View的滚动距离
                int deltaY = doScroll(dy);
                mOffsetIncludeChildren += deltaY;
                print(String.format("parent cost %s , total offset is %d", deltaY, mOffsetIncludeChildren));
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
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
        int deltaY = doScroll(dy);
        consumed[1] = deltaY;
        //这里dy是子View和父View消费前全部的滚动距离
        mOffsetIncludeChildren += dy;
        print(String.format("parent cost %s , total offset is %d", deltaY, mOffsetIncludeChildren));
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        //手指从下往上
        if (dyConsumed < 0) {
            //当前的滚动距离（父View+子View）减去子View没有消费的距离
            mMaxNestedOffset = mOffsetIncludeChildren - dyUnconsumed;
        }
        //这里的dy是父View第一次消费完和子View消费完之后剩下没有消费的距离
        int deltaY = doScroll(dyUnconsumed);
        //要减去父View和子View都没有消耗的距离
        mOffsetIncludeChildren -= (dyUnconsumed - deltaY);
        print(String.format("parent cost %s , total offset is %d", deltaY, mOffsetIncludeChildren));
    }

    private int doScroll(int dy) {
        int deltaY = dy;
        //手指从上往下，屏幕相机从下到上，mScrollY由大变小
        if (dy > 0) {
            //滚动隐藏mTailView
            if (isNestedChildScrollCompleted() && mOffsetIncludeChildren < mMaxNestedOffset) {
                if (deltaY + mOffsetIncludeChildren > mMaxNestedOffset) {
                    deltaY = mMaxNestedOffset - mOffsetIncludeChildren;
                }
            }
            //滚动显示mHeadView
            else if (mOffsetIncludeChildren >= mMinNestedOffset) {
                if (deltaY + mOffsetIncludeChildren > 0) {
                    deltaY = 0 - mOffsetIncludeChildren;
                }
            } else {
                deltaY = 0;
            }
        }
        //手指从下往上
        else if (dy < 0) {
            //滚动隐藏mHeadView
            if (mOffsetIncludeChildren > mMinNestedOffset) {//负数间比较
                if (deltaY + mOffsetIncludeChildren < mMinNestedOffset) {
                    deltaY = mMinNestedOffset - mOffsetIncludeChildren;
                }
            }
            //滚动显示mTailView
            else if (isNestedChildScrollCompleted() && mOffsetIncludeChildren <= mMaxNestedOffset) {//负数间比较
                if (deltaY + mOffsetIncludeChildren < mMaxNestedOffset - mTailHeight) {
                    deltaY = mMaxNestedOffset - mTailHeight - mOffsetIncludeChildren;
                }
            } else {
                deltaY = 0;
            }
        }
        scrollBy(0, -deltaY);
        return deltaY;
    }

    private boolean isNestedChildScrollCompleted() {
        return mMaxNestedOffset < mMinNestedOffset;
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public int getNestedScrollAxes() {
        return getParentHelper().getNestedScrollAxes();
    }
}
