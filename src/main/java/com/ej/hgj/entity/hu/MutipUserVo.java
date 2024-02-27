package com.ej.hgj.entity.hu;

import com.ej.hgj.base.BaseRespVo;
import com.ej.hgj.result.dto.HuHgjBindDto;
import lombok.Data;

import java.util.List;

@Data
public class MutipUserVo extends BaseRespVo {

    List<HuHgjBindDto> huHgjBindList;

}
