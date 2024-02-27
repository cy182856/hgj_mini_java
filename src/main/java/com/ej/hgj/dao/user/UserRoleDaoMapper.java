package com.ej.hgj.dao.user;

import com.ej.hgj.entity.user.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface UserRoleDaoMapper {

    UserRole getById(String id);

    UserRole getByStaffId(String staffId);

    List<UserRole> getList(UserRole userRole);

    void save(UserRole userRole);

    void update(UserRole userRole);

    void delete(String id);

}
