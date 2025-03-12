package com.ej.hgj.dao.moncarren;

import com.ej.hgj.entity.carrenew.CarRenewOrder;
import com.ej.hgj.entity.moncarren.MonCarRenOrder;
import com.ej.hgj.vo.carrenew.CarRenewOrderStatusVo;
import com.ej.hgj.vo.moncarren.MonCarRenOrderStatusVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface MonCarRenOrderDaoMapper {

    MonCarRenOrder getById(String id);

    MonCarRenOrder getMonCarRenOrder(MonCarRenOrderStatusVo monCarRenOrderStatusVo);

    List<MonCarRenOrder> getOrderList_1(String beForTenMin);

    void save(MonCarRenOrder monCarRenOrder);

    void update(MonCarRenOrder monCarRenOrder);

    void updateCallBackCode(MonCarRenOrder monCarRenOrder);

    List<MonCarRenOrder> getList(MonCarRenOrder monCarRenOrder);


}
