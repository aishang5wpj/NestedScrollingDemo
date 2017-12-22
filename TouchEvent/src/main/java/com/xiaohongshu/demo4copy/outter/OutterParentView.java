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
public class OutterParentView extends FrameLayout {

    private float mLastY;
    private OutterChildView mChildView;

    public OutterParentView(@NonNull Context context) {
        this(context, null);
    }

    public OutterParentView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OutterParentView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        if (child instanceof OutterChildView) {
            mChildView = (OutterChildView) child;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean result = super.dispatchTouchEvent(ev);
        mLastY = ev.getRawY();
        return result;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean result = super.onInterceptTouchEvent(event);
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                result = false;
                break;
            case MotionEvent.ACTION_MOVE:
                int dy = (int) (mLastY - event.getRawY());
                if (dy != 0) {
                    result = dy > 0 && !mChildView.canScrollUp()
                            || dy < 0 && !mChildView.canScrollDown();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                final int dy = (int) (mLastY - event.getRawY());
                if (dy != 0) {
                    scrollBy(0, dy);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }
}
