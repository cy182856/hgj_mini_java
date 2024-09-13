package com.ej.hgj.utils;

import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    public static SimpleDateFormat sdfYmd = new SimpleDateFormat("yyyyMMdd");
    public static SimpleDateFormat sdfHms = new SimpleDateFormat("HH:mm:ss");
    public static SimpleDateFormat sdf_Ymd = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat sdfYmdHms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 默认日期格式
     */
    private static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // 获取系统时间 年月日时分秒
    public static String strYmdHms(){
        Date date = new Date();
        String ymdHms = DateUtils.sdfYmdHms.format(date);
        return ymdHms;
    }

    public static String strYmd(){
        Date date = new Date();
        String ymd = DateUtils.sdfYmd.format(date);
        return ymd;
    }

    public static String strYmd(Date date){
        String ymd = DateUtils.sdf_Ymd.format(date);
        return ymd;
    }

    public static String strYmdHms(Date date){
        String ymd = DateUtils.sdfYmdHms.format(date);
        return ymd;
    }

    /**判断是否超过多少小时 如：2
     *
     * @param createTime 创建时间
     * @param minute 多少分钟
     * @return boolean
     * @throws Exception
     */
    public static boolean compareDate(Date createTime, Integer minute) throws Exception {
        // 系统时间
        Date sysTime = sdfYmdHms.parse(DateUtils.strYmdHms());
        long cha = sysTime.getTime() - createTime.getTime();
        if (cha < 0) {
            return false;
        }
        double result = cha * 1.0 / (1000 * 60);
        if (result < minute) {
            //是小于等于 minute 分钟
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        Date updateDate =  sdfYmdHms.parse("2024-01-26 08:57:59");
        boolean b = DateUtils.compareDate(updateDate, 10);
        System.out.println(b);
    }
}
