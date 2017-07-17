package com.mainandroid.mainview;

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
import android.widget.Button;
import android.widget.TextView;

import com.jianghe.androidrndemo.R;
import com.jianghe.hotupdate.HotUpdateTools;
import com.jianghe.hotupdate.ReactNativeConstant;
import com.jianghe.preload.PreLoadReactNative;
import com.jianghe.reactnative.RNActivity;
import com.jianghe.hotupdate.CallBack;
import com.jianghe.hotupdate.CallBackMess;


public class HotUpdateActivity extends Activity {

    MyHandler myHandler;
    //热更新回调方法
    private CallBack callBack;
    //热更新工具类
    private HotUpdateTools hut;

    //下载总长多
    public int allLength = 0;
    //已下载长度
    public int Lengthed = 0;

    Button btn1 = null;// 按钮1
    TextView tv1 = null;// 提示

    // Handler
    class MyHandler extends Handler {
        public MyHandler() {
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == ReactNativeConstant.HAN_VERSION_OK) {
                // 获取版本信息成功,需要更新
                tv1.setText("获取最新版本信息成功" + msg.obj);
            } else if (msg.what == ReactNativeConstant.HAN_VERSION_GO) {
                //获取版本信息成功,不需要更新
                tv1.setText("获取版本信息成功" + msg.obj);
                //预加载
                proLoadAll();
                // 给按钮添加事件监听
                btn1.setOnClickListener(new MyClickListener());//添加监听
            } else if (msg.what == ReactNativeConstant.DOWNLOAD_OK) {
                //下载新版本更新包成功
                tv1.setText("下载更新包成功");
            } else if (msg.what == ReactNativeConstant.HAN_HOT_UPDATE_OK) {
                //热更新解压合并文件成功
                tv1.setText("热更新解压合并文件成功");
                //预加载
                proLoadAll();
                // 给按钮添加事件监听
                btn1.setOnClickListener(new MyClickListener());//添加监听
            } else if (msg.what == ReactNativeConstant.HAN_HOT_UPDATE_NO) {
                //热更新解压合并文件失败
                tv1.setText("热更新解压合并文件失败");
            } else if (msg.what == ReactNativeConstant.DOWN_LOAD_PROGRESS) {
                //下载进度
                tv1.setText("下载进度：" + msg.obj);
            } else if (msg.what == ReactNativeConstant.HAN_ERROR) {
                //出错
                tv1.setText("出错");
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_astest);
        this.init();
    }

    //初始化方法
    private void init() {
        // 获取按钮
        btn1 = (Button) findViewById(R.id.btn1);
        tv1 = (TextView) findViewById(R.id.tv1);
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
                        ms1.obj = Lengthed + "/" + allLength;
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

    /**
     * 预加载所有页面的方法必须在主线中执行
     */
    public void proLoadAll() {
        // 预加载
        for (int n = 0; n < FirstActivity.regName.length; n++) {
            System.out.println("预加载：" + FirstActivity.regName[n]);
            PreLoadReactNative.preLoad(HotUpdateActivity.this, FirstActivity.regName[n], MainApplication.prams);
        }
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

    //事件处理内部类
    class MyClickListener implements OnClickListener {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn1:
                    Intent intent = new Intent();
                    intent.setClass(HotUpdateActivity.this, RNActivity.class);
                    HotUpdateActivity.this.startActivity(intent);//跳转到RN的activity
                    HotUpdateActivity.this.finish();
                    break;
                default:
                    break;
            }
        }
    }
}
