package com.ej.hgj.service.user;

import com.ej.hgj.dao.user.UserDaoMapper;
import com.ej.hgj.entity.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserDaoMapper userDaoMapper;

    @Override
    public User queryUser(String userName, String password) {
        return userDaoMapper.queryUser(userName, password);
    }

    @Override
    public User getById(String id){
        return userDaoMapper.getById(id);
    }

    public List<User> getList(User user){
        return userDaoMapper.getList(user);
    }

    @Override
    public List<User> getDeptList(User user) {
        return userDaoMapper.getDeptList(user);
    }

    @Override
    public void insertList(List<User> users) {
        userDaoMapper.insertList(users);
    }

    @Override
    public void save(User user) {
        userDaoMapper.save(user);
    }

    @Override
    public void update(User user) {
        userDaoMapper.update(user);
    }

    @Override
    public void delete(String id) {
        userDaoMapper.delete(id);
    }


}
