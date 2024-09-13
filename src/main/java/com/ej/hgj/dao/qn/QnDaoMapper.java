package com.ej.hgj.dao.qn;

import com.ej.hgj.entity.qn.Qn;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface QnDaoMapper {

    Qn getById(String id);

    List<Qn> getList(Qn qn);

}
