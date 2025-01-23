package com.ej.hgj.service.carpay;

import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.dao.bill.BillMergeDaoMapper;
import com.ej.hgj.dao.bill.BillMergeDetailDaoMapper;
import com.ej.hgj.dao.bill.PaymentRecordDaoMapper;
import com.ej.hgj.dao.card.CardCstBatchDaoMapper;
import com.ej.hgj.dao.card.CardCstBillDaoMapper;
import com.ej.hgj.dao.carpay.ParkPayOrderDaoMapper;
import com.ej.hgj.dao.carpay.ParkPayOrderTempDaoMapper;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.hu.CstIntoMapper;
import com.ej.hgj.entity.bill.Bill;
import com.ej.hgj.entity.bill.BillMerge;
import com.ej.hgj.entity.bill.BillMergeDetail;
import com.ej.hgj.entity.bill.PaymentRecord;
import com.ej.hgj.entity.card.CardCstBatch;
import com.ej.hgj.entity.card.CardCstBill;
import com.ej.hgj.entity.carpay.ParkPayOrder;
import com.ej.hgj.entity.carpay.ParkPayOrderTemp;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.service.bill.BillService;
import com.ej.hgj.sy.dao.bill.BillDaoMapper;
import com.ej.hgj.utils.DateUtils;
import com.ej.hgj.utils.HttpClientUtil;
import com.ej.hgj.utils.SyPostClient;
import com.ej.hgj.utils.bill.MyPrivatekey;
import com.ej.hgj.utils.bill.TimestampGenerator;
import com.ej.hgj.vo.bill.BillRequestVo;
import com.ej.hgj.vo.bill.RequestPaymentStatusVo;
import com.ej.hgj.vo.bill.SignInfoVo;
import okhttp3.HttpUrl;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CarPayServiceImpl implements CarPayService {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${private.key}")
    private String privateKey;

    @Value("${private.key.yy}")
    private String privateKeyYy;

    @Autowired
    private ParkPayOrderDaoMapper parkPayOrderDaoMapper;

    @Autowired
    private ParkPayOrderTempDaoMapper parkPayOrderTempDaoMapper;

    @Autowired
    private ConstantConfDaoMapper constantConfDaoMapper;

    @Autowired
    private CardCstBatchDaoMapper cardCstBatchDaoMapper;

    @Autowired
    private CardCstBillDaoMapper cardCstBillDaoMapper;

    @Override
    public String getToken(String method, HttpUrl url, String body, SignInfoVo signInfoVo, String proNum) {
        String nonceStr = signInfoVo.getNonceStr();
        long timestamp = Long.valueOf(signInfoVo.getTimeStamp());
        String message = buildMessage(method, url, timestamp, nonceStr, body);
        String signature = null;
        try {
            signature = sign(message.getBytes("utf-8"), proNum);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String mchId = "";

        mchId = signInfoVo.getSpMchId();

        String serialNo = signInfoVo.getSerialNo();
        return "mchid=\"" + mchId + "\","
                + "serial_no=\"" + serialNo + "\","
                + "nonce_str=\"" + nonceStr + "\","
                + "timestamp=\"" + timestamp + "\","
                + "signature=\"" + signature + "\"";
    }

    String sign(byte[] message, String proNum) throws NoSuchAlgorithmException, SignatureException {
        Signature sign = Signature.getInstance("SHA256withRSA");
        String key = "";
        if("10000".equals(proNum)){
            key = privateKey;
        }else if("10001".equals(proNum)){
            key = privateKeyYy;
        }
        try {
            sign.initSign(MyPrivatekey.getPrivateKey(key));
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sign.update(message);

        return Base64.getEncoder().encodeToString(sign.sign());
    }

    String buildMessage(String method, HttpUrl url, long timestamp, String nonceStr, String body) {
        String canonicalUrl = url.encodedPath();
        if (url.encodedQuery() != null) {
            canonicalUrl += "?" + url.encodedQuery();
        }
        return method + "\n"
                + canonicalUrl + "\n"
                + timestamp + "\n"
                + nonceStr + "\n"
                + body + "\n";
    }

    @Override
    public void updateStatusSuccess(ParkPayOrder parkPayOrder) {
        // 订单号
        String id = parkPayOrder.getId();
        // 更新订单表支付状态为支付成功
        parkPayOrder.setOrderStatus(Constant.ORDER_STATUS_SUCCESS);
        parkPayOrderDaoMapper.update(parkPayOrder);
        logger.info("--------------更新订单状态为支付成功----------订单号orderId:" + id);
        // 调用停车场支付回调接口
        ParkPayOrderTemp parkPayOrderTemp = parkPayOrderTempDaoMapper.getParkPayOrderTemp(id);
        ConstantConfig zhtc_api_url = constantConfDaoMapper.getByKey(Constant.ZHTC_API_URL);
        String apiUrl = zhtc_api_url.getConfigValue();
        ConstantConfig zhtc_api_app_id = constantConfDaoMapper.getByKey(Constant.ZHTC_API_APP_ID);
        String appId = zhtc_api_app_id.getConfigValue();
        ConstantConfig zhtc_api_app_secret = constantConfDaoMapper.getByKey(Constant.ZHTC_API_APP_SECRET);
        String appSecret = zhtc_api_app_secret.getConfigValue();
        ConstantConfig zhtc_api_auth_code = constantConfDaoMapper.getByKey(Constant.ZHTC_API_AUTH_CODE);
        String authCode = zhtc_api_auth_code.getConfigValue();
        Random random = new Random();
        // 生成一个9位小数的随机数
        double randomNumber = random.nextDouble();
        // 保留9位小数
        randomNumber = Math.round(randomNumber * 1e9) / 1e9;
        String start = "{";
        String appid = "\"appid\":\"" + appId + "\",";
        String parkKey = "\"parkKey\":\"" + authCode + "\",";
        String payedMoney = "\"payedMoney\":\"" + parkPayOrder.getPayAmount() + "\",";
        String payedSN = "\"payedSN\":\"" + id + "\",";
        String payOrderNo = "\"payOrderNo\":\"" + parkPayOrderTemp.getPayOrderNo() + "\",";
        String rand = "\"rand\":\"" + randomNumber + "\",";
        String version = "\"version\":\"v1.0\"";
        String end = "}";
        String signData = "appid=" + appId + "&parkKey=" + authCode + "&payedMoney=" + parkPayOrder.getPayAmount() + "&payedSN=" + id + "&payOrderNo=" + parkPayOrderTemp.getPayOrderNo() + "&rand=" + randomNumber + "&version=v1.0&";
        String stringSignTemp = signData + appSecret;
        String sign = DigestUtils.md5Hex(stringSignTemp).toUpperCase();
        String signJson = ",\"sign\":\"" + sign + "\"";
        String pramJson = start + appid + parkKey + payedMoney + payedSN + payOrderNo + rand + version + signJson + end;
        JSONObject resultJson = HttpClientUtil.sendPost(apiUrl + "/Inform/PayNotify", pramJson);
        String code = resultJson.get("code").toString();
        String msg = resultJson.getString("msg");
        logger.info("------------临停车支付回调接口返回----------code:" + code +"|msg:" + msg);
        // 更新临停车回调状态
        ParkPayOrderTemp orderTemp = parkPayOrderTempDaoMapper.getParkPayOrderTemp(id);
        ParkPayOrderTemp parkPayOrderTempPram = new ParkPayOrderTemp();
        parkPayOrderTempPram.setId(orderTemp.getId());
        parkPayOrderTempPram.setPayCallBackCode(code);
        parkPayOrderTempPram.setPayCallBackMsg(msg);
        parkPayOrderTempPram.setUpdateTime(new Date());
        parkPayOrderTempDaoMapper.update(parkPayOrderTempPram);
        logger.info("--------更新临停车支付状态成功--------");
        // 1-表示客户缴费时使用了停车卡抵扣
        if(parkPayOrder.getIsDeduction() == 1 && "1".equals(code)){
           // 查询客户停车卡信息
            CardCstBatch cardCstBatch = cardCstBatchDaoMapper.getById(parkPayOrder.getCardCstBatchId());
            if(cardCstBatch != null) {
                // 抵扣时长
                Integer deductionNum = parkPayOrder.getDeductionNum();
                // 计算停车卡剩余小时
//            Integer expNum = cardCstBatch.getTotalNum() - cardCstBatch.getApplyNum();
                // 计算客户停车分钟
//            String inTime = parkPayOrder.getInTime();
//            String outTime = parkPayOrder.getOutTime();
//            Date inDate = DateUtils.strDate(inTime);
//            Date outDate = DateUtils.strDate(outTime);
//            long[] distanceTimes = DateUtils.getDistanceTimes(inDate, outDate);
//            Long hour = distanceTimes[0] * 24  + distanceTimes[1];
//            Long minute = distanceTimes[2];
//            if(minute > 0){
//                hour = hour + 1;
//            }
                // 停车卡剩余时长扣除
                //Integer subNum = expNum - hour.intValue();
                // 本次扣减小时
//            Integer hourNum = 0;
//            if(subNum > 0){
//                cardCstBatch.setApplyNum(cardCstBatch.getApplyNum() + hour.intValue());
//                hourNum = hour.intValue();
//            }else {
//                cardCstBatch.setApplyNum(cardCstBatch.getApplyNum() + expNum);
//                hourNum = expNum;
//            }
                cardCstBatch.setApplyNum(cardCstBatch.getApplyNum() + deductionNum);
                if (cardCstBatch.getApplyNum() > 0) {
                    // 更新停车卡数量
                    CardCstBatch cstBatchPram = new CardCstBatch();
                    cstBatchPram.setId(cardCstBatch.getId());
                    cstBatchPram.setApplyNum(cardCstBatch.getApplyNum());
                    cstBatchPram.setUpdateTime(new Date());
                    cardCstBatchDaoMapper.update(cstBatchPram);
                    logger.info("-------------更新停车卡数量成功----------");
                    // 新增卡账单扣减记录
                    CardCstBill cardCstBillInsert = new CardCstBill();
                    cardCstBillInsert.setId(TimestampGenerator.generateSerialNumber());
                    cardCstBillInsert.setCardCstBatchId(cardCstBatch.getId());
                    cardCstBillInsert.setProNum(cardCstBatch.getProNum());
                    cardCstBillInsert.setCardType(cardCstBatch.getCardType());
                    cardCstBillInsert.setCardId(cardCstBatch.getCardId());
                    cardCstBillInsert.setCardCode(cardCstBatch.getCardCode());
                    cardCstBillInsert.setCstCode(cardCstBatch.getCstCode());
                    //cardCstBillInsert.setBillNum(-hourNum);
                    cardCstBillInsert.setBillNum(-deductionNum);
                    cardCstBillInsert.setBillType(2);
                    cardCstBillInsert.setWxOpenId(parkPayOrder.getWxOpenId());
                    cardCstBillInsert.setCreateTime(new Date());
                    cardCstBillInsert.setCreateBy("");
                    cardCstBillInsert.setUpdateTime(new Date());
                    cardCstBillInsert.setUpdateBy("");
                    cardCstBillInsert.setDeleteFlag(Constant.DELETE_FLAG_NOT);
                    cardCstBillDaoMapper.save(cardCstBillInsert);
                    logger.info("支付成功回调，停车卡账单插入成功:" + JSONObject.toJSONString(cardCstBillInsert));
                }
            }
        }

    }

    @Override
    public void updateStatusFail(ParkPayOrder parkPayOrder) {
        // 支付失败更新订单状态
        parkPayOrder.setOrderStatus(Constant.ORDER_STATUS_FAIL);
        parkPayOrderDaoMapper.update(parkPayOrder);
    }

    @Override
    public void updateStatusPro(ParkPayOrder parkPayOrder_0) {
        // 更新支付记录
        parkPayOrder_0.setOrderStatus(Constant.ORDER_STATUS_PRO);
        parkPayOrder_0.setUpdateTime(new Date());
        parkPayOrderDaoMapper.update(parkPayOrder_0);
    }



    public static void main(String[] args) {
        String s = "2024-12-24 15:40:52";
        String e = "2024-12-24 18:45:42";
        Date sDate = DateUtils.strDate(s);
        Date eDate = DateUtils.strDate(e);
        long[] distanceTimes = DateUtils.getDistanceTimes(sDate, eDate);
       // System.out.println(distanceTimes[0]+"天"+distanceTimes[1]+"小时"+distanceTimes[2]+"分"+distanceTimes[3]+"秒");
        System.out.println(130/60);
    }
}
