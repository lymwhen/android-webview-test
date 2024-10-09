package com.chunshu.jdsjwt;

import android.app.Application;
import android.content.Context;

public class APPAplication extends Application {
	private static final String TAG = "SFApplication";

	private static APPAplication instance;

	public static APPAplication getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public Context getContext(){
		return getApplicationContext();
	}
}
