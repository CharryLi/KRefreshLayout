package com.charry.krefresh;

import android.animation.Animator;

/**
 * 下拉刷新操作过程中事件回调
 * Created by CharryLi on 16/5/3.
 */
public interface KRefreshHeadOpListener {

    /**
     * 下拉事件过程回调，回调多次（即下拉刷新状态）
     * @param progress
     */
    void pullAction(float progress);

    /**
     * 下拉到一定高度后释放刷新回调，仅回调一次（即释放刷新状态）
     */
    void releaseRefreshAction();

    /**
     * 释放后进入刷新状态回调，仅回调一次（即刷新状态）
     */
    void refreshAction();

    /**
     * 刷新完成回调
     */
    void refreshCompleteAction();

    /**
     * 为了适配特殊的场景，如自定义头部在刷新结束最后需要执行完某些动画后再回弹头部视图，
     * 此时就可以调用此回调给头部结束动画设置动画时间及监听，在监听回调中调用refreshCompleteAction，
     * 参见KRefreshLayout中finishRefreshWithAnimEnd
     * @param duration
     * @param animatorListener
     */
    void refreshEndAnimAction(long duration, Animator.AnimatorListener animatorListener);

}
