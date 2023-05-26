package com.example.smp.config.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    Logger logger = LoggerFactory.getLogger(getClass());

    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object object) {
        logger.info("拦截器开始执行");
        return true;
    }
}
