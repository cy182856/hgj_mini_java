package com.ej.hgj.sy.dao.bill.item;

import com.ej.hgj.entity.bill.Bill;
import com.ej.hgj.entity.bill.BillItem;
import com.ej.hgj.entity.bill.BillYear;
import com.ej.hgj.vo.bill.BillRequestVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface BillItemDaoMapper {

    List<BillItem> getBillItem_09(BillRequestVo billRequestVo);

    List<BillItem> getBillItem_0(BillRequestVo billRequestVo);

}
