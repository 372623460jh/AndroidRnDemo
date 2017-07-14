package com.jianghe.preload;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.modules.core.PermissionAwareActivity;
import com.facebook.react.modules.core.PermissionListener;
import com.mainandroid.mainview.MainApplication;

import javax.annotation.Nullable;

/**
 * 预加载基类重写ReactActivity
 * Created by jianghe on 2017/6/27.
 */
public abstract class PreLoadReactActivity extends Activity
        implements DefaultHardwareBackBtnHandler, PermissionAwareActivity {

    private PreLoadReactDelegate mPreLoadReactDelegate;

    //构造方法中创建PreLoadReactDelegate对象
    protected PreLoadReactActivity() {
        mPreLoadReactDelegate = createPreLoadReactDelegate();
    }

    /**
     * 创建PreLoadReactDelegate对象
     *
     * @return
     */
    private PreLoadReactDelegate createPreLoadReactDelegate() {
        /**
         * getMainComponentName:组件名
         * getPrams：传递到RN页面的参数
         */
        return new PreLoadReactDelegate(this, getMainComponentName(), getPrams());
    }

    /**
     * 获取RN注册也名字的方法需要在RN的Activity中重写方法对应的RN注册名
     */
    protected
    @Nullable
    String getMainComponentName() {
        return null;
    }

    /**
     * 返回传递到RN的json字符串,需要子类重写
     *
     * @return
     */
    protected
    @Nullable
    String getPrams() {
        return MainApplication.prams;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreLoadReactDelegate.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPreLoadReactDelegate.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreLoadReactDelegate.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPreLoadReactDelegate.onDestroy();
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (!mPreLoadReactDelegate.onNewIntent(intent)) {
            super.onNewIntent(intent);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mPreLoadReactDelegate.onKeyUp(keyCode, event) || super.onKeyUp(keyCode, event);
    }

    // 申请悬浮窗权限
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPreLoadReactDelegate.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (!mPreLoadReactDelegate.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        super.onBackPressed();
    }


    /**
     * 处理权限授权
     */
    @Override
    public void requestPermissions(String[] permissions, int requestCode, PermissionListener listener) {
        mPreLoadReactDelegate.requestPermissions(permissions, requestCode, listener);
    }

    /**
     * 授权结果
     */
    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        mPreLoadReactDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
