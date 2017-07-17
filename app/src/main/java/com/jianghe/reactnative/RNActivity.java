package com.jianghe.reactnative;

import com.jianghe.preload.PreLoadReactActivity;

/**
 * Created by jianghe on 2017/6/27.
 */

public class RNActivity extends PreLoadReactActivity {

    /**
     * 重写PreLoadReactActivity的getMainComponentName方法
     */
    @Override
    protected String getMainComponentName() {
        return "Hello";
    }

}