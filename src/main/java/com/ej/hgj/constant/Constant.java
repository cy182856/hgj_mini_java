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
    // 微信小程序智慧社区-智慧凡享-凡享小程序
    public static final String MINI_PROGRAM_APP_EJ_ZHSQ = "mini_program_app_ej_zhsq";
    // 微信小程序ej物业管家-智慧凡享-宜悦小程序
    public static final String MINI_PROGRAM_APP_EJ_WYGJ = "mini_program_app_ej_wygj";
    // 微信租户平台公众号
    public static final String WECHAT_PUB_APP = "wechat_pub_app";
    // 服务商户号-凡享服务商
    public static final String SP_MCH_ID = "sp_mch_id";
    // 子服务商户号-东方渔人码头
    public static final String SUB_MCH_ID = "sub_mch_id";
    // 服务商户号-宜悦网络服务商
    public static final String SP_MCH_ID_YY = "sp_mch_id_yy";
    // 子服务商户号-凡享
    public static final String SUB_MCH_ID_FX = "sub_mch_id_fx";
    // 直连商户号
    public static final String MCH_ID = "mch_id";
    // 服务商证书序列号
    public static final String SERIAL_NO = "serial_no";
    // 服务商宜悦证书序列号
    public static final String SERIAL_NO_YY = "serial_no_yy";
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
    //public static Integer INTO_ROLE_OWNER = 0;
    // 入住角色 1-租户
    //public static Integer INTO_ROLE_TENANT = 1;
    // 入住角色 1-客户
    public static Integer INTO_ROLE_CST = 0;
    // 入住角色 2-委托人
    public static Integer INTO_ROLE_ENTRUST = 1;
    // 入住角色 3-产权人
    public static Integer INTO_ROLE_PROPERTY_OWNER = 2;
    // 入住角色 4-住户
    public static Integer INTO_ROLE_HOUSEHOLD = 3;
    // 入住状态 0-未入住
    public static Integer INTO_STATUS_N = 0;
    // 入住角色 1-已入住
    public static Integer INTO_STATUS_Y = 1;
    // 入住状态 2-已解绑
    public static Integer INTO_STATUS_U = 2;
    // 入住状态 3-待审核
    public static Integer INTO_STATUS_A = 3;

    // 访客二维码说明文字
    public static String VISITOR_CODE = "visitor_code";

    // 快速通行码说明文字
    public static String QUICK_ACCESS_CODE = "quick_access_code";

    // token
    public static final String TOKEN = "hgj20230719";
    // 微信公众号orgId
    public static String WE_CHAT_PUB_ORG_ID = "wechat_pub_org_id";
    public static final String RESP_MESSAGE_TYPE_TEXT = "text";
    /**上海朴由科技有限公司  菜单C1点击事件返回**/
    public static final String PY_CLICK_TEXT = "感谢您的关注！\n" +
            "\n" +
            "联系方式：\n" +
            "139 1672 9765  Allen庞\n" +
            "133 8601 8090  Bob王\n" +
            "\n" +
            "联系地址：\n" +
            "上海市长宁区凯旋路1398弄IM长宁国际4号楼3楼";
    /**绑定页面-房主**/
    public static final String BIND_PAGE_OWNER = "pages/hu/hubind/hubind";
    // 消息模板id
    //public static final String TEMP_LATE_ID = "x8uO5Kg9FcgzgeA9HUaLIFFGngQJiZ2uTcG4r4YTENE";
    public static final String TEMP_LATE_ID = "temp_late_id";

    public static final String MD5SALT = "pzucp57mn7j9183ppww4bm7omje47449";
    // 微信公众号
    public static String WE_CHAT_PUB_APP = "wechat_pub_app";
    /**绑定页面-租户**/
    public static final String BIND_PAGE_TENANT = "pages/hu/hubindTenant/hubindTenant";
    /** 匿名投诉建议表扬主页 **/
    public static final String ADVICE_PAGE = "pages/anonymityMain/anonymityMain";

    /**客户入住页面**/
    public static final String BIND_PAGE = "pages/hu/hubind/hubind";


    // 缴费付款标签编号
    public static String PAY_TAG_ID = "pay_tag_id";
    // 缴费开票标签编号
    public static String INV_TAG_ID = "inv_tag_id";
}
