package com.ej.hgj.dao.carpay;

import com.ej.hgj.entity.carpay.ParkPayInvoice;
import com.ej.hgj.entity.moncarren.MonCarRenInvoice;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface ParkPayInvoiceDaoMapper {

    void save(ParkPayInvoice parkPayInvoice);

    void update(ParkPayInvoice parkPayInvoice);

    ParkPayInvoice getByOrderId(String orderId);
}
