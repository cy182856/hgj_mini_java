package com.ej.hgj.controller.base;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;

/**
 * @author tty
 * @version 1.0 2020-08-17 10:46
 */
@Controller
public  class BaseController{
    /**
     * 只要有一个参数为空,则返回true
     * @param str
     * @return
     */
    protected boolean isBank(String... str){
        for (String s : str) {
            if (StringUtils.isBlank(s)) {
                return Boolean.TRUE;
            }

        }
        return Boolean.FALSE;
    }
    protected boolean isBlank(String str) {
        return StringUtils.isBlank(str);
    }
    protected boolean isNotBank(String str){
        return StringUtils.isNotBlank(str);
    }

}
