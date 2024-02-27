package com.ej.hgj.enums;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 * 快捷面板模块-反馈模块
 * @author tty
 * @version 1.0 2021-01-04 18:13
 */
public enum QpadEnum {
    Q01("建议",1,10,true,"/images/mine/jianyi.png","/subpages/advice/add/addAdvice?isLogin=Y&adviceType=A","adviceJob"),
    Q02("投诉",2,10,true,"/images/mine/tousu.png","/subpages/advice/add/addAdvice?isLogin=Y&adviceType=C","adviceJob"),
    Q03("表扬",3,10,true,"/images/mine/biaoyang.png","/subpages/advice/add/addAdvice?isLogin=Y&adviceType=P","adviceJob"),
        ;

    /**
     * 功能描述
     */
    private String funDesc;
    /**
     * 功能权限位图
     */
    private int bit;
    /**
     * 所属上一级父的位图
     */
    private int parentBit;
    /**
     * 是否有权限
     */
    private Boolean hasAuth;
    /**
     * 图片logo
     */
    private String logoPath;
    /**
     * 功能跳转路径
     */
    private String pagePath;
    /**
     * 绑定的事件
     */
    private String bindEvent;

    QpadEnum(String funDesc, int bit, int parentBit, Boolean hasAuth, String logoPath, String pagePath, String bindEvent) {
        this.funDesc = funDesc;
        this.bit = bit;
        this.parentBit = parentBit;
        this.hasAuth = hasAuth;
        this.logoPath = logoPath;
        this.pagePath = pagePath;
        this.bindEvent = bindEvent;
    }

    public int getParentBit() {
        return parentBit;
    }

    public void setParentBit(int parentBit) {
        this.parentBit = parentBit;
    }

    public String getFunDesc() {
        return funDesc;
    }

    public void setFunDesc(String funDesc) {
        this.funDesc = funDesc;
    }

    public int getBit() {
        return bit;
    }

    public void setBit(int bit) {
        this.bit = bit;
    }

    public Boolean getHasAuth() {
        return hasAuth;
    }

    public void setHasAuth(Boolean hasAuth) {
        this.hasAuth = hasAuth;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public String getPagePath() {
        return pagePath;
    }

    public void setPagePath(String pagePath) {
        this.pagePath = pagePath;
    }

    public String getBindEvent() {
        return bindEvent;
    }

    public void setBindEvent(String bindEvent) {
        this.bindEvent = bindEvent;
    }

    public static QpadEnum getQpadEnumByBit(int bit){
        String bits = StringUtils.leftPad(String.valueOf(bit),2,'0');
        return QpadEnum.valueOf("Q"+bits);
    }

    /**
     * 获取
     * @param bitMap
     * @return
     */
    public static JSONArray getAuthBitMapList(String bitMap){
        if(StringUtils.isBlank(bitMap)){
            return null;
        }
        JSONArray objects = new JSONArray();
        int startIndex = 0;
        for (char c : bitMap.toCharArray()) {
            startIndex ++;
            if(startIndex > QpadEnum.values().length){
                break;
            }
            Boolean hasAuth = c == '1';
            QpadEnum qpadEnumByBit = getQpadEnumByBit(startIndex);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("funDesc",qpadEnumByBit.getFunDesc());
            jsonObject.put("bit",qpadEnumByBit.getBit());
            jsonObject.put("parentBit",qpadEnumByBit.getParentBit());
            jsonObject.put("hasAuth",hasAuth);
            jsonObject.put("logoPath", qpadEnumByBit.getLogoPath());
            jsonObject.put("pagePath", qpadEnumByBit.getPagePath());
            jsonObject.put("bindEvent", qpadEnumByBit.getBindEvent());
            objects.add(jsonObject);
        }
        return objects;
    }

//    public static void main(String[] args) {
//        System.out.println(getAuthBitMapList("10", 0, 2));
//    }
}
