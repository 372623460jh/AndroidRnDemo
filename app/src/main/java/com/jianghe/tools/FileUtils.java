package com.jianghe.tools;

import com.jianghe.hotupdate.ReactNativeConstant;
import com.mainandroid.mainview.MainApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * 文件工具类
 * Created by jianghe on 2017/7/12.
 */
public class FileUtils {

    /**
     * 解压文件
     *
     * @param zippath  压缩包位置
     * @param filePath 解压到哪（路径解压出来的文件名依据压缩包中的文件名）
     * @throws Exception
     */
    public static void unpack(String zippath, String filePath) {
        //读取需要解压缩的压缩包文件
        ZipInputStream inZip = null;
        FileOutputStream fos = null;
        try {
            inZip = new ZipInputStream(new FileInputStream(zippath));
            ZipEntry zipEntry;
            String szName;
            while ((zipEntry = inZip.getNextEntry()) != null) {
                szName = zipEntry.getName();
                // 如果是个文件夹
                if (zipEntry.isDirectory()) {
                    szName = szName.substring(0, szName.length() - 1);
                    File folder = new File(filePath + File.separator + szName);
                    folder.mkdirs();
                } else {
                    File file1 = new File(filePath + File.separator + szName);
                    file1.createNewFile();
                    fos = new FileOutputStream(file1);
                    int len;
                    byte[] buffer = new byte[1024];
                    while ((len = inZip.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                        fos.flush();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inZip != null) {
                    inZip.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 复制单个文件
     *
     * @param srcFileName  待复制的文件
     * @param destFileName 复制到哪
     * @param overlay      如果目标文件存在，是否覆盖
     * @return boolean     拷贝是否成功
     */
    public static boolean copyFile(String srcFileName, String destFileName, boolean overlay) {

        InputStream in = null;
        OutputStream out = null;
        try {
            // 判断原文件是否存在
            File srcFile = new File(srcFileName);
            if (!srcFile.exists()) {
                System.out.println("复制文件失败：原文件" + srcFileName + "不存在！");
                return false;
            } else if (!srcFile.isFile()) {
                System.out.println("复制文件失败：" + srcFileName + "不是一个文件！");
                return false;
            }
            // 判断目标文件是否存在
            File destFile = new File(destFileName);
            // 初始化文件路径
            if (!FileUtils.initFile(destFileName, overlay)) {
                return false;
            }
            // 准备复制文件
            int byteread = 0;// 读取的位数
            // 打开原文件
            in = new FileInputStream(srcFile);
            // 打开连接到目标文件的输出流
            out = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            // 一次读取1024个字节，当byteread为-1时表示文件已经读完
            while ((byteread = in.read(buffer)) != -1) {
                // 将读取的字节写入输出流
                out.write(buffer, 0, byteread);
            }
            System.out.println("复制单个文件" + srcFileName + "至" + destFileName + "成功！");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭输入输出流，注意先关闭输出流，再关闭输入流
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 删除指定File
     *
     * @param filePath
     */
    public static boolean deleteFile(String filePath) {
        File patFile = new File(filePath);
        if (patFile.exists()) {
            return patFile.delete();
        }
        return true;
    }

    /**
     * 获取文件的MD5工具类
     *
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    public static String getMd5ByFile(String file) {
        String value = "";
        FileInputStream in = null;
        try {
            File file1 = new File(file);
            if (file1.exists()) {
                in = new FileInputStream(file1);
                MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file1.length());
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(byteBuffer);
                BigInteger bi = new BigInteger(1, md5.digest());
                value = bi.toString(16);
            } else {
                throw new Exception("文件不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != in) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return value;
    }

    /**
     * 拷贝Assets文件下的bundle到相应目录下（如果存在就覆盖）
     *
     * @param fileName 拷贝到哪
     * @return boolean 是否拷贝成功
     */
    public static boolean copyAssets(String fileName) {

        InputStream in = null;
        OutputStream out = null;
        try {
            //获取assets下的bundle文件的输入流
            in = MainApplication.appContext.getAssets().open(ReactNativeConstant.JS_BUNDLE_NAME);
            // 判断目标文件是否存在
            File destFile = new File(fileName);
            // 初始化文件路径
            if (!FileUtils.initFile(fileName, true)) {
                return false;
            }
            // 准备复制文件
            int byteread = 0;// 读取的位数
            // 打开连接到目标文件的输出流
            out = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            // 一次读取1024个字节，当byteread为-1时表示文件已经读完
            while ((byteread = in.read(buffer)) != -1) {
                // 将读取的字节写入输出流
                out.write(buffer, 0, byteread);
            }
            System.out.println("复制Assets文件下的bundle到" + fileName + "成功");
            // 关闭输入输出流，注意先关闭输出流，再关闭输入流
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 解析文件为String
     *
     * @param filePath
     * @return
     */
    public static String getFileString(String filePath) {
        String result = "";
        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            result = new String(buffer, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return result;
    }

    /**
     * 初始化文件路径如果文件存在就根据overlay是否删除，如果文件不存在就创建文件所在目录
     *
     * @param path    初始化扥文件路径
     * @param overlay 当文件存在时是否删除
     * @return boolean 初始化目录是否成功
     */
    public static boolean initFile(String path, boolean overlay) {
        try {
            // 判断目标文件是否存在
            File destFile = new File(path);
            if (destFile.exists()) {
                // 如果目标文件存在，而且复制时允许覆盖。
                if (overlay) {
                    // 删除已存在的目标文件，无论目标文件是目录还是单个文件
                    System.out.println("目标文件已存在，删除它！");
                    if (!FileUtils.deleteFile(path)) {
                        System.out.println("删除目标文件" + path + "失败！");
                        return false;
                    }
                } else {
                    System.out.println("目标文件" + path + "已存在！");
                    return false;
                }
            } else {
                if (!destFile.getParentFile().exists()) {
                    // 如果目标文件所在的目录不存在，则创建目录
                    if (!destFile.getParentFile().mkdirs()) {
                        System.out.println("创建目标文件所在的目录失败！");
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 删除文件夹和文件夹里面的文件
     *
     * @param pPath 要删除的目录
     */
    public static void deleteDir(final String pPath) {
        File dir = new File(pPath);
        deleteDirWihtFile(dir);
    }

    public static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWihtFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }

//
//    /**
//     * 将图片复制到bundle所在文件夹下的drawable-mdpi
//     *
//     * @param srcFilePath
//     * @param destFilePath
//     */
//    public static void copyPatchImgs(String srcFilePath, String destFilePath) {
//
//        File root = new File(srcFilePath);
//        File[] files;
//        if (root.exists() && root.listFiles() != null) {
//            files = root.listFiles();
//            for (File file : files) {
//                File oldFile = new File(srcFilePath + file.getName());
//                File newFile = new File(destFilePath + file.getName());
//                DataInputStream dis = null;
//                DataOutputStream dos = null;
//                try {
//                    dos = new DataOutputStream(new FileOutputStream(newFile));
//                    dis = new DataInputStream(new FileInputStream(oldFile));
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//
//                int temp;
//                try {
//                    while ((temp = dis.read()) != -1) {
//                        dos.write(temp);
//                    }
//                    dis.close();
//                    dos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    /**
//     * 遍历删除文件夹下所有文件
//     *
//     * @param filePath
//     */
//    public static void traversalFile(String filePath) {
//        File file = new File(filePath);
//        if (file.exists()) {
//            File[] files = file.listFiles();
//            for (File f : files) {
//                if (f.isDirectory()) {
//                    traversalFile(f.getAbsolutePath());
//                } else {
//                    f.delete();
//                }
//            }
//            file.delete();
//        }
//    }


}
