package com.ej.hgj.vo.gonggao;

import com.ej.hgj.entity.gonggao.Gonggao;
import com.ej.hgj.entity.gonggao.GonggaoType;
import lombok.Data;

import java.util.List;

/**
 *
 * @author  xia
 * @version $Id: HouseInfoVO.java, v 0.1 2020年10月27日 下午4:14:51 xia Exp $
 */
@Data
public class GonggaoVo {
   private String cstCode;
   private String wxOpenId;
   private String proNum;

   private String id;
   private String typeId;

   private Integer pageNum = 1;//页数
   private Integer pageSize = 10;//页大小
   private Integer totalNum;//总记录数
   private Integer pages;//总页数
   private List<Gonggao> gonggaoList;
   private Gonggao gonggao;
   private String respCode;


}
