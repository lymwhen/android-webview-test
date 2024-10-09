package com.chunshu.jdsjwt.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class UiUtils {

    /**
     * 推出确认框
     * @param activity
     * @param msg
     * @param noActionFlag true则不弹出，用于判断用户标志位
     */
    public static void exitAlert(Activity activity, String msg, boolean noActionFlag){
        if(noActionFlag) {
            return;
        }

        new AlertDialog.Builder(activity)
                .setTitle("提示")
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }
                }).show();
    }

    public static void showAlert(Activity activity, String msg){
        new AlertDialog.Builder(activity)
                .setTitle("提示")
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("确定", null).show();
    }
}
