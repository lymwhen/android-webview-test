package com.chunshu.jdsjwt.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.chunshu.jdsjwt.MainActivity;
import com.chunshu.jdsjwt.R;

public class WidgetUtils {
    public static AlertDialog createLoading(Context context, String title){
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_wait, null);
        TextView text = view.findViewById(R.id.text);
        text.setText(title);
        return new AlertDialog.Builder(context)
//                .setIcon(R.mipmap.ic_launcher)
//                .setTitle(title)
                .setView(view)
                .setCancelable(false)
                .create();
    }
}
