package com.ej.hgj.entity.carpay;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class ParkPayInvoice {

    private String id;

    private String orderId;

    private String requestId;

    private String serialNo;

    private String buyerName;

    private String buyerTaxNo;

    private String pushEmail;

    private Integer invoiceType;

    private String pdfUrl;

    private String resCode;

    private String resMsg;

    private Integer invoiceStatus;

    private String createBy;

    private String updateBy;

    private Integer deleteFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
