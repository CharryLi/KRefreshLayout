package com.charry.krefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * 底部视图基类，实现自定义底部视图需继承此基类
 * Created by CharryLi on 16/5/6.
 */
public class KBaseLoadMoreFoot extends FrameLayout implements KRefreshHeadOpListener {

    public KBaseLoadMoreFoot(Context context) {
        this(context, null);
    }

    public KBaseLoadMoreFoot(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KBaseLoadMoreFoot(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void pullAction(float progress) {}

    @Override
    public void releaseRefreshAction() {}

    @Override
    public void refreshAction() {}

    @Override
    public void refreshCompleteAction() {}
}
