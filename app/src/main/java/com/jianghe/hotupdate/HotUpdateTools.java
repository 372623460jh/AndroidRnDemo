package com.jianghe.hotupdate;

import com.jianghe.tools.DownLoadUtils;
import com.jianghe.tools.FileUtils;
import com.jianghe.tools.HttpUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.LinkedList;

/**
 * 热更新工具类
 * Created by jianghe on 2017/7/6.
 */
public class HotUpdateTools {

    /**
     * 热更新线程
     */
    public Thread T = null;

    /**
     * 热更新消息通知类
     */
    private CallBack callBack = null;

    /**
     * 构造方法
     *
     * @param callBack 回调方法类RN和android原生有区别具体看CallBack
     */
    public HotUpdateTools(CallBack callBack) {
        this.callBack = callBack;
    }

    /**
     * 暴露的热更新方法
     */
    public void hotUpdate() {
        this.T = new Thread(new Runnable() {
            public void run() {
                //初始化bundle文件
                initBundle();
                getVersion();
            }
        });
        this.T.start();
    }

    /**
     * 初始化bundle文件
     * 检查ReactNativeConstant.JS_BUNDLE_FILE_PATH||getNativeRNVersion是否存在不存在的话从assets中拷贝
     */
    private void initBundle() {
        File bundle = new File(ReactNativeConstant.JS_BUNDLE_FILE_PATH);
        // 如果bundle文件不存在或者本机版本信息是初始值
        if (!bundle.exists()) {
            //拷贝assets中的bundle到bundle的真实加载目录下
            if (FileUtils.copyAssets(ReactNativeConstant.JS_BUNDLE_FILE_PATH)) {
                //拷贝Assets文件成功
                CallBackMess ms1 = new CallBackMess();
                ms1.setMessTag(ReactNativeConstant.COPY_BUNDLE_SUCC);
                callBack.handleMessage(ms1);
            } else {
                //拷贝Assets文件失败
                CallBackMess ms1 = new CallBackMess();
                ms1.setMessTag(ReactNativeConstant.HAN_ERROR);
                ms1.setStrMess("拷贝Assets文件失败");
                callBack.handleMessage(ms1);
            }
        }
    }

    /**
     * 获取版本号的方法。本方法会从ReactNativeConstant.VERSION_URL接口请求数据
     * 并和HotUpdateTools.getNativeRNVersion()中的本机版本信息进行比对后通过入参
     * myHandler通知Activity主线程是否需要更新
     */
    private void getVersion() {
        //发消息通知
        CallBackMess ms1 = new CallBackMess();
        try {
            String req = HttpUtils.doGet(ReactNativeConstant.VERSION_URL);
            JSONObject version = new JSONObject(req);
            boolean REV = version.getBoolean("REV");
            ReactNativeConstant.VERSION_INFO = version.getJSONObject("msg");
            //获取版本号成功
            if (REV == true) {
                // 和线上版本md5比对
                if (!FileUtils.getMd5ByFile(ReactNativeConstant.JS_BUNDLE_FILE_PATH).equals(ReactNativeConstant.VERSION_INFO.getString("bundleMd5"))) {
                    // 和线上最新版不相等
                    ms1.setMessTag(ReactNativeConstant.HAN_VERSION_OK);
                    ms1.setStrMess("需更新，开始下载更新包");
                    callBack.handleMessage(ms1);
                    this.DownLoad();
                } else {
                    // 和线上最新版相等
                    ms1.setMessTag(ReactNativeConstant.HAN_VERSION_GO);
                    ms1.setStrMess("无需更新,加载页面");
                    callBack.handleMessage(ms1);
                }
            } else {
                ms1.setMessTag(ReactNativeConstant.HAN_ERROR);
                // 获取版本信息失败
                ms1.setStrMess("获取版本信息失败");
                callBack.handleMessage(ms1);
            }
        } catch (Exception e) {
            // 获取版本信息出错
            ms1.setMessTag(ReactNativeConstant.HAN_ERROR);
            ms1.setStrMess("获取版本信息出错");
            callBack.handleMessage(ms1);
            e.printStackTrace();
        }
    }

    /**
     * 下载的工具类
     *
     * @return
     */
    private void DownLoad() {
        //发消息通知
        CallBackMess ms1 = new CallBackMess();
        try {
            String url = ReactNativeConstant.VERSION_INFO.getString("downloadAdd");
            String path = ReactNativeConstant.JS_ZIP_PATH;
            // 下载工具类
            DownLoadUtils dlu = new DownLoadUtils(url, path);
            // 获取下载包总长度
            int length = dlu.getLength();
            /**
             * 调用同步下载的方法返回是否下载成功
             * callBack回调中返回下载进度
             */
            if (dlu.down2sd(callBack, length)) {
                // 下载成功
                ms1.setMessTag(ReactNativeConstant.DOWNLOAD_OK);
                ms1.setStrMess("下载成功");
                callBack.handleMessage(ms1);
                //校验解压合并压缩包
                this.handleZIP();
            } else {
                // 下载失败
                ms1.setMessTag(ReactNativeConstant.HAN_ERROR);
                ms1.setStrMess("下载失败");
                callBack.handleMessage(ms1);
            }
        } catch (Exception e) {
            // 下载失败
            ms1.setMessTag(ReactNativeConstant.HAN_ERROR);
            ms1.setStrMess("下载出错");
            callBack.handleMessage(ms1);
            e.printStackTrace();
        }
    }

    /**
     * 增量热更新的操作
     */
    private void handleZIP() {
        CallBackMess ms1 = new CallBackMess();
        try {
            //1.验证下载完整性
            String JHzipMd5 = FileUtils.getMd5ByFile(ReactNativeConstant.JS_ZIP_PATH);
            if (JHzipMd5.equals(ReactNativeConstant.VERSION_INFO.getString("addMd5"))) {
                //下载增量包完整
                //解压增量包解压JS_ZIP_PATH到JS_OTHER_PATH目录下文件名是bundle.pat
                FileUtils.unpack(ReactNativeConstant.JS_ZIP_PATH, ReactNativeConstant.JS_OTHER_PATH);
                //合并文件
                if (this.mergePatAndBundle()) {
                    //删除所有产生的中间文件（下载下来压缩包，解压后的文件夹，合并后的新文件）
                    FileUtils.deleteDir(ReactNativeConstant.JS_OTHER_PATH);
                    //通知主线程下载解压合并校验完成，开启预加载
                    ms1.setMessTag(ReactNativeConstant.HAN_HOT_UPDATE_OK);
                } else {
                    ms1.setStrMess("合并出错");
                    //通知主线程下载解压合并校验完成，开启预加载
                    ms1.setMessTag(ReactNativeConstant.HAN_HOT_UPDATE_NO);
                }
            } else {
                ms1.setStrMess("下载更新包不完整");
                //通知主线程下载解压合并校验完成，开启预加载
                ms1.setMessTag(ReactNativeConstant.HAN_HOT_UPDATE_NO);
            }
            callBack.handleMessage(ms1);
        } catch (Exception e) {
            ms1.setMessTag(ReactNativeConstant.HAN_ERROR);
            ms1.setStrMess("处理更新包出错");
            callBack.handleMessage(ms1);
            e.printStackTrace();
        }
    }

    /**
     * JS_OTHER_PATH目录下文件名是bundle.pat与JS_BUNDLE_FILE_PATH进行合并
     */
    private boolean mergePatAndBundle() {
        try {
            // 1.解析bunlde
            String bundleStr = FileUtils.getFileString(ReactNativeConstant.JS_BUNDLE_FILE_PATH);
            // 2.解析最新下载的.pat文件字符串
            String patcheStr = FileUtils.getFileString(ReactNativeConstant.JS_UNZIP_PATH);
            // 3.合并
            this.merge(patcheStr, bundleStr);
            // 4.验证合并后完整性
            String newBundleMd5 = FileUtils.getMd5ByFile(ReactNativeConstant.JS_NEWBUNDLE_PATH);
            if (newBundleMd5.equals(ReactNativeConstant.VERSION_INFO.getString("bundleMd5"))) {
                //合并成功，将其拷贝到正式bundle地址
                return FileUtils.copyFile(ReactNativeConstant.JS_NEWBUNDLE_PATH, ReactNativeConstant.JS_BUNDLE_FILE_PATH, true);
            } else {
                //合并后校验失败,进行全量下载
                return false;
            }
        } catch (Exception e) {
            // 合并出错
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 合并,生成新的bundle文件
     */
    private void merge(String patcheStr, String bundle) throws Exception {
        // 1.初始化dmp
        diff_match_patch dmp = new diff_match_patch();
        // 2.转换pat
        LinkedList<diff_match_patch.Patch> pathes = (LinkedList<diff_match_patch.Patch>) dmp.patch_fromText(patcheStr);
        // 3.pat与bundle合并，生成新的bundle
        Object[] bundleArray = dmp.patch_apply(pathes, bundle);
        // 4.保存新的bundle文件
        Writer writer = new FileWriter(ReactNativeConstant.JS_NEWBUNDLE_PATH);
        String newBundle = (String) bundleArray[0];
        writer.write(newBundle);
        writer.close();
    }
}
