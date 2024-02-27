/**
 * 
 * 上海云之富金融信息服务有限公司
 * Copyright (c) 2014 YunCF,Inc.All Rights Reserved.
 */
package com.ej.hgj.utils;


import com.ej.hgj.constant.Constant;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 
 * @author DandelionL
 * @version $Id: CookieUtil.java, v 0.1 2014-12-2 上午10:21:43 DandelionL Exp $
 */
public class CookieUtil {
    /**
    *
    * @desc 删除指定Cookie
    * @param response
    * @param cookie
    */
   public static void removeCookie(HttpServletResponse response, Cookie cookie)
   {
           if (cookie != null)
           {
                   cookie.setPath("/");
                   cookie.setValue("");
                   cookie.setMaxAge(0);
                   response.addCookie(cookie);
           }
   }

   /**
    *
    * @desc 删除指定Cookie
    * @param response
    * @param cookie
    * @param domain
    */
   public static void removeCookie(HttpServletResponse response, Cookie cookie,String domain)
   {
           if (cookie != null)
           {
                   cookie.setPath("/");
                   cookie.setValue("");
                   cookie.setMaxAge(0);
                   cookie.setDomain(domain);
                   response.addCookie(cookie);
           }
   }

   /**
    *
    * @desc 根据Cookie名称得到Cookie的值，没有返回Null
    * @param request
    * @param name
    * @return
    */
   public static String getCookieValue(HttpServletRequest request, String name)
   {
           Cookie cookie = getCookie(request, name);
           if (cookie != null)
           {
                   return cookie.getValue();
           }
           else
           {
                   return null;
           }
   }

   /**
    *
    * @desc 根据Cookie名称得到Cookie对象，不存在该对象则返回Null
    * @param request
    * @param name
    */
   public static Cookie getCookie(HttpServletRequest request, String name)
   {
           Cookie cookies[] = request.getCookies();
           if (cookies == null || name == null || name.length() == 0)
                   return null;
           Cookie cookie = null;
           for (int i = 0; i < cookies.length; i++)
           {
                   if (!cookies[i].getName().equals(name))
                           continue;
                   cookie = cookies[i];
                   if (request.getServerName().equals(cookie.getDomain()))
                           break;
           }

           return cookie;
   }

   /**
    *
    * @desc 添加一条新的Cookie信息，默认有效时间为一个月
    * @param response
    * @param name
    * @param value
    */
   public static void setCookie(HttpServletResponse response, String name, String value)
   {
           setCookie(response, name, value, Constant.COOKIE_MAX_AGE);
   }

   /**
    *
    * @desc 添加一条新的Cookie信息，可以设置其最长有效时间(单位：秒)
    * @param response
    * @param name
    * @param value
    * @param maxAge
    */
   public static void setCookie(HttpServletResponse response, String name, String value, int maxAge)
   {
           if (value == null)
                   value = "";
           Cookie cookie = new Cookie(name, value);
           if(maxAge!=0){
               cookie.setMaxAge(maxAge);
           }else{
               cookie.setMaxAge(Constant.COOKIE_MAX_AGE);
           }
           cookie.setPath("/");
           response.addCookie(cookie);
   }

}
