package com.ej.hgj.task.carpay.service;

import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.dao.bill.BillMergeDaoMapper;
import com.ej.hgj.dao.bill.BillMergeDetailDaoMapper;
import com.ej.hgj.dao.bill.PaymentRecordDaoMapper;
import com.ej.hgj.dao.carpay.ParkPayOrderDaoMapper;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.cst.HgjCstDaoMapper;
import com.ej.hgj.dao.hu.CstIntoMapper;
import com.ej.hgj.entity.bill.BillMerge;
import com.ej.hgj.entity.bill.BillMergeDetail;
import com.ej.hgj.entity.bill.PaymentRecord;
import com.ej.hgj.entity.carpay.ParkPayOrder;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.service.bill.BillService;
import com.ej.hgj.service.carpay.CarPayService;
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
public class CarPayTaskService {

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

    @Autowired
    private ParkPayOrderDaoMapper parkPayOrderDaoMapper;

    @Autowired
    private CarPayService carPayService;

    // 支付中订单报错才可以改为失败,因为预支付订单会返回订单未支付,这时候并未真的支付,所以不能改为支付失败
    public void updateOrderStatus_1() {
        logger.info("----------------------停车缴费支付中订单定时任务处理开始--------------------------- ");
        // 查询5分钟前支付中订单
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -5);
        Date beForHourDate = calendar.getTime();
        String beForTenMin = DateUtils.sdfYmdHms.format(beForHourDate);
        logger.info("beForTenMin:" + beForTenMin);
        List<ParkPayOrder> parkPayOrderList = parkPayOrderDaoMapper.getOrderList_1(beForTenMin);
        if(!parkPayOrderList.isEmpty()){
            for(ParkPayOrder parkPayOrder : parkPayOrderList){
                String proNum = parkPayOrder.getProNum();
                // 订单号
                String outTradeNo = parkPayOrder.getId();
                // 根据订单号查询微信接口获取状态
                String result = null;
                try {
                    ConstantConfig subMiniProgramApp = constantConfDaoMapper.getByKey(Constant.MINI_PROGRAM_APP_EJ_ZHSQ);
                    // 设置签名对象
                    SignInfoVo signInfo = new SignInfoVo();
                    // 微信小程序智慧管家appId
                    signInfo.setSubAppId(subMiniProgramApp.getAppId());
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
                        ConstantConfig spMchIdCon = constantConfDaoMapper.getByProNumAndKey(parkPayOrder.getProNum(), Constant.SP_MCH_ID_YY);
                        spMchId = spMchIdCon.getConfigValue();
                        // 子服务商户号-东方渔人码头
                        ConstantConfig subMchIdCon = constantConfDaoMapper.getByProNumAndKey(parkPayOrder.getProNum(), Constant.SUB_MCH_ID);
                        subMchId = subMchIdCon.getConfigValue();
                        signInfo.setSpMchId(spMchId);
                        // 子服务商户号
                        signInfo.setSubMchId(subMchId);
                    }else if("10001".equals(proNum)){
                        // 服务商户号-宜悦
                        ConstantConfig spMchIdCon = constantConfDaoMapper.getByKey(Constant.SP_MCH_ID_YY);
                        spMchId = spMchIdCon.getConfigValue();
                        // 子服务商户号-凡享
                        ConstantConfig subMchIdCon = constantConfDaoMapper.getByProNumAndKey(parkPayOrder.getProNum(), Constant.SUB_MCH_ID_FX);
                        subMchId = subMchIdCon.getConfigValue();
                        signInfo.setSpMchId(spMchId);
                        // 子服务商户号
                        signInfo.setSubMchId(subMchId);
                    }
                    // 证书序列号 10000-东方渔人码头 10001-凡享
                    ConstantConfig serialNo = constantConfDaoMapper.getByKey(Constant.SERIAL_NO_YY);
                    signInfo.setSerialNo(serialNo.getConfigValue());
                    String url = "";
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
                parkPayOrder.setTransactionId(transactionId);
                parkPayOrder.setTradeType(tradeType);
                parkPayOrder.setTradeState(tradeState);
                parkPayOrder.setTradeStateDesc(tradeStateDesc);
                parkPayOrder.setBankType(bankType);
                parkPayOrder.setSuccessTime(successTime);
                parkPayOrder.setUpdateTime(new Date());
                if("SUCCESS".equals(tradeState)){
                    carPayService.updateStatusSuccess(parkPayOrder);
                }else {
                    carPayService.updateStatusFail(parkPayOrder);
                }
            }
        }
        logger.info("----------------------停车缴费支付中订单定时任务处理结束--------------------------- ");
    }


}
