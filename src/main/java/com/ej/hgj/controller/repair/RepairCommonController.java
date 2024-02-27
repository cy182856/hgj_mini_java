//package com.ej.hgj.controller.repair;
//
//import com.huiguan.monster.common.constants.code.respCode.MonsterBasicRespCode;
//import com.monster.facade.result.dto.RepairCommonDto;
//import com.monster.facade.result.repair.QueryRepairCommonListResult;
//import com.monster.facade.result.repair.UpdRepairCommonCntResult;
//import com.monster.jiasv.common.JiasvBasicRespCode;
//import com.monster.jiasv.integration.repair.RepairLogClient;
//import com.monster.jiasv.serivce.domain.base.BaseRespVo;
//import com.monster.jiasv.serivce.domain.repair.RepairCommonVo;
//import com.monster.jiasv.utils.exception.BusinessException;
//import com.monster.jiasv.webapp.controller.base.BaseController;
//import org.apache.commons.lang.StringUtils;
//import org.apache.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import java.util.List;
//
///**
// * @author tty
// * @version 1.0 2021-01-19 11:02
// */
//@Controller
//public class RepairCommonController extends BaseController {
//    private static final Logger logger = Logger.getLogger(RepairCommonController.class);
//    @Autowired
//    private RepairLogClient repairLogClient;
//    @ResponseBody
//    @RequestMapping("/repairCommonList")
//    public BaseRespVo queryRepairList(){
//        try{
//            QueryRepairCommonListResult queryRepairCommonListResult = repairLogClient.queryRepairCommonList();
//            logInfo(logger,"查询常用描述语返回结果",showDetails(queryRepairCommonListResult));
//            if (!StringUtils.equals(queryRepairCommonListResult.getRespCode(), MonsterBasicRespCode.SUCCESS.getReturnCode())) {
//                logInfo(logger,"获取常用描述语失败");
//                throw new BusinessException(queryRepairCommonListResult.getRespCode(), queryRepairCommonListResult.getErrCode(), queryRepairCommonListResult.getErrDesc());
//            }
//            List<RepairCommonDto> repairCommonDtoList = queryRepairCommonListResult.getRepairCommonDtoList();
////            if (CollectionUtils.isNotEmpty(repairCommonDtoList)) {
////                for (RepairCommonDto repairCommonDto : repairCommonDtoList) {
////
////                }
////            }
//            return successVo(repairCommonDtoList);
//        }catch(BusinessException e){
//            logError(logger,e,"查询常用描述发生业务异常");
//            return errorVo(e);
//        }catch(Exception e){
//            logError(logger,e,"查询常用描述发生系统异常");
//            return errorVo(MonsterBasicRespCode.RESULT_FAILED.getReturnCode(),
//                    JiasvBasicRespCode.SYSTEM_EXCEPTION.getRespCode(), JiasvBasicRespCode.SYSTEM_EXCEPTION.getRespDesc());
//        }
//
//    }
//
//
//}