package com.ej.hgj.request.acct;

import com.ej.hgj.result.base.BaseResult;
import com.ej.hgj.result.dto.DtAcctInfoDto;
import lombok.Data;

import java.util.List;

@Data
public class QueryDtAcctInfoResult extends BaseResult {

	private List<DtAcctInfoDto> dtAcctInfoDtos;

}
