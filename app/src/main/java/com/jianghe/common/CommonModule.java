package com.jianghe.common;

import android.content.Intent;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.jianghe.reactnative.HotUpdateActivity;

/**
 * CommonModule继承ReactContextBaseJavaModule用于实现原生模块方法
 *
 * @author jiangHe
 * @version 1.0.0
 * @date 2017-6-19
 */
public class CommonModule extends ReactContextBaseJavaModule {

    //react的上下文对象
    public ReactContext reactContext;

    //react的事件传递类DeviceEventEmitter对象
    //通过DeviceEventEmitter.emit方法可向RN发送消息
    private DeviceEventManagerModule.RCTDeviceEventEmitter deviceEventEmitter = null;

    //该类的实例化对象
    private static CommonModule commonModule = null;

    //构造方法初始化ReactContext对象
    protected CommonModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    //返回模块名
    @Override
    public String getName() {
        return "CommonModule";
    }

    //静态方法实例化本类返回实例化对象
    public static CommonModule initCommonModule(ReactApplicationContext reactContext) {
        commonModule = new CommonModule(reactContext);
        return commonModule;
    }

    /**
     * RN调用原生的热更新方法
     *
     * @param appRegistryName 调用热更新的RN页面的注册名
     */
    @ReactMethod
    public void hotUpdate(String appRegistryName) {

        Intent intent = new Intent();
        intent.setClass(reactContext, HotUpdateActivity.class);
        intent.putExtra("Name", appRegistryName);
        intent.putExtra("Source", "1");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        reactContext.startActivity(intent);//跳转到热更新的activity

    }

    //获取实例化对象的静态方法
    public static CommonModule getCommonModule() {
        return commonModule;
    }

}
