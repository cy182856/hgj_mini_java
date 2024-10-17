package com.ej.hgj.dao.tag;

import com.ej.hgj.entity.cst.HgjCst;
import com.ej.hgj.entity.tag.TagCst;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface TagCstDaoMapper {

    List<TagCst> getList(TagCst tagCst);

}
