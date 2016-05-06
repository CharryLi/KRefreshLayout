package com.charry.krefreshlayout.widget;

/**
 * 下拉刷新操作过程中事件回调
 * Created by CharryLi on 16/5/3.
 */
public interface KRefreshHeadOpLinstener {

    void pullAction(float progress);
    void releaseRefreshAction();
    void refreshAction();
    void refreshCompleteAction();

}
