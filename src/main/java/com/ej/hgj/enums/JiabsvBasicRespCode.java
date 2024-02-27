package com.ej.hgj.enums;

public enum JiabsvBasicRespCode {

    SUCCESS("0000", "交易成功"),
    RESULT_FAILED("0101", "请求失败"),
    DATA_NULL("0001", "请求参数含有空数据"),
    DATA_LENGTH_ERROR("0002", "请求参数长度不正确"),
    FINAL_DATA_NOT_EXIST("0003", "固定数据没有匹配项"),
    DATA_FOMART_MATCH_ERROR("0004", "数据格式不正确"),
    DATA_IS_NULL("0005", "暂无数据"),
    RESULT_UNCERTAIN("0006", "交易结果不确定"),
    INFO_IS_NOT_EXIST("0007","数据信息不存在"),
    WORK_DATE_INFO_NOT_EXIST("0008","工作日信息未配置"),
    QUERY_DATA_IS_NULL("0009", "查询数据为空"),
    PART_OF_THE_REQUEST_PARAMS_IS_MISSING("0010", "部分参数缺失"),
    REPEAD_SUCCESS("0011", "交易已经成功"),
    DB_INSERT_FAILED("0012", "数据库插入操作失败"),
    DB_UPD_FAILED("0013", "数据库更新操作失败"),
    DATA_IS_ILLEGAL("0014", "请求数据不合法"),
    WECHATPUB_NOT_NORMAL("0015", "公众号状态不正常"),
    HOUSEUSR_CLOSE("0016", "用户被关闭"),
    REQ_DATA_ILLEGAL("0017", "不合法的参数"),
    INFO_IS_EXIST("0018","信息已存在"),
    OPER_ID_NOT_EXIST("0019", "操作员信息不存在"),
    OPER_DOWNLOAD_MORE_THAN_TEN("0020", "下载请求不能超过10条"),
    PAGE_SIZE_BEYOND_500("0021","分页查询最大支持500笔"),
    PAGE_AGRS_BEYOND_0("0022","分页参数必须大于0"),
    DATE_FORMAT_ERROR("0023", "日期格式错误"),
    NON_SHARED_DIRECTORY("0024","非共享目录文件"),
    INFO_NOT_EXIST("0025","数据项不存在"),
    FILE_RUN_STAT_ERROR("0026","状态不正确"),
    INFO_IS_ERROR("0027","数据项有误"),
    NO_MATCHED_PAY_TYPE("0028","未找到路由信息"),
    SYSTEM_EXCEPTION("0999", "系统异常"),

    
    EW_INFO_NOT_EXIST("1001","企业微信信息不存在"),
    PROP_OPER_INFO_NOT_EXIST("1002","职员信息不存在"),
    BIND_LIST_NOT_EXIST("1003", "绑定数据不存在"),
    BIND_LIST_STAT_ISCLOSED("1004", "绑定数据状态关闭"),
    PROP_OPER_INFO_STAT_ISCLOSED("1005", "职员信息状态关闭"),
    USR_INFO_NOT_EXIST("1007", "企业信息数据不存在"),
    USR_STAT_NOT_IN_NORMAL("1008", "企业状态非正常"),
    USR_CONF_NOT_EXIST("1009", "企业配置数据不存在"),
    PM_AUTH_INFO_EXIST("1010", "物管权限已存在"),
    ADD_PM_AUTH_INFO_FAIL("1011", "新增物管权限失败"),
    UPD_PM_AUTH_INFO_FAIL("1012", "修改物管权限失败"),
    HOUSE_INFO_NOT_EXIST("1015", "住房信息不存在"),
    CHECK_CODE_IS_BLANK("1016", "校验码为空"),
    CHECK_CODE_IS_ERROR("1017", "校验码错误"),
    HOUSE_USR_INFO_NOT_EXIST("1018", "房屋住户信息不存在"),
    HAS_NO_AUTH("1019", "没有权限"),
    ALREADY_SUBMIT ("1020", "已经提交"),
    NUMBER_OF_ANSWERS_NOT_MATCH ("1021", "答案个数不匹配"),
    ANSWERS_IS_NULL ("1022", "答案为空"),
    QUETYPE_ILLEGAL ("1023", "问题类型非法"),
    PROP_AREA_INFO_NOT_EXIST("1024", "物业区域信息不存在"),
    PROP_BUILDING_INFO_NOT_EXIST("1025", "物业楼号信息不存在"),
    USR_WECHAT_PUB_CONF_NOT_EXIST("1026", "企业公众号配置不存在，请联系物业"),
    QN_EXP ("1027", "问卷已过期"),
    PUB_RES_NOT_EXIST ("1028", "公共资源信息不存在"),
    HOUSE_INFO_NOT_NORMAL ("1029", "房屋状态非正常"),
    HOUSE_INFO_OWNER_NOT_MATCH ("1030", "房屋户主不匹配"),
    OWNER_HOUSE_USR_NOT_EXIST ("1031", "房屋户主信息不存在"),
    OWNER_HOUSE_USR_NOT_NORMAL ("1032", "房屋户主状态非正常"),
    HOUSE_USR_NOT_NORMAL ("1033", "房屋成员状态非正常"),
    GET_HOUSE_QRCODE_EXCEPTION ("1034", "生成房屋绑定二维码异常"),
    ANSWERS_NOT_MATCH ("1035", "答案不匹配"),
    ANSWERS_NUM_VALUE_TOO_LARGE ("1036", "答案数值过大"),
    PROV_INFO_NOT_EXIST("1037","省份信息不存在"),
    AREA_INFO_NOT_EXIST("1038","地区信息不存在"),
    HU_ROLE_U("1039","住户角色未知"),
    TEST_HOUSE_CANT_SUBMIT("1040","测试房屋不可提交问卷"),

    /*********************邻里圈  1100-1150*******************/
    HEO_INFO_NOT_EXIST("1101","邻里圈信息数据不存在"),
    HEO_DTL_NOT_EXIST("1102","邻里圈明细信息数据不存在"),
    HEO_TOP_INFO_OVER_MAX("1103","帖子置顶个数已超过最大限制，请先取消部分已经置顶的帖子。"),
    
    /**住户房屋绑定专用返回码范围 2001 ~ 2099 */
    OFFLINE_AUTH_OPEN("2001", "线下手工开通"),
    ONLINE_CLAIM("2002", "线上认领"),
    THE_HOUSE_HAS_BEEN_CLAIMED_BY_OTHERS("2003", "房屋已经被其他人认领"),
    HU_ROLE_IS_UNKNOWN("2004", "住户角色未知"),
    OWNER_HOUSE_HAS_NOT_CERTIFIED("2005", "户主住房未绑定"),
    THE_HOUSE_HAS_NOT_BEEN_CLAIMED("2006", "住房未认领"),
    NOT_SUPPORT_NOTIFY_STAT("2007", "无法识别的异步通知状态"),
    OWNER_UNTIE_EXCEPT_OHTER_MEMBER("2008", "房屋有其他成员，不能注销！"),
    USER_NOT_BIND("2009", "您当前账号状态异常，请联系物业管理人员开通。"),
    USER_B_CLOSE("2010","您当前账户已关闭，请联系房屋管理员开通"),
    USER_R_CLOSE("2011","您当前账户已关闭，请联系户主开通"),
    USER_NOT_EXIT("2012", "账户未开通,请您联系物业管理人员进行开通"),
    CUST_CLOSE("2013","您所在的物业服务已到期,请联系物业续费"),
    DT_ACCT_GREATER_ZERO("2014", "账户余额大于0，不能注销！"),
    /** 支付交易 2100 ~ 2199 */
    ORDER_ALREADY_REFUND("2100", "支付订单已退款"),
    NO_SUPPORTED_PAY_TYPE("2101", "不支持的支付方式"),
    PAY_CHNL_CONF_NOT_EXIST("2102", "通道配置不存在"),
    PAY_TRANS_LOG_NOT_EXIST("2103", "支付交易日志不存在"),
    COMPLETE_PAY_EXCEPTION("2104", "推单异常"),
    PAY_ORD_AMT_IS_ILLEGAL("2105", "订单金额格式错误"),
    SING_EXCEPTION("2016", "签名发生异常"),
    PAY_LIQ_INFO_NOT_EXIST("2016", "支付结算信息不存在"),
    GET_PAY_INFO_FAILED("2017", "获取支付信息失败"),
    CHNL_RESP_VERIFY_SIGN_FAILED("2018", "订单异常，渠道返回参数验签失败"),
    REFUND_AMT_IS_ILLEGAL("2019", "退款金额格式错误"),
    ORG_PAY_ORDER_NOT_EXIST("2020", "原支付订单不存在"),
    ORG_PAY_ORDER_NOT_SUCCEED("2021", "原支付订单未成功"),
    AVAILABLE_CAN_REFUND_AMOUNT_NOT_ENOUGH("2022", "原支付订单可退款金额不足"),
    REFUND_OCCUR_EXCEPTION("2023", "调用退款接口发生异常"),
    REFUND_PROCESS("2024", "退款处理中"),
    COMPLETE_REFUND_EXCEPTION("2025", "退款推单异常"),
    REFUND_FAILED("2026", "退款失败"),
    ORDER_DATE_OR_ORDER_SEQ_ID_DOES_NOT_MATCHED("2027", "退款订单不匹配"),
    TRANS_QUERY_OCCUR_EXCEPTION("2028", "交易查询发生异常"),
    RECEPIT_FAILED_FOR_ORDER_ALREADY_REFUND("2029", "原订单已退款，收货确认失败"),
    RECEPIT_FAILED_FOR_ORDER_NOT_SUCCEED("2030", "原订单未支付成功，收货确认失败"),
    ORDER_HAS_ALREADY_ACKED("2031", "订单已经收货确认"),
    UNKNOWN_BUSI_ID("2032", "业务类型未知"),
    RECEPIT_OCCUR_EXCEPTION("2033", "确认收货发生异常"),
    RECEPIT_FAILED("2034", "确认收货失败"),
    RETURN_AMT_NOT_MATCHED_THE_ORD_AMT("2035", "退货金额必须为原订单金额"),
    RETURN_FAILED("2036", "退货失败"),
    
    /**
     * 业主信息管理 2200~2250
     */
    REFRESH_CHECK_CODE_FAIL("2201","刷新出行码信息失败"),
    UNKNOW_SCAN_TYPE("2202","验证不通过，未知条码类型"),
    CHECK_CODE_LENGTH_ERROR("2203","验证不通过，条码长度不正确"),
    CHECK_CODE_FIRST_STR_NOT_MATCH("2204","验证不通过，条码格式不正确。"),
    CHECK_CODE_PARAM_NOT_EXIST("2205","验证不通过，验证信息不存在。"),
    CHECK_CODE_CUST_ID_NOT_MATCH("2206","验证不通过，非当前小区出行码。"),
    CHECK_CODE_CHECK_CODE_NOT_MATCH("2207","验证不通过，验证码已失效。"),
    CHECK_CODE_HOUSE_USR_NOT_EXIST("2208","验证不通过，邀请人信息不存在。"),
    CHECK_CODE_VISIT_INFO_NOT_EXIST("2208","验证不通过，访客信息不存在。"),
    CHECK_CODE_OUT_TIME("2209","通行码已过期，请重新生成。"),
    CHECK_CODE_IS_VISIT_CODE("2210","通行码为访客验证码。"),
    CHECK_CODE_HOUSE_INFO_NOT_EXIST("2211","验证不通过，房屋信息不存在。"),
    VISIT_LOG_HAS_BEEN_CLOSE("2212","验证不通过，访客信息已经被关闭。"),
    CHECK_VISIT_EXCEED_EXP_CNT("2213","验证不通过，已超过最大可使用次数限制。"),

    /**预约订单日志记录信息返回码  3001 ~ 3099*/
    APPT_ORD_INFO_NOT_EXIST("3001","预约订单记录信息未找到"),
    APPT_ORD_STAT_UPD("3002","预约订单记录操作不支持,请联系工作人员"),
    APPT_OBJ_INFO_NOT_EIXIST("3003","预约标的信息不存在"),
    APPT_OBJ_PRICE_NOT_CONFIG("3004","预约标的价格信息未配置"),
    APPT_TIME_DTL_ADD_HAS_EXSIT("3005","预约明细存在已维护记录"),
    APPT_ORD_PAY_OVER_TIME("3006","预约订单支付超过最迟付款时间，请重新下单"),
    APPT_ORD_DTL_NOT_EXIST("3007","预约订单明细记录不存在"),
    APPT_TIME_DTL_UNIT_TIME_DIFF("3008","预约订单明细记录区间单元开始时间不一致"),
    APPT_ORD_HAS_SUCCESS("3009","预约订单已经支付成功"),
    APPT_ORD_DTL_REF_OVER_LIMIT("3010","预约订单明细部分退款超过可退款上限"),
    APPT_ORD_REF_NOT_FOUND("3011","预约订单明细退款记录未找到"),
    APPT_TIME_OVERLAP("3012","时间重叠"),
    APPT_TIME_PRICE_INFO_NOT_EXIST("3013","预约标的价格记录未找到"),
    APPT_ORD_NOT_SUPPORT_REF("3014","预约标的不支持退款"),
    APPT_ORD_NOT_SUPPORT_PAY("3015","预约订单支付失败"),
    APPT_ORD_REFUND_NOT_SUPPORT("3016","预约订单退款操作不支持"),
    APPT_ORD_REFUND_HAS_COMPLETE("3017","预约订单退款失败,请稍后再试"),
    APPT_ORD_REFUND_FAIL("3018","预约订单退款失败"),
    
    /**报修接口信息返回码  3100 ~ 3149*/
    FAILED_TO_ADD_REPAIR_LOG_TABLE("3100","报修日志表新增失败！"),
    FAILED_TO_ADD_REPAIR_MSG_DTL_TABLE("3101","报修留言明细表新增失败！"),
    REPAIR_PROC_STAT_ERROR("3102","报修单处理状态异常"),
    PAYTYPE_UNSURE("3103","支付方式未知"),
    MAX_UPD("3104","达到修改上限,无法再次修改"),
    REPLY_STAT_ERROR("3105","留言单状态异常"),
    REPAIR_LOG_NOT_EXIST("3106","报修日志不存在"),
    CHANGE_REPAIR_MAN_IS_SAME("3107","不能改派同一个维修员"),
    NOT_ASSIGN("3108","非分配模式,无法进行分配"),
    UN_SUPORT_REPAIR_TYPE("3109","公共报修不支持"),
    ORDAMT_IS_SAME("3110","修改订单金额不能和原金额相同"),
    PAY_STAT_ERROR("3111","支付状态非法"),
    CAL_ERROR("3112","计算发生转化异常"),
    CANCEL_ERROR("3113","报修单已处理,无法取消"),
    /**物业缴费接口信息返回码  3150 ~ 3200*/
    PFEE_MON_BILL_EXIST("3150", "存在已缴费的账单,请勿重复缴费"),
    PFEE_ORD_LOG_ADD_FAIL("3151", "物业订单日志新增记录失败"),
    PFEE_ORD_DTL_ADD_FAIL("3152", "物业订单明细新增记录失败"),
    START_MIN_END_MON("3153", "起始月份小于结束月份"),
    PFEE_ORD_LOG_NOT_EXIST("3154", "物业订单日志不存在"),
    PFEE_ORD_DTL_NOT_EXIST("3155", "物业订单明细缺失"),
    PFEE_ORD_DTL_EXIST("3156", "存在已经缴费的物业账单，请先关闭"),
    PFEEBILLLOAD_AUDITSTAT_P_EXIST("3157", "存在审核状态为已缴费的物业账单导入信息，请重新选择日期"),
    
    PFEE_ORD_LOG_CLOSED("3158", "物业订单已被关闭"),
    PFEEBILLLOAD_AUDITSTAT_NOT_I("3159", "物业账单导入信息的审核状态不是初始未缴费，不可进行审核"),
    /**访客管理接口信息返回码  3201 ~ 3250*/
    VISIT_LOG_NOT_EXIST ("3201", "访客日志不存在"),
    VISIT_LOG_IS_PAST_DUE ("3202", "访客信息已过期"),
    VISIT_LOG_IS_CLOSE ("3203", "访客信息已关闭"),
    DATE_INTERVAL_TOO_SHORT ("3204", "生成访客码间隔太短"),
    
    /**物业缴费接口信息返回码  3251 ~ 3300*/
    WECHAT_TEMP_NOT_EXIST("3251", "微信模板消息不存在"),
    WECHAT_TEMP_STAT_NOT_NORMAL("3252", "微信模板消息状态不正常"),
    NOT_SUPPORT_BUSI_TYPE("3253", "不支持该业务类型"),
    
    /** 消息 3301 ~ 3350 */
    MSG_RECEIVER_NOT_EXIST("3301", "消息接收人不存在"), 
    MSG_SEND_FAILURE("3302","消息发送失败"),

    /** 公告 3351~3370 **/
    BB_LIST_IS_NOT_NEED_SIGN("3351", "公告板无需签收" ),


    /**反馈3371-3400*/
    ADVICE_STAT_ERROR("3351","反馈状态异常"),
    
    /**供应商3401~3600**/
    SUP_OPER_UNBIND("3401","供应商操作员未关注微信公众号"),
    SUP_OPER_HAS_CLOSED("3402","供应商操作员账号信息已被注销"),
    SUP_OPER_INFO_NOT_EXIST("3403","供应商操作员账号信息不存在"),
    GET_SESSIONKEY_FAIL("3450","获取小程序关键凭证失败"),
    GET_SUPWX_UNION_FAIL("3451","获取供应商微信union信息失败"),
    GET_SUPWX_PHONENUM_FAIL("3452","获取供应商微信手机号信息失败"),
    ADD_SUPWXUNION_FAIL("3453","新增供应商微信union信息失败"),
    UPD_SUPWXUNION_FAIL("3454","更新供应商微信union信息失败"),
    SUPWX_UNION_NOT_EXIST("3455","供应商操作员微信union信息不存在"),
    SUPWX_OPENID_NOT_EXIST("3456","供应商操作员未关注微信公众号"),
    SUPWX_HAS_NOT_REGISTER("3457","供应商还未注册"),
    SO_HAS_NOT_REGISTER("3458","供应商操作员还未注册"),
    SUP_INFO_NOT_MATCH("3459","供应商信息不匹配"),
    PROP_LIFE_LIST_IS_NOT_EXIST("3460", "生活服务类目不存在"),
    WECHAT_MINIAPP_CONF_NOT_EXIST("3461","小程序配置信息不存在"),
    QUERY_MINIAPP_TOKEN_FAIL("3462","获取小程序accessToken失败"),
    SUP_INFO_HAS_CLOSED("3463","供应商信息被关闭"),
    SUP_CERT_INFO_NOT_EXIST("3464","供应商证照信息不存在"),
    SUP_CERT_INFO_INCLUD_STAT_C("3465","通道审核中，不能修改证件类型"),
    SUP_CERT_INFO_INCLUD_STAT_A("3466","系统审核中，不能修改"),
    SUP_CERT_INFO_INCLUD_STAT_S("3467","所有证照信息已审核通过，不能修改"), 
    SUP_PUB_CONF_NOT_EXIT("3468","供应商公众号配置信息不存在"),
    SUP_CERT_INFO_IS_EXIST("3469","供应商证照信息已存在，不能重复提交。"),
    SUP_INFO_NOT_EXIST("3470","供应商信息不存在"),
    SUP_INFO_HAS_CANCEL("3471","供应商已销户"),
    SUP_INFO_HAS_SETTLED("3472","供应商已入驻物业"),
    SUP_INFO_HAS_OTHER_NORMAL_OPER("3473","供应商下还有其他未销户操作员"),
    SUP_OPER_INFO_HAS_CANCEL("3474","供应商下还有其他未销户操作员"),
    SUP_OPER_HAS_NOTEXIST_CANCEL("3475","操作员信息不存在或已销户"),
    ORG_SUP_INFO_NOT_EXIST("3476","供应商信息不存在，无法加入。"),
    ORG_SUP_INFO_HAS_CLOSE("3477","供应商信息已被关闭，无法加入。"),
    ITEM_SHOW_NOT_EXIST("3478","商品展位不存在"),
    ITEM_SHOW_NOT_USEFUL("3479","商品展位失效"),
    ITEM_NOT_EXIST("3480","商品不存在"),
    ITEM_NOT_USEFUL("3481","商品已经关闭"),
    ITEM_SHOW_EXIST("3482","展位上已经存在商品"),
    ITEM_NOT_MATCH("3483","信息不匹配"),

    PROP_SUP_LIST_IS_NOT_EXIST("3484", "物业没有该供应商"),
    USR_LIFE_CONF_IS_NOT_EXIST("3485", "企业生活服务配置不存在"),
    SUP_PAY_CHNL_IS_NOT_EXIST("3486", "供应商支付通道数据不存在"),


    /**供应商商品信息 3601 ~ 3650**/
    CHOOSE_PACKAGE_ADD_ITEMINFO_FAIL("3601","选择封装商品信息处理类失败"),

    /**文件信息 3651 ~ 3699**/
    FM_DIR_HAS_EXIST_NORMAL("3651","文件目录已存在且正常使用"),
    FM_DIR_DELTE_UN_SUPPORT("3652","文件目录下存在正常使用的目录或者文件,删除操作不支持"),
    FM_FILE_NOT_FOUND("3653","文件信息未找到"),
    GET_FILE_INFO_FAIL("3654","获取文件信息失败"),
    FM_FILE_HAS_EXIST_NORMAL("3651","文件已存在且正常使用"),
    
    /** 充电桩 3701 ~ 3799 **/
    CHAREG_OWE_FEE("3701", "充电欠费"),
    COOP_INFO_IS_NOT_EXIST("3702", "合作商信息不存在"),
    COOP_INFO_STAT_ISNOT_NORMAL("3704", "合作商信息非正常状态"),
    CHAREG_DEV_INFO_IS_NOT_EXIST("3703", "充电桩设备信息不存在"),
    CHAREG_DEV_INFO_STAT_ISNOT_NORMAL("3705", "充电桩设备信息非正常状态"),
    COOP_DEV_CONF_IS_NOT_EXIST("3706", "合作商设备类型信息不存在"),
    COOP_DEV_CONF_STAT_ISNOT_NORMAL("3707", "合作商设备类型信息非正常状态"),
    PROP_CHARGE_CONF_IS_NOT_EXIST("3708", "物业充电桩配置不存在"),
    NO_SUPPORTED_CHARGE_DEV("3709", "充电设备路由不存在"),
    DT_ACCT_INFO_IS_NOT_EXIST("3710", "账户不存在"),
    DT_ACCT_INFO_STAT_ISNOT_NORMAL("3710", "账户状态非正常"),
    ACCT_BAL_NOT_ENOUGH("3711", "账户余额不足"),
    PART_REFUND_FAIL("3712", "部分退款失败"),
    SAVE_REF_LOG_NOT_EXIST("3713", "充值退款交易日志信息不存在"),
    REF_TRANS_LOG_NOT_EXIST("3714", "退款交易日志信息不存在"),
    HAS_PROCESS_REF_REQ("3715", "含有处理中的退款请求"),
    REFAMT_GREATER_THAN_CANREFAMT("3716", "退款上送余额大于可退款余额"),
    CHAREGE_FAILED("3717", "充电失败"),
    CHARGE_QUERY_FAILED("3718", "充电详情查询失败"),
    IS_CHARGEING("3719", "充电桩正在充电中，请勿重复启动"),
    END_CHAREGE_FAILED("3720", "结束充电失败"),
    PROP_CHARGE_CONF_HAS_EXIST("3721", "物业充电桩配置已存在"),
    HAS_CHARGEING_ORD("3722", "账户退款，存在正在充电的订单"),
    DT_ACCT_INFO_ISNOT_EXIST("3723", "账户信息不存在"),
    
    /**
     * 门禁信息 3800  ~  3899
     */
    AC_BUILDLIST_NOT_EXIST("3800", "门禁楼号信息不存在"),
    AC_HU_MAP_NOT_EXIST("3801","门禁业主映射信息不存在"),
    FIRST_CODE_NOT_MATCH("3802","业主码首位不匹配"),
    AC_BUILD_INFO_NOT_EXIST("3803","门禁楼号列表信息不存在"),
    AC_HU_MAP_INFO_NOT_EXIST("3804","门禁业主映射表信息不存在"),
    QR_CODE_MD5_CHECK_FAIL("3805","门禁二维码加密信息验证失败"),

    /**
     * 对账 3901~3999
     */
    CHECK_FILE_INTO_ERROR("3901", "对账文件信息参数不合法"),
    CHECK_FILE_NOT_EXIST("3902", "对账文件不存在"),
    NOT_SUPP_UNCOMP_WAY("3903", "不支持这种方式来解压文件"),
    NO_MATCHED_TRADE_CHECK_GATE_INFO("3904", "没有对应对账网关信息"),
    NO_MATCHED_FTP_INFO("3905", "对应的FTP信息不存在"),
    NOT_SUPP_WAY_GET_CHECK_FILE("3906", "不支持该方式来获取对账文件"),
    CHECK_FILE_UNCOMP_FAILED("3907", "对账文件解析失败"),
    USR_TYPE_NO_SUPPORT_THE_SECEN("3908", "用户类型不支持该支付场景"),
    ;

    private String respCode;
    private String respDesc;
    
    JiabsvBasicRespCode(String respCode, String respDesc) {
    	this.respCode = respCode;
    	this.respDesc = respDesc;
    }

	public String getRespCode() {
		return JiaSubsystem.JIABSV.getWholeSystemNo().concat(respCode);
	}

	public void setRespCode(String respCode) {
		this.respCode = respCode;
	}

	public String getRespDesc() {
		return respDesc;
	}

	public void setRespDesc(String respDesc) {
		this.respDesc = respDesc;
	}
    
}
