package com.xiaohongshu.demo4copy.outter;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by wupengjian on 17/12/13.
 */
public class OutterChildView extends FrameLayout {

    private float mLastY;

    public OutterChildView(@NonNull Context context) {
        this(context, null);
    }

    public OutterChildView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OutterChildView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                final int dy = (int) (mLastY - event.getRawY());
                int deltaY = dy;
                if (dy > 0) {
                    if (getScrollY() + deltaY >= getMaxScrollY()) {
                        deltaY = getMaxScrollY() - getScrollY();
                    }
                } else {
                    if (getScrollY() + deltaY <= getMinScrollY()) {
                        deltaY = getMinScrollY() - getScrollY();
                    }
                }
                if (deltaY != 0) {
                    scrollBy(0, deltaY);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        mLastY = event.getRawY();
        return true;
    }

    private int getMinScrollY() {
        View child = getChildAt(0);
        return -(getMeasuredHeight() - child.getMeasuredHeight());
    }

    private int getMaxScrollY() {
        return 0;
    }

    public boolean canScrollUp() {
        return getScrollY() != getMaxScrollY();
    }

    public boolean canScrollDown() {
        return getScrollY() != getMinScrollY();
    }
}
