package com.ej.hgj.result.base;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @author  xia
 * @version $Id: BaseResult.java, v 0.1 2020年8月6日 下午3:50:54 xia Exp $
 */
@Data
public class BaseResult implements Serializable {

    /**  */
    private static final long serialVersionUID = 2585339599036429552L;
    
    private String respCode;
    
    private String errCode;
    
    private String errDesc;

}
