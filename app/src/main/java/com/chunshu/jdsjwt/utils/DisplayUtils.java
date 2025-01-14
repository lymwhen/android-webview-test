package com.chunshu.jdsjwt.utils;

import android.content.Context;
import android.util.TypedValue;

public class DisplayUtils {

    public static int dp2px(Context context, int dpValue){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dpValue,
                context.getResources().getDisplayMetrics());
    }

    public static int sp2px(Context context, int spValue){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,spValue,
                context.getResources().getDisplayMetrics());
    }
}
