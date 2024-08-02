package com.ej.hgj.dao.wechat;

import com.ej.hgj.entity.wechat.WechatPub;
import com.ej.hgj.entity.wechat.WechatPubUser;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface WechatPubDaoMapper {

    WechatPub getById(String id);

    List<WechatPub> getList(WechatPub wechatPub);

    void update(WechatPub wechatPub);

    void save(WechatPub wechatPub);

    WechatPub getMaxId();

    WechatPub getByOrgId(String originalId);

}
