package com.ej.hgj.vo;

import com.ej.hgj.base.BaseRespVo;
import com.ej.hgj.entity.feedback.FeedBack;
import com.ej.hgj.entity.repair.RepairLog;
import com.ej.hgj.entity.workord.Material;
import com.ej.hgj.vo.repair.RepairDto;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ResponseVo extends BaseRespVo {

    private Integer pageNum;//页码数
    private Integer pageSize;//页码大小
    private Integer totalNum;//总记录数
    private Integer pages;//总页数

    private String[] fileList;

    private List<FeedBack> feedbackList;



}
