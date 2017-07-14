package com.jianghe.preload;

/**
 * Created by jianghe on 2017/6/27.
 */

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;

import com.facebook.react.ReactRootView;
import com.mainandroid.mainview.MainApplication;

import java.util.HashMap;
import java.util.Map;


/**
 * 预加载缓存类
 * 用来缓存ReactRootView
 * Created by jianghe on 2017/6/27.
 */
public class PreLoadReactNative {

    private static final Map<String, ReactRootView> CACHE = new HashMap<>();

    /**
     * 初始化ReactRootView，并添加到缓存
     *
     * @param activity
     * @param componentName Rn注册名
     * @param prams         传递的参数(json字串)
     */
    public static void preLoad(Activity activity, String componentName, String prams) {

        //当基础页重新获取焦点时移除rnrootview
        PreLoadReactNative.deatchView(componentName);
        // 1.创建ReactRootView（使用MainApplication中创建的RNhost对象）
        ReactRootView rootView = new ReactRootView(activity);
        rootView.startReactApplication(
                MainApplication.getInstance().getReactNativeHost().getReactInstanceManager(),//
                componentName,
                PreLoadReactNative.getLaunchOptions(prams));
        // 2.添加到缓存
        CACHE.put(componentName, rootView);
    }

    static Bundle getLaunchOptions(String prams) {
        Bundle bundle = new Bundle();
        bundle.putString("bundle", prams);
        return bundle;
    }

    /**
     * 获取ReactRootView
     *
     * @param componentName
     * @return
     */
    public static ReactRootView getReactRootView(String componentName) {
        return CACHE.get(componentName);
    }

    /**
     * 从当前界面移除 ReactRootView
     *
     * @param component
     */
    public static void deatchView(String component) {
        try {
            ReactRootView rootView = getReactRootView(component);
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null) {
                parent.removeView(rootView);
            }
            if (CACHE.get(component) != null) {
                CACHE.remove(component);
            }
        } catch (Throwable e) {
        }
    }
}
