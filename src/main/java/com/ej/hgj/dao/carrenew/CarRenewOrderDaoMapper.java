package com.ej.hgj.dao.carrenew;

import com.ej.hgj.entity.carpay.ParkPayOrder;
import com.ej.hgj.entity.carrenew.CarRenewOrder;
import com.ej.hgj.vo.carpay.CarPayOrderStatusVo;
import com.ej.hgj.vo.carrenew.CarRenewOrderStatusVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface CarRenewOrderDaoMapper {

    CarRenewOrder getById(String id);

    CarRenewOrder getCarRenewOrder(CarRenewOrderStatusVo carRenewOrderStatusVo);

    List<CarRenewOrder> getOrderList_1(String beForTenMin);

    void save(CarRenewOrder carRenewOrder);

    void update(CarRenewOrder carRenewOrder);

    void updateCallBackCode(CarRenewOrder carRenewOrder);

}
