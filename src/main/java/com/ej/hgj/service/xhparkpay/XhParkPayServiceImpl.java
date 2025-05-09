package com.ej.hgj.service.xhparkpay;

import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.dao.card.CardCstBatchDaoMapper;
import com.ej.hgj.dao.card.CardCstBillDaoMapper;
import com.ej.hgj.dao.carpay.ParkPayOrderDaoMapper;
import com.ej.hgj.dao.carpay.ParkPayOrderTempDaoMapper;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.xhparkpay.XhParkCouponLogDaoMapper;
import com.ej.hgj.entity.card.CardCstBatch;
import com.ej.hgj.entity.card.CardCstBill;
import com.ej.hgj.entity.carpay.ParkPayOrder;
import com.ej.hgj.entity.carpay.ParkPayOrderTemp;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.xhparkpay.XhParkCouponLog;
import com.ej.hgj.service.carpay.CarPayService;
import com.ej.hgj.utils.DateUtils;
import com.ej.hgj.utils.HttpClientUtil;
import com.ej.hgj.utils.bill.MyPrivatekey;
import com.ej.hgj.utils.bill.TimestampGenerator;
import com.ej.hgj.vo.bill.SignInfoVo;
import com.ej.hgj.vo.carpay.CarPayRequestVo;
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
public class XhParkPayServiceImpl implements XhParkPayService {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ConstantConfDaoMapper constantConfDaoMapper;

    @Autowired
    private CardCstBatchDaoMapper cardCstBatchDaoMapper;

    @Autowired
    private CardCstBillDaoMapper cardCstBillDaoMapper;

    @Autowired
    private XhParkCouponLogDaoMapper xhParkCouponLogDaoMapper;

    @Override
    public void carNoCouponSuccess(CarPayRequestVo carPayRequestVo) {
        Integer hourNumValue = carPayRequestVo.getHourNumValue();
        // 查询客户停车卡信息
        CardCstBatch cardCstBatch = cardCstBatchDaoMapper.getById(carPayRequestVo.getCardCstBatchId());
        if(cardCstBatch != null) {
            // 新增车牌优惠记录
            XhParkCouponLog xhParkCouponLog = new XhParkCouponLog();
            xhParkCouponLog.setId(TimestampGenerator.generateSerialNumber());
            xhParkCouponLog.setProNum(carPayRequestVo.getProNum());
            xhParkCouponLog.setCouponNo(carPayRequestVo.getCouponNo());
            xhParkCouponLog.setCardCstBatchId(carPayRequestVo.getCardCstBatchId());
            xhParkCouponLog.setCarCode(carPayRequestVo.getCarCode());
            xhParkCouponLog.setWxOpenId(carPayRequestVo.getWxOpenId());
            xhParkCouponLog.setCstCode(carPayRequestVo.getCstCode());
            xhParkCouponLog.setDeductionNum(hourNumValue);
            xhParkCouponLog.setCreateTime(new Date());
            xhParkCouponLog.setUpdateTime(new Date());
            xhParkCouponLog.setDeleteFlag(Constant.DELETE_FLAG_NOT);
            xhParkCouponLogDaoMapper.save(xhParkCouponLog);
            logger.info("车牌优惠，新增优惠记录成功:" + JSONObject.toJSONString(xhParkCouponLog));
            // 抵扣时长
            cardCstBatch.setApplyNum(cardCstBatch.getApplyNum() + hourNumValue);
            // 更新停车卡数量
            CardCstBatch cstBatchPram = new CardCstBatch();
            cstBatchPram.setId(cardCstBatch.getId());
            cstBatchPram.setApplyNum(cardCstBatch.getApplyNum());
            cstBatchPram.setUpdateTime(new Date());
            cardCstBatchDaoMapper.update(cstBatchPram);
            logger.info("车牌优惠，更新停车卡数量成功:" + JSONObject.toJSONString(cstBatchPram));
            // 新增卡账单扣减记录
            CardCstBill cardCstBillInsert = new CardCstBill();
            cardCstBillInsert.setId(TimestampGenerator.generateSerialNumber());
            cardCstBillInsert.setCardCstBatchId(cardCstBatch.getId());
            cardCstBillInsert.setProNum(cardCstBatch.getProNum());
            cardCstBillInsert.setCardType(cardCstBatch.getCardType());
            cardCstBillInsert.setCardId(cardCstBatch.getCardId());
            cardCstBillInsert.setCardCode(cardCstBatch.getCardCode());
            cardCstBillInsert.setCstCode(cardCstBatch.getCstCode());
            cardCstBillInsert.setBillNum(-hourNumValue);
            cardCstBillInsert.setBillType(2);
            cardCstBillInsert.setWxOpenId(carPayRequestVo.getWxOpenId());
            cardCstBillInsert.setCreateTime(new Date());
            cardCstBillInsert.setCreateBy("");
            cardCstBillInsert.setUpdateTime(new Date());
            cardCstBillInsert.setUpdateBy("");
            cardCstBillInsert.setDeleteFlag(Constant.DELETE_FLAG_NOT);
            cardCstBillDaoMapper.save(cardCstBillInsert);
            logger.info("车牌优惠，停车卡账单插入成功:" + JSONObject.toJSONString(cardCstBillInsert));
        }

    }

}
