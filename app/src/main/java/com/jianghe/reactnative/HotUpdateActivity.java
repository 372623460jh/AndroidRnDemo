package com.jianghe.reactnative;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.jianghe.androidrndemo.R;
import com.jianghe.hotupdate.CallBack;
import com.jianghe.hotupdate.CallBackMess;
import com.jianghe.hotupdate.HotUpdateTools;
import com.jianghe.hotupdate.ReactNativeConstant;
import com.jianghe.preload.PreLoadReactNative;
import com.mainandroid.mainview.MainApplication;
import com.mainandroid.mainview.RNActivity;


public class HotUpdateActivity extends Activity {

    MyHandler myHandler;
    //热更新回调方法
    private CallBack callBack;
    //热更新工具类
    private HotUpdateTools hut;

    //热更新后将要跳转的页面
    private String willGoto;
    //来源（0安卓原生,1RN页）
    private String source;
    //标志（0无需更新 1需更新）
    private int tip;

    //下载总长多
    public int allLength = 0;
    //已下载长度
    public int Lengthed = 0;

    TextView tv1 = null;// 提示
    TextView tv2 = null;// 下载进度
    TextView tv3 = null;// 确认按钮

    // Handler
    class MyHandler extends Handler {
        public MyHandler() {
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == ReactNativeConstant.HAN_VERSION_OK) {
                // 获取版本信息成功,需要更新
                tv1.setText("开始下载更新包");
            } else if (msg.what == ReactNativeConstant.HAN_VERSION_GO) {
                //获取版本信息成功,不需要更新
                tv1.setText("已是最新版本1");
                tip = 0;
                tv3.setVisibility(View.VISIBLE);
            } else if (msg.what == ReactNativeConstant.DOWNLOAD_OK) {
                //下载新版本更新包成功
                tv1.setText("下载更新包成功！");
            } else if (msg.what == ReactNativeConstant.HAN_HOT_UPDATE_OK) {
                //热更新解压合并文件成功
                tv1.setText("增量热更新成功");
                // 彻底清空预加载数据
                PreLoadReactNative.clear();
                // 预加载
                PreLoadReactNative.proload(HotUpdateActivity.this);
                // 跳转到下一个页面
                tip = 1;
                tv3.setVisibility(View.VISIBLE);
            } else if (msg.what == ReactNativeConstant.HAN_HOT_UPDATE_NO) {
                //热更新解压合并文件失败
                tv1.setText("热更新解压合并文件失败");
            } else if (msg.what == ReactNativeConstant.DOWN_LOAD_PROGRESS) {
                //下载进度
                tv2.setText("下载进度：" + msg.obj);
            } else if (msg.what == ReactNativeConstant.COPY_BUNDLE_SUCC) {
                //拷贝bundle文件到SD卡成功
                tv1.setText("拷贝bundle文件到SD卡成功");
                // 彻底清空预加载数据
                PreLoadReactNative.clear();
            } else if (msg.what == ReactNativeConstant.HAN_ERROR) {
                //出错
                tv1.setText("出错");
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotupdate);
        this.init();
    }

    //初始化方法
    private void init() {
        // 获取按钮
        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);
        tv3 = (TextView) findViewById(R.id.tv3);
        tv3.setVisibility(View.INVISIBLE);
        //tv3的点击事件
        tv3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(source.equals("0")){
                    if(tip == 0){
                        gotoRnPage();
                    }else{
                        gotoRnPage();
                    }
                }else{
                    if(tip == 0){
                        removePage();
                    }else{
                        RNActivity.mainActivity.finish();
                        gotoRnPage();
                    }
                }
            }
        });
        // 获取传参
        this.willGoto = this.getIntent().getExtras().getString("Name");
        // 来源
        this.source = this.getIntent().getExtras().getString("Source");
        this.myHandler = new MyHandler();
        this.callBack = new CallBack(myHandler) {
            @Override
            public void handleMessage(CallBackMess data) {
                Message ms1 = new Message();
                ms1.what = data.getMessTag();
                switch (data.getMessTag()) {
                    case ReactNativeConstant.HAN_ERROR:
                        // 出错
                        ms1.obj = data.getStrMess();
                        this.mHandler.sendMessage(ms1);
                        break;
                    case ReactNativeConstant.COPY_BUNDLE_SUCC:
                        // 拷贝bundle文件到sd卡成功
                        this.mHandler.sendMessage(ms1);
                        break;
                    case ReactNativeConstant.DOWNLOAD_OK:
                        // 下载完成
                        ms1.obj = data.getStrMess();
                        this.mHandler.sendMessage(ms1);
                        break;
                    case ReactNativeConstant.HAN_VERSION_OK:
                        // 获取版本成功需更新
                        ms1.obj = data.getStrMess();
                        this.mHandler.sendMessage(ms1);
                        break;
                    case ReactNativeConstant.HAN_VERSION_GO:
                        // 获取版本成功不更新
                        ms1.obj = data.getStrMess();
                        this.mHandler.sendMessage(ms1);
                        break;
                    case ReactNativeConstant.HAN_HOT_UPDATE_OK:
                        // 热更新解压合并文件成功
                        ms1.obj = data.getStrMess();
                        this.mHandler.sendMessage(ms1);
                        break;
                    case ReactNativeConstant.HAN_HOT_UPDATE_NO:
                        // 热更新解压合并文件失败
                        ms1.obj = data.getStrMess();
                        this.mHandler.sendMessage(ms1);
                        break;
                    case ReactNativeConstant.DOWN_LOAD_PROGRESS:
                        //下载进度
                        if (allLength == 0) {
                            allLength = Integer.parseInt(data.getStrMess());
                        }
                        Lengthed += data.getIntMess();
                        ms1.obj = Lengthed + "B/" + allLength + "B";
                        this.mHandler.sendMessage(ms1);
                        break;
                }
            }
        };
        this.hut = new HotUpdateTools(this.callBack);
        // 申请存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            verifyStoragePermissions();
        } else {
            this.hut.hotUpdate();
        }
    }


    //跳转到下一个页面的方法
    private void gotoRnPage() {
        Intent intent = new Intent();
        intent.setClass(HotUpdateActivity.this, MainApplication.RNMAP.get(this.willGoto));
        HotUpdateActivity.this.startActivity(intent);//跳转到RN的activity
        HotUpdateActivity.this.finish();
    }

    // 移除模拟模态框
    private void removePage() {
        HotUpdateActivity.this.finish();
    }

    //申请读写权限权限
    public void verifyStoragePermissions() {
        try {
            int permissionW = ActivityCompat.checkSelfPermission(HotUpdateActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionW != PackageManager.PERMISSION_GRANTED) {
                // 两个权限都需要申请
                ActivityCompat.requestPermissions(HotUpdateActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                this.hut.hotUpdate();
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
                    this.hut.hotUpdate();
                    break;
                }
        }
    }
}
