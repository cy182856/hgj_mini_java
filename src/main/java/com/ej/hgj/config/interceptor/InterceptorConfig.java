package com.ej.hgj.config.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class InterceptorConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")    //拦截所有请求 通过判断token是否合法来决定是否登陆
                .excludePathPatterns("/**/doLogin/**",
                        "/**/queryMutipUsr/**",
                        "/**/queryMutipUsr.do/**",
                        "/**/queryImgUrl/**",
                        "/**/wechatPub/**",
                        "/**/callBack/**",
                        "/**/carPay/callBack/**",
                        "/**/carRenew/callBack/**",
                        "/**/monCarRen/callBack/**",
                        "/**/monCarRen/invoice/callBack/**",
                        "/**/carPay/invoice/callBack/**",
                        "/**/hu/**",
                        "/**/pass/code/**"
                ); //放行接口
        super.addInterceptors(registry);
    }

}

