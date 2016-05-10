package com.charry.krefreshsample.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.charry.krefresh.KBaseRefreshHead;
import com.charry.krefreshsample.R;

/**
 * 下拉刷新头部视图
 * Created by CharryLi on 16/5/3.
 */
public class CustomRefreshHeadView extends KBaseRefreshHead {

    private ImageView sunImgView, cloudImgView1, cloudImgView2, balloonImgView, singleBallImgView;
    private Animation sunAnimation;// 太阳动画
    private Animation backAnimation1, backAnimation2;// 两张背景图动画
    private ObjectAnimator ballObjAnim, singleBallObjAnim;

    public CustomRefreshHeadView(Context context) {
        this(context, null);
    }

    public CustomRefreshHeadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomRefreshHeadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 初始化视图控件
        View xmlView = LayoutInflater.from(getContext()).inflate(R.layout.refresh_headerview, this, true);
        sunImgView = (ImageView) xmlView.findViewById(R.id.iv_header_sun);
        cloudImgView1 = (ImageView) xmlView.findViewById(R.id.iv_header_cloud1);
        cloudImgView2 = (ImageView) xmlView.findViewById(R.id.iv_header_cloud2);
        balloonImgView = (ImageView) xmlView.findViewById(R.id.iv_header_balloon);
        singleBallImgView = (ImageView) xmlView.findViewById(R.id.iv_header_singleball);

        // 太阳动画和云动画
        LinearInterpolator lir = new LinearInterpolator();
        sunAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.sun_rotate);
        sunAnimation.setInterpolator(lir);
        backAnimation1 = AnimationUtils.loadAnimation(getContext(), R.anim.cloud_queue1);
        backAnimation1.setInterpolator(lir);
        backAnimation2 = AnimationUtils.loadAnimation(getContext(), R.anim.cloud_queue2);
        backAnimation2.setInterpolator(lir);

        // 大气球动画
        ballObjAnim = ObjectAnimator.ofFloat(balloonImgView, "translationY", 0.0f, 10.0f, 0.0f);
        ballObjAnim.setDuration(1000);
        ballObjAnim.setInterpolator(new DecelerateInterpolator());
        ballObjAnim.setRepeatMode(Animation.REVERSE);
        ballObjAnim.setRepeatCount(-1);

        // 小气球动画
        singleBallObjAnim= ObjectAnimator.ofFloat(singleBallImgView, "translationY", 0.0f, 12.0f, 0.0f);
        singleBallObjAnim.setDuration(1200);
        singleBallObjAnim.setInterpolator(new DecelerateInterpolator(2));
        singleBallObjAnim.setRepeatMode(Animation.REVERSE);
        singleBallObjAnim.setRepeatCount(-1);

        this.setAlpha(0.0f);
    }

    @Override
    public void pullAction(float progress) {
        super.pullAction(progress);
        if (this.getAlpha() == 0.0f) this.setAlpha(1.0f);
        int margin = ((MarginLayoutParams) balloonImgView.getLayoutParams()).topMargin;
        ViewCompat.setTranslationY(balloonImgView, (-getHeight() + progress + margin) * 2 / 3);
        ViewCompat.setRotation(sunImgView, progress);
    }

    @Override
    public void refreshAction() {
        super.refreshAction();
        if (this.getAlpha() == 0.0f) this.setAlpha(1.0f);
        startAnim();
    }

    @Override
    public void refreshCompleteAction() {
        super.refreshCompleteAction();
        stopAnim();
    }

    /**
     * 开启动画
     */
    public void startAnim() {
        cloudImgView1.startAnimation(backAnimation1);
        cloudImgView2.startAnimation(backAnimation2);
        sunImgView.startAnimation(sunAnimation);
        ballObjAnim.start();
        singleBallObjAnim.start();
    }

    /**
     * 关闭动画
     */
    public void stopAnim() {
        cloudImgView1.clearAnimation();
        cloudImgView2.clearAnimation();
        sunImgView.clearAnimation();
        balloonImgView.clearAnimation();
        ballObjAnim.cancel();
        singleBallObjAnim.cancel();
        this.animate().alpha(0.0f).setDuration(100).start();
    }
}
