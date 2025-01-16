package com.ej.hgj.vo.carpay;

import com.ej.hgj.base.BaseRespVo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ParkCardVo extends BaseRespVo {

    private String cardCstBatchId;

    private String cardName;

    private Integer expNum;

}
