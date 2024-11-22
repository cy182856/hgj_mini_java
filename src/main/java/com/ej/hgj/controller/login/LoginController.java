package com.ej.hgj.controller.login;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.dao.bill.item.HgjBillItemDaoMapper;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.config.ProConfDaoMapper;
import com.ej.hgj.dao.cst.HgjCstDaoMapper;
import com.ej.hgj.dao.hu.CstIntoMapper;
import com.ej.hgj.dao.menu.mini.MenuMiniDaoMapper;
import com.ej.hgj.entity.bill.Bill;
import com.ej.hgj.entity.bill.BillItem;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.config.ProConfig;
import com.ej.hgj.entity.cst.HgjCst;
import com.ej.hgj.entity.hu.CstInto;
import com.ej.hgj.entity.menu.mini.MenuMini;
import com.ej.hgj.enums.*;
import com.ej.hgj.context.JiasvOperationContextHolder;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.entity.login.JiasvOperationContext;
import com.ej.hgj.entity.login.LoginInfo;
import com.ej.hgj.sy.dao.bill.BillDaoMapper;
import com.ej.hgj.sy.dao.bill.item.BillItemDaoMapper;
import com.ej.hgj.utils.CookieUtil;
import com.ej.hgj.utils.TokenUtils;
import com.ej.hgj.vo.bill.BillRequestVo;
import com.ej.hgj.vo.bill.BillResponseVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tty
 * @version 1.0 2020-08-17 10:43
 */
@Controller
public class LoginController extends BaseController {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ConstantConfDaoMapper constantConfDaoMapper;

    @Autowired
    private CstIntoMapper cstIntoMapper;

    @Autowired
    private MenuMiniDaoMapper menuMiniDaoMapper;

    @Autowired
    private HgjBillItemDaoMapper hgjBillItemDaoMapper;

    @Autowired
    private BillDaoMapper billDaoMapper;

    @Autowired
    private HgjCstDaoMapper hgjCstDaoMapper;

    @Autowired
    private ProConfDaoMapper proConfDaoMapper;


    @ResponseBody
    @RequestMapping("/doLogin")
    public LoginInfo login(ModelMap model, HttpServletRequest request,HttpServletResponse resp,@RequestBody LoginInfo loginInfo) {
        logger.info("---------------------------------开始登录-----------------------------------");
        String sessionId = request.getSession().getId();
        CookieUtil.removeCookie(resp, CookieUtil.getCookie(request, Constant.SESSION_KEY));
        CookieUtil.setCookie(resp, Constant.SESSION_KEY,sessionId, Constant.COOKIE_MAX_AGE);
        loginInfo.setSessionId(sessionId);
        JiasvOperationContext oc = new JiasvOperationContext();
        oc.setProNum(loginInfo.getProNum());
        oc.setCstCode(loginInfo.getCstCode());
        oc.setWxOpenId(loginInfo.getWxOpenId());
        JiasvOperationContextHolder.setJiasvOperationContext(oc);
        request.getSession().setAttribute(JiasvOperationContextHolder.jiasvOperationContextSessionKey, oc);

        // 查询客户是否欠费
        BigDecimal priRev = new BigDecimal("0");
        BillRequestVo billRequestVo = new BillRequestVo();
        billRequestVo.setProNum(loginInfo.getProNum());
        billRequestVo.setCstCode(loginInfo.getCstCode());
        billRequestVo.setWxOpenId(loginInfo.getWxOpenId());
        BillResponseVo billResponseVo = queryPriRev(billRequestVo);
        if(billResponseVo != null){
            priRev = billResponseVo.getPriRevAmount();
        }

        // 查询是否有入住审核
        CstInto cstIntoPram = new CstInto();
        cstIntoPram.setCstCode(loginInfo.getCstCode());
        cstIntoPram.setIntoStatus(Constant.INTO_STATUS_A);
        List<CstInto> cstIntoList = cstIntoMapper.getList(cstIntoPram);
        // 查询登录角色
        CstInto byWxOpenIdAndStatus_1 = cstIntoMapper.getByWxOpenIdAndStatus_1(loginInfo.getWxOpenId());
        // 判断客户是否已入住
        CstInto cstInto = new CstInto();
        cstInto.setWxOpenId(loginInfo.getWxOpenId());
        cstInto.setIntoStatus(Constant.INTO_STATUS_Y);
        List<CstInto> list = cstIntoMapper.getList(cstInto);
        if(list != null && list.size() > 0){
            loginInfo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
            //List<MenuMini> menuMinis = menuMiniDaoMapper.findMenuByCstCode(loginInfo.getCstCode());
            List<MenuMini> menuMinis = menuMiniDaoMapper.findMenuByProNumAndWxOpenId(loginInfo.getProNum(),loginInfo.getWxOpenId());
            if(!menuMinis.isEmpty()){
                for(MenuMini menuMini : menuMinis){
                    // 5-物业缴费
                    if((menuMini.getId() == 5 || menuMini.getId() == 8) && priRev.compareTo(BigDecimal.ZERO) > 0){
                        // 欠费菜单显示小红点
                        menuMini.setDot(true);
                    }
                    // 7-我的房屋
                    if(menuMini.getId() == 7 && !cstIntoList.isEmpty() && byWxOpenIdAndStatus_1 != null && (byWxOpenIdAndStatus_1.getIntoRole() == 0 || byWxOpenIdAndStatus_1.getIntoRole() == 2)){
                        // 有待审核的入住申请菜单显示小红点， 仅限于业主租户、产权人
                        menuMini.setDot(true);
                    }
                    // 19-租客管理
                    //if(menuMini.getId() == 19 && !cstIntoList.isEmpty() && byWxOpenIdAndStatus_1 != null && (byWxOpenIdAndStatus_1.getIntoRole() == 0 || byWxOpenIdAndStatus_1.getIntoRole() == 2)){
                        // 有待审核的入住申请菜单显示小红点， 仅限于业主租户、产权人
                        //menuMini.setDot(true);
                    //}
                }
            }
            loginInfo.setFunList(menuMinis);
        }else {
            loginInfo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
            loginInfo.setErrDesc("申请成功，请联系房主审批!");
        }

        // 首页我的 有待审核的入住申请菜单小红点提示 , 仅限于业主租户、产权人
        if(!cstIntoList.isEmpty() && byWxOpenIdAndStatus_1 != null && (byWxOpenIdAndStatus_1.getIntoRole() == 0 || byWxOpenIdAndStatus_1.getIntoRole() == 2)){
            loginInfo.setHomeDot(true);
        }

        //主页功能菜单
//        JSONArray jsonArray = new JSONArray();
//        for (MainModuleEnum moduleEnum : MainModuleEnum.values()) {
//            if(!list.isEmpty()){
//                if((moduleEnum.getIndex() == 1 && moduleEnum.getFunName().equals("报事报修"))
//                        || (moduleEnum.getIndex() == 2 && moduleEnum.getFunName().equals("我的账单"))
//                        || (moduleEnum.getIndex() == 2 && moduleEnum.getFunName().equals("我的报修"))
//                ){
//                    moduleEnum.setHasAuth(true);
//                }
//            }
//            JSONObject jsonObject = MainModuleEnum.setJsonObj(moduleEnum);
//            jsonArray.add(jsonObject);
//        }
        logger.info("---------------------------------登录成功-----------------------------------");
        String token = TokenUtils.getToken(loginInfo.getWxOpenId(), loginInfo.getWxOpenId());
        loginInfo.setToken(token);
        return loginInfo;
    }

    public BillResponseVo queryPriRev(BillRequestVo billRequestVo) {
        BillResponseVo billResponseVo = new BillResponseVo();
        billRequestVo.setPageNum(null);
        billRequestVo.setPageSize(null);
        String cstCode = billRequestVo.getCstCode();
        String proNum = billRequestVo.getProNum();
        HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(cstCode);
        billRequestVo.setCstId(hgjCst.getId());
        List<String> houseIdList = new ArrayList<>();
        // 查询租户入住的房屋
        CstInto cstInto = new CstInto();
        cstInto.setCstCode(cstCode);
        cstInto.setWxOpenId(billRequestVo.getWxOpenId());
        List<CstInto> cstIntos = cstIntoMapper.getList(cstInto);
        if(!cstIntos.isEmpty()){
            for (CstInto cst : cstIntos){
                if(StringUtils.isNotBlank(cst.getHouseId()) && cst.getIntoRole() == Constant.INTO_ROLE_ENTRUST && cst.getIntoStatus() == Constant.INTO_STATUS_Y){
                    houseIdList.add(cst.getHouseId());
                }
            }
        }
        billRequestVo.setHouseIdList(houseIdList);

        // 获取需要查询的费用类型列表,数据库配置
        List<String> billItemNameList = new ArrayList<>();
        List<BillItem> billItemNames = hgjBillItemDaoMapper.getList(proNum);
        for(BillItem billItem : billItemNames){
            billItemNameList.add(billItem.getItemName());
        }
        billRequestVo.setBillItemNameList(billItemNameList);

        // 获取项目名
        ProConfig proConfig = proConfDaoMapper.getByProjectNum(billRequestVo.getProNum());
        String proName = proConfig.getProjectName();
        billResponseVo.setProName(proName);
        Bill bill = billDaoMapper.priRevAmount(billRequestVo);
        BigDecimal priRev = new BigDecimal(0);
        if(bill != null){
            priRev = bill.getPriRev();
        }
        billResponseVo.setPriRevAmount(priRev);
        billResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
        billResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
        billResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
        return billResponseVo;
    }
}
