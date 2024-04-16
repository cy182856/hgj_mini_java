package com.ej.hgj.sy.dao.cst;

import com.ej.hgj.entity.cst.HgjCst;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface HgjSyCstDaoMapper {

    HgjCst getCstNameByResId(String resId);

}
