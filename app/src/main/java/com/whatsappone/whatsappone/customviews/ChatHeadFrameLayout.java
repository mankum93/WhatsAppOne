package com.whatsappone.whatsappone.customviews;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by DJ on 10/16/2017.
 */

public class ChatHeadFrameLayout extends FrameLayout {

    /**
     * Store the initial touch down x coordinate
     */
    private float initialTouchX;
    /**
     * Store the initial touch down y coordinate
     */
    private float initialTouchY;

    public ChatHeadFrameLayout(@NonNull Context context) {
        super(context);
    }

    public ChatHeadFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatHeadFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(21)
    public ChatHeadFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // If the event is a move, the ViewGroup needs to handle it and return
        // the indication here
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                //remember the initial position
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                return true;

            /*case MotionEvent.ACTION_UP:
                int Xdiff = (int) (event.getRawX() - initialTouchX);
                int Ydiff = (int) (event.getRawY() - initialTouchY);


                //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                //So that is click event.
                        *//*if (Xdiff < 10 && Ydiff < 10) {
                            if (isViewCollapsed()) {
                                //When user clicks on the image view of the collapsed layout,
                                //visibility of the collapsed layout will be changed to "View.GONE"
                                //and expanded view will become visible.
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(View.VISIBLE);
                            }
                        }*//*
                return false;*/

            /*case MotionEvent.ACTION_MOVE:
                //Calculate the X and Y coordinates of the view.
                setX(event.getRawX() - initialTouchX);
                setY(event.getRawY() - initialTouchY);


                //Update the layout with new X & Y coordinate
                invalidate();

                return true;*/
        }
        return false;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Will be called in case we decide to intercept the event
        // Handle the view move-with-touch behavior here

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                //remember the initial position
                /*initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();*/
                //super.onTouchEvent(event);
                return true;

            case MotionEvent.ACTION_UP:
                int Xdiff = (int) (event.getRawX() - initialTouchX);
                int Ydiff = (int) (event.getRawY() - initialTouchY);


                //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                //So that is click event.
                        if (Xdiff < 10 && Ydiff < 10) {
                            super.onTouchEvent(event);
                            return false;
                        }
                return true;

            case MotionEvent.ACTION_MOVE:
                //Calculate the X and Y coordinates of the view.
                setX(event.getRawX());
                setY(event.getRawY());


                //Update the layout with new X & Y coordinate
                invalidate();

                return true;
        }
        return false;
    }
}
