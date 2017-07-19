package com.mainandroid.mainview;

import android.os.Bundle;

import com.jianghe.preload.PreLoadReactActivity;

/**
 * Created by jianghe on 2017/6/27.
 */

public class RNActivity extends PreLoadReactActivity {

    public static RNActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
    }

    /**
     * 重写PreLoadReactActivity的getMainComponentName方法
     */
    @Override
    protected String getMainComponentName() {
        return "Hello";
    }

}