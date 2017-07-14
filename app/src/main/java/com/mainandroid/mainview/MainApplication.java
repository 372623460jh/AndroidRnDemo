package com.mainandroid.mainview;

import android.app.Application;
import android.content.Context;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.jianghe.hotupdate.ReactNativeConstant;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

public class MainApplication extends Application implements ReactApplication {
    public static Context appContext;
    private static MainApplication instance;
    //传递到RN页的参数
    public static String prams = "";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;//MainApplication的实例化对象
        appContext = getApplicationContext();//应用的上下文对象
        SoLoader.init(this, false);
    }

    /**
     * 创建一个ReactNativeHost对象该对象是RN的主机对象
     * 包含RN主机加载的bundle
     * 包含的ReactPackage
     * 包含RN是否处于调试状态
     */
    private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {

        /**
         * 重写抽象类中的getJSBundleFile获取Bundle文件的方法
         * @return
         * */
        @Nullable
        @Override
        protected String getJSBundleFile() {

            File file = new File(ReactNativeConstant.JS_BUNDLE_FILE_PATH);
            if (file != null && file.exists()) {
                // 如果在SD卡的bundle存储路径下存在bundle文件就加载该文件
                return ReactNativeConstant.JS_BUNDLE_FILE_PATH;
            } else {
                // 如果不存在就去assets中加载
                return super.getJSBundleFile();
            }
        }

        /**
         * 是否是debug状态
         * @return
         */
        @Override
        public boolean getUseDeveloperSupport() {
            return ReactNativeConstant.DEBUG;
        }

        /**
         * 包含哪些ReactPackage包
         * @return List<ReactPackage>
         */
        @Override
        protected List<ReactPackage> getPackages() {
            return Arrays.<ReactPackage>asList(
                    new MainReactPackage()
            );
        }
    };

    /**
     * 实现了ReactApplication中的getReactNativeHost接口返回ReactNativeHost对象
     *
     * @return ReactNativeHost
     */
    @Override
    public ReactNativeHost getReactNativeHost() {
        return mReactNativeHost;
    }

    /**
     * 包名
     */
    public String getAppPackageName() {
        return this.getPackageName();
    }

    /**
     * 获取Application实例
     */
    public static MainApplication getInstance() {
        return instance;
    }

}
