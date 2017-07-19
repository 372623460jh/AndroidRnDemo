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
import com.jianghe.reactnative.HotUpdateActivity;

/**
 * Created by jianghe on 2017/7/14.
 */

public class FirstActivity extends Activity {

    Button btn1 = null;// 按钮1
    Button btn2 = null;// 按钮2
    boolean hasPermissions = false;
    boolean first = true;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstactivity);
        //申请读写权限
        this.verifyStoragePermissions();
    }

    /**
     * 首次执行方法
     * onCreate方法中只是用来申请权限，当权限申请成功后才会执行本方法
     */
    protected void onCreateTrue() {
        this.hasPermissions = true;
        this.init();
        PreLoadReactNative.proload(FirstActivity.this);
    }

    /**
     * 当每次进入页面的时候从新预加载RN页面
     * 预加载时需要文件读写权限，加载读写权限需要悬浮窗权限
     */
    protected void onStart() {
        if (this.hasPermissions && this.first == false) {
            // 如果有权限才预加载
            PreLoadReactNative.proload(FirstActivity.this);
        }
        if (this.first == true) {
            this.first = false;
        }
        super.onStart();
    }

    //申请读写权限
    public void verifyStoragePermissions() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permissionW = ActivityCompat.checkSelfPermission(FirstActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionW != PackageManager.PERMISSION_GRANTED) {
                    // 两个权限都需要申请
                    ActivityCompat.requestPermissions(FirstActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    this.onCreateTrue();
                }
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
                    this.onCreateTrue();
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
                    intent.setClass(FirstActivity.this, MainApplication.RNMAP.get("Hello"));
                    FirstActivity.this.startActivity(intent);//跳转到RN的activity
                    break;
                case R.id.btn2:
                    intent.setClass(FirstActivity.this, HotUpdateActivity.class);
                    intent.putExtra("Name", "Hello");
                    intent.putExtra("Source", "0");
                    FirstActivity.this.startActivity(intent);//跳转到热更新的activity
                    break;
                default:
                    break;
            }
        }
    }
}
