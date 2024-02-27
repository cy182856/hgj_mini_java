package com.ej.hgj.enums;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public enum MainModuleEnum {
    //主页
    QR(1,14,false,"业主码", "toFun","/images/home/icon_kaimen.png","/pages/smartDoor/smartDoor",false,1,false),
    REPAIR(2,15,false,"报事报修","toFun","/images/home/icon_baoxiu.png","/subpages/repair/pages/repairApply/repairApply",false,1,false),
    RESERVE(3,1,false,"预约","toFun","/images/home/icon_yuyue.png","/subpages/appt/show_apptObjInfo",false,1,false),
    SHOP(4,6,false,"店铺收款","toFun","/images/home/icon_shoukuan.png","/pages/await/await",false,1,false),
    HEO(5,3,false,"邻里圈","toFun","/images/home/icon_bangzhu.png","/subpages/heo/heoinfo/heoinfo",false,1,false),
    TOPN(6,15,false,"排行榜","toFun","/images/home/icon_paihangbang.png","/subpages/repair/pages/ranking/ranking",false,1,false),
    QPAPER(7,4,false,"问卷调查","toFun","/images/home/icon_diaocha.png","/subpages/qn/queryQnInfo/queryQnInfo",false,1,false),
    PEFF(8,2,false,"物业费缴纳","toFun","/images/home/icon_wuyefei.png","/subpages/pfee/pfeeMonBillSubmit",false,1,false),
    VISIT(9,7,false,"访客管理","toFun","/images/home/icon_fangke.png","/subpages/visit/visitInfo/visitInfo",false,1,false),
    CONVSER(10,17,false,"便民信息","toFun","/images/home/icon_bianming.png","/pages/wuyeInfo/wuyeInfo",false,1,false),
    BULLETIN(11,999,false,"公告","toFun","/images/home/icon_gonggao.png","/subpages/gonggao/query/queryGGListPage",false,1,false),
    SMART(12,5,false,"水电煤抄表", "toFun","/images/home/icon_shuidianmei.png","/pages/await/await",false,1,false)

    ,FILEPUB(13,13,false,"文件公示","toFun","/images/home/icon_fm.png","/subpages/filepub/filepub",false,1,false)
    ,CARFEE(14,12,false,"车辆缴费","toFun","/images/home/icon_car.png","/subpages/carfee/carfee",false,1,false)
    ,CHARGE(15,18,false,"充电桩","toFun","/images/home/icon_charge.png","/subpages/charge/charge",false,1,false)
    ,DOOR(16,19,false,"远程开门","toFun","/images/home/icon_opendoor.png","/subpages/door/myDoor/myDoor",false,1,false)
    //我的
    ,ORD(1,999,false,"我的付款","toFun","/images/mine/icon_dingdang.png","/subpages/order/order",false,2,false)
    ,MYREPAIR(2,15,false,"我的报修","toFun","/images/mine/icon_baoxiu.png","/subpages/repair/pages/repairQuery/repairQuery",false,2,false)
    ,MYYUYUE(3,1,false,"我的预约","toFun","/images/mine/icon_yuyue.png","../../subpages/appt/mine_appt",false,2,false)
    ,MYWENJUAN(4,4,false,"我的问卷","toFun","/images/mine/icon_wenjuan.png","/subpages/qn/queryQnInfo/queryQnInfo?submitted=Y",false,2,false)
    ,MYFABU(5,3,false,"我的发布","toFun","/images/mine/icon_fabu.png","/subpages/heo/mineRelease/mineRelease?type=release",false,2,false)
    ,CHENGYUAN(6,999,false, "成员管理","onTapHouse","/images/mine/icon_fangwu.png","../../subpages/family/memberList",false,2,false)
    ,ZHANGHU(7,999,false,"个人资料","house","/images/mine/icon_zhanghu.png","/subpages/family/memberDetail?PageType=S&canUnRegist=Y",false,2,false)
    ,MYACCT(8,999,false,"充值账户","toFun","/images/mine/icon_charge.png","/subpages/acct/myAcctInfo",false,2,false)
    ,ADVICE(9,10,false,"问题反馈","advice","/images/mine/icon_advice.png","",false,2,false)
    ,BILL(10,999,false,"我的账单","toFun","/images/mine/icon_dingdang.png","/subpages/bill/bill",false,2,false)

    ;

    /**
     * 对于我的页面的子功能,如果也需要和主页中的父功能一样,需要权限控制,需要在这里配置
     * @return
     */
    public static Map<Integer, MainModuleEnum> getParentOrSon(){
        Map<Integer, MainModuleEnum> map = new HashMap<Integer, MainModuleEnum>();
        // 位图,子功能
        map.put(1, MainModuleEnum.MYYUYUE);
        map.put(3, MainModuleEnum.MYFABU);
        map.put(4, MainModuleEnum.MYWENJUAN);
        map.put(10, MainModuleEnum.ADVICE);
        map.put(15, MainModuleEnum.MYREPAIR);
        return map;
    }

//    public static Map<Integer,QpadEnum> getQpad(){
//        Map<Integer, QpadEnum> map = new HashMap<Integer, QpadEnum>();
////        map.put(10,QpadEnum);
//    }

    /**
     * 功能顺序
     */
    private Integer ordId;
    /**
     * 库表中的位图位置 0-代表还不知道位图的位置
     */
    private int bit;
    /**
     * 是否有该功能权限,默认true-有功能权限
     */
    private boolean hasAuth;

    /**
     * 功能名
     */
    private String funName;
    /**
     * 绑定的点击事件
     */
    private String bindEvent;

    public String getBindEvent() {
        return bindEvent;
    }

    public void setBindEvent(String bindEvent) {
        this.bindEvent = bindEvent;
    }

    /**
     * logo的地址
     */
    private String logoPath;
    /**
     * 功能跳转的页
     */
    private String pagePath;
    /**
     * 是否需要进行权限功能的校验,true需要
     */
    private Boolean checkAuth;
    /**
     * 功能显示的地址 1-主页 2-我的
     */
    private int index;
    /**
     * 是否存在共享位图,默认不存在
     */
    private Boolean shareBit;

    public Boolean getShareBit() {
        return shareBit;
    }

    public void setShareBit(Boolean shareBit) {
        this.shareBit = shareBit;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Boolean getCheckAuth() {
        return checkAuth;
    }

    public void setCheckAuth(Boolean checkAuth) {
        this.checkAuth = checkAuth;
    }

    /**
     *
     * @param ordId 功能ID
     * @param bit   位图
     * @param hasAuth   是否有权限
     * @param funName   功能名
     * @param logoPath  logo的前台路径
     * @param pagePath  跳转页
     * @param checkAuth 是否校验权限,针对p状态
     * @param shareBit 是否存在共享位图,默认false
     */
    MainModuleEnum(Integer ordId, int bit, boolean hasAuth, String funName, String bindEvent, String logoPath, String pagePath, Boolean checkAuth, int index,Boolean shareBit) {
        this.ordId = ordId;
        this.bit = bit;
        this.hasAuth = hasAuth;
        this.funName = funName;
        this.bindEvent = bindEvent;
        this.logoPath = logoPath;
        this.pagePath = pagePath;
        this.checkAuth = checkAuth;
        this.index = index;
        this.shareBit = shareBit;
    }

    public Integer getOrdId() {
        return ordId;
    }

    public void setOrdId(Integer ordId) {
        this.ordId = ordId;
    }

    public String getFunName() {
        return funName;
    }

    public void setFunName(String funName) {
        this.funName = funName;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public String getPagePath() {
        return pagePath;
    }

    public void setPagePath(String pagePath) {
        this.pagePath = pagePath;
    }

    public int getBit() {
        return bit;
    }

    public void setBit(int bit) {
        this.bit = bit;
    }

    public boolean isHasAuth() {
        return hasAuth;
    }

    public void setHasAuth(boolean hasAuth) {
        this.hasAuth = hasAuth;
    }

    /**
     * 给功能设置权限(目前只有主页的功能需要权限校验),我的页面需要在枚举中配置
     * @param bit 位图位置
     * @param authValue 0-无权限 ,1-有权限,位图字符数据
     * @param isHasAcct 是否有账户，对于小程序充值账户菜单显示，有账号才可以显示
     */
    private static void setAuth(int bit, char authValue, String huStat, String propType, JSONArray jsonArray, boolean isHasAcct){
        boolean hasAuth = (authValue == '1' || authValue == '2');
        //在我的页面中的需要进行权限判断
        Map<Integer, MainModuleEnum> son = MainModuleEnum.getParentOrSon();
        MainModuleEnum sonModule = son.get(bit);
        if (sonModule != null) {
            JSONObject jsonObject = setJsonObj(sonModule);
            jsonObject.put("hasAuth",hasAuth);
            if (authValue == '2' &&  HouseUsrInfoStatEnum.P.getStat().equals(huStat)) {
                jsonObject.put("checkAuth",true);
            }
            jsonArray.add(jsonObject);
        }

        //我的页面功能,对呀非做权限控制功能,只获取一次
        if (bit == 1) {
            for (MainModuleEnum value : MainModuleEnum.values()) {
                if (value.getBit() == 999) {
                    if(value == MainModuleEnum.MYACCT && !isHasAcct){
                        continue;
                    }
                    JSONObject jsonObject = setJsonObj(value);
                    jsonArray.add(jsonObject);
                }
            }
        }

        //主页功能权限判断
        for (MainModuleEnum moduleEnum : MainModuleEnum.values()) {
            if (moduleEnum.getIndex() == 1 && moduleEnum.getBit() == bit) {
                JSONObject jsonObject = setJsonObj(moduleEnum);
                if(StringUtils.equals(propType, PropTypeEnum.B.getCode())) {
                    if(moduleEnum == MainModuleEnum.QR) {
                        jsonObject.put("funName", "出行码");
                    }
                }
                jsonObject.put("hasAuth",hasAuth);
                //对于位图是2的需要进行权限校验设置
                if (authValue == '2' &&  HouseUsrInfoStatEnum.P.getStat().equals(huStat)) {
                    jsonObject.put("checkAuth",true);
                }
                jsonArray.add(jsonObject);
                //对于没有共享位图的,当存在已经匹配值的,直接跳出循环
                if (!moduleEnum.getShareBit()) {
                    break;
                }
            }
        }
    }

    public static JSONObject setJsonObj(MainModuleEnum value) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("funName",value.getFunName());
        jsonObject.put("ordId",value.getOrdId());
        jsonObject.put("bit",value.getBit());
        jsonObject.put("hasAuth",value.isHasAuth());
        jsonObject.put("logoPath",value.getLogoPath());
        jsonObject.put("pagePath",value.getPagePath());
        jsonObject.put("index",value.getIndex());
        jsonObject.put("bindEvent",value.getBindEvent());
        jsonObject.put("checkAuth",value.getCheckAuth());
        return jsonObject;
    }

    public static JSONArray getFunMenu(String authBit, String propType,String huStat,boolean isHasAcct) {
        //权限位图不为空的时候,根据权限位图返回给前台具体的功能权限通知
        JSONArray jsonArray = new JSONArray();
        if (StringUtils.isNotBlank(authBit)) {
            char[] chars = authBit.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                setAuth((i+1),chars[i],huStat,propType,jsonArray, isHasAcct);
            }
        }

        //JSONObject jsonObject = null;
        //for (MainModuleEnum value : MainModuleEnum.values()) {
        //    jsonObject = new JSONObject();
        //    jsonObject.put("funName",value.getFunName());
        //    jsonObject.put("ordId",value.getOrdId());
        //    jsonObject.put("bit",value.getBit());
        //    jsonObject.put("hasAuth",value.isHasAuth());
        //    jsonObject.put("logoPath",value.getLogoPath());
        //    jsonObject.put("pagePath",value.getPagePath());
        //    jsonObject.put("checkAuth",value.getCheckAuth());
        //    jsonObject.put("index",value.getIndex());
        //    jsonObject.put("bindEvent",value.getBindEvent());
        //
        //    //如果是商业模式//TODO 简单改一下
        //    if(StringUtils.equals(propType, PropTypeEnum.B.getCode())) {
        //        if(value == MainModuleEnum.QR) {
        //            jsonObject.put("funName", "出行码");
        //        }
        //    }
        //    jsonArray.add(jsonObject);
        //}
        return sortProxyAndCdn(jsonArray);
    }

    private static JSONArray sortProxyAndCdn(JSONArray bindArrayResult) {
        List<JSONObject> list = JSONArray.parseArray(bindArrayResult.toJSONString(), JSONObject.class);
        //System.out.println("排序前："+bindArrayResult);
        Collections.sort(list, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                int a = o1.getInteger("ordId");
                int b = o2.getInteger("ordId");
                if (a < b) {
                    return -1;
                } else if(a == b) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        JSONArray jsonArray = JSONArray.parseArray(list.toString());
        //System.out.println("排序后："+jsonArray);
        return jsonArray;
    }
}
