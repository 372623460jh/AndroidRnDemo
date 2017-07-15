package com.jianghe.hotupdate;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.jianghe.preload.PreLoadReactNative;
import com.jianghe.tools.FileUtils;
import com.jianghe.tools.HttpUtil;
import com.mainandroid.mainview.MainApplication;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;

import static android.content.Context.MODE_PRIVATE;
import static com.jianghe.hotupdate.ReactNativeConstant.JS_OTHER_PATH;
import static com.jianghe.hotupdate.ReactNativeConstant.JS_ZIP_PATH;
import static com.jianghe.hotupdate.ReactNativeConstant.VERSION_INFO;
import static com.jianghe.tools.FileUtils.getMd5ByFile;

/**
 * 热更新工具类
 * Created by jianghe on 2017/7/6.
 */
public class HotUpdateTools {

    public static Thread versionT = null;

    public static Thread handleT = null;

    /**
     * 读取本地RN信息
     */
    public static RnInfoPojo getNativeRNVersion() {
        // 从SharedPreferences中读取Rn信息
        SharedPreferences sp = MainApplication.appContext.getSharedPreferences("RNINFO", MODE_PRIVATE);
        RnInfoPojo rnInfoPojo = new RnInfoPojo();
        rnInfoPojo.setMd5(sp.getString("RNMD5", "0"));
        return rnInfoPojo;
    }

    /**
     * 设置本地RN信息
     */
    public static void setNativeRNVersion(String md5) {
        // 将Rn信息设置到SharedPreferences中
        SharedPreferences sp = MainApplication.appContext.getSharedPreferences("RNINFO", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("RNMD5", md5);
        editor.commit();
    }

    /**
     * 初始化bundle文件
     * 检查ReactNativeConstant.JS_BUNDLE_FILE_PATH||getNativeRNVersion是否存在不存在的话从assets中拷贝
     */
    public static void initBundle() {

        File bundle = new File(ReactNativeConstant.JS_BUNDLE_FILE_PATH);
        // 获取当前本地信息
        RnInfoPojo rnInfoPojo = HotUpdateTools.getNativeRNVersion();
        // 如果bundle文件不存在或者本机版本信息是初始值
        if (!bundle.exists()) {
            //拷贝assets中的bundle到bundle的真实加载目录下
            if (FileUtils.copyAssets(ReactNativeConstant.JS_BUNDLE_FILE_PATH)) {
                // 彻底清空预加载数据
                PreLoadReactNative.clear();
                HotUpdateTools.setNativeRNVersion(getMd5ByFile(ReactNativeConstant.JS_BUNDLE_FILE_PATH));
            } else {
                //拷贝Assets文件失败
            }
        } else if (rnInfoPojo.getMd5().equals("0")) {
            HotUpdateTools.setNativeRNVersion(getMd5ByFile(ReactNativeConstant.JS_BUNDLE_FILE_PATH));
        }

    }

    /**
     * 获取版本号的方法本方法会从ReactNativeConstant.VERSION_URL接口请求数据
     * 并和HotUpdateTools.getNativeRNVersion()中的本机版本信息进行比对后通过入参
     * myHandler通知Activity主线程是否需要更新
     *
     * @param url       接口地址
     * @param myHandler 页面handle
     */
    public static void getVersion(final String url, final Handler myHandler) {
        HotUpdateTools.versionT = new Thread(new Runnable() {
            public void run() {
                //发登录消息通知
                Message ms1 = new Message();
                try {
                    String req = HttpUtil.doGet(url);
                    JSONObject version = new JSONObject(req);
                    boolean REV = version.getBoolean("REV");
                    VERSION_INFO = version.getJSONObject("msg");
                    //获取版本号成功
                    if (REV == true) {
                        //获取当前本地信息
                        RnInfoPojo rnInfoPojo = HotUpdateTools.getNativeRNVersion();
                        // 和线上版本md5比对
                        if (!rnInfoPojo.getMd5().equals(VERSION_INFO.getString("bundleMd5"))) {
                            ms1.what = ReactNativeConstant.HAN_VERSION_OK;
                            // 和线上最新版不相等
                            ms1.obj = "需更新，开始下载更新包...";
                            myHandler.sendMessage(ms1);
                        } else {
                            ms1.what = ReactNativeConstant.HAN_VERSION_GO;
                            //和线上最新版相等
                            ms1.obj = "无需更新,加载页面";
                            //保存最新版本信息
                            setNativeRNVersion(VERSION_INFO.getString("bundleMd5"));
                            myHandler.sendMessage(ms1);
                        }
                    } else {
                        ms1.what = ReactNativeConstant.HAN_ERROR;
                        ms1.obj = "获取版本信息失败";
                        myHandler.sendMessage(ms1);
                    }
                } catch (Exception e) {
                    // 获取版本信息出错
                    e.printStackTrace();
                }
            }
        });
        HotUpdateTools.versionT.start();
    }

    /**
     * 下载的工具类
     *
     * @param activity 调用者的activity实例
     * @return
     */
    public static long DownLoad(Activity activity) {
        long mDownLoadId = 0;
        try {
            String url = VERSION_INFO.getString("downloadAdd");
            String path = JS_ZIP_PATH;
            // 1.初始化文件目录（查看path存不存在，存在的话删除）
            FileUtils.initFile(path, true);
            DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
            // 2.下载地址
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            // 3.不在通知栏显示下载
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            // 4.在使用手机流量和wifi时都进行下载
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
            // 5.下载完后的保存路径
            request.setDestinationUri(Uri.parse("file://" + path));
            // 6.执行下载
            mDownLoadId = downloadManager.enqueue(request);
            System.out.println("开始下载" + mDownLoadId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mDownLoadId;
    }

    /**
     * 增量热更新的操作
     *
     * @param mhandler 通知主线程更新状况
     */
    public static void handleZIP(final Handler mhandler) {
        HotUpdateTools.handleT = new Thread(new Runnable() {
            @Override
            public void run() {
                Message ms1 = new Message();
                try {
                    //1.验证下载完整性
                    String JHzipMd5 = getMd5ByFile(JS_ZIP_PATH);
                    if (JHzipMd5.equals(VERSION_INFO.getString("addMd5"))) {
                        System.out.println("下载增量包完整");
                        //下载增量包完整
                        //解压增量包解压JS_ZIP_PATH到JS_OTHER_PATH目录下文件名是bundle.pat
                        FileUtils.unpack(JS_ZIP_PATH, JS_OTHER_PATH);
                        //合并文件
                        if (mergePatAndBundle()) {
                            //删除所有产生的中间文件（下载下来压缩包，解压后的文件夹，合并后的新文件）
                            FileUtils.deleteDir(JS_OTHER_PATH);
                            // 彻底清空预加载数据
                            PreLoadReactNative.clear();
                            //通知主线程下载解压合并校验完成，开启预加载
                            ms1.what = ReactNativeConstant.HAN_HOT_UPDATE_OK;
                            // 将新版信息写入到本地信息
                            setNativeRNVersion(VERSION_INFO.getString("bundleMd5"));
                        } else {
                            //通知主线程下载解压合并校验完成，开启预加载
                            ms1.what = ReactNativeConstant.HAN_HOT_UPDATE_NO;
                        }
                    } else {
                        System.out.println("下载增量包不完整");
                        //通知主线程下载解压合并校验完成，开启预加载
                        ms1.what = ReactNativeConstant.HAN_HOT_UPDATE_NO;
                    }
                    mhandler.sendMessage(ms1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        HotUpdateTools.handleT.start();
    }

    /**
     * JS_OTHER_PATH目录下文件名是bundle.pat与JS_BUNDLE_FILE_PATH进行合并
     */
    public static boolean mergePatAndBundle() {
        // 1.解析bunlde
        String bundleStr = FileUtils.getFileString(ReactNativeConstant.JS_BUNDLE_FILE_PATH);
        // 2.解析最新下载的.pat文件字符串
        String patcheStr = FileUtils.getFileString(ReactNativeConstant.JS_UNZIP_PATH);
        // 3.合并
        merge(patcheStr, bundleStr);
        // 4.验证合并后完整性
        String newBundleMd5 = getMd5ByFile(ReactNativeConstant.JS_NEWBUNDLE_PATH);
        try {
            if (newBundleMd5.equals(VERSION_INFO.getString("bundleMd5"))) {
                //合并成功，将其拷贝到正式bundle地址
                return FileUtils.copyFile(ReactNativeConstant.JS_NEWBUNDLE_PATH, ReactNativeConstant.JS_BUNDLE_FILE_PATH, true);
            } else {
                //合并后校验失败,进行全量下载
                System.out.println("合并后校验失败,进行全量下载");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 合并,生成新的bundle文件
     */
    private static void merge(String patcheStr, String bundle) {
        // 1.初始化 dmp
        diff_match_patch dmp = new diff_match_patch();
        // 2.转换pat
        LinkedList<diff_match_patch.Patch> pathes = (LinkedList<diff_match_patch.Patch>) dmp.patch_fromText(patcheStr);
        // 3.pat与bundle合并，生成新的bundle
        Object[] bundleArray = dmp.patch_apply(pathes, bundle);
        // 4.保存新的bundle文件
        try {
            Writer writer = new FileWriter(ReactNativeConstant.JS_NEWBUNDLE_PATH);
            String newBundle = (String) bundleArray[0];
            writer.write(newBundle);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
