package com.ej.hgj.task.payment.service;

import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.dao.bill.BillMergeDaoMapper;
import com.ej.hgj.dao.bill.BillMergeDetailDaoMapper;
import com.ej.hgj.dao.bill.PaymentRecordDaoMapper;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.cst.HgjCstDaoMapper;
import com.ej.hgj.dao.hu.CstIntoMapper;
import com.ej.hgj.entity.bill.Bill;
import com.ej.hgj.entity.bill.BillMerge;
import com.ej.hgj.entity.bill.BillMergeDetail;
import com.ej.hgj.entity.bill.PaymentRecord;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.cst.HgjCst;
import com.ej.hgj.entity.hu.CstInto;
import com.ej.hgj.service.bill.BillService;
import com.ej.hgj.sy.dao.bill.BillDaoMapper;
import com.ej.hgj.utils.DateUtils;
import com.ej.hgj.utils.bill.HttpRequestUtils;
import com.ej.hgj.utils.bill.RandomStringUtils;
import com.ej.hgj.vo.bill.BillRequestVo;
import com.ej.hgj.vo.bill.RequestPaymentStatusVo;
import com.ej.hgj.vo.bill.SignInfoVo;
import okhttp3.HttpUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class PaymentTaskService {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private PaymentRecordDaoMapper paymentRecordDaoMapper;

    @Autowired
    private BillService billService;

    @Autowired
    private HgjCstDaoMapper hgjCstDaoMapper;

    @Autowired
    private BillDaoMapper billDaoMapper;

    @Autowired
    private ConstantConfDaoMapper constantConfDaoMapper;

    @Autowired
    private CstIntoMapper cstIntoMapper;

    @Autowired
    private BillMergeDaoMapper billMergeDaoMapper;

    @Autowired
    private BillMergeDetailDaoMapper billMergeDetailDaoMapper;

    // 支付中订单报错才可以改为失败,因为预支付订单会返回订单未支付,这时候并未真的支付,所以不能改为支付失败
    public void updatePaymentStatus_1() {
        logger.info("----------------------支付中订单定时任务处理开始--------------------------- ");
        // 查询前十分钟支付中订单
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -10);
        Date beForHourDate = calendar.getTime();
        String beForTenMin = DateUtils.sdfYmdHms.format(beForHourDate);
        logger.info("beForTenMin:" + beForTenMin);
        List<PaymentRecord> paymentRecordList = paymentRecordDaoMapper.getOrderList_1(beForTenMin);
        if(!paymentRecordList.isEmpty()){
            for(PaymentRecord paymentRecord : paymentRecordList){
                String proNum = paymentRecord.getProNum();
                // 订单号
                String outTradeNo = paymentRecord.getOrderNo();
                // 根据订单号查询微信接口获取状态
                String result = null;
                try {
                    ConstantConfig miniProgramApp = constantConfDaoMapper.getByKey(Constant.MINI_PROGRAM_APP_EJ_ZHSQ);
                    // 设置签名对象
                    SignInfoVo signInfo = new SignInfoVo();
                    // 微信小程序智慧管家appId
                    signInfo.setAppId(miniProgramApp.getAppId());
                    // 时间戳
                    signInfo.setTimeStamp(String.valueOf(System.currentTimeMillis()/1000));
                    // 随机串
                    signInfo.setNonceStr(RandomStringUtils.getRandomStringByLength(32));
                    // 签名方式
                    signInfo.setSignType("RSA");
                    String spMchId = "";
                    String subMchId = "";
                    String mchId = "";
                    if("10000".equals(proNum)){
                        // 服务商户号-宜悦
                        ConstantConfig spMchIdCon = constantConfDaoMapper.getByProNumAndKey(paymentRecord.getProNum(), Constant.SP_MCH_ID_YY);
                        spMchId = spMchIdCon.getConfigValue();
                        // 子服务商户号-东方渔人码头
                        ConstantConfig subMchIdCon = constantConfDaoMapper.getByProNumAndKey(paymentRecord.getProNum(), Constant.SUB_MCH_ID);
                        subMchId = subMchIdCon.getConfigValue();
                        signInfo.setSpMchId(spMchId);
                        // 子服务商户号
                        signInfo.setSubMchId(subMchId);
//                    }else if("10001".equals(proNum)){
//                        // 直连商户号
//                        ConstantConfig mchIdCon = constantConfDaoMapper.getByProNumAndKey(paymentRecord.getProNum(), Constant.MCH_ID);
//                        mchId = mchIdCon.getConfigValue();
//                        signInfo.setMchId(mchId);
//                    }
                    }else if("10001".equals(proNum)){
                        // 服务商户号-宜悦
                        ConstantConfig spMchIdCon = constantConfDaoMapper.getByProNumAndKey(paymentRecord.getProNum(), Constant.SP_MCH_ID_YY);
                        spMchId = spMchIdCon.getConfigValue();
                        // 子服务商户号-凡享
                        ConstantConfig subMchIdCon = constantConfDaoMapper.getByProNumAndKey(paymentRecord.getProNum(), Constant.SUB_MCH_ID_FX);
                        subMchId = subMchIdCon.getConfigValue();
                        signInfo.setSpMchId(spMchId);
                        // 子服务商户号
                        signInfo.setSubMchId(subMchId);
                    }
                    // 证书序列号 10000-东方渔人码头 10001-凡享
                    ConstantConfig serialNo = constantConfDaoMapper.getByProNumAndKey(paymentRecord.getProNum(), Constant.SERIAL_NO);
                    signInfo.setSerialNo(serialNo.getConfigValue());
                    String url = "";
//                    if("10000".equals(proNum)){
//                        url = Constant.QUERY_ORDER_URL + "/" + outTradeNo + "?sp_mchid=" + spMchId + "&sub_mchid=" + subMchId + "";
//                    }else if("10001".equals(proNum)){
//                        url = Constant.QUERY_ORDER_URL_BUS + "/" + outTradeNo + "?mchid=" + mchId + "";
//                    }
                    url = Constant.QUERY_ORDER_URL + "/" + outTradeNo + "?sp_mchid=" + spMchId + "&sub_mchid=" + subMchId + "";

                    HttpUrl httpurl = HttpUrl.parse(url);
                    // 获取证书token
                    String authorization = billService.getToken("GET", httpurl, "", signInfo, proNum);
                    authorization =  "WECHATPAY2-SHA256-RSA2048" + " " + authorization;

                    result = HttpRequestUtils.get(url, authorization);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                JSONObject jsonObject = JSONObject.parseObject(result);
                // 微信支付订单号
                String transactionId = jsonObject.getString("transaction_id");
                // 交易类型
                String tradeType = jsonObject.getString("trade_type");
                // 交易状态
                String tradeState = jsonObject.getString("trade_state");
                // 交易状态描述
                String tradeStateDesc = jsonObject.getString("trade_state_desc");
                // 付款银行
                String bankType = jsonObject.getString("bank_type");
                // 支付完成时间
                String successTime = jsonObject.getString("success_time");
                paymentRecord.setTransactionId(transactionId);
                paymentRecord.setTradeType(tradeType);
                paymentRecord.setTradeState(tradeState);
                paymentRecord.setTradeStateDesc(tradeStateDesc);
                paymentRecord.setBankType(bankType);
                paymentRecord.setSuccessTime(successTime);
                paymentRecord.setUpdateTime(new Date());
                if("SUCCESS".equals(tradeState)){
                    billService.updateStatusSuccessNotSy(paymentRecord);
                }else {
                    billService.updateStatusFail(paymentRecord);
                }
            }
        }

        // 小订单支付成功后同步已付金额
        List<Integer> billStatusList_2 = new ArrayList<>();
        billStatusList_2.add(Constant.BILL_STATUS_SUCCESS);
        BillMergeDetail billMergeDetail = new BillMergeDetail();
        billMergeDetail.setBillStatusList(billStatusList_2);
        billMergeDetail.setPriPaid(new BigDecimal("0"));
        List<BillMergeDetail> billMergeDetailList = billMergeDetailDaoMapper.getList(billMergeDetail);
        if(!billMergeDetailList.isEmpty()){
            for(BillMergeDetail b : billMergeDetailList){
               b.setPriPaid(b.getPriRev());
               b.setUpdateTime(new Date());
               billMergeDetailDaoMapper.update(b);
            }
        }

        logger.info("----------------------支付中订单定时任务处理结束--------------------------- ");
    }

    public void updatePaymentStatus_5() {
        logger.info("----------------------预支付或已生成大订单超时未支付任务处理开始------------------------------ ");
        // 查询大订单表待支付记录
        BillRequestVo billRequestVo_0 = new BillRequestVo();
        List<Integer> billStatusList = new ArrayList<>();
        billStatusList.add(Constant.BILL_STATUS_WAIT);
        billRequestVo_0.setBillStatusList(billStatusList);
        List<BillMerge> billMergeList = billMergeDaoMapper.getList(billRequestVo_0);
        if(!billMergeList.isEmpty()){
            for(BillMerge billMerge : billMergeList){
                // 查询10分钟内未支付订单 小于10分钟为true 超出10分钟为false
                try {
                    boolean b = DateUtils.compareDate(billMerge.getCreateTime(), 10);
                    if(b == false){
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
                        if(!paymentRecordList.isEmpty()){
                            for(PaymentRecord paymentRecord : paymentRecordList){
                                PaymentRecord p = new PaymentRecord();
                                p.setId(paymentRecord.getId());
                                p.setPaymentStatus(Constant.PAYMENT_STATUS_TIMEOUT);
                                p.setUpdateTime(new Date());
                                paymentRecordDaoMapper.update(p);
                            }
                        }
                    }
                }catch (Exception e){
                    e.getMessage();
                }
            }
        }

        logger.info("----------------------预支付或已生成大订单超时未支付任务处理结束------------------------------ ");
    }

    public void PaymentOrderSync_2() {
        logger.info("----------------------智慧管家支付成功订单与思源订单同步定时任务处理开始------------------------------ ");
        // 查询已支付成功并且未与思源同步或者同步失败的订单
//        List<Integer> billStatusList_2 = new ArrayList<>();
//        List<Integer> syPayStatusList_02 = new ArrayList<>();
//        billStatusList_2.add(Constant.BILL_STATUS_SUCCESS);
//        syPayStatusList_02.add(Constant.SY_ORDER_SYNC_NOT);
//        syPayStatusList_02.add(Constant.SY_ORDER_SYNC_FAIL);
//        BillMergeDetail billMergeDetail = new BillMergeDetail();
//        billMergeDetail.setBillStatusList(billStatusList_2);
//        billMergeDetail.setSyPayStatusList(syPayStatusList_02);
//        List<BillMergeDetail> billMergeDetailList = billMergeDetailDaoMapper.getList(billMergeDetail);

        // 查询前一小时支付成功思源未同步的数据
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        Date beForHourDate = calendar.getTime();
        String formatDate = DateUtils.sdfYmdHms.format(beForHourDate);
        logger.info("formatDate:" + formatDate);
        List<BillMergeDetail> billMergeDetailList = billMergeDetailDaoMapper.getBillByStatus(formatDate);
        if(!billMergeDetailList.isEmpty()){
            for(BillMergeDetail b : billMergeDetailList){
//                try {
//                    // 睡眠3秒
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                Integer intTotalAmount = b.getPriRev().multiply(new BigDecimal("100")).intValue();
                billService.updateSyOrderStatus(b.getId(), b.getWxOpenId(), b.getBillId(), intTotalAmount.toString(),
                        b.getProNum(),b.getOrderNo(), b.getPriRev().toString(), "小程序");
                }
        }
        logger.info("----------------------智慧管家支付成功订单与思源订单同步定时任务处理结束------------------------------ ");
    }

}
