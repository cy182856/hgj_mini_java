package com.ej.hgj.service.bill;

import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.dao.bill.BillMergeDaoMapper;
import com.ej.hgj.dao.bill.BillMergeDetailDaoMapper;
import com.ej.hgj.dao.bill.PaymentRecordDaoMapper;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.hu.CstIntoMapper;
import com.ej.hgj.entity.bill.Bill;
import com.ej.hgj.entity.bill.BillMerge;
import com.ej.hgj.entity.bill.BillMergeDetail;
import com.ej.hgj.entity.bill.PaymentRecord;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.hu.CstInto;
import com.ej.hgj.enums.JiasvBasicRespCode;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.sy.dao.bill.BillDaoMapper;
import com.ej.hgj.utils.DateUtils;
import com.ej.hgj.utils.SyPostClient;
import com.ej.hgj.utils.bill.MyPrivatekey;
import com.ej.hgj.utils.bill.TimestampGenerator;
import com.ej.hgj.vo.bill.BillRequestVo;
import com.ej.hgj.vo.bill.RequestPaymentStatusVo;
import com.ej.hgj.vo.bill.SignInfoVo;
import okhttp3.HttpUrl;
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
public class BillServiceImpl implements BillService {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${private.key}")
    private String privateKey;

    @Value("${private.key.yy}")
    private String privateKeyYy;

    @Value("${private.key.fx}")
    private String privateKeyFx;

    @Value("${private.key.bus}")
    private String privateKeyBus;

    @Autowired
    private ConstantConfDaoMapper constantConfDaoMapper;

    @Autowired
    private PaymentRecordDaoMapper paymentRecordDaoMapper;

    @Autowired
    private BillMergeDaoMapper billMergeDaoMapper;

    @Autowired
    private BillMergeDetailDaoMapper billMergeDetailDaoMapper;

    @Autowired
    private CstIntoMapper cstIntoMapper;

    @Autowired
    private BillDaoMapper billDaoMapper;

    @Override
    public void updateSyOrderStatus(String id, String openId, String outTradeNo, String fee,
                                    String orgId, String fnRevId, String payMoney,
                                    String fillProName) {
        // 获取思源接口地址
        ConstantConfig constantConfig = constantConfDaoMapper.getByKey("sy_url");
        String p7 = initReturnVisit(openId, outTradeNo, fee, orgId, fnRevId , payMoney, fillProName);
        // 获取请求结果, 调用思源接口-微信缴费
        JSONObject jsonObject = SyPostClient.appWeChatPayFee("AppWeChat_PayFee", p7, null, "SSSySWIN*(SK_WH()", constantConfig.getConfigValue());
        // 1-成功 0-失败
        String status = jsonObject.getString("status");
        String msg = jsonObject.getString("msg");
        // 更新思源接口返回信息
        BillMergeDetail billMergeDetail = new BillMergeDetail();
        billMergeDetail.setId(id);
        billMergeDetail.setUpdateTime(new Date());
        if("1".equals(status)){
            billMergeDetail.setSyPayStatus(Constant.SY_ORDER_SYNC_SUCCESS);
        }else if("0".equals(status)){
            billMergeDetail.setSyPayStatus(Constant.SY_ORDER_SYNC_FAIL);
        }
        billMergeDetail.setSyPayMsg(msg);
        billMergeDetailDaoMapper.update(billMergeDetail);
        logger.info("思源订单状态同步成功! ID:"+ id + " 订单号:" + outTradeNo);
    }

    @Override
    public void updateStatusSuccess(PaymentRecord paymentRecord) {
        // 大订单号
        String orderNo = paymentRecord.getOrderNo();
        // 更新支付记录表支付状态为支付成功
        paymentRecord.setPaymentStatus(Constant.PAYMENT_STATUS_SUCCESS);
        paymentRecordDaoMapper.update(paymentRecord);
        // 更新大订单支付状态为支付成功并写入已付金额
        BillMerge billMerge = new BillMerge();
        billMerge.setId(orderNo);
        billMerge.setBillStatus(Constant.BILL_STATUS_SUCCESS);
        billMerge.setPriPaid(paymentRecord.getPriRev());
        billMerge.setUpdateTime(new Date());
        billMergeDaoMapper.update(billMerge);
        // 更新小订单支付状态为支付成功
        BillMergeDetail billMergeDetail = new BillMergeDetail();
        billMergeDetail.setBillId(orderNo);
        billMergeDetail.setBillStatus(Constant.BILL_STATUS_SUCCESS);
        billMergeDetail.setUpdateTime(new Date());
        billMergeDetailDaoMapper.updateByBillId(billMergeDetail);
        logger.info("微信支付回调成功,更新思源订单状态");
        // 查询小订单列表
        BillMergeDetail billMd = new BillMergeDetail();
        billMd.setBillId(orderNo);
        List<Integer> billStatusList = new ArrayList<>();
        billStatusList.add(Constant.BILL_STATUS_WAIT);
        billStatusList.add(Constant.BILL_STATUS_PRO);
        billStatusList.add(Constant.BILL_STATUS_SUCCESS);
        billMd.setBillStatusList(billStatusList);
        List<BillMergeDetail> billMergeDetailList = billMergeDetailDaoMapper.getList(billMd);
        if(!billMergeDetailList.isEmpty()){
            for(BillMergeDetail b : billMergeDetailList){
                // 睡眠1000毫秒（即1秒）
//                try {
//                    // 睡眠500毫秒
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                Integer intTotalAmount = b.getPriRev().multiply(new BigDecimal("100")).intValue();
                updateSyOrderStatus(b.getId(), b.getWxOpenId(), b.getBillId(), intTotalAmount.toString(),
                        paymentRecord.getProNum(),b.getOrderNo(), b.getPriRev().toString(),
                        "小程序");
            }
        }
    }

    @Override
    public void updateStatusSuccessNotSy(PaymentRecord paymentRecord) {
        // 大订单号
        String orderNo = paymentRecord.getOrderNo();
        // 更新支付记录表支付状态为支付成功
        paymentRecord.setPaymentStatus(Constant.PAYMENT_STATUS_SUCCESS);
        paymentRecordDaoMapper.update(paymentRecord);
        // 更新大订单支付状态为支付成功并写入已付金额
        BillMerge billMerge = new BillMerge();
        billMerge.setId(orderNo);
        billMerge.setBillStatus(Constant.BILL_STATUS_SUCCESS);
        billMerge.setPriPaid(paymentRecord.getPriRev());
        billMerge.setUpdateTime(new Date());
        billMergeDaoMapper.update(billMerge);
        // 更新小订单支付状态为支付成功
        BillMergeDetail billMergeDetail = new BillMergeDetail();
        billMergeDetail.setBillId(orderNo);
        billMergeDetail.setBillStatus(Constant.BILL_STATUS_SUCCESS);
        billMergeDetail.setUpdateTime(new Date());
        billMergeDetailDaoMapper.updateByBillId(billMergeDetail);
    }

    @Override
    public void updateStatusFail(PaymentRecord paymentRecord) {
        // 大订单号
        String orderNo = paymentRecord.getOrderNo();
        // 支付失败更新付款记录状态
        paymentRecord.setPaymentStatus(Constant.PAYMENT_STATUS_FAIL);
        paymentRecordDaoMapper.update(paymentRecord);
        // 支付失败更新大订单状态
        BillMerge billMerge = new BillMerge();
        billMerge.setId(orderNo);
        billMerge.setBillStatus(Constant.BILL_STATUS_FAIL);
        billMerge.setUpdateTime(new Date());
        billMergeDaoMapper.update(billMerge);
        // 支付失败更新小订单状态
        BillMergeDetail billMergeDetail = new BillMergeDetail();
        billMergeDetail.setBillId(orderNo);
        billMergeDetail.setBillStatus(Constant.BILL_STATUS_FAIL);
        billMergeDetail.setUpdateTime(new Date());
        billMergeDetailDaoMapper.updateByBillId(billMergeDetail);

    }

    @Override
    public void billMerge(String proNum, String wxOpenId, String cstCode,
                          BigDecimal totalAmount,String[] orderIds) {
        // 保存合并的账单
        String billId = TimestampGenerator.generateSerialNumber();
        BillMerge billMerge = new BillMerge();
        billMerge.setId(billId);
        billMerge.setProNum(proNum);
        billMerge.setWxOpenId(wxOpenId);
        billMerge.setCstCode(cstCode);
        billMerge.setPriRev(totalAmount);
        billMerge.setPriPaid(new BigDecimal(0));
        billMerge.setIpItemName(billId);
        billMerge.setBillStatus(Constant.BILL_STATUS_WAIT);
        billMerge.setCreateTime(new Date());
        billMerge.setUpdateTime(new Date());
        billMerge.setDeleteFlag(Constant.DELETE_FLAG_NOT);
        billMergeDaoMapper.save(billMerge);

        // 保存合并的账单明细
        BillRequestVo billRequest = new BillRequestVo();
        List<String> ids = Arrays.stream(orderIds).collect(Collectors.toList());
        billRequest.setBillIds(ids);
        billRequest.setPageNum(null);
        billRequest.setPageSize(null);
        List<Bill> billList = billDaoMapper.getOrderByIds(billRequest);
        List<BillMergeDetail> billMergeDetailList = new ArrayList<>();
        if(!billList.isEmpty()){
            for(Bill bill : billList){
                BillMergeDetail billMergeDetail = new BillMergeDetail();
                billMergeDetail.setId(TimestampGenerator.generateSerialNumber());
                billMergeDetail.setBillId(billId);
                billMergeDetail.setRepYears(bill.getRepYears());
                billMergeDetail.setOrderNo(bill.getId());
                billMergeDetail.setProNum(proNum);
                billMergeDetail.setWxOpenId(wxOpenId);
                billMergeDetail.setCstCode(cstCode);
                billMergeDetail.setPriRev(bill.getPriRev());
                billMergeDetail.setPriPaid(bill.getPriPaid());
                billMergeDetail.setIpItemName(bill.getIpItemName());
                billMergeDetail.setBillStatus(Constant.BILL_STATUS_WAIT);
                billMergeDetail.setSyPayStatus(Constant.SY_ORDER_SYNC_NOT);
                billMergeDetail.setCreateTime(new Date());
                billMergeDetail.setUpdateTime(new Date());
                billMergeDetail.setDeleteFlag(Constant.DELETE_FLAG_NOT);
                billMergeDetailList.add(billMergeDetail);
            }
            billMergeDetailDaoMapper.insertList(billMergeDetailList);
        }
    }

    @Override
    public void updateStatusTimeOut(PaymentRecord paymentRecord_0) {
        //  超出两个小时，重新下单并且修改原有预支付订单状态为 5-超时未支付
        PaymentRecord paymentRecord_5 = new PaymentRecord();
        paymentRecord_5.setId(paymentRecord_0.getId());
        paymentRecord_5.setPaymentStatus(Constant.PAYMENT_STATUS_TIMEOUT);
        paymentRecord_5.setUpdateTime(new Date());
        paymentRecordDaoMapper.update(paymentRecord_5);
        // 修改大订单为超时状态
        BillMerge billMerge_5 = new BillMerge();
        billMerge_5.setId(paymentRecord_0.getOrderNo());
        billMerge_5.setBillStatus(Constant.BILL_STATUS_TIMEOUT);
        billMerge_5.setUpdateTime(new Date());
        billMergeDaoMapper.update(billMerge_5);
        // 修改小订单为超时状态
        BillMergeDetail billMergeDetail_5 = new BillMergeDetail();
        billMergeDetail_5.setBillId(paymentRecord_0.getOrderNo());
        billMergeDetail_5.setBillStatus(Constant.BILL_STATUS_TIMEOUT);
        billMergeDetail_5.setUpdateTime(new Date());
        billMergeDetailDaoMapper.updateByBillId(billMergeDetail_5);
    }

    @Override
    public void updateStatusPro(PaymentRecord paymentRecord_0) {
        // 更新支付记录
        paymentRecord_0.setPaymentStatus(Constant.PAYMENT_STATUS_PRO);
        paymentRecord_0.setUpdateTime(new Date());
        paymentRecordDaoMapper.updatePayRecord(paymentRecord_0);
        // 更新大订单状态
        BillMerge billMerge = billMergeDaoMapper.getById(paymentRecord_0.getOrderNo());
        if(billMerge != null && Constant.BILL_STATUS_WAIT == billMerge.getBillStatus()){
            BillMerge bm = new BillMerge();
            bm.setId(paymentRecord_0.getOrderNo());
            bm.setBillStatus(Constant.BILL_STATUS_PRO);
            bm.setUpdateTime(new Date());
            billMergeDaoMapper.updateBillMergeStatus(bm);
            // 更新小订单状态
            BillMergeDetail billMergeDetail = new BillMergeDetail();
            billMergeDetail.setBillId(paymentRecord_0.getOrderNo());
            billMergeDetail.setBillStatus(Constant.BILL_STATUS_PRO);
            billMergeDetail.setUpdateTime(new Date());
            billMergeDetailDaoMapper.updateMerDetailStatus(billMergeDetail);
        }
    }

    @Override
    public void updateStatusCancel(String orderNo) {
        // 取消订单更新支付记录状态
        List<Integer> paymentStatusList_0 = new ArrayList<>();
        paymentStatusList_0.add(Constant.PAYMENT_STATUS_PRE);
        RequestPaymentStatusVo requestPaymentStatusVo_0 = new RequestPaymentStatusVo();
        requestPaymentStatusVo_0.setOrderNo(orderNo);
        requestPaymentStatusVo_0.setPaymentStatusList(paymentStatusList_0);
        PaymentRecord paymentRecord = paymentRecordDaoMapper.getPaymentRecord(requestPaymentStatusVo_0);
        if(paymentRecord != null){
            paymentRecord.setPaymentStatus(Constant.PAYMENT_STATUS_CANCEL);
            paymentRecord.setUpdateTime(new Date());
            paymentRecordDaoMapper.update(paymentRecord);
        }
        // 取消订单更新大订单状态
        BillMerge billMerge = new BillMerge();
        billMerge.setId(orderNo);
        billMerge.setBillStatus(Constant.BILL_STATUS_CANCEL);
        billMerge.setUpdateTime(new Date());
        billMergeDaoMapper.update(billMerge);
        // 取消订单更新小订单状态
        BillMergeDetail billMergeDetail = new BillMergeDetail();
        billMergeDetail.setBillId(orderNo);
        billMergeDetail.setBillStatus(Constant.BILL_STATUS_CANCEL);
        billMergeDetail.setUpdateTime(new Date());
        billMergeDetailDaoMapper.updateByBillId(billMergeDetail);
    }

    @Override
    public Boolean updateStatusTimeOut(BillMerge billMerge) {
        try {
            // 查询10分钟内未支付订单 小于10分钟true 超出1小时为false
            boolean b = DateUtils.compareDate(billMerge.getCreateTime(), 10);
            if (b == false) {
                // 超出10分钟，修改状态为5-超时未支付
                BillMerge bm = new BillMerge();
                bm.setId(billMerge.getId());
                bm.setBillStatus(Constant.BILL_STATUS_TIMEOUT);
                bm.setUpdateTime(new Date());
                billMergeDaoMapper.update(bm);
                // 更新小订单为超时
                BillMergeDetail billMergeDetail = new BillMergeDetail();
                billMergeDetail.setBillId(billMerge.getId());
                billMergeDetail.setBillStatus(Constant.BILL_STATUS_TIMEOUT);
                billMergeDetailDaoMapper.updateByBillId(billMergeDetail);
                // 查询支付记录预下单成功订单
                List<Integer> paymentStatusList_0 = new ArrayList<>();
                paymentStatusList_0.add(Constant.PAYMENT_STATUS_PRE);
                RequestPaymentStatusVo requestPaymentStatusVo_0 = new RequestPaymentStatusVo();
                requestPaymentStatusVo_0.setOrderNo(billMerge.getId());
                requestPaymentStatusVo_0.setPaymentStatusList(paymentStatusList_0);
                List<PaymentRecord> paymentRecordList = paymentRecordDaoMapper.getPaymentRecordList(requestPaymentStatusVo_0);
                if (!paymentRecordList.isEmpty()) {
                    for (PaymentRecord paymentRecord : paymentRecordList) {
                        PaymentRecord p = new PaymentRecord();
                        p.setId(paymentRecord.getId());
                        p.setPaymentStatus(Constant.PAYMENT_STATUS_TIMEOUT);
                        p.setUpdateTime(new Date());
                        paymentRecordDaoMapper.update(p);
                    }
                }
            }
            return b;
        } catch (Exception e) {
            e.getMessage();
        }

        return null;
    }

    // 组装参数-思源缴费
    public String initReturnVisit(String openId, String outTradeNo, String fee, String orgId, String fnRevId, String payMoney, String fillProName){
            String OpenID = "{ \"OpenID\":\"" + openId + "\",";
            String OutTradeNo = "\"OutTradeNo\":\"" + outTradeNo + "\",";
            String Fee = "\"Fee\":\"" + fee + "\",";
            String List = "\"List\":[{ \"OrgID\":\"" + orgId +"\",\"FnRevID\":\"" + fnRevId + "\",\"PayMoney\":" + payMoney + ",\"FillProName\":\"" + fillProName +"\"}]}";
            return OpenID + OutTradeNo + Fee + List;
    }

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
//        if("10000".equals(proNum)){
//            mchId = signInfoVo.getSpMchId();
//        }else if("10001".equals(proNum)){
//            mchId = signInfoVo.getMchId();
//        }
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
            //key = privateKeyBus;
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
}
