package com.ej.hgj.request.hu;

import lombok.Data;

@Data
public class HuCheckInRequest {

    private String wxOpenId;

    private String cstCode;

    private String userName;

    private String houseId;

    private String cstIntoId;

}
