package com.whatsappone.whatsappone.customviews;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by DJ on 10/16/2017.
 */

/**
 * A FrameLayout that intercepts the drag events but delegates the click events
 * to the children
 */
public class ChatHeadBubbleFrameLayout extends FrameLayout {

    private static final String TAG = "ChatHeadBubbleFrameLayout";

    /**
     * Store the initial touch down x coordinate
     */
    private float initialTouchX;
    /**
     * Store the initial touch down y coordinate
     */
    private float initialTouchY;

    public ChatHeadBubbleFrameLayout(@NonNull Context context) {
        super(context);
    }

    public ChatHeadBubbleFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatHeadBubbleFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(21)
    public ChatHeadBubbleFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // The plan is to intercept the events here and if,
        // 1) The move is an ACTION_DOWN, return false indicating we want more events
        // to be able to ascertain if we want this ViewGroup to move or the children
        // views to handle the click event.
        // 2) On receiving the ACTION_MOVE, of course, move the ViewGroup but the sure short
        // indication of click would be to check it for in ACTION_DOWN with threshold of 5px.
        // In that case, we would return super.onInterceptTouchEvent(event); meaning, let the
        // event handling happen normally without interception.
        // (Remember - the ACTION_DOWN had been recorded by the children previously when we
        // returned false and on receiving the ACTION_UP, it would constitute the complete event)
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                // remember the initial position
                initialTouchX = event.getX();
                //Log.d(TAG, "onInterceptTouchEvent().ACTION_DOWN.initialTouchX: " + initialTouchX);
                initialTouchY = event.getY();
                //Log.d(TAG, "onInterceptTouchEvent().ACTION_DOWN.initialTouchY: " + initialTouchY);
                return false;

            case MotionEvent.ACTION_UP:
                int xDiff = (int) (event.getX() - initialTouchX);
                int yDiff = (int) (event.getY() - initialTouchY);


                //The check for Xdiff <5 && YDiff< 5 because sometime elements moves a little while clicking.
                //So that is click event.
                if (xDiff < 5 && yDiff < 5) {
                    return super.onInterceptTouchEvent(event);
                }
                return true;

            case MotionEvent.ACTION_MOVE:

                int moveX = (int) (event.getX() - initialTouchX + getX());
                int moveY = (int) (event.getY() - initialTouchY + getY());

                setX(moveX);
                setY(moveY);

                //Update the layout with new X & Y coordinate
                invalidate();
                return  false;
        }
        return false;

    }
}
