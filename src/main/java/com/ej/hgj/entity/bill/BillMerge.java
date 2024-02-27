package com.ej.hgj.entity.bill;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class BillMerge {

    private String id;

    // 项目号
    private String proNum;

    // 微信openId
    private String wxOpenId;

    // 客户编号
    private String cstCode;

    // 应收金额
    private BigDecimal priRev;

    // 已收金额
    private BigDecimal priPaid;

    // 收付项目名称（此笔费用的类型，如“电费”，“水费”，“管理费”等）
    private String ipItemName;

    // 账单状态: 0-缴费中  1-缴费成功  2-已取消  3-缴费失败
    private Integer billStatus;

    private String createBy;

    private String updateBy;

    private Integer deleteFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    // 缴费权限 0-无 1-有
    private Integer isPayment;

    // 开票权限 0-无 1-有
    private Integer isInvoice;

    // 付款时限,一小时内
    @JsonFormat(pattern = "HH:mm", timezone = "GMT+8")
    @DateTimeFormat(pattern = "HH:mm")
    private Date timeLimit;

}
