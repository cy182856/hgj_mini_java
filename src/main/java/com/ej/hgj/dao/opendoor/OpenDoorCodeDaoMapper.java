package com.ej.hgj.dao.opendoor;

import com.ej.hgj.entity.opendoor.OpenDoorCode;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface OpenDoorCodeDaoMapper {

    List<OpenDoorCode> getQrCodeByExpDate(OpenDoorCode openDoorCode);

    List<OpenDoorCode> getQrCodeByExpDate2(OpenDoorCode openDoorCode);

    void save(OpenDoorCode openDoorCode);


}
