package com.lyml.widgetlibrary;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by LYML on 2018-02-24.
 */

public class SwipeCloseLayout extends LinearLayout {

    private static final String TAG = "SwipeCloseLayout";

    private final int scaledTouchSlop;
    private float downY;
    private boolean toScroll = false;
    private float lastTranY;
    private ValueAnimator animator;

    private boolean isInit = true;
    private boolean isShow;

    private OnShowListener onShowListener;

    public OnShowListener getOnShowListener() {
        return onShowListener;
    }

    public void setOnShowListener(OnShowListener onShowListener) {
        this.onShowListener = onShowListener;
    }

    public SwipeCloseLayout(Context context) {
        this(context, null);
    }

    public SwipeCloseLayout(Context context, AttributeSet attrs) {

        this(context, attrs, 0);
    }

    public SwipeCloseLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        scaledTouchSlop = ViewConfiguration.get(this.getContext()).getScaledTouchSlop();
        // 启用clickable才有touchEvent事件
        setClickable(true);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SwipeOutLayout, defStyleAttr, 0);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.SwipeOutLayout_isShow)
                this.isShow = a.getBoolean(attr, false);
        }
        a.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if(isInit && !isShow){
            isInit = false;
            setTranslationY(getHeightWithMargin(this));
        }
    }

    /**
     * 在子控件上发生横向滑动时拦截Touch事件
     *
     * @param event
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = event.getRawY();
            case MotionEvent.ACTION_MOVE:
                Log.i(TAG, "Intercept ACTION_MOVE");
//                float moveX = event.getRawX();
                float moveY = event.getRawY();
                //发生横向滑动拦截
                if (Math.abs(moveY - downY) > scaledTouchSlop) {
                    return true;
                }
        }
        return super.onInterceptTouchEvent(event);
    }

    /**
     * 滚动处理
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTranY = this.getTranslationY();
                downY = event.getRawY();
                if (animator != null && animator.isRunning())
                    animator.cancel();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveY = event.getRawY();

                //请求父控件不拦截
                getParent().requestDisallowInterceptTouchEvent(true);
                //取消事件
                MotionEvent cancelEvent = MotionEvent.obtain(event);
                cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                onTouchEvent(cancelEvent);

                //滑动限制
                float tranY = lastTranY + (moveY - downY);
                float viewHeight = getHeightWithMargin(this);
                if (tranY < 0)
                    tranY = 0;
                else if (tranY > viewHeight) {
                    tranY = viewHeight;
                }
                this.setTranslationY(tranY);
                break;
            case MotionEvent.ACTION_UP:
                float viewHeight1 = getHeightWithMargin(this);

                setShow(!(getTranslationY() >= viewHeight1 * 1 / 3));
                toScroll = false;
                break;
        }
        return super.onTouchEvent(event);
    }

    public void toggleShow(){
        setShow(!isShow);
    }

    public boolean getShow(){
        return isShow;
    }

    public void setShow(boolean isShow){
        float viewHeight = getHeightWithMargin(this);
        if (isShow) {
            animator = ValueAnimator.ofFloat(getTranslationY(), 0);
        } else {
            animator = ValueAnimator.ofFloat(getTranslationY(), viewHeight);
        }

        animator.removeAllUpdateListeners();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setTranslationY((float) animation.getAnimatedValue());
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(onShowListener != null){
                    onShowListener.onShow(SwipeCloseLayout.this.isShow = (Float.compare(getTranslationY(), 0) == 0));
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    private int getHeightWithMargin(View view) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
        return view.getHeight() + lp.topMargin + lp.bottomMargin;
    }

    public interface OnShowListener{
        void onShow(boolean isShow);
    }
}
