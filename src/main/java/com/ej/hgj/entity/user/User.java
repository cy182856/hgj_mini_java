package com.ej.hgj.entity.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class User {
    private String id;

    private String projectNum;

    private String staffId;

    private String userId;

    private String userName;

    private String qrCode;

    private String roleId;

    private String roleName;

    private String password;

    private String alias;

    private String deptName;

    private String position;

    private String mobile;

    private String bizMail;

    private String avatar;

    private String thumbAvatar;

    private Integer gender;

    private Integer status;

    private String createBy;

    private String updateBy;

    private Integer deleteFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    // 是否工作时间 1-是  0-否
    private Integer isWorkTime;

}
