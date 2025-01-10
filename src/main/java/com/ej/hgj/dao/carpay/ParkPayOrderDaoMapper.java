package com.ej.hgj.dao.carpay;

import com.ej.hgj.entity.carpay.ParkPayOrder;
import com.ej.hgj.vo.carpay.CarPayOrderStatusVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface ParkPayOrderDaoMapper {

    ParkPayOrder getById(String id);

    ParkPayOrder getParkPayOrder(CarPayOrderStatusVo requestOrderStatusVo);

    List<ParkPayOrder> getOrderList_1(String beForTenMin);

    void save(ParkPayOrder parkPayOrder);

    void update(ParkPayOrder parkPayOrder);

}
