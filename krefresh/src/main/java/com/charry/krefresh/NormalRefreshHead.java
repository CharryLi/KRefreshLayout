package com.charry.krefresh;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

/**
 * 仅文字头部
 * Created by CharryLi on 16/5/6.
 */
public class NormalRefreshHead extends KBaseRefreshHead {

    private TextView mTextView;

    public NormalRefreshHead(Context context) {
        this(context, null);
    }

    public NormalRefreshHead(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NormalRefreshHead(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mTextView = new TextView(getContext());
        mTextView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mTextView.setText("下拉刷新");
        mTextView.setTextColor(Color.BLACK);
        addView(mTextView);
    }

    @Override
    public void pullAction(float progress) {
        super.pullAction(progress);
        if (mTextView != null) mTextView.setText("下拉刷新");
    }

    @Override
    public void refreshAction() {
        super.refreshAction();
        if (mTextView != null) mTextView.setText("正在刷新...");
    }

    @Override
    public void releaseRefreshAction() {
        super.releaseRefreshAction();
        if (mTextView != null) mTextView.setText("松开刷新");
    }

    @Override
    public void refreshCompleteAction() {
        super.refreshCompleteAction();
        if (mTextView != null) mTextView.setText("下拉刷新");
    }

}
