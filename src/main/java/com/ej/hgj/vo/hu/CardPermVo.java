package com.ej.hgj.vo.hu;

import lombok.Data;

@Data
public class CardPermVo {


   private String cstCode;

   private String tenantWxOpenId;

   private String wxOpenId;

   private String proNum;

   private Integer[] cardIds;

}
