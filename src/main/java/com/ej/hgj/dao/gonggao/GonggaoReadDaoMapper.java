package com.ej.hgj.dao.gonggao;

import com.ej.hgj.entity.gonggao.GonggaoRead;
import com.ej.hgj.entity.gonggao.GonggaoType;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface GonggaoReadDaoMapper {

    GonggaoRead getById(String id);

    List<GonggaoRead> getList(GonggaoRead gonggaoRead);

    void save(GonggaoRead gonggaoRead);

    void update(GonggaoRead gonggaoRead);

    void delete(String id);

}
