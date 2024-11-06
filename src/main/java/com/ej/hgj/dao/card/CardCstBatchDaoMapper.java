package com.ej.hgj.dao.card;

import com.ej.hgj.entity.card.CardCstBatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface CardCstBatchDaoMapper {

    CardCstBatch getById(String id);

    List<CardCstBatch> getList(CardCstBatch cardCstBatch);

    void update(CardCstBatch cardCstBatch);



}
