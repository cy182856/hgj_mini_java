package com.ej.hgj.dao.carrenew;

import com.ej.hgj.entity.carrenew.CarRenewOrder;
import com.ej.hgj.entity.carrenew.CarType;
import com.ej.hgj.vo.carrenew.CarRenewOrderStatusVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface CarTypeDaoMapper {

    List<CarType> getListByCarType(String carTypeNo);

}
