package com.charry.krefresh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;

/**
 * 自定义下拉刷新Layout
 * Created by CharryLi on 16/5/3.
 */
public class KRefreshLayout extends FrameLayout {

    private static final String TAG = "KRefreshLayout";

    private static final int DONE               = 0;// 刷新完毕状态
    private static final int PULL_TO_REFRESH    = 1;// 下拉刷新状态
    private static final int RELEASE_TO_REFRESH = 2;// 释放刷新状态
    private static final int REFRESHING         = 3;// 正在刷新状态
    private int state;// 状态值
    private boolean isRefreshing;// 是否正在刷新
    private DecelerateInterpolator decelerateInterpolator;
    private View mContentView;// 内容视图（ListView、GridView等）
    private int mHeadHeight;// 刷新头部视图高度
    private int mFootHeight;// 刷新尾部视图高度
    private float mTouchY;// 下拉时，手指按压记录的第一个Y坐标值
    private float mCurrentY;// 下拉时，手指移动时不断记录的Y坐标值
    private boolean isTouchEnd;// 触摸事件是否结束
    private boolean isOverlay;// 头部视图是否被内容视图覆盖
    private boolean isAttached;// 是否已执行过onAttachedToWindow
    private boolean enablePullRefresh;// 是否启用下拉刷新
    private boolean enableLoadMore;// 是否启用加载更多
    private boolean isLoadMore;// 区别是下拉还是上拉
    private KBaseRefreshHead mRefreshHeadView;// 头部视图
    private KBaseLoadMoreFoot mRefreshFootView;
    private KOnRefreshListener mOnRefreshListener;// 刷新回调

    public KRefreshLayout(Context context) {
        this(context, null);
    }

    public KRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (isInEditMode()) {
            return;
        }

        if (getChildCount() > 1) {
            throw new RuntimeException("can only have one child widget");
        }

        mHeadHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics());
        mFootHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());
        decelerateInterpolator = new DecelerateInterpolator(10);
        isRefreshing = false;
        isTouchEnd = true;
        isOverlay = true;
        enablePullRefresh = true;
        enableLoadMore = false;
        state = DONE;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //Log.d(TAG, "onAttachedToWindow");

        mContentView = getChildAt(0);
        if (mContentView == null) {
            return;
        }

        // 初始化默认头部视图
        if (mRefreshHeadView == null) {
            mRefreshHeadView = new NormalRefreshHead(getContext());
        }
        // 添加头部视图
        addRefreshHeader();

        // 添加加载更多视图
        addRefreshFooter();

        isAttached = true;
    }

    /**
     * 添加刷新头部视图
     */
    private void addRefreshHeader() {
        if (mRefreshHeadView == null) {
            throw new RuntimeException("add a head view please");
        }

        // 如果已经添加过头部视图，则先删除
        if (getChildCount() > 1) removeViewAt(0);
        // 重新添加新头部视图
        addView(mRefreshHeadView, 0, new LayoutParams(LayoutParams.MATCH_PARENT, mHeadHeight));
        // 判断是否是覆盖模式
        if (!isOverlay) {
            ViewCompat.setTranslationY(mRefreshHeadView, -mHeadHeight);
        }
    }

    /**
     * 添加刷新尾部视图
     */
    private void addRefreshFooter() {
        mRefreshFootView = new NormalLoadMoreFoot(getContext());
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, mFootHeight);
        lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        addView(mRefreshFootView, 1, lp);

        // 判断是否是覆盖模式
        if (!isOverlay) {
            ViewCompat.setTranslationY(mRefreshFootView, mFootHeight);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchY = ev.getY();
                mCurrentY = mTouchY;
                break;

            case MotionEvent.ACTION_MOVE:
                float dy = ev.getY() - mTouchY;
                if (dy > 0 && !canChildScrollUp() && !isRefreshing && state != REFRESHING && isTouchEnd && enablePullRefresh) {// 如果是下拉并且内容视图已经到顶部且不是正在刷新状态，则拦截事件
                    //Log.d(TAG, "ACTION_MOVE 已拦截");
                    isLoadMore = false;
                    return true;
                } else if (dy < 0 && !canChildScrollDown() && !isRefreshing && state != REFRESHING && isTouchEnd && enableLoadMore) {// 上拉加载更多
                    isLoadMore = true;
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isRefreshing) {
            return super.onTouchEvent(event);
        }

        if (!isLoadMore) {// 下拉刷新
            mCurrentY = event.getY();
            float dy = mCurrentY - mTouchY;// 计算下拉了多少距离
            dy = Math.min(mHeadHeight * 3, dy);// 最大可以下来头部视图高度的3倍距离
            dy = Math.max(0, dy);// 排除负数情况
            float offsetY = getHeadOffsetY(dy);

            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    if (mContentView != null) {
                        isTouchEnd = false;
                        //Log.d(TAG, "ACTION_MOVE --> dy:" + dy + " offsetY:" + offsetY);
                        if (offsetY < mHeadHeight) {// 切换为下拉刷新状态
                            changeHeaderByState(PULL_TO_REFRESH, offsetY);
                        } else {// 切换为释放刷新状态，且只调用一次
                            if (state != RELEASE_TO_REFRESH) changeHeaderByState(RELEASE_TO_REFRESH, offsetY);
                        }
                        // 移动内容视图坐标
                        ViewCompat.setTranslationY(mContentView, offsetY);
                        // 移动头部视图
                        if (!isOverlay) {
                            ViewCompat.setTranslationY(mRefreshHeadView, -mRefreshHeadView.getLayoutParams().height + offsetY);
                        }
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    //Log.d(TAG, "ACTION_UP --> dy:" + dy + " offsetY:" + offsetY);
                    if (mContentView != null) {// 切换为刷新状态
                        if (ViewCompat.getTranslationY(mContentView) >= mHeadHeight) {// 释放后进入刷新状态
                            createAnimTransYForHead(mContentView, mHeadHeight, mRefreshHeadView);
                            changeHeaderByState(REFRESHING, offsetY);
                        } else {// 结束刷新
                            finishRefreshing();
                        }
                        isTouchEnd = true;
                    }
                    return true;
            }
        } else {// 上拉加载
            mCurrentY = event.getY();
            float dy = mTouchY - mCurrentY;// 计算上拉了多少距离
            dy = Math.min(mFootHeight * 3, dy);// 最大可以下来头部视图高度的3倍距离
            dy = Math.max(0, dy);// 排除负数情况
            float offsetY = getFootOffsetY(dy);

            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    if (mContentView != null) {
                        isTouchEnd = false;
                        //Log.d(TAG, "ACTION_MOVE --> dy:" + dy + " offsetY:" + offsetY);
                        if (offsetY < mFootHeight) {// 切换为下拉刷新状态
                            changeFooterByState(PULL_TO_REFRESH, offsetY);
                        } else {// 切换为释放刷新状态，且只调用一次
                            if (state != RELEASE_TO_REFRESH) changeFooterByState(RELEASE_TO_REFRESH, offsetY);
                        }
                        // 移动内容视图坐标
                        ViewCompat.setTranslationY(mContentView, -offsetY);
                        // 移动头部视图
                        if (!isOverlay) {
                            ViewCompat.setTranslationY(mRefreshFootView, mRefreshFootView.getLayoutParams().height - offsetY);
                        }
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    //Log.d(TAG, "ACTION_UP --> dy:" + dy + " offsetY:" + offsetY);
                    if (mContentView != null) {// 切换为刷新状态
                        if (ViewCompat.getTranslationY(mContentView) <= -mFootHeight) {// 释放后进入刷新状态
                            createAnimTransYForFoot(mContentView, -mFootHeight, mRefreshFootView);
                            changeFooterByState(REFRESHING, offsetY);
                        } else {// 结束加载更多
                            finishLoadingMore();
                        }
                        isTouchEnd = true;
                    }
                    return true;
            }
        }

        return super.onTouchEvent(event);
    }

    /**
     * 根据状态改变headerView的动画和文字显示
     *
     * @param state
     */
    private void changeHeaderByState(int state, float offsetY) {
        if (mRefreshHeadView == null) return;

        switch (state) {
            case DONE:// 如果的隐藏的状态
                this.state = DONE;
                isRefreshing = false;
                mRefreshHeadView.refreshCompleteAction();
                break;

            case PULL_TO_REFRESH:// 当前状态为下拉刷新
                this.state = PULL_TO_REFRESH;
                mRefreshHeadView.pullAction(offsetY);
                break;

            case RELEASE_TO_REFRESH:// 当前状态为放开刷新
                this.state = RELEASE_TO_REFRESH;
                mRefreshHeadView.releaseRefreshAction();
                break;

            case REFRESHING:// 当前状态为正在刷新
                this.state = REFRESHING;
                isRefreshing = true;
                mRefreshHeadView.refreshAction();
                // 刷新回调
                if (mOnRefreshListener != null) mOnRefreshListener.onRefresh();
                break;

            default:
                break;
        }
    }

    /**
     * 根据状态改变footerView的动画和文字显示
     * @param state
     * @param offsetY
     */
    private void changeFooterByState(int state, float offsetY) {
        if (mRefreshFootView == null) return;

        switch (state) {
            case DONE:// 如果的隐藏的状态
                this.state = DONE;
                isRefreshing = false;
                mRefreshFootView.refreshCompleteAction();
                break;

            case PULL_TO_REFRESH:// 当前状态为下拉刷新
                this.state = PULL_TO_REFRESH;
                mRefreshFootView.pullAction(offsetY);
                break;

            case RELEASE_TO_REFRESH:// 当前状态为放开刷新
                this.state = RELEASE_TO_REFRESH;
                mRefreshFootView.releaseRefreshAction();
                break;

            case REFRESHING:// 当前状态为正在刷新
                this.state = REFRESHING;
                isRefreshing = true;
                mRefreshFootView.refreshAction();
                // 刷新回调
                if (mOnRefreshListener != null) mOnRefreshListener.onLoadMore();
                break;

            default:
                break;
        }
    }

    /**
     * 开始刷新
     */
    private void startRefreshing() {
        if (isRefreshing || !enablePullRefresh) return;
        if (mContentView != null) {
            createAnimTransYForHead(mContentView, mHeadHeight, mRefreshHeadView);
            changeHeaderByState(REFRESHING, 0);
        }
    }

    /**
     * 开始刷新，该方法回调onRefresh
     */
    public void startRefreshWithCallBack() {
        if (isRefreshing || !enablePullRefresh) return;
        this.post(new Runnable() {
            @Override
            public void run() {
                startRefreshing();
            }
        });
    }

    /**
     * 延迟刷新，该方法回调onRefresh
     *
     * @param delayMillis
     */
    public void startRefreshWithCallBack(long delayMillis) {
        if (isRefreshing || !enablePullRefresh) return;
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                startRefreshing();
            }
        }, delayMillis < 0 ? 0 : delayMillis);
    }

    /**
     * 结束刷新
     */
    private void finishRefreshing() {
        if (mContentView != null && mRefreshHeadView != null) {
            createAnimTransYForHead(mContentView, 0, mRefreshHeadView);
        }
    }

    /**
     * 结束刷新
     */
    public void finishRefresh() {
        this.post(new Runnable() {
            @Override
            public void run() {
                finishRefreshing();
            }
        });
    }

    /**
     * 结束刷新（此方法用于刷新结束时需执行特定动画后再结束刷新的特殊需求）
     */
    public void finishRefreshWithAnimEnd() {
        if (mContentView != null && mRefreshHeadView != null) {
            mRefreshHeadView.refreshEndAnimAction(400, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    finishRefreshing();
                }
            });
        }
    }

    /**
     * 结束加载更多
     */
    private void finishLoadingMore() {
        if (mContentView != null && mRefreshFootView != null) {
            createAnimTransYForFoot(mContentView, 0, mRefreshFootView);
        }
    }

    /**
     * 结束加载更多
     */
    public void finishLoadMore() {
        this.post(new Runnable() {
            @Override
            public void run() {
                finishLoadingMore();
            }
        });
    }

    /**
     * 判断内容控件是否还能往上滚动
     *
     * @return
     */
    public boolean canChildScrollUp() {
        if (mContentView == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < 14) {
            if (mContentView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mContentView;
                return absListView.getChildCount() > 0 && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0).getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mContentView, -1) || mContentView.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mContentView, -1);
        }
    }

    /**
     * 判断内容控件是否还能往下滚动
     *
     * @return
     */
    public boolean canChildScrollDown() {
        if (mContentView == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < 14) {
            if (mContentView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mContentView;
                if (absListView.getChildCount() > 0) {
                    int lastChildBottom = absListView.getChildAt(absListView.getChildCount() - 1).getBottom();
                    return absListView.getLastVisiblePosition() == absListView.getAdapter().getCount() - 1 && lastChildBottom <= absListView.getMeasuredHeight();
                } else {
                    return false;
                }

            } else {
                return ViewCompat.canScrollVertically(mContentView, 1) || mContentView.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mContentView, 1);
        }
    }

    /**
     * 创建一个移动动画(下拉时)
     *
     * @param targetView
     * @param valueY
     * @param headLayout
     */
    private void createAnimTransYForHead(final View targetView, final float valueY, final FrameLayout headLayout) {
        //Log.d(TAG, "getTranslationY:" + targetView.getTranslationY() + " valueY:" + valueY);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(targetView.getTranslationY(), valueY);
        valueAnimator.setDuration(350);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                /*if (headOpListener == null) return;
                float transY = ViewCompat.getTranslationY(targetView);
                float offsetY = getHeadOffsetY(Math.max(0, transY));
                headOpListener.pullAction(offsetY);*/

                float transY = (float) animation.getAnimatedValue();
                //Log.d(TAG, "update value:" + transY);
                // 移动内容视图
                ViewCompat.setTranslationY(targetView, transY);
                // 移动头部视图
                if (headLayout != null && !isOverlay) {
                    ViewCompat.setTranslationY(headLayout, -headLayout.getLayoutParams().height + transY);
                }
                // 设置内容视图底部padding-解决刷新状态下内容视图无法滚动到底部问题
                targetView.setPadding(0, 0, 0, (int) Math.abs(transY));
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //Log.d(TAG, "onAnimationEnd");
                if (valueY == 0) {// 如果目标Y坐标为0，则置为隐藏状态
                    changeHeaderByState(DONE, valueY);
                }
            }
        });
        valueAnimator.start();
    }

    /**
     * 创建一个移动动画(上拉拉时)
     * @param targetView
     * @param valueY
     * @param footLayout
     */
    private void createAnimTransYForFoot(final View targetView, final float valueY, final FrameLayout footLayout) {
        //Log.d(TAG, "getTranslationY:" + targetView.getTranslationY() + " valueY:" + valueY);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(targetView.getTranslationY(), valueY);
        valueAnimator.setDuration(350);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float transY = (float) animation.getAnimatedValue();
                //Log.d(TAG, "update value:" + transY);
                // 移动内容视图
                ViewCompat.setTranslationY(targetView, transY);
                // 移动头部视图
                if (footLayout != null && !isOverlay) {
                    ViewCompat.setTranslationY(footLayout, mRefreshFootView.getLayoutParams().height + transY);
                }
                // 设置内容视图头部padding-解决刷新状态下内容视图无法滚动到顶部问题
                targetView.setPadding(0, (int) Math.abs(transY), 0, 0);
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //Log.d(TAG, "onAnimationEnd");
                if (valueY == 0) {// 如果目标Y坐标为0，则置为隐藏状态
                    changeFooterByState(DONE, valueY);
                }
            }
        });
        valueAnimator.start();
    }

    private float getHeadOffsetY(float y) {
        return decelerateInterpolator.getInterpolation(y / mHeadHeight / 3) * y / 2;
    }

    private float getFootOffsetY(float y) {
        return decelerateInterpolator.getInterpolation(y / mFootHeight / 3) * y / 3;
    }

    public boolean isOverlay() {
        return isOverlay;
    }

    public void setOverlay(boolean overlay) {
        if (isRefreshing) return;
        isOverlay = overlay;
        if (isOverlay) {
            ViewCompat.setTranslationY(mRefreshHeadView, 0);
            ViewCompat.setTranslationY(mRefreshFootView, 0);
        }
    }

    public boolean isRefreshing() {
        return isRefreshing;
    }

    public boolean isEnablePullRefresh() {
        return enablePullRefresh;
    }

    public void setEnablePullRefresh(boolean enablePullRefresh) {
        this.enablePullRefresh = enablePullRefresh;
    }

    public boolean isEnableLoadMore() {
        return enableLoadMore;
    }

    public void setEnableLoadMore(boolean enableLoadMore) {
        this.enableLoadMore = enableLoadMore;
    }

    /**
     * 设置自定义头部视图
     * @param headView
     */
    public void setCustomHeadView(KBaseRefreshHead headView) {
        if (headView == null || isRefreshing) return;
        this.mRefreshHeadView = headView;
        // 如果已经Attacted过，则手动执行切换头部
        if (isAttached) {
            addRefreshHeader();
        }
    }

    /**
     * 设置头部视图高度
     * @param headHeight
     */
    public void setHeadViewHeight(int headHeight) {
        mHeadHeight = headHeight;
    }

    /**
     * 设置底部加载更多视图高度
     * @param footHeight
     */
    public void setFootViewHeight(int footHeight) {
        mFootHeight = footHeight;
    }

    /**
     * 设置刷新状态，该方法不进行onRefresh回调，仅改变头部视图状态
     *
     * @param refreshing
     */
    public void setRefreshing(boolean refreshing) {
        if (!isRefreshing && refreshing) {// 设置为刷新状态，但是不回调事件
            this.state = REFRESHING;
            isRefreshing = true;
            if (mRefreshHeadView != null) {
                mRefreshHeadView.refreshAction();
            }
        } else {// 结束刷新
            finishRefreshing();
        }
    }

    public void setOnRefreshListener(KOnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }

    public interface KOnRefreshListener {
        void onRefresh();
        void onLoadMore();
    }
}
