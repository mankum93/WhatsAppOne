package com.whatsappone.whatsappone.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by DJ on 10/1/2017.
 */

public class ViewUtils {

    public static float dpToPx(Context context, float dp){
        Resources r = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public static int getStatusBarHeight(Context context) {
        // Default Height
        int result = (int)dpToPx(context, 25);

        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getActionBarHeight(Context context, Resources.Theme theme){

        // Default height
        int actionBarHeight = (int)dpToPx(context, 56);

        TypedValue tv = new TypedValue();
        if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,context.getResources().getDisplayMetrics());
        }

        return actionBarHeight;
    }
}
