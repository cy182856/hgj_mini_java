package com.ej.hgj.dao.hu;

import com.ej.hgj.entity.hu.CstInto;
import com.ej.hgj.entity.hu.CstIntoCard;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface CstIntoCardMapper {

    List<CstIntoCard> getList(CstIntoCard cstIntoCard);

    void save(CstIntoCard cstIntoCard);

    void delete(String id);

    void deleteCardPerm(String proNum, String tenantWxOpenId);
}
