package com.chunshu.jdsjwt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class SplashActivity extends AppCompatActivity {

    private final int SPLASH_DISPLAY_LENGHT = 200;

    private Handler handler;

    ImageView image_start;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        image_start = findViewById(R.id.image_start);
        handler = new Handler();

        applyPermission();
    }

    private void startMainActivity(){
        // 延迟SPLASH_DISPLAY_LENGHT时间然后跳转到MainActivity
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this,MainActivity.class);
                startActivity(intent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGHT);
    }

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    public void applyPermission() {
        //判断权限是否打开
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            startMainActivity();
        }
    }

    @Override
    //回调方法
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startMainActivity();
            } else {
                Toast.makeText(SplashActivity.this, "请允许存储权限以保证APP正常运行", Toast.LENGTH_SHORT).show();
                startMainActivity();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
