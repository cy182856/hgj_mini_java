package com.ej.hgj.dao.menu.mini;

import com.ej.hgj.entity.menu.mini.MenuMini;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface MenuMiniDaoMapper {

    List<MenuMini> getList(MenuMini menuMini);

    List<MenuMini> findMenuByCstCode(String cstCode);

}
