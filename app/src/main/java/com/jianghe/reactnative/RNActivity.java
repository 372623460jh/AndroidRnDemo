package com.jianghe.reactnative;

import com.facebook.react.ReactActivity;

/**
 * Created by jianghe on 2017/6/27.
 */

public class RNActivity extends ReactActivity {

    /**
     * 重写PreLoadReactActivity的getMainComponentName方法
     */
    @Override
    protected String getMainComponentName() {
        return "Hello";
    }

}