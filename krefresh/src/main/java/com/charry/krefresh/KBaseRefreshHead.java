package com.charry.krefresh;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * 头部视图基类，实现自定义头部视图需继承此基类
 * Created by CharryLi on 16/5/6.
 */
public class KBaseRefreshHead extends FrameLayout implements KRefreshHeadOpListener {

    protected long endAnimDuration;
    protected Animator.AnimatorListener endAnimtorListener;

    public KBaseRefreshHead(Context context) {
        this(context, null);
    }

    public KBaseRefreshHead(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KBaseRefreshHead(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void pullAction(float progress) {

    }

    @Override
    public void releaseRefreshAction() {

    }

    @Override
    public void refreshAction() {

    }

    @Override
    public void refreshCompleteAction() {

    }

    @Override
    public void refreshEndAnimAction(long duration, Animator.AnimatorListener animatorListener) {
        this.endAnimDuration = duration;
        this.endAnimtorListener = animatorListener;
    }
}
