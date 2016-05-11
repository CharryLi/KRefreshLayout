package com.charry.krefreshsample.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.charry.krefresh.KBaseRefreshHead;
import com.charry.krefreshsample.R;

/**
 * Created by CharryLi on 16/5/10.
 */
public class CustomEndAnimHeadView extends KBaseRefreshHead {

    private ImageView balloonImgView, cloudImgView;
    private ObjectAnimator ballObjAnim;
    private AnimatorSet ballEndAnimSet;

    public CustomEndAnimHeadView(Context context) {
        this(context, null);
    }

    public CustomEndAnimHeadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomEndAnimHeadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        View xmlView = LayoutInflater.from(getContext()).inflate(R.layout.cus_refresh_headview_endanim, this, true);
        balloonImgView = (ImageView) xmlView.findViewById(R.id.iv_header_balloon);
        cloudImgView = (ImageView) xmlView.findViewById(R.id.iv_header_cloud);

        // 大气球动画
        ballObjAnim = ObjectAnimator.ofFloat(balloonImgView, "translationY", 0.0f, 10.0f, 0.0f);
        ballObjAnim.setDuration(1000);
        ballObjAnim.setRepeatMode(Animation.REVERSE);
        ballObjAnim.setRepeatCount(-1);

        // 刷新结束动画
        float valueY = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        ObjectAnimator ballYAnim = ObjectAnimator.ofFloat(balloonImgView, "translationY", 0.0f, -valueY);
        ballYAnim.setRepeatCount(0);
        ObjectAnimator ballXAnim = ObjectAnimator.ofFloat(balloonImgView, "translationX", 0.0f, valueY*2);
        ballXAnim.setRepeatCount(0);
        ballEndAnimSet = new AnimatorSet();
        ballEndAnimSet.play(ballXAnim).with(ballYAnim);

        this.setAlpha(0.0f);
    }

    @Override
    public void pullAction(float progress) {
        super.pullAction(progress);
        if (this.getAlpha() == 0.0f) this.setAlpha(1.0f);
        int margin = ((MarginLayoutParams) balloonImgView.getLayoutParams()).topMargin;
        ViewCompat.setTranslationY(balloonImgView, (-getHeight() + progress + margin) * 2 / 3);
    }

    @Override
    public void releaseRefreshAction() {
        super.releaseRefreshAction();
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
        balloonImgView.setTranslationX(0);
        balloonImgView.setTranslationY(0);
    }

    @Override
    public void refreshEndAnimAction(long duration, Animator.AnimatorListener animatorListener) {
        super.refreshEndAnimAction(duration, animatorListener);
        ballEndAnimSet.setDuration(duration);
        ballEndAnimSet.removeAllListeners();
        ballEndAnimSet.addListener(animatorListener);
        ballEndAnimSet.start();
        ballObjAnim.cancel();
    }

    private void startAnim() {
        ballObjAnim.start();
    }

    private void stopAnim() {
        ballObjAnim.cancel();
        ballEndAnimSet.cancel();
        this.animate().alpha(0.0f).setDuration(50).start();
    }
}
