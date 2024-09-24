package com.ej.hgj.dao.active;

import com.ej.hgj.entity.active.CouponQrCode;
import com.ej.hgj.entity.opendoor.OpenDoorCode;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface CouponQrCodeDaoMapper {

    List<CouponQrCode> getQrCodeByExpDate(CouponQrCode couponQrCode);

    void save(CouponQrCode couponQrCode);


}
