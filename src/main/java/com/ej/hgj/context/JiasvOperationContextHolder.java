package com.ej.hgj.context;

import com.ej.hgj.entity.login.JiasvOperationContext;

public class JiasvOperationContextHolder {
    public static final String jiasvOperationContextSessionKey = "jiasvOperationContextSessionKey";
    private static final ThreadLocal threadLocal = new ThreadLocal();

    public JiasvOperationContextHolder() {
    }

    public static JiasvOperationContext get() {
        return (JiasvOperationContext)threadLocal.get();
    }

    public static void setJiasvOperationContext(JiasvOperationContext context) {
        threadLocal.set(context);
    }
}
