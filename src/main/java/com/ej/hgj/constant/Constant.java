package com.ej.hgj.constant;

public class Constant<https> {

    // 删除标识 0-未删除
    public static Integer DELETE_FLAG_NOT = 0;
    // 删除标识 1-已删除
    public static Integer DELETE_FLAG_YES = 1;

    //-----------------------------------------------------------------

    // web平台
    public static Integer PLAT_FORM_WEB = 1;
    // 企微平台
    public static Integer PLAT_FORM_WE_COM = 2;

    //------------------------------------------------------------------

    // 返回值code-成功
    public static String SUCCESS = "000";
    public static Integer SUCCESS_RESULT_CODE = 20000;
    // 返回值message-成功
    public static String SUCCESS_RESULT_MESSAGE = "成功";
    // 返回值code-失败
    public static Integer FAIL_RESULT_CODE = 99999;
    // 返回值message-失败
    //public static String FAIL_RESULT_MESSAGE = "失败";

    // 文件访问地址
    //public static String FILE_URL =  getConfigStringValue("jiasv.file.url.key");
    //public static String FILE_URL =  "https://hgj.shofw.com/upload/";

    public static String SESSION_KEY = "key123456";
    public static String FAIL_CODE = "999";
    /**session失效时间*/
    public static final int COOKIE_MAX_AGE = 1800;
    // 微信小程序智慧管家
    //public static final String MINI_PROGRAM_APP = "mini_program_app";
    // 微信小程序智慧社区
    public static final String MINI_PROGRAM_APP_EJ_ZHSQ = "mini_program_app_ej_zhsq";
    // 微信租户平台公众号
    public static final String WECHAT_PUB_APP = "wechat_pub_app";
    // 服务商户号
    public static final String SP_MCH_ID = "sp_mch_id";
    // 子服务商户号
    public static final String SUB_MCH_ID = "sub_mch_id";
    // 直连商户号
    public static final String MCH_ID = "mch_id";
    // 东方渔人码头服务商证书序列号
    public static final String SERIAL_NO = "serial_no";
    // 小程序秘钥
    public static final String MINI_PROGRAM_APP_SECRET = "mini_program_app_secret";
    // 获取商户平台证书的URL
    // public static String MER_PLAT_CERT_URL = "https://api.mch.weixin.qq.com/v3/certificates";
    // public static String MER_PLAT_CERT_URL = "https://api.mch.weixin.qq.com/v3/pay/partner/transactions/jsapi";
    // JSAPI下单-服务商
    public static String PREPAY_URL =  "https://api.mch.weixin.qq.com/v3/pay/partner/transactions/jsapi";
    // JSAPI下单-直连商户
    public static String PREPAY_URL_BUS =  "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";
    // 支付回调
    //public static String CALLBACK_URL =  "https://hgj.shofw.com/jiasv/callBack";
    // 服务商回调地址
    public static String CALLBACK_URL =  "https://zhgj.shofw.com/wx/callBack";
    // 直连商户回调地址
    public static String CALLBACK_URL_BUS =  "https://zhgj.shofw.com/wx/callBackBus";

    // 商户订单号查询订单-服务商
    public static String QUERY_ORDER_URL =  "https://api.mch.weixin.qq.com/v3/pay/partner/transactions/out-trade-no";
    // 商户订单号查询订单-直连商户
    public static String QUERY_ORDER_URL_BUS =  "https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no";

    // 思源订单同步 0-未同步默认状态
    public static final int SY_ORDER_SYNC_NOT = 0;
    // 思源订单同步 1-同步成功
    public static final int SY_ORDER_SYNC_SUCCESS = 1;
    // 思源订单同步 2-同步失败
    public static final int SY_ORDER_SYNC_FAIL = 2;

    //--------------------------------------------------------------

    // 支付状态 0-预下单成功
    public static final int PAYMENT_STATUS_PRE = 0;
    // 支付状态 1-支付中
    public static final int PAYMENT_STATUS_PRO = 1;
    // 支付状态 2-支付成功
    public static final int PAYMENT_STATUS_SUCCESS = 2;
    // 支付状态 3-支付失败
    public static final int PAYMENT_STATUS_FAIL = 3;
    // 支付状态 4-已取消
    public static final int PAYMENT_STATUS_CANCEL = 4;
    // 支付状态 5-超时未支付
    public static final int PAYMENT_STATUS_TIMEOUT = 5;


    //----------------------------------------------------------------------

    // 大订单 账单状态 0-待支付
    public static final int BILL_STATUS_WAIT = 0;
    // 大订单 账单状态 1-支付中
    public static final int BILL_STATUS_PRO = 1;
    // 大订单 账单状态 2-支付成功
    public static final int BILL_STATUS_SUCCESS = 2;
    // 大订单 账单状态 3-支付失败
    public static final int BILL_STATUS_FAIL= 3;
    // 大订单 账单状态 4-已取消
    public static final int BILL_STATUS_CANCEL = 4;
    // 大订单 账单状态 5-超时
    public static final int BILL_STATUS_TIMEOUT = 5;

    // 公共报修单号
    public static String P_REPAIR_NUM = "p_repair_num";
    // 客户报修单号
    public static String S_REPAIR_NUM = "s_repair_num";

    // 入住角色 0-房主
    public static Integer INTO_ROLE_OWNER = 0;
    // 入住角色 1-租户
    public static Integer INTO_ROLE_TENANT = 1;
    // 入住状态 0-未入住
    public static Integer INTO_STATUS_N = 0;
    // 入住角色 1-已入住
    public static Integer INTO_STATUS_Y = 1;

}
