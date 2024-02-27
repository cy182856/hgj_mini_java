package com.ej.hgj.dao.bill;

import com.ej.hgj.entity.bill.PaymentRecord;
import com.ej.hgj.entity.role.Role;
import com.ej.hgj.entity.workord.WorkOrd;
import com.ej.hgj.vo.bill.RequestPaymentStatusVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface PaymentRecordDaoMapper {

    PaymentRecord getPaymentRecord(RequestPaymentStatusVo requestPaymentStatusVo);

    List<PaymentRecord> getPaymentRecordList(RequestPaymentStatusVo requestPaymentStatusVo);

    List<PaymentRecord> getList(PaymentRecord paymentRecord);

    List<PaymentRecord> getOrderList_1(String beForTenMin);

    List<PaymentRecord> getListByOrderNo(List<String> orderNoList);

    void save(PaymentRecord paymentRecord);

    void update(PaymentRecord paymentRecord);

    void updatePayRecord(PaymentRecord paymentRecord);

}
