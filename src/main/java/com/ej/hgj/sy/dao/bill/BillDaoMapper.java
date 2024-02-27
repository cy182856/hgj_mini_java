package com.ej.hgj.sy.dao.bill;

import com.ej.hgj.entity.bill.Bill;
import com.ej.hgj.entity.bill.PaymentRecord;
import com.ej.hgj.entity.workord.Material;
import com.ej.hgj.vo.bill.BillRequestVo;
import com.ej.hgj.vo.bill.RequestPaymentStatusVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface BillDaoMapper {

    List<Bill> getList(BillRequestVo billRequestVo);

    Bill getBillMonthAmount(BillRequestVo billRequestVo);

    List<Bill> getMonthBill_09(BillRequestVo billRequestVo);

    List<Bill> getMonthBill_0(BillRequestVo billRequestVo);

    Bill priRevAmount(BillRequestVo billRequestVo);

    List<Bill> getListByRepYears(BillRequestVo billRequestVo);

    Bill getOrderById(String id);

    List<Bill> getOrderByIds(BillRequestVo billRequestVo);


}
