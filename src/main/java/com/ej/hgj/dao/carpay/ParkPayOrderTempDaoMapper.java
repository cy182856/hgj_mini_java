package com.ej.hgj.dao.carpay;

import com.ej.hgj.entity.carpay.ParkPayOrder;
import com.ej.hgj.entity.carpay.ParkPayOrderTemp;
import com.ej.hgj.vo.carpay.CarPayRequestVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface ParkPayOrderTempDaoMapper {

    ParkPayOrderTemp getParkPayOrderTemp(String orderId);

    void save(ParkPayOrderTemp parkPayOrderTemp);

    void update(ParkPayOrderTemp parkPayOrderTemp);
}
