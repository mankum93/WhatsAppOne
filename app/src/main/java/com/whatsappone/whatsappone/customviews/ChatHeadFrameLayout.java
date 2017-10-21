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

/**
 * A FrameLayout that routes its events to its children
 */
public class ChatHeadFrameLayout extends FrameLayout {

    private static final String TAG = "ChatHeadFrameLayout";

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
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
