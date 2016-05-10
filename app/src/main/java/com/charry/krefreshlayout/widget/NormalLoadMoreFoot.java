package com.charry.krefreshlayout.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

/**
 * Created by CharryLi on 16/5/10.
 */
public class NormalLoadMoreFoot extends KBaseLoadMoreFoot {

    private TextView mTextView;

    public NormalLoadMoreFoot(Context context) {
        this(context, null);
    }

    public NormalLoadMoreFoot(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NormalLoadMoreFoot(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mTextView = new TextView(getContext());
        mTextView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mTextView.setText("加载更多");
        mTextView.setTextColor(Color.BLACK);
        addView(mTextView);
    }

    @Override
    public void pullAction(float progress) {
        if (mTextView != null) mTextView.setText("上拉加载更多");
    }

    @Override
    public void releaseRefreshAction() {
        if (mTextView != null) mTextView.setText("松开加载更多");
    }

    @Override
    public void refreshAction() {
        if (mTextView != null) mTextView.setText("正在加载...");
    }

    @Override
    public void refreshCompleteAction() {
        if (mTextView != null) mTextView.setText("上拉加载更多");
    }
}
