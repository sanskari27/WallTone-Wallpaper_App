package com.excessivemedia.walltone.widgets;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.CountDownTimer;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.excessivemedia.walltone.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class DownloadProgressIndicator extends Dialog {
    private final int ANIMATION_TIME = 3000;
    private final int ANIMATION_DURATION = 500;
    private final String LOADING = "Loading";

    private CircularProgressIndicator progress;
    private TextView title, percentage;
    private CountDownTimer animator;
    public DownloadProgressIndicator(@NonNull Context context) {
        super(context);
        init();
    }

    private void init() {
        setTitle(null);
        setCancelable(false);
        setOnCancelListener(null);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        setContentView(R.layout.widget_progress_bar);

        progress = findViewById(R.id.pb_circularProgress);
        title = findViewById(R.id.pb_title);
        percentage = findViewById(R.id.pb_percentage);
    }

    private void animateTitle() {
        String _1 = ".";
        String _2 = "..";
        String _3 = "...";
        if(animator == null) {
            animator = new CountDownTimer(ANIMATION_TIME,ANIMATION_DURATION) {
                @Override
                public void onTick(long left) {
                    int now = (int) ((ANIMATION_TIME-left)/ANIMATION_DURATION);
                    switch (now){
                        case 0:
                        case 1:
                            title.setText(String.format("%s%s", LOADING, _1)); break;
                        case 2:
                        case 5:
                            title.setText(String.format("%s%s", LOADING, _2)); break;
                        case 3:
                        case 4:
                            title.setText(String.format("%s%s", LOADING, _3)); break;
                    }
                }

                @Override
                public void onFinish() {
                    if(isShowing() && animator!=null)
                        animator.start();
                }
            };
        }
        animator.start();

    }

    public void setProgress(int progress) {
        this.percentage.setText(String.valueOf(progress));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.progress.setProgress(progress,true);
        }else{
            this.progress.setProgress(progress);
        }
    }

    public void setIndeterminate(boolean val) {
        progress.setIndeterminate(val);
    }
    public void setMessage(String msg){
        title.setText(msg);
        animateLoading(msg.contains(LOADING));
    }

    private void animateLoading(boolean val) {
        if(val) animateTitle();
    }

    @Override
    public void show() {
        super.show();
        animateLoading(title.getText().toString().contains(LOADING));
    }

    @Override
    public void dismiss() {
        super.dismiss();
        animator = null;
    }
}
