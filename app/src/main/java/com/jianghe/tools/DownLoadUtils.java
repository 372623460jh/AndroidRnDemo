package com.jianghe.tools;

import com.jianghe.hotupdate.CallBack;
import com.jianghe.hotupdate.CallBackMess;
import com.jianghe.hotupdate.ReactNativeConstant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 下载工具类
 * Created by jianghe on 2017/7/17.
 */
public class DownLoadUtils {
    /**
     * 连接url
     */
    private String urlstr;
    /**
     * sd卡目录路径
     */
    private String sdcard;
    /**
     * http连接管理类
     */
    private HttpURLConnection urlcon;

    public DownLoadUtils(String url, String path) {
        //下载地址
        this.urlstr = url;
        //保存地址
        this.sdcard = path;
        //获取连接
        urlcon = getConnection();
    }

    /**
     * 获取http连接处理类HttpURLConnection
     */
    private HttpURLConnection getConnection() {
        URL url;
        HttpURLConnection urlcon = null;
        try {
            url = new URL(urlstr);
            urlcon = (HttpURLConnection) url.openConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return urlcon;
    }

    /**
     * 读取网络文本
     */
    public String downloadAsString() {
        StringBuilder sb = new StringBuilder();
        String temp = null;
        try {
            InputStream is = urlcon.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while ((temp = br.readLine()) != null) {
                sb.append(temp);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 获取连接文件长度。
     */
    public int getLength() {
        return urlcon.getContentLength();
    }

    /**
     * 写文件到sd卡 demo
     * 前提需要设置模拟器sd卡容量，否则会引发EACCES异常
     * 先创建文件夹，在创建文件
     */
    public boolean down2sd(CallBack cb, int length) {
        //初始化项目路径
        FileUtils.initFile(this.sdcard, true);
        File file = new File(this.sdcard);
        FileOutputStream fos = null;
        try {
            InputStream is = urlcon.getInputStream();
            //创建文件
            file.createNewFile();
            fos = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len = 0 ;
            while ((len = is.read(buf)) > 0) {
                fos.write(buf, 0, len);
                //执行回调
                CallBackMess cbm = new CallBackMess();
                cbm.setMessTag(ReactNativeConstant.DOWN_LOAD_PROGRESS);
                cbm.setIntMess(len);
                cbm.setStrMess(length + "");
                cb.handleMessage(cbm);
            }
            is.close();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

}
