package com.ej.hgj.utils;

import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {
    public static SimpleDateFormat sdfYmd = new SimpleDateFormat("yyyyMMdd");
    public static SimpleDateFormat sdfHms = new SimpleDateFormat("HH:mm:ss");
    public static SimpleDateFormat sdf_Ymd = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat sdf_Ym = new SimpleDateFormat("yyyy-MM");
    public static SimpleDateFormat sdf_Y = new SimpleDateFormat("yyyy");
    public static SimpleDateFormat sdfYmdHms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static DateTimeFormatter formatter_ymd_hms = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static DateTimeFormatter formatter_ymd = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 默认日期格式
     */
    public static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 获取系统时间 年月日时分秒
    public static String strYmdHms() {
        Date date = new Date();
        String ymdHms = DateUtils.sdfYmdHms.format(date);
        return ymdHms;
    }

    public static String strYmd() {
        Date date = new Date();
        String ymd = DateUtils.sdfYmd.format(date);
        return ymd;
    }

    public static String strYmd(Date date) {
        String ymd = DateUtils.sdf_Ymd.format(date);
        return ymd;
    }

    public static String strYm(Date date) {
        String ym = DateUtils.sdf_Ym.format(date);
        return ym;
    }

    public static String strY(Date date) {
        String y = DateUtils.sdf_Y.format(date);
        return y;
    }

    public static String strYmdHms(Date date) {
        String ymd = DateUtils.sdfYmdHms.format(date);
        return ymd;
    }

    public static Date strDate(String strDate) {
        try {
            Date date = formatter.parse(strDate);
            return date;
        } catch(
                ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取停车时长
     * @param inTime
     * @param outTime
     * @return
     */
    public static long[] getDistanceTimes(Date inTime, Date outTime) {
        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        long diff;
        diff = outTime.getTime() - inTime.getTime();
        day = diff / (24 * 60 * 60 * 1000);
        hour = (diff / (60 * 60 * 1000) - day * 24);
        min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
        sec = (diff/1000-day*24*60*60-hour*60*60-min*60);
        long[] times = {day, hour, min, sec};
        return times;
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
