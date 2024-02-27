package com.ej.hgj.result.hu;

import com.ej.hgj.result.base.BaseResult;
import com.ej.hgj.result.dto.HuHgjBindDto;

import java.util.List;

/**
 * @author tty
 * @version 1.0 2020-09-28 15:50
 */
public class QueryManyUsrInfoResult extends BaseResult {

    private static final long serialVersionUID = 1404200217741416883L;
    /**
     * 用户多账户信息
     */
    List<HuHgjBindDto> userList;

    public List<HuHgjBindDto> getUserList() {
        return userList;
    }

    public void setUserList(List<HuHgjBindDto> userList) {
        this.userList = userList;
    }
}
