package com.chunshu.jdsjwt.utils;

import android.app.Activity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chunshu.jdsjwt.MainActivity;
import com.chunshu.jdsjwt.ServerConfig;

public class JsInterface {

    private MainActivity activity;
    private WebView webView;

    public JsInterface(Activity activity, WebView webView) {
        this.activity = (MainActivity)activity;
        this.webView = webView;
    }

    @JavascriptInterface
    public void onFinishActivity() {
        activity.finish();
    }

    /**
     *
     * @param key
     * @param value
     */
    @JavascriptInterface
    public void setPrefString(String key, String value){
        PrefUtils.setString(activity.getApplicationContext(), key, value);
    }

    @JavascriptInterface
    public String getPrefString(String key){
        return PrefUtils.getString(activity.getApplicationContext(), key, "");
    }

    @JavascriptInterface
    public void delPrefString(String key){
        PrefUtils.delString(activity.getApplicationContext(), key);
    }

    @JavascriptInterface
    public void showLoadingAlert(boolean isShow){
        if(isShow) {
            activity.loadingAlert.show();
        }else{
            activity.loadingAlert.dismiss();
        }
    }

}