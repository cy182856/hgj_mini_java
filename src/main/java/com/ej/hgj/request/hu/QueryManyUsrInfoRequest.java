package com.ej.hgj.request.hu;


import lombok.Data;

import java.io.Serializable;

/**
 * @author tty
 * @version 1.0 2020-09-28 15:55
 */
@Data
public class QueryManyUsrInfoRequest implements Serializable {
    private static final long serialVersionUID = -6041631741717499532L;
    /**
     * 用户的慧管家openID,必传
     */
    private String hgjOpenId;
    /**
     * 企业客户号
     */
    private String custId;

}
