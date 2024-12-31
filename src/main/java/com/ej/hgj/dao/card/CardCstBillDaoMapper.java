package com.ej.hgj.dao.card;

import com.ej.hgj.entity.card.CardCstBill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface CardCstBillDaoMapper {

    void save(CardCstBill cardCstBill);

}
