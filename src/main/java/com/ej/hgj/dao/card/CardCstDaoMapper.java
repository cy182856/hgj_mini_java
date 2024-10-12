package com.ej.hgj.dao.card;

import com.ej.hgj.entity.card.CardCst;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface CardCstDaoMapper {

    CardCst getById(String id);

    CardCst getCardInfo(String proNum, String cstCode, String cardType);
}
