package com.ej.hgj.entity.workord;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Material {

    // 材料名称
    private String materialName;

    // 材料数量
    private String planNum;


}
