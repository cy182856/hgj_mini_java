package com.ej.hgj.dao.wechat;

import com.ej.hgj.entity.login.WechatPubConfDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 
 * 
 * @version $Id: WechatPubConfMapper.java, v 0.1 Aug 14, 2020 3:12:24 PM juqi Exp $
 */
@Mapper
@Component
public interface WechatPubConfMapper {
	List<WechatPubConfDo> getWechatPubConfByPk(@Param("pubOrgId") String pubOrgId);

}
