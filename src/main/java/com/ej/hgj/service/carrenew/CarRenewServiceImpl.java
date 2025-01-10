package com.ej.hgj.service.carrenew;

import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.dao.card.CardCstBatchDaoMapper;
import com.ej.hgj.dao.card.CardCstBillDaoMapper;
import com.ej.hgj.dao.carpay.ParkPayOrderDaoMapper;
import com.ej.hgj.dao.carpay.ParkPayOrderTempDaoMapper;
import com.ej.hgj.dao.carrenew.CarRenewOrderDaoMapper;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.entity.card.CardCstBatch;
import com.ej.hgj.entity.card.CardCstBill;
import com.ej.hgj.entity.carpay.ParkPayOrder;
import com.ej.hgj.entity.carpay.ParkPayOrderTemp;
import com.ej.hgj.entity.carrenew.CarRenewOrder;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.service.carpay.CarPayService;
import com.ej.hgj.utils.DateUtils;
import com.ej.hgj.utils.HttpClientUtil;
import com.ej.hgj.utils.bill.MyPrivatekey;
import com.ej.hgj.utils.bill.TimestampGenerator;
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Date;
import java.util.Random;

@Service
@Transactional
public class CarRenewServiceImpl implements CarRenewService {

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
    private CarRenewOrderDaoMapper carRenewOrderDaoMapper;

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
    public void updateStatusSuccess(CarRenewOrder carRenewOrder) {
        // 订单号
        String id = carRenewOrder.getId();
        // 更新订单表支付状态为支付成功
        carRenewOrder.setOrderStatus(Constant.ORDER_STATUS_SUCCESS);
        carRenewOrderDaoMapper.update(carRenewOrder);
        logger.info("--------------更新订单状态为支付成功----------订单号orderId:" + id);
        // 调用月租车充值（按时间段）接口
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
        double carRenewRandomNumber = Math.round(randomNumber * 1e9) / 1e9;
        String start = "{";
        String appid = "\"appid\":\"" + appId + "\",";
        String parkKey = "\"parkKey\":\"" + authCode + "\",";
        String carRenewRand = "\"rand\":\"" + carRenewRandomNumber + "\",";
        String mthChargeMoney = "\"mthChargeMoney\":\"" + carRenewOrder.getPayAmount() + "\",";
        String beginTimeDate = "\"beginTime\":\"" + carRenewOrder.getRenewBeginTime() + "\",";
        String endTimeData = "\"endTime\":\"" + carRenewOrder.getRenewEndTime() + "\",";
        String isUpdateBeginTime = "\"isUpdateBeginTime\":\"" + 1 + "\",";
        String carNoData = "\"carNo\":\"" + carRenewOrder.getCarCode() + "\",";
        String version = "\"version\":\"v1.0\"";
        String end = "}";
        String carRenewSignData = "appid=" + appId + "&beginTime=" + carRenewOrder.getRenewBeginTime() + "&carNo=" + carRenewOrder.getCarCode() + "&endTime=" + carRenewOrder.getRenewEndTime() + "&isUpdateBeginTime=" + 1 + "&mthChargeMoney=" + carRenewOrder.getPayAmount() + "&parkKey=" + authCode + "&rand=" + carRenewRandomNumber + "&version=v1.0&";
        String carRenewSignTemp = carRenewSignData + appSecret;
        String carRenewSign = DigestUtils.md5Hex(carRenewSignTemp).toUpperCase();
        String carRenewSignJson = ",\"sign\":\"" + carRenewSign + "\"";
        String carRenewPramJson = start + appid + carNoData + mthChargeMoney + beginTimeDate + endTimeData + isUpdateBeginTime + parkKey  + carRenewRand + version + carRenewSignJson + end;
        JSONObject carRenewResultJson = HttpClientUtil.sendPost(apiUrl + "/Inform/ChargeMonthCar", carRenewPramJson);
        String carRenewCode = carRenewResultJson.get("code").toString();
        String carRenewMsg = carRenewResultJson.getString("msg");
        logger.info("------------月租车充值（按时间段）接口返回----------carRenewCode:" + carRenewCode +"|carRenewMsg:" + carRenewMsg);
        // 更新订单表月租车充值回调状态
        CarRenewOrder carRenewOrderPram = new CarRenewOrder();
        carRenewOrderPram.setId(id);
        carRenewOrderPram.setCallBackCode(carRenewCode);
        carRenewOrderPram.setCallBackMsg(carRenewMsg);
        carRenewOrderPram.setUpdateTime(new Date());
        carRenewOrderDaoMapper.updateCallBackCode(carRenewOrderPram);
        logger.info("--------更新月租车充值（按时间段）状态成功--------");
    }

    @Override
    public void updateStatusFail(CarRenewOrder carRenewOrder) {
        // 支付失败更新订单状态
        carRenewOrder.setOrderStatus(Constant.ORDER_STATUS_FAIL);
        carRenewOrderDaoMapper.update(carRenewOrder);
    }

    @Override
    public void updateStatusPro(CarRenewOrder carRenewOrder_0) {
        // 更新支付记录
        carRenewOrder_0.setOrderStatus(Constant.ORDER_STATUS_PRO);
        carRenewOrder_0.setUpdateTime(new Date());
        carRenewOrderDaoMapper.update(carRenewOrder_0);
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
