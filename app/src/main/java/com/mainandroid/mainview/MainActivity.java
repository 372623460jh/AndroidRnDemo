package com.mainandroid.mainview;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.jianghe.reactnative.RNActivity;
import com.jianghe.hotupdate.ReactNativeConstant;


public class MainActivity extends Activity {

    //DownLoadManageID
    private long mDownLoadId;
    //下载完成的广播对象
    private CompleteReceiver localReceiver;
    MyHandler myHandler;

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
                // 下载更新包
                mDownLoadId = HotUpdateTools.DownLoad(MainActivity.this);
            } else if (msg.what == ReactNativeConstant.HAN_VERSION_GO) {
                //获取版本信息成功,不需要更新
                tv1.setText("获取最新版本信息成功" + msg.obj);
                // 给按钮添加事件监听
                btn1.setOnClickListener(new MyClickListener());//添加监听
            } else if (msg.what == ReactNativeConstant.DOWNLOAD_OK) {
                //下载新版本更新包成功
                tv1.setText("下载更新包成功");
            } else if (msg.what == ReactNativeConstant.HAN_HOT_UPDATE_OK) {
                //热更新解压合并文件成功
                tv1.setText("热更新解压合并文件成功");
                // 给按钮添加事件监听
                btn1.setOnClickListener(new MyClickListener());//添加监听
            } else if (msg.what == ReactNativeConstant.HAN_HOT_UPDATE_NO) {
                //热更新解压合并文件失败
                tv1.setText("热更新解压合并文件失败");
                //全量更新
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
        myHandler = new MyHandler();
        //注册下载通知广播
        registeReceiver();
        // 申请读写权限
        //申请存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            verifyStoragePermissions();
        } else {
            this.hotupdate();
        }
    }

    /**
     * 热更新的方法
     */
    public void hotupdate() {
        // 初始化bundle
        HotUpdateTools.initBundle();
        // 获取版本
        HotUpdateTools.getVersion(ReactNativeConstant.VERSION_URL, myHandler);
    }

    /**
     * 注册下载完成通知广播
     */
    private void registeReceiver() {
        if (localReceiver != null) {
            unregisterReceiver(localReceiver);
            localReceiver = null;
        }
        localReceiver = new CompleteReceiver();
        //注册广播
        registerReceiver(localReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    /**
     * 自定义广播类
     */
    public class CompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            long completeId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            //下载完成
            if (completeId == mDownLoadId) {
                System.out.println("下载完成" + completeId);
                //发消息给handler下载完成
                Message ms1 = new Message();
                ms1.what = ReactNativeConstant.DOWNLOAD_OK;
                myHandler.sendMessage(ms1);
                // 下载完成调用处理压缩文件类
                HotUpdateTools.handleZIP(myHandler);
            }
        }
    }

    //申请读写权限权限
    public void verifyStoragePermissions() {
        try {
            int permissionW = ActivityCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionW != PackageManager.PERMISSION_GRANTED) {
                // 两个权限都需要申请
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                this.hotupdate();
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
                    this.hotupdate();
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
                    intent.setClass(MainActivity.this, RNActivity.class);
                    MainActivity.this.startActivity(intent);//跳转到RN的activity
                    break;
                default:
                    break;
            }
        }
    }
}
