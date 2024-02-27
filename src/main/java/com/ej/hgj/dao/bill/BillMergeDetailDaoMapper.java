package com.ej.hgj.dao.bill;

import com.ej.hgj.entity.bill.BillMergeDetail;
import com.ej.hgj.entity.bill.PaymentRecord;
import com.ej.hgj.entity.role.RoleMenu;
import com.ej.hgj.vo.bill.RequestPaymentStatusVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface BillMergeDetailDaoMapper {

    List<BillMergeDetail> getList(BillMergeDetail billMergeDetail);

    List<BillMergeDetail> getBillByStatus(String beForOneHour);

    void update(BillMergeDetail billMergeDetail);

    void updateByBillId(BillMergeDetail billMergeDetail);

    void updateMerDetailStatus(BillMergeDetail billMergeDetail);

    void insertList(@Param("list") List<BillMergeDetail> billMergeDetailList);


}
