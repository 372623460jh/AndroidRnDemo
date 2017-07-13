package com.jianghe.hotupdate;

import android.os.Environment;

import com.mainandroid.mainview.MainApplication;

import org.json.JSONObject;

import java.io.File;

/**
 * Created by Song on 2017/2/15.
 */
public class ReactNativeConstant {

    /**
     * RNbundle初始APK中的版本号每次打包需要更新
     */
    public static final String APK_RN_VERSION = "1";

    /**
     * RN界面是否启动调试
     */
    public static final boolean DEBUG = false;

    /**
     * bundle文件名
     */
    public static final String JS_BUNDLE_NAME = "index.android.bundle";

    /**
     * zip的文件名
     */
    public static final String ZIP_NAME = "jianghe";

    /**
     * 获取版本号的URL
     */
    public static final String VERSION_URL = "http://192.168.18.77:3000/getRnVersion";

    /**
     * handle中标志
     */
    public static final int HAN_ERROR = 1; //出错

    public static final int DOWNLOAD_OK = 4; //下载完成

    public static final int HAN_VERSION_OK = 5; //获取版本成功需更新
    public static final int HAN_VERSION_GO = 6; //获取版本成功不更新

    public static final int HAN_HOT_UPDATE_OK = 7; //热更新解压合并文件成功
    public static final int HAN_HOT_UPDATE_NO = 8; //热更新解压合并文件失败

    /**
     * SDcard中应用对应的文件夹跟目录
     */
    public static final String JS_PATCH_BASIC = Environment.getExternalStorageDirectory().toString()
            + File.separator + MainApplication.getInstance().getAppPackageName();

    /**
     * 保存正式bundle的文件路径
     */
    public static final String JS_BUNDLE_PATH = JS_PATCH_BASIC + "/RN/1/";

    /**
     * 正式bundle文件绝对路径
     */
    public static final String JS_BUNDLE_FILE_PATH = JS_BUNDLE_PATH + JS_BUNDLE_NAME;

    /**
     * 保存下载压缩包，解压文件，合并文件的文件路径
     */
    public static final String JS_OTHER_PATH = JS_PATCH_BASIC + "/RN/0/";

    /**
     * 热更下载后的zip文件绝对路径
     */
    public static final String JS_ZIP_PATH = JS_OTHER_PATH + ZIP_NAME + ".zip";

    /**
     * 合并后额新bundle文件
     */
    public static final String JS_NEWBUNDLE_PATH = JS_OTHER_PATH + JS_BUNDLE_NAME;

    /**
     * 增量包解压后的.pat文件
     */
    public static final String JS_UNZIP_PATH = JS_OTHER_PATH +"/jianghe/diff.pat";

    /**
     * 从服务器获取的版本信息
     */
    public static JSONObject VERSION_INFO;

}
