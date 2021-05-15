package com.excessivemedia.walltone.widgets;

import android.animation.TimeAnimator;
import android.content.Context;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.excessivemedia.walltone.R;

public class GoogleSignIn extends FrameLayout implements TimeAnimator.TimeListener {

    private static final int LEVEL_INCREMENT = 400;
    private static final int MAX_LEVEL = 40000;

    private TimeAnimator mAnimator;
    private int mCurrentLevel = 0;
    private ClipDrawable redColor,yellowColor,greenColor,blueColor;

    private OnGoogleSignInListener listener;

    public interface OnGoogleSignInListener{
        void googleSignInClicked();
    }

    public GoogleSignIn(@NonNull Context context) {
        super(context);
        init(context);
    }

    public GoogleSignIn(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    public GoogleSignIn(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View root = LayoutInflater.from(context).inflate(R.layout.widget_google_signin,this,true);
        root.setOnClickListener(this::animateButton);
        LayerDrawable layerDrawable = (LayerDrawable) findViewById(R.id.background_provider).getBackground();
        redColor = (ClipDrawable) layerDrawable.findDrawableByLayerId(R.id.google_redColor);
        yellowColor = (ClipDrawable) layerDrawable.findDrawableByLayerId(R.id.google_yellowColor);
        greenColor = (ClipDrawable) layerDrawable.findDrawableByLayerId(R.id.google_greenColor);
        blueColor = (ClipDrawable) layerDrawable.findDrawableByLayerId(R.id.google_blueColor);
        mAnimator = new TimeAnimator();
        mAnimator.setTimeListener(this);
    }


    @Override
    public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
        redColor.setLevel(mCurrentLevel);
        yellowColor.setLevel(mCurrentLevel-10000);
        greenColor.setLevel(mCurrentLevel-20000);
        blueColor.setLevel(mCurrentLevel-30000);
        if(mCurrentLevel>=MAX_LEVEL){
            mAnimator.cancel();
        }
        mCurrentLevel = mCurrentLevel + LEVEL_INCREMENT;
    }

    public void animateButton(View view) {
        if(listener!=null){
            listener.googleSignInClicked();
        }
    }

    public void setListener(OnGoogleSignInListener listener) {
        this.listener = listener;
    }


    public void startAnimation() {
        if (!mAnimator.isRunning() && mCurrentLevel<=0) {
            mCurrentLevel = 0;
            mAnimator.start();
        }
    }
}
