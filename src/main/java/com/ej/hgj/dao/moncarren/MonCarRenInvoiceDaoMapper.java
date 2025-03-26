package com.ej.hgj.dao.moncarren;

import com.ej.hgj.entity.moncarren.MonCarRenInvoice;
import com.ej.hgj.entity.moncarren.MonCarRenOrder;
import com.ej.hgj.vo.moncarren.MonCarRenOrderStatusVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface MonCarRenInvoiceDaoMapper {

    void save(MonCarRenInvoice monCarRenInvoice);

    void update(MonCarRenInvoice monCarRenInvoice);

    MonCarRenInvoice getByOrderId(String orderId);
}
