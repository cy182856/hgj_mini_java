package com.ej.hgj.sy.dao.bill.year;

import com.ej.hgj.entity.bill.Bill;
import com.ej.hgj.entity.bill.BillYear;
import com.ej.hgj.vo.bill.BillRequestVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface BillYearDaoMapper {

    List<BillYear> getBillYear_09(BillRequestVo billRequestVo);

    List<BillYear> getBillYear_0(BillRequestVo billRequestVo);

}
