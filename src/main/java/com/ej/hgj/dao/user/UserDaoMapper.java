package com.ej.hgj.dao.user;

import com.ej.hgj.entity.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface UserDaoMapper {

    User getById(String id);

    List<User> getByCstCode(String cstCode);

    User queryUser(@Param("userName") String userName, @Param("password") String password);

    void insertList(@Param("list") List<User> users);

    List<User> getList(User user);

    List<User> getDeptList(User user);

    void save(User user);

    void update(User user);

    void delete(String id);

}
