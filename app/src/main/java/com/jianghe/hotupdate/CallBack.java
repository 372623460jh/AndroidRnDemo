package com.jianghe.hotupdate;

import android.os.Handler;


/**
 * Created by jianghe on 2017/7/17.
 */

/**
 * RN界面中热更新的HotUpdateTools构造方法
 */

//Android原生
//CallBack callBack = new CallBack(mHandler) {
//    @Override
//    public void handleMessage(CallBackMess data) {
//        switch (data.getMessTag()) {
//            case ReactNativeConstant.HAN_ERROR:
//                // 出错
//                break;
//            case ReactNativeConstant.DOWNLOAD_OK:
//                // 下载完成
//                break;
//        }
//    }
//};

//RN
//CallBack callBack = new CallBack() {
//    @Override
//    public void handleMessage(CallBackMess data) {
//        switch (data.getMessTag()) {
//            case ReactNativeConstant.HAN_ERROR:
//                // 出错
//                break;
//            case ReactNativeConstant.DOWNLOAD_OK:
//                // 下载完成
//                break;
//        }
//    }
//};

public abstract class CallBack {

    public Handler mHandler = null;

    //Android原生中更新的构造方法
    public CallBack(Handler myHandler) {
        this.mHandler = myHandler;
    }

    //rn中更新的构造方法
    public CallBack() {
    }

    public abstract void handleMessage(CallBackMess data);
}
