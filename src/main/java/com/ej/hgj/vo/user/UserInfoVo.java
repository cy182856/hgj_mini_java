package com.ej.hgj.vo.user;

import com.ej.hgj.base.BaseRespVo;
import com.ej.hgj.entity.user.User;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserInfoVo extends BaseRespVo {

    private List<User> userList = new ArrayList<>();

}
