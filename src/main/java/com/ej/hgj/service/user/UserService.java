package com.ej.hgj.service.user;



import com.ej.hgj.entity.user.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserService {

    User queryUser(String userName, String password);

    User getById(String id);

    List<User> getList(User user);

    List<User> getDeptList(User user);

    void insertList(List<User> users);

    void save(User user);

    void update(User user);

    void delete(String id);


}
