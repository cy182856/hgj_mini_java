package com.ej.hgj.entity.bill;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class BillMergeDetail {

    private String id;

    // 账单合并表ID
    private String billId;

    // 所属账期
    private String repYears;

    // 思源订单号
    private String orderNo;

    // 项目号
    private String proNum;

    // 微信openId
    private String wxOpenId;

    // 客户编号
    private String cstCode;

    // 本金应收
    private BigDecimal priRev;

    // 本金已收
    private BigDecimal priPaid;

    // 收付项目名称（此笔费用的类型，如“电费”，“水费”，“管理费”等）
    private String ipItemName;

    // 账单状态: 0-缴费中  1-缴费成功  2-已取消  3-缴费失败
    private Integer billStatus;

    // 思源缴费接口返回状态 0-未同步 1-成功  2-失败
    private Integer syPayStatus;

    // 思源缴费接口返回消息
    private String syPayMsg;

    private String createBy;

    private String updateBy;

    private Integer deleteFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    // 账单状态集合
    List<Integer> billStatusList;

    // 思源账单ID集合
    List<String> billOrderNoList;

    // 思源同步状态集合
    List<Integer> syPayStatusList;

}
