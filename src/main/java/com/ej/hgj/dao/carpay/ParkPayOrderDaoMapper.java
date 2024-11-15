package com.ej.hgj.dao.carpay;

import com.ej.hgj.entity.bill.BillMerge;
import com.ej.hgj.entity.bill.PaymentRecord;
import com.ej.hgj.entity.carpay.ParkPayOrder;
import com.ej.hgj.vo.bill.RequestPaymentStatusVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface ParkPayOrderDaoMapper {

    ParkPayOrder getById(String id);

    void save(ParkPayOrder parkPayOrder);



}
