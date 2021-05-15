package com.excessivemedia.walltone.helpers;

import android.app.Activity;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class SimpleGestureListener extends SimpleOnGestureListener{


    public final static int MODE_SOLID       = 1;
    public final static int MODE_DYNAMIC     = 2;

    private final static int ACTION_FAKE = -13; //just an unlikely number

    private int mode             = MODE_DYNAMIC;
    private boolean tapIndicator = false;

    private final Activity context;
    private final GestureDetector detector;
    private final OnGestureDetected listener;

    public SimpleGestureListener(Activity context,OnGestureDetected sgl) {

        this.context = context;
        this.detector = new GestureDetector(context, this);
        this.listener = sgl;
    }

    public void onTouchEvent(MotionEvent event){

        boolean result = this.detector.onTouchEvent(event);

        if(this.mode == MODE_SOLID)
            event.setAction(MotionEvent.ACTION_CANCEL);
        else if (this.mode == MODE_DYNAMIC) {

            if(event.getAction() == ACTION_FAKE)
                event.setAction(MotionEvent.ACTION_UP);
            else if (result)
                event.setAction(MotionEvent.ACTION_CANCEL);
            else if(this.tapIndicator){
                event.setAction(MotionEvent.ACTION_DOWN);
                this.tapIndicator = false;
            }

        }
        //else just do nothing, it's Transparent
    }

    public void setMode(int m){
        this.mode = m;
    }


    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {

        final float xDistance = Math.abs(e1.getX() - e2.getX());
        final float yDistance = Math.abs(e1.getY() - e2.getY());
//        Log.wtf(SimpleGestureListener.class.getSimpleName(),xDistance+"  "+yDistance);
        int swipe_Max_Distance = 1080;
        if(xDistance > swipe_Max_Distance || yDistance > swipe_Max_Distance)
            return false;

        velocityX = Math.abs(velocityX);
        velocityY = Math.abs(velocityY);
//        Log.wtf(SimpleGestureListener.class.getSimpleName(),velocityY+"  "+swipe_Min_Velocity);
        boolean result = false;

        int swipe_Min_Distance = 100;
        int swipe_Min_Velocity = 100;
        if(velocityX > swipe_Min_Velocity && xDistance > swipe_Min_Distance){
            if(e1.getX() > e2.getX()) // right to left
                this.listener.onSwipeLeft();
            else
                this.listener.onSwipeRight();

            result = true;
        }
        if(velocityY > swipe_Min_Velocity && yDistance > swipe_Min_Distance){
            if(e1.getY() > e2.getY()) // bottom to up
                this.listener.onSwipeUp();
            else
                this.listener.onSwipeDown();

            result = true;
        }

        return result;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        this.tapIndicator = true;
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent arg) {
        this.listener.onDoubleTap();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent arg) {
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent arg) {

        if(this.mode == MODE_DYNAMIC){        // we owe an ACTION_UP, so we fake an
            arg.setAction(ACTION_FAKE);      //action which will be converted to an ACTION_UP later.
            this.context.dispatchTouchEvent(arg);
        }

        return false;
    }

    public interface OnGestureDetected{
        default void onSwipeUp(){}
        default void onSwipeDown(){}
        default void onSwipeLeft(){}
        default void onSwipeRight(){}
        default void onDoubleTap(){}
    }

}