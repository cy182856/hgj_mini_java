package com.ej.hgj.entity.bill;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class Bill {

    private String id;

    private String year;

    private String yearMonth;

    // 所属账期
    private String repYears;

    // 本金应收
    private BigDecimal priRev;

    // 本金已收
    private BigDecimal priPaid;

    // 本金欠收
    private BigDecimal priFailures;

    // 滞纳金应收
    private BigDecimal lFRev;

    // 滞纳金已收
    private BigDecimal lFPaid;

    // 滞纳金欠收
    private BigDecimal lFFailures;

    // 资源ID
    private String resId;

    // 摘要（包括当前费用的详细备注信息或收费情况）
    private String revAbstract;

    // 费用状态，0表示未开交，1表示已开交，9表示已交清
    private Integer lockLogo;

    // 应收开始日期
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date revPSDate;

    // 应收结束日期
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date revPEDate;

    // 仪表编码
    private String imaCode;

    // 抄表年月
    private String doMr;

    // 读数所属账期
    private String dataOfRead;

    // 上次抄表日期
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lMrd;

    // 本次抄表日期
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date tMrd;

    // 上次抄表读数
    private BigDecimal lMr;

    // 本次抄表读数
    private BigDecimal tMr;

    // 上月用量
    private BigDecimal lciAmount;

    // 本期用量
    private BigDecimal ciAmount;

    // 房间名称
    private String resName;

    // 收付项目名称（此笔费用的类型，如“电费”，“水费”，“管理费”等）
    private String ipItemName;

    // 缴费权限 0-无 1-有
    private Integer isPayment;

    // 开票权限 0-无 1-有
    private Integer isInvoice;

    private Boolean checked;

    private String title;

}
