package com.xiaohongshu.nestscrollingframelayout;

/**
 * Created by wupengjian on 17/11/22.
 */

public interface INestedView {

    boolean canMove2Top();

    boolean canMove2Bottom();

    int getMinScrollY();

    int getMaxScrollY();

    int getCurrentScrollY();
}
