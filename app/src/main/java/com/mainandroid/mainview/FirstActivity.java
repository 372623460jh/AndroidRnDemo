package com.mainandroid.mainview;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;

import com.jianghe.androidrndemo.R;
import com.jianghe.preload.PreLoadReactNative;
import com.jianghe.reactnative.RNActivity;

/**
 * Created by jianghe on 2017/7/14.
 */

public class FirstActivity extends Activity {
    public static String[] regName = new String[]{"Hello"};

    Button btn1 = null;// 按钮1
    Button btn2 = null;// 按钮1

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstactivity);
        //申请存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            verifyStoragePermissions();
        } else {
            this.init();
        }
    }

    /**
     * 当每次进入页面的时候从新预加载RN页面
     */
    protected void onStart() {
        // 预加载
        for (int n = 0; n < FirstActivity.regName.length; n++) {
            System.out.println("预加载：" + FirstActivity.regName[n]);
            PreLoadReactNative.preLoad(FirstActivity.this, FirstActivity.regName[n], MainApplication.prams);
        }
        super.onStart();
    }

    //申请读写权限权限
    public void verifyStoragePermissions() {
        try {
            int permissionW = ActivityCompat.checkSelfPermission(FirstActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionW != PackageManager.PERMISSION_GRANTED) {
                // 两个权限都需要申请
                ActivityCompat.requestPermissions(FirstActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                this.init();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 申请读写权限后回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1://写权限
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.init();
                    break;
                }
        }
    }

    //初始化方法
    private void init() {
        // 获取按钮
        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        btn1.setOnClickListener(new MyClickListener());//添加监听btn1.setOnClickListener(new MyClickListener());//添加监听
        btn2.setOnClickListener(new MyClickListener());//添加监听
    }

    //事件处理内部类
    class MyClickListener implements View.OnClickListener {
        public void onClick(View v) {
            Intent intent = new Intent();
            switch (v.getId()) {
                case R.id.btn1:
                    intent.setClass(FirstActivity.this, RNActivity.class);
                    FirstActivity.this.startActivity(intent);//跳转到RN的activity
                    break;
                case R.id.btn2:
                    intent.setClass(FirstActivity.this, HotUpdateActivity.class);
                    FirstActivity.this.startActivity(intent);//跳转到热更新的activity
                    break;
                default:
                    break;
            }
        }
    }
}
