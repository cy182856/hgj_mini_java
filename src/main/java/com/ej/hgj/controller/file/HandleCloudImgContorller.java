package com.ej.hgj.controller.file;

import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.vo.file.CloudImgVo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 云图片存储和获取
 * @author tty
 * @version 1.0 2020-10-16 10:53
 */
@Controller
public class HandleCloudImgContorller extends BaseController {
    private final String uploadPath = "D:/nginx/nginx-1.24.0/html/upload";
//    @Autowired
//    private CloudUploadManager cloudUploadManager;
//    @Autowired
//    private RepairUploadFileMapper repairUploadFileMapper;

    @ResponseBody
    @RequestMapping("/uploadFile")
    public JSONObject uploadFile(HttpServletRequest request, @RequestParam("file") MultipartFile file, CloudImgVo cloudImgVo) {
//        logInfo(logger,"开始文件上传,入口参数:",showDetails(cloudImgVo));
//        String custId = getCustId(request);
//        if(StringUtils.isBlank(custId)){
//            custId = cloudImgVo.getCustId();
//        }
//        String fileType = cloudImgVo.getFileType();
//        String busiId = cloudImgVo.getBusiId();
//        String imgId = cloudImgVo.getImgId();
//        String busiDate = cloudImgVo.getBusiDate();
//        String busiSeqId = cloudImgVo.getBusiSeqId();
//        JSONObject jsonObject = new JSONObject();
//        try {
//            if (isBlank(fileType, custId, busiId, imgId)) {
//                logInfo(logger, "获取上传文件的文件名失败,关键信息缺失", fileType, custId, busiId, imgId);
//                throw new BusinessException(MonsterBasicRespCode.REQ_DATA_NULL.getReturnCode(),
//                        JiamsvBasicRespCode.DATA_NULL.getRespCode(), JiamsvBasicRespCode.DATA_NULL.getRespCode());
//            }
//            if (StringUtils.equals(fileType, TencentCloudFileTypeEnum.LOG.getFileType())) {
//                if (isBlank(busiDate, busiSeqId)) {
//                    logInfo(logger, "获取上传文件名失败,日志类数据,业务日期和业务序列号不能为空", busiDate, busiSeqId);
//                    throw new BusinessException(MonsterBasicRespCode.REQ_DATA_NULL.getReturnCode(),
//                            JiamsvBasicRespCode.DATA_NULL.getRespCode(), JiamsvBasicRespCode.DATA_NULL.getRespCode());
//                }
//                if (!ImgBusiIdEnum.checkMatchBusi(busiId)) {
//                    logInfo(logger, "还未配置该图片业务类型,上传失败");
//                    throw new BusinessException(MonsterBasicRespCode.INFO_NOT_EXIST.getReturnCode(),
//                        JiamsvBasicRespCode.FINAL_DATA_NOT_EXIST.getRespCode(), JiamsvBasicRespCode.FINAL_DATA_NOT_EXIST.getRespCode());
//                }
//            }else{
//                if(null==FileInfoImageBusiIdEnum.getMatched(busiId)){
//                    logInfo(logger, "文件信息类图片上传时,还未配置该图片业务类型,上传失败");
//                    throw new BusinessException(MonsterBasicRespCode.INFO_NOT_EXIST.getReturnCode(),
//                        JiamsvBasicRespCode.FINAL_DATA_NOT_EXIST.getRespCode(), JiamsvBasicRespCode.FINAL_DATA_NOT_EXIST.getRespCode());
//                }
//            }
//
//            // 保存图片到附件表
//            uploadFile(file,busiDate,busiSeqId,custId);
//            cloudUploadManager.uploadFile(custId, busiId, imgId, busiDate, busiSeqId, fileType, file.getInputStream());
//            jsonObject.put(JiamsvConstants.RESP_CODE, MonsterBasicRespCode.SUCCESS.getReturnCode());
//            jsonObject.put(JiamsvConstants.ERR_CODE, JiamsvBasicRespCode.SUCCESS.getRespCode());
//            jsonObject.put(JiamsvConstants.ERR_DESC, JiamsvBasicRespCode.SUCCESS.getRespDesc());
//        } catch (BusinessException businessException) {
//            jsonObject.put(JiamsvConstants.RESP_CODE, businessException.getRespCode());
//            jsonObject.put(JiamsvConstants.ERR_CODE, businessException.getErrCode());
//            jsonObject.put(JiamsvConstants.ERR_DESC, businessException.getErrCode());
//        } catch (Exception e) {
//            jsonObject.put(JiamsvConstants.RESP_CODE, MonsterBasicRespCode.SYSTEM_ERROR.getReturnCode());
//            jsonObject.put(JiamsvConstants.ERR_CODE, JiamsvBasicRespCode.SYSTEM_EXCEPTION.getRespCode());
//            jsonObject.put(JiamsvConstants.ERR_DESC, JiamsvBasicRespCode.SYSTEM_EXCEPTION.getRespDesc());
//        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("RESPCODE", "000");
        jsonObject.put("RESPCODE", "0000");
        jsonObject.put("RESPCODE", "0000");
        return jsonObject;
    }

    public void uploadFile(MultipartFile file, String busiDate, String busiSeqId, String custId) {
        DateFormat dateFormatYmd = new SimpleDateFormat("yyyyMMdd");
        DateFormat dateFormatYmdhms = new SimpleDateFormat("yyyyMMddHHmmss");
        if(file != null){
            //目录不存在则直接创建
            File filePath = new File(uploadPath);
            if(!filePath.exists()){
                filePath.mkdirs();
            }
            //创建年月日文件夹
            //Calendar date = Calendar.getInstance();
            //File ymdFile = new File(uploadPath + File.separator + date.get(Calendar.YEAR) + (date.get(Calendar.MONTH)+1) + date.get(Calendar.DAY_OF_MONTH));
            File ymdFile = new File(uploadPath + File.separator + dateFormatYmd.format(new Date()));
            //目录不存在则直接创建
            if(!ymdFile.exists()){
                ymdFile.mkdirs();
            }
            String uploadPath = ymdFile.getPath();
            //获取文件名
            String fileName = file.getOriginalFilename();
            String strDate = dateFormatYmdhms.format(new Date());
            String newFileName = strDate + "_" + fileName;
            String savePath = uploadPath +"/"+ newFileName;
            try {
                file.transferTo(new File(savePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //保存文件
//            RepairUploadFileDO repairUploadFileDO = new RepairUploadFileDO();
//            repairUploadFileDO.setRepairDate(busiDate);
//            repairUploadFileDO.setRepairSeqId(busiSeqId);
//            repairUploadFileDO.setCustId(custId);
//            repairUploadFileDO.setFileName(newFileName);
//            repairUploadFileDO.setFilePath(dateFormatYmd.format(new Date()) + "/" + newFileName);
//            repairUploadFileMapper.insertUploadFile(repairUploadFileDO);

        }
    }
}