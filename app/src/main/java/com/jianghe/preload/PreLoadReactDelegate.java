
package com.jianghe.preload;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.Callback;
import com.facebook.react.devsupport.DoubleTapReloadRecognizer;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.modules.core.PermissionListener;

import javax.annotation.Nullable;

/**
 * 重写RN的页面加载方法实现预加载
 *
 * @author：jianghe
 */
public class PreLoadReactDelegate {

    private final int REQUEST_OVERLAY_PERMISSION_CODE = 1111;

    private final
    @Nullable
    Activity mActivity;
    private final
    @Nullable
    FragmentActivity mFragmentActivity;
    private final
    @Nullable
    String mMainComponentName;
    private
    @Nullable
    ReactRootView mReactRootView;
    private
    @Nullable
    DoubleTapReloadRecognizer mDoubleTapReloadRecognizer;
    private
    @Nullable
    PermissionListener mPermissionListener;
    private
    @Nullable
    Callback mPermissionsCallback;
    private
    @Nullable
    String mPrams;

    /**
     * 构造方法
     *
     * @param activity
     * @param mainComponentName 跳转到RN页面的注册名字
     * @param prams             跳转到RN页面的传参（json字符串）
     */
    public PreLoadReactDelegate(Activity activity, @Nullable String mainComponentName, String prams) {
        mActivity = activity;
        mMainComponentName = mainComponentName;
        mFragmentActivity = null;
        mPrams = prams;
    }

    public PreLoadReactDelegate(
            FragmentActivity fragmentActivity,
            @Nullable String mainComponentName) {
        mFragmentActivity = fragmentActivity;
        mMainComponentName = mainComponentName;
        mActivity = null;
    }

    protected ReactRootView createRootView() {
        return new ReactRootView(getContext());
    }

    /**
     * 重写父类中的该方法，该方法用于返回一个Bundle用于初始化ReactRootView时从activity传递参数给RN
     *
     * @return
     */
    protected
    @Nullable
    Bundle getLaunchOptions() {
        Bundle bundle = new Bundle();
        bundle.putString("bundle", mPrams);
        return bundle;
    }

    /**
     * 获取myapplication中得RN主机
     *
     * @return
     */
    protected ReactNativeHost getReactNativeHost() {
        return ((ReactApplication) getPlainActivity().getApplication()).getReactNativeHost();
    }

    public ReactInstanceManager getReactInstanceManager() {
        return getReactNativeHost().getReactInstanceManager();
    }

    protected void onCreate(Bundle savedInstanceState) {
        boolean needsOverlayPermission = false;
        if (getReactNativeHost().getUseDeveloperSupport() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 是否具有悬浮窗权限
            if (!Settings.canDrawOverlays(getContext())) {
                needsOverlayPermission = true;
                Intent serviceIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName()));
                ((Activity) getContext()).startActivityForResult(serviceIntent, REQUEST_OVERLAY_PERMISSION_CODE);
            }
        }

        if (mMainComponentName != null && !needsOverlayPermission) {
            loadApp(mMainComponentName);
        }

        mDoubleTapReloadRecognizer = new DoubleTapReloadRecognizer();
    }

    /**
     * 自定义的RN页面加载机制加载RN页面的真实方法
     * 先从工具类中PreLoadReactNative的缓存集合中读取如果有直接加载
     */
    protected void loadApp(String appKey) {
        // 1.从缓存中获取RootView
        mReactRootView = PreLoadReactNative.getReactRootView(mMainComponentName);
        if (mReactRootView == null) {
            // 2.缓存中不存在RootView,直接创建
            mReactRootView = new ReactRootView(mActivity);
            mReactRootView.startReactApplication(
                    getReactInstanceManager(),
                    appKey,
                    getLaunchOptions());
        }
        // 3.将RootView设置到Activity布局
        mActivity.setContentView(mReactRootView);
    }

    protected void onPause() {
        if (getReactNativeHost().hasInstance()) {
            getReactNativeHost().getReactInstanceManager().onHostPause(getPlainActivity());
        }
    }

    protected void onResume() {
        if (getReactNativeHost().hasInstance()) {
            getReactNativeHost().getReactInstanceManager().onHostResume(
                    getPlainActivity(),
                    (DefaultHardwareBackBtnHandler) getPlainActivity());
        }

        if (mPermissionsCallback != null) {
            mPermissionsCallback.invoke();
            mPermissionsCallback = null;
        }
    }

    protected void onDestroy() {
        if (mReactRootView != null) {
            mReactRootView.unmountReactApplication();
            mReactRootView = null;
        }
        if (getReactNativeHost().hasInstance()) {
            getReactNativeHost().getReactInstanceManager().onHostDestroy(getPlainActivity());
        }
    }

    /**
     * 当获得悬浮窗权限成功
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /**
         * 由于预加载的缘故始终都会进入该条件，此处修改了源代码
         */
        if (getReactNativeHost().hasInstance()) {
            getReactNativeHost().getReactInstanceManager()
                    .onActivityResult(getPlainActivity(), requestCode, resultCode, data);
        }
        if (requestCode == REQUEST_OVERLAY_PERMISSION_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(getContext())) {
                if (mMainComponentName != null) {
                    loadApp(mMainComponentName);
                }
            }
        }
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (getReactNativeHost().hasInstance() && getReactNativeHost().getUseDeveloperSupport()) {
            if (keyCode == KeyEvent.KEYCODE_MENU) {
                getReactNativeHost().getReactInstanceManager().showDevOptionsDialog();
                return true;
            }
            boolean didDoubleTapR = Assertions.assertNotNull(mDoubleTapReloadRecognizer)
                    .didDoubleTapR(keyCode, getPlainActivity().getCurrentFocus());
            if (didDoubleTapR) {
                getReactNativeHost().getReactInstanceManager().getDevSupportManager().handleReloadJS();
                return true;
            }
        }
        return false;
    }

    public boolean onBackPressed() {
        if (getReactNativeHost().hasInstance()) {
            getReactNativeHost().getReactInstanceManager().onBackPressed();
            return true;
        }
        return false;
    }

    public boolean onNewIntent(Intent intent) {
        if (getReactNativeHost().hasInstance()) {
            getReactNativeHost().getReactInstanceManager().onNewIntent(intent);
            return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions(
            String[] permissions,
            int requestCode,
            PermissionListener listener) {
        mPermissionListener = listener;
        getPlainActivity().requestPermissions(permissions, requestCode);
    }

    public void onRequestPermissionsResult(
            final int requestCode,
            final String[] permissions,
            final int[] grantResults) {
        mPermissionsCallback = new Callback() {
            @Override
            public void invoke(Object... args) {
                if (mPermissionListener != null && mPermissionListener.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
                    mPermissionListener = null;
                }
            }
        };
    }

    private Context getContext() {
        if (mActivity != null) {
            return mActivity;
        }
        return Assertions.assertNotNull(mFragmentActivity);
    }

    private Activity getPlainActivity() {
        return ((Activity) getContext());
    }
}
