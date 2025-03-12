package com.ej.hgj.service.moncarren;

import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.dao.card.CardCstBatchDaoMapper;
import com.ej.hgj.dao.card.CardCstBillDaoMapper;
import com.ej.hgj.dao.carpay.ParkPayOrderDaoMapper;
import com.ej.hgj.dao.carpay.ParkPayOrderTempDaoMapper;
import com.ej.hgj.dao.carrenew.CarRenewOrderDaoMapper;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.moncarren.MonCarRenOrderDaoMapper;
import com.ej.hgj.entity.carrenew.CarRenewOrder;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.moncarren.MonCarRenOrder;
import com.ej.hgj.service.carrenew.CarRenewService;
import com.ej.hgj.utils.AESUtils;
import com.ej.hgj.utils.DateUtils;
import com.ej.hgj.utils.HttpClientUtil;
import com.ej.hgj.utils.bill.MyPrivatekey;
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
import java.security.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.Random;

@Service
@Transactional
public class MonCarRenServiceImpl implements MonCarRenService {

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

    @Autowired
    private MonCarRenOrderDaoMapper monCarRenOrderDaoMapper;

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
            //key = privateKey;
            key = privateKeyYy;
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
    public void updateStatusSuccess(MonCarRenOrder monCarRenOrder) {
        // 订单号
        String id = monCarRenOrder.getId();
        // 更新订单表支付状态为支付成功
        monCarRenOrder.setOrderStatus(Constant.ORDER_STATUS_SUCCESS);
        monCarRenOrderDaoMapper.update(monCarRenOrder);
        logger.info("--------------更新订单状态为支付成功----------订单号orderId:" + id);
        // 调用长期车续费接口
        ConstantConfig lftc_api_url = constantConfDaoMapper.getByKey(Constant.LFTC_API_URL);
        ConstantConfig lftc_api_key = constantConfDaoMapper.getByKey(Constant.LFTC_API_KEY);
        Key k = AESUtils.toKey(org.apache.commons.codec.binary.Base64.decodeBase64(lftc_api_key.getConfigValue()));
        // 支付成功时间格式化
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(monCarRenOrder.getSuccessTime(), formatter);
        String successTime = zonedDateTime.format(DateUtils.formatter_ymd_hms);
        String data = "{\"carCode\":\"" +
                monCarRenOrder.getCarCode() +
                "\",\"chargeMoney\":" +
                monCarRenOrder.getAmountTotal() +
                ",\"endTime\":\"" +
                monCarRenOrder.getRenewEndTime() +
                "\",\"payTime\":\"" +
                successTime +
                "\",\"chargeType\":11,\"amountType\":1,\"chargeSource\":\"3\"}";
        try {
            byte[] encryptData = AESUtils.encrypt(data.getBytes("utf-8"), k);
            String encryptStr = AESUtils.parseByte2HexStr(encryptData);
            String resultData = HttpClientUtil.sendPostParking(lftc_api_url.getConfigValue() + "/RenewalLongUser", encryptStr);
            byte[] decryptData = AESUtils.decrypt(AESUtils.parseHexStr2Byte(resultData), k);
            String strData = new String(decryptData, "utf-8");
            JSONObject jsonResult = JSONObject.parseObject(strData);
            String resCode = jsonResult.getString("resCode");
            String resMsg = jsonResult.getString("resMsg");
            logger.info("------------长期车续费接口返回----------resCode:" + resCode + "|resMsg:" + resMsg);
            // 更新订单表月租车充值回调状态
            MonCarRenOrder monCarRenOrderPram = new MonCarRenOrder();
            monCarRenOrderPram.setId(id);
            monCarRenOrderPram.setCallBackCode(resCode);
            monCarRenOrderPram.setCallBackMsg(resMsg);
            monCarRenOrderPram.setUpdateTime(new Date());
            monCarRenOrderDaoMapper.updateCallBackCode(monCarRenOrderPram);
            logger.info("--------更新月租车充值（按时间段）状态成功--------");
        }catch (Exception e){
            e.printStackTrace();
            logger.info("------长期车续费失败----------");
        }
    }

    @Override
    public void updateStatusFail(MonCarRenOrder monCarRenOrder) {
        // 支付失败更新订单状态
        monCarRenOrder.setOrderStatus(Constant.ORDER_STATUS_FAIL);
        monCarRenOrderDaoMapper.update(monCarRenOrder);
    }

    @Override
    public void updateStatusPro(MonCarRenOrder monCarRenOrder_0) {
        // 更新支付记录
        monCarRenOrder_0.setOrderStatus(Constant.ORDER_STATUS_PRO);
        monCarRenOrder_0.setUpdateTime(new Date());
        monCarRenOrderDaoMapper.update(monCarRenOrder_0);
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
