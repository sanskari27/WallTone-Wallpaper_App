package com.excessivemedia.walltone.widgets.Highlight;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.excessivemedia.walltone.R;
import com.excessivemedia.walltone.helpers.HighlightUtils;

import java.util.ArrayList;
import java.util.List;

public class Highlights extends CardView {

    private CardView rootView,imageCV;
    private TextView appnameTV;
    private ViewPager2 highlightsViewPager;

    private HighlightsAdapter highlightsAdapter;

    private ViewGroup.LayoutParams rootLayoutParams,viewPagerLayoutParams;
    private ViewGroup.MarginLayoutParams imageMarginLayoutParams;

    private List<String> list;

    private int heightRootView;
    private int heightViewPager;
    private int marginTopImageCV;
    private int position;
    private int prevY ;
    private int maxHeight;

    private int padding;

    private boolean tempBoolean;

    private final int startRed = Color.red(Color.BLACK);
    private final int startGreen = Color.red(Color.BLACK);
    private final int startBlue = Color.red(Color.BLACK);
    private final int endRed = Color.red(Color.WHITE);
    private final int endGreen = Color.red(Color.WHITE);
    private final int endBlue = Color.red(Color.WHITE);


    public Highlights(@NonNull Context context) {
        super(context);
        init(context);
    }

    public Highlights(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Highlights(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.widget_highlights,
                        this, true);
        rootView = view.findViewById(R.id.rootView);
        imageCV = view.findViewById(R.id.highlightImageCV);
        appnameTV = view.findViewById(R.id.appnameTV);
        highlightsViewPager = findViewById(R.id.highlightsViewPager);
        initValues();
    }

    private void initValues() {
        rootLayoutParams = rootView.getLayoutParams();
        imageMarginLayoutParams = (ViewGroup.MarginLayoutParams)imageCV.getLayoutParams();
        viewPagerLayoutParams = highlightsViewPager.getLayoutParams();
        heightRootView =0;
        marginTopImageCV =0;
        prevY = 0;
        heightViewPager = 0;
        tempBoolean = false;
        maxHeight = (int) (getResources().getDisplayMetrics().heightPixels*0.15);


        list = new ArrayList<>();
        list.addAll(HighlightUtils.getInstance().getHighlights());

        highlightsAdapter = new HighlightsAdapter(list);
        highlightsViewPager.setAdapter(highlightsAdapter);

        highlightsViewPager.setClipToPadding(false);
        highlightsViewPager.setClipChildren(false);
        highlightsViewPager.setOffscreenPageLimit(list.size()>0?list.size():3);
        highlightsViewPager.getChildAt(0).setOverScrollMode(OVER_SCROLL_NEVER);
        position = Byte.MAX_VALUE/4;
        highlightsViewPager.setCurrentItem(position,false);


       setUpTransformer(1);

        highlightsViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int p) {
                super.onPageSelected(p);
                position = p;
            }
        });



    }

    public void setScrollPointer(boolean scrollingDown,int y){
        if(heightRootView ==0) {
            heightRootView = rootLayoutParams.height;
            marginTopImageCV = imageMarginLayoutParams.topMargin;
            heightViewPager = highlightsViewPager.getHeight();

            padding = highlightsViewPager.getPaddingLeft();
            //Init integer values
        }
//        Log.wtf(TAG,"heightRootView  " +heightRootView);
//        Log.wtf(TAG,"heightViewPager  " +heightViewPager);
//        Log.wtf(TAG,"y  " +y);
        if(prevY == heightRootView && scrollingDown) return;
        if(y>heightRootView )    y = heightRootView;
        prevY = y;

        double ratioVP = Math.max(((double) (heightViewPager - y))/(double)heightViewPager,0);
        double ratioRV = Math.max(((double) (heightRootView - y)) /(double)heightRootView,0);

        setUpTransformer(ratioVP);

        int baseHeight = (int) (heightRootView * ratioRV);
        rootLayoutParams.height = Math.max(baseHeight,maxHeight);
        rootView.setLayoutParams(rootLayoutParams);

        int heightVP = (int) (heightViewPager * ratioVP);
        viewPagerLayoutParams.height =  Math.max(heightVP,maxHeight-35);
        highlightsViewPager.setPadding(
                (int) (padding * ratioVP),
                0,
                (int) (padding * ratioVP),
                0
        );

        highlightsViewPager.setLayoutParams(viewPagerLayoutParams);
        if(tempBoolean){
            tempBoolean = false;// this is temp variable helping in alligning top layout to center
            highlightsViewPager.setCurrentItem(position-list.size(),false);
        }else{
            tempBoolean = true;
            highlightsViewPager.setCurrentItem(position+list.size(),false);
        }

        imageMarginLayoutParams.setMargins(
                imageMarginLayoutParams.leftMargin,
                (int) (ratioRV * marginTopImageCV)-10,
                imageMarginLayoutParams.rightMargin,
                imageMarginLayoutParams.bottomMargin
        );
        imageCV.requestLayout();

        highlightsViewPager.setUserInputEnabled(y == 0);

        changeTextColor(y);

    }

    private void changeTextColor(int val) {

        int red = startRed + (val * (endRed - startRed) / heightRootView);
        int blue = startBlue + (val * (endBlue - startBlue) / heightRootView);
        int green = startGreen + (val * (endGreen - startGreen) / heightRootView);
        appnameTV.setTextColor(Color.rgb(red,green,blue));
    }
    private void setUpTransformer(double ratio){
        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(
                new MarginPageTransformer(
                        (int) (40 + (1-ratio)*40)
                )
        );
        compositePageTransformer.addTransformer((page, position) -> {
            float r = Math.min(1 - Math.abs(position),0.1f);
            page.setScaleY(0.83f + r );
        });
        highlightsViewPager.setPageTransformer(compositePageTransformer);
    }
    public void setOnHighlightsClickListener(OnHighlightsClickListener onHighlightsClickListener) {
        highlightsAdapter.setOnHighlightsClickListener(onHighlightsClickListener);
    }



}
