package com.ej.hgj.dao.bill.item;

import com.ej.hgj.entity.bill.BillItem;
import com.ej.hgj.vo.bill.BillRequestVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface HgjBillItemDaoMapper {

    List<BillItem> getList(String proNum);

}
