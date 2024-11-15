package com.ej.hgj.dao.carpay;

import com.ej.hgj.entity.bill.PaymentRecord;
import com.ej.hgj.entity.carpay.ParkPayRecord;
import com.ej.hgj.vo.bill.RequestPaymentStatusVo;
import com.ej.hgj.vo.carpay.CarPayRequestVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface ParkPayRecordDaoMapper {

    ParkPayRecord getParkPayRecord(CarPayRequestVo carPayRequestVo);

    void save(ParkPayRecord parkPayRecord);


}
