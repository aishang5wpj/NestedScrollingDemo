package com.xiaohongshu.nestedscrollingrecyclerview;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by wupengjian on 17/12/5.
 */
public class NestedScrollingLinearLayoutManager extends LinearLayoutManager {

    private IScrollingHelper mScrollingHelper;

    public NestedScrollingLinearLayoutManager(Context context) {
        this(context, LinearLayoutManager.VERTICAL, false);
    }

    public NestedScrollingLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int restDeltaY = dy;
        //1、pre nested scroll
        int preNestedDeltaY = doPreNestedScroll(restDeltaY);
        if (preNestedDeltaY != 0) {
            restDeltaY -= preNestedDeltaY;
            super.scrollVerticallyBy(preNestedDeltaY, recycler, state);
        }
        //2、nested scroll
        //嵌套的子view滚动
        int nestedDeltaY = doNestedScroll(restDeltaY);
        if (nestedDeltaY != 0) {
            restDeltaY -= nestedDeltaY;
        }
        //3、after nested scroll
        if (restDeltaY != 0) {
            super.scrollVerticallyBy(restDeltaY, recycler, state);
        }
        //很重要，必须返回全部的滚动值，虽然子view滚动了一部分，但是如果不全部返回，ViewFlinger会认为
        //已经滚动到了头，导致fling的行为被终止
        return dy;
    }

    private int doPreNestedScroll(int dy) {
        if (dy == 0) {
            return 0;
        }
        int deltaY = 0;
        if (mScrollingHelper != null) {
            int top = mScrollingHelper.getNestedScrollingTop();
            boolean isCurrentTop = top == 0;
            boolean isOverScrollingTop = 0 < top && top < dy;
            boolean isOverScrollingBottom = dy < top && top < 0;
            //如果webview已经完全滚动到顶部，就不再滚了
            if (isCurrentTop
                    //如果webview没有完全滚动到顶部，但是接下来的滚动距离会让webview超出屏幕
                    || isOverScrollingTop
                    //如果webview没有完全显示，但是接下来的滚动距离会让webview完全显示之后继续向下滚
                    || isOverScrollingBottom) {
                //则recyclerview最多只能滚动top的距离
                deltaY = top;
            }
            //其他情况下，有多少滚多少
            else {
                deltaY = dy;
            }
        }
        return deltaY;
    }

    private int doNestedScroll(int dy) {
        if (dy == 0) {
            return 0;
        }
        if (mScrollingHelper != null) {
            int deltaY = mScrollingHelper.calculateAndScrollY(dy);
            return deltaY;
        }
        return 0;
    }

    public void setScrollingHelper(IScrollingHelper helper) {
        mScrollingHelper = helper;
    }
}
