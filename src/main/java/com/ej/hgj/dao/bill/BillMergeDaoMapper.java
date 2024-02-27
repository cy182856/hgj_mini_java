package com.ej.hgj.dao.bill;

import com.ej.hgj.entity.bill.Bill;
import com.ej.hgj.entity.bill.BillMerge;
import com.ej.hgj.entity.bill.PaymentRecord;
import com.ej.hgj.vo.bill.BillRequestVo;
import com.ej.hgj.vo.bill.RequestPaymentStatusVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface BillMergeDaoMapper {

    BillMerge getById(String id);

    List<BillMerge> getList(BillRequestVo billRequestVo);

    void save(BillMerge billMerge);

    void update(BillMerge billMerge);

    void updateBillMergeStatus(BillMerge billMerge);

}
