package com.jianghe.hotupdate;

/**
 * Created by jianghe on 2017/7/17.
 */

/**
 * 回调方法中的消息对象
 */
public class CallBackMess {

    /**
     * 消息的标识
     */
    private int messTag;

    /**
     * 字符串消息
     */
    private String strMess;

    /**
     * 整型消息
     */
    private int intMess;

    /**
     * 对象消息
     */
    private Object objMess;

    public void setMessTag(int messTag) {
        this.messTag = messTag;
    }

    public void setStrMess(String strMess) {
        this.strMess = strMess;
    }

    public void setIntMess(int intMess) {
        this.intMess = intMess;
    }

    public void setObjMess(Object objMess) {
        this.objMess = objMess;
    }

    public Object getObjMess() {
        return objMess;
    }

    public int getMessTag() {
        return messTag;
    }

    public String getStrMess() {
        return strMess;
    }

    public int getIntMess() {
        return intMess;
    }
}
