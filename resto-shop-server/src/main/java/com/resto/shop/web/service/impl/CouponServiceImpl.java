package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSONObject;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.*;
import com.resto.brand.web.dto.CouponDto;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.model.WechatConfig;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.brand.web.service.WechatConfigService;
import com.resto.shop.web.constant.*;
import com.resto.shop.web.dao.CouponMapper;
import com.resto.shop.web.dao.NewCustomCouponMapper;
import com.resto.shop.web.dao.OrderMapper;
import com.resto.shop.web.datasource.DynamicDataSource;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.*;
import com.resto.shop.web.service.*;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static com.resto.brand.core.util.HttpClient.doPostAnsc;
import static com.resto.brand.core.util.LogUtils.url;

/**
 *
 */
@RpcService
public class CouponServiceImpl extends GenericServiceImpl<Coupon, String> implements CouponService {

    @Resource
    private CouponMapper couponMapper;

    @Override
    public GenericDao<Coupon, String> getDao() {
        return couponMapper;
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private NewCustomCouponMapper newCustomCouponMapper;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    CustomerService customerService;

    @Autowired
    private BrandSettingService brandSettingService;

    @Autowired
    private ShopDetailService shopDetailService;


    @Autowired
    private WechatConfigService wechatConfigService;

    @Autowired
    private BrandService brandService;


    @Autowired
    SmsLogService smsLogService;

    @Override
    public List<Coupon> listCoupon(Coupon coupon,String brandId,String shopId) {
        List<Coupon> list = new ArrayList<>();
        //查询出品牌的
        coupon.setBrandId(brandId);
        coupon.setShopDetailId(shopId);

        List<Coupon> brandList = couponMapper.listCouponByBrandId(coupon);
        //查询出店铺的专属优惠券
        List<Coupon> shopList = couponMapper.listCouponByShopId(coupon);
        list.addAll(brandList);
        list.addAll(shopList);
        return list;
    }

    @Override
    public List<Coupon> listCouponUsed(Coupon coupon) {
        List<Coupon> list=couponMapper.listCouponUsed(coupon);
        return list;
    }

    @Resource
    OrderPaymentItemService orderPaymentItemService;

    @Override
    public void insertCoupon(Coupon coupon) {
        coupon.setId(ApplicationUtils.randomUUID());
        coupon.setUsingTime(null);
        coupon.setIsUsed(false);
        couponMapper.insertSelective(coupon);
    }

    @Override
    public Coupon useCoupon(BigDecimal totalMoney, Order order) throws AppException {

        Coupon coupon = selectById(order.getUseCoupon());
        //判断优惠券是否已使用
        if(coupon.getIsUsed()){
            throw new AppException(AppException.COUPON_IS_USED);
        }
        //判断优惠券有效期
        Date beginDate = DateUtil.getDateBegin(coupon.getBeginDate());
        Date endDate = DateUtil.getDateEnd(coupon.getEndDate());
        Date now = new Date();
        if(beginDate.getTime()>now.getTime()||endDate.getTime()<now.getTime()){
            throw new AppException(AppException.COUPON_IS_EXPIRE);
        }
        //判断优惠券使用时间段
        int beginMin = DateUtil.getMinOfDay(coupon.getBeginTime());
        int endMin = DateUtil.getMinOfDay(coupon.getEndTime());
        int nowMin = DateUtil.getMinOfDay(now);
        if(beginMin>nowMin||endMin<nowMin){
            throw new AppException(AppException.COUPON_TIME_ERR);
        }

        //判断优惠券使用类型是否0 或者是否等于订单类型
        if(coupon.getDistributionModeId()!=0&&coupon.getDistributionModeId()!=order.getDistributionModeId()){
            if(coupon.getDistributionModeId()!=1&&order.getDistributionModeId()!=3){
                throw new AppException(AppException.COUPON_MODE_ERR);
            }
        }
        //判断优惠券订单金额是否大于优惠券可用金额
        if(totalMoney.compareTo(totalMoney)<0){
            throw new AppException(AppException.COUPON_MIN_AMOUNT_ERR);
        }
        //判断是否使用了余额 并且 当前优惠券可否使用余额
        if(order.isUseAccount()&&!coupon.getUseWithAccount()){
            throw new AppException(AppException.COUPON_NOT_USEACCOUNT);
        }

        //判断是否是店铺专属的优惠券  优惠券有店铺id并且店铺id和订单中的店铺id不一样就会抛异常
        if(coupon.getShopDetailId()!=null &&!order.getShopDetailId().equals(coupon.getShopDetailId())){
            throw new AppException(AppException.COUPON_IS_SHOP);
        }
        coupon.setIsUsed(true);
        coupon.setUsingTime(new Date());
        this.update(coupon);
        return coupon;
    }

    @Override
    public void refundCoupon(String id) {
        Coupon coupon = selectById(id);
        coupon.setIsUsed(false);
        coupon.setRemark("退还优惠券");
        update(coupon);
    }



    @Override
    public List<Coupon> listCouponByStatus(String status, String customerId,String brandId,String shopId) {
        String IS_EXPIRE = null;
        String NOT_EXPIRE = null;
        if("2".equals(status)){
            IS_EXPIRE = "IS_EXPIRE";
            status = null;
        }
        if("0".equals(status)){
            NOT_EXPIRE = "NOT_EXPIRE";
        }
        List<Coupon> list = new ArrayList<>();
        //查询品牌优惠券
        List<Coupon> brandList = couponMapper.listCouponByStatusAndBrandId(status, IS_EXPIRE, NOT_EXPIRE, customerId,brandId);
        //当前店铺专属的优惠券
        List<Coupon> shopList = couponMapper.listCouponByStatusAndShopId(status, IS_EXPIRE, NOT_EXPIRE, customerId,shopId);
        list.addAll(brandList);
        list.addAll(shopList);
        return list;
    }

    @Override
    public void useCouponById(String orderId, String id) {
        Coupon coupon = selectById(id);
        coupon.setIsUsed(true);
        coupon.setRemark("后付款消费优惠券");
        coupon.setUsingTime(new Date());
        update(coupon);


        Order order = orderMapper.selectByPrimaryKey(orderId);
//        order.setPaymentAmount(order.getPaymentAmount().subtract(coupon.getValue()));
//        orderService.update(order);

        OrderPaymentItem item = new OrderPaymentItem();
        item.setId(ApplicationUtils.randomUUID());
        item.setOrderId(orderId);
        item.setPaymentModeId(PayMode.COUPON_PAY);
        item.setPayTime(new Date());
        item.setPayValue(coupon.getValue());
        item.setRemark("优惠券支付:" + item.getPayValue());
//        item.setResultData(coupon.getId()); //现在resultData字段只用来存放微信、支付宝支付的回调  20171213 wtl
        item.setToPayId(coupon.getId());
        orderPaymentItemService.insert(item);
    }

    @Override
    public List<Coupon> getListByCustomerId(String customerId) {
        return couponMapper.getListByCustomerId(customerId);
    }

    @Override
    public List<CouponDto> selectCouponDto(Map<String, Object> selectMap) {
        return couponMapper.selectCouponDto(selectMap);
    }

    @Override
    public List<Coupon> usedCouponBeforeByOrderId(String orderId) {
        return couponMapper.usedCouponBeforeByOrderId(orderId);
    }

    @Override
    public List<Coupon> addRealTimeCoupon(List<NewCustomCoupon> newCustomCoupons, Customer customer) {
        List<Coupon> coupons = new ArrayList<>();
        try{
            String realTimeCouponIds = "";
            for (NewCustomCoupon customCoupon : newCustomCoupons){
                Coupon coupon = new Coupon();
                Date beginDate = new Date();

                //判断优惠券有效日期类型
                if (customCoupon.getTimeConsType().equals(TimeCons.MODELA)){ //按天
                    coupon.setBeginDate(beginDate);
                    coupon.setEndDate(DateUtil.getAfterDayDate(beginDate,customCoupon.getCouponValiday()));
                }else if (customCoupon.getTimeConsType()==TimeCons.MODELB){ //按日期
                    coupon.setBeginDate(customCoupon.getBeginDateTime());
                    coupon.setEndDate(customCoupon.getEndDateTime());
                }

                //判断是店铺优惠券还是品牌优惠券
                if(customCoupon.getIsBrand() == 1 && customCoupon.getBrandId() != null){
                    coupon.setBrandId(customCoupon.getBrandId());
                }else{
                    coupon.setShopDetailId(customCoupon.getShopDetailId());
                }
                //如果没有设置优惠券推送时间，那么，默认为3天
                if(customCoupon.getPushDay() != null){
                    coupon.setPushDay(customCoupon.getPushDay());
                }else{
                    coupon.setPushDay(3);
                }
                coupon.setName(customCoupon.getCouponName());
                coupon.setValue(customCoupon.getCouponValue());
                coupon.setMinAmount(customCoupon.getCouponMinMoney());
                coupon.setCouponType(customCoupon.getCouponType());
                coupon.setBeginTime(customCoupon.getBeginTime());
                coupon.setEndTime(customCoupon.getEndTime());
                coupon.setUseWithAccount(customCoupon.getUseWithAccount());
                coupon.setDistributionModeId(customCoupon.getDistributionModeId());
                coupon.setCouponSource(CouponSource.getCouponSourceByType(coupon.getCouponType()));
                coupon.setCustomerId(customer.getId());
                coupon.setRecommendDelayTime(0);
                coupon.setNewCustomCouponId(customCoupon.getId());
                for(int i = 0; i < customCoupon.getCouponNumber(); i++){
                    insertCoupon(coupon);
                    coupons.add(coupon);
                }
                if (coupon.getCouponSource().equalsIgnoreCase(CouponSource.REAL_TIME_COUPON)) {
                    realTimeCouponIds = realTimeCouponIds.concat(customCoupon.getId().toString()).concat(",");
                }
            }
            if (org.apache.commons.lang3.StringUtils.isNotBlank(realTimeCouponIds)){
                Customer newCustomer = new Customer();
                newCustomer.setId(customer.getId());
                //得到用户领取过的实时优惠券Id
                realTimeCouponIds = realTimeCouponIds.substring(0,realTimeCouponIds.length() - 1);
                if (customer.getRealTimeCouponIds() != null){
                    realTimeCouponIds = customer.getRealTimeCouponIds().concat(",").concat(realTimeCouponIds);
                }
                newCustomer.setRealTimeCouponIds(realTimeCouponIds);
                customerService.update(newCustomer);
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("发放实时优惠券出错！");
            return new ArrayList<>();
        }
        return coupons;
    }

    @Override
    public Coupon selectPosPayOrderCanUseCoupon(Map<String, Object> selectMap) {
        return couponMapper.selectPosPayOrderCanUseCoupon(selectMap);
    }

    @Override
    public List<Coupon> getCouponByShopId(String shopId, Integer day, Integer type) {
        return couponMapper.getCouponByShopId(shopId,day,type);
    }

    /**
     * 根据所设置的优惠卷以及用户发放优惠卷
     * @param newCustomCoupon
     * @param customer
     */
    @Override
    public void addCoupon(NewCustomCoupon newCustomCoupon, Customer customer) {
        Coupon coupon = new Coupon();
        Date beginDate = new Date();
        //判断优惠券有效日期类型
        if (newCustomCoupon.getTimeConsType().equals(TimeCons.MODELA)){ //按天
            coupon.setBeginDate(beginDate);
            coupon.setEndDate(DateUtil.getAfterDayDate(beginDate,newCustomCoupon.getCouponValiday()));
        }else if (newCustomCoupon.getTimeConsType()==TimeCons.MODELB){ //按日期
            coupon.setBeginDate(newCustomCoupon.getBeginDateTime());
            coupon.setEndDate(newCustomCoupon.getEndDateTime());
        }
        //判断是店铺优惠券还是品牌优惠券
        if(newCustomCoupon.getIsBrand() == 1 && newCustomCoupon.getBrandId() != null){
            coupon.setBrandId(newCustomCoupon.getBrandId());
        }else{
            coupon.setShopDetailId(newCustomCoupon.getShopDetailId());
        }
        //如果没有设置优惠券推送时间，那么，默认为3天
        if(newCustomCoupon.getPushDay() != null){
            coupon.setPushDay(newCustomCoupon.getPushDay());
        }else{
            coupon.setPushDay(3);
        }
        coupon.setName(newCustomCoupon.getCouponName());
        coupon.setValue(newCustomCoupon.getCouponValue());
        coupon.setMinAmount(newCustomCoupon.getCouponMinMoney());
        coupon.setCouponType(newCustomCoupon.getCouponType());
        coupon.setBeginTime(newCustomCoupon.getBeginTime());
        coupon.setEndTime(newCustomCoupon.getEndTime());
        coupon.setUseWithAccount(newCustomCoupon.getUseWithAccount());
        coupon.setDistributionModeId(newCustomCoupon.getDistributionModeId());
        coupon.setCouponSource(CouponSource.getCouponSourceByType(coupon.getCouponType()));
        coupon.setCustomerId(customer.getId());
        coupon.setRecommendDelayTime(0);
        for(int i = 0; i < newCustomCoupon.getCouponNumber(); i++){
            insertCoupon(coupon);
        }
    }


    private List<Coupon> insertCouponBatch(NewCustomCoupon newCustomCoupon, Customer customer){

        List<Coupon> coupons = new ArrayList<>();

        for(int i = 0; i < newCustomCoupon.getCouponNumber(); i++){
            Coupon coupon = new Coupon();
            Date beginDate = new Date();
            //判断优惠券有效日期类型
            if (newCustomCoupon.getTimeConsType().equals(TimeCons.MODELA)){ //按天
                coupon.setBeginDate(beginDate);
                coupon.setEndDate(DateUtil.getAfterDayDate(beginDate,newCustomCoupon.getCouponValiday()));
            }else if (newCustomCoupon.getTimeConsType()==TimeCons.MODELB){ //按日期
                coupon.setBeginDate(newCustomCoupon.getBeginDateTime());
                coupon.setEndDate(newCustomCoupon.getEndDateTime());
            }
            //判断是店铺优惠券还是品牌优惠券
            if(newCustomCoupon.getIsBrand() == 1 && newCustomCoupon.getBrandId() != null){
                coupon.setBrandId(newCustomCoupon.getBrandId());
            }else{
                coupon.setShopDetailId(newCustomCoupon.getShopDetailId());
            }
            //如果没有设置优惠券推送时间，那么，默认为3天
            if(newCustomCoupon.getPushDay() != null){
                coupon.setPushDay(newCustomCoupon.getPushDay());
            }else{
                coupon.setPushDay(3);
            }
            coupon.setName(newCustomCoupon.getCouponName());
            coupon.setValue(newCustomCoupon.getCouponValue());
            coupon.setMinAmount(newCustomCoupon.getCouponMinMoney());
            coupon.setCouponType(newCustomCoupon.getCouponType());
            coupon.setBeginTime(newCustomCoupon.getBeginTime());
            coupon.setAddTime(new Date());
            coupon.setEndTime(newCustomCoupon.getEndTime());
            coupon.setUseWithAccount(newCustomCoupon.getUseWithAccount());
            coupon.setDistributionModeId(newCustomCoupon.getDistributionModeId());
            coupon.setCouponSource(CouponSource.getCouponSourceByType(coupon.getCouponType()));
            coupon.setCustomerId(customer.getId());
            coupon.setRecommendDelayTime(0);
            coupon.setId(ApplicationUtils.randomUUID());
            coupon.setUsingTime(null);
            coupon.setIsUsed(false);
            coupons.add(coupon);
        }
        return coupons;
    }

    @Override
    public void addCouponBatch(List<Customer> customerList, NewCustomCoupon newCustomCoupon,String brandId) throws SQLException {
        //得到相应的品牌信息
        ShopDetail shopDetail = new ShopDetail();
        Brand brand = brandService.selectById(brandId);
        BrandSetting brandSetting = brandSettingService.selectByBrandId(brandId);
        WechatConfig config = wechatConfigService.selectByBrandId(brandId);
        if (newCustomCoupon.getIsBrand().equals(Common.NO)) {
            shopDetail = shopDetailService.selectById(newCustomCoupon.getShopDetailId());
        }
        //封装推送文案的信息
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("name", newCustomCoupon.getIsBrand() == 0 ? shopDetail.getName() : brand.getBrandName());
        valueMap.put("value", newCustomCoupon.getCouponValue().multiply(new BigDecimal(newCustomCoupon.getCouponNumber())));
        valueMap.put("url", newCustomCoupon.getIsBrand() == 0 ? brandSetting.getWechatWelcomeUrl() + "?dialog=myCoupon&qiehuan=qiehuan&subpage=my&shopId=" + shopDetail.getId() + ""
                : brandSetting.getWechatWelcomeUrl() + "?dialog=myCoupon&qiehuan=qiehuan&subpage=my");
        String text = "I Miss U ，好久不见，你最近好吗？${name}给您寄来价值${value}元的“回归礼券”，<a href='${url}'>赶紧来尝尝我们的新品吧！~</a>";
        StrSubstitutor substitutor = new StrSubstitutor(valueMap);
        text = substitutor.replace(text);
        List<Coupon> coupons = new ArrayList<>();
        for (Customer customer : customerList) {
            coupons.addAll(insertCouponBatch(newCustomCoupon,customer));
            //判断是否开启微信推送
            if (brandSetting.getWechatPushGiftCoupons().equals(Common.YES)) {
                WeChatUtils.sendCustomerMsg(text, customer.getWechatId(), config.getAppid(), config.getAppsecret());
            }
            //有手机号则发送短信
            if (StringUtils.isNotBlank(customer.getTelephone()) && brandSetting.getSmsPushGiftCoupons().equals(Common.YES)) {
                JSONObject smsParam = new JSONObject();
                smsParam.put("name", valueMap.get("name").toString());
                smsParam.put("value", valueMap.get("value").toString());
                JSONObject jsonObject = smsLogService.sendMessage(brandId, customer.getLastOrderShop() == null ? shopDetail.getId() : customer.getLastOrderShop(),
                        SmsLogType.WAKELOSS, SMSUtils.SIGN, SMSUtils.SMS_WAKE_LOSS, customer.getTelephone(), smsParam);
                log.info("短信发送结果：" + jsonObject.toJSONString());
            }
            Map logMap = new HashMap(4);
            logMap.put("brandName", brand.getBrandName());
            logMap.put("fileName", shopDetail.getName());
            logMap.put("type", "shopAction");
            logMap.put("content", "向用户Id为：" + customer.getId() + "的微信用户'" + customer.getNickname() + "'发放礼品优惠券名称为：'" + newCustomCoupon.getCouponName() + "'优惠卷金额为：" + newCustomCoupon.getCouponValue() + "数量为：" + newCustomCoupon.getCouponNumber() + "，请求服务器地址为:" + MQSetting.getLocalIP());
            doPostAnsc(url, logMap);
        }

        couponMapper.insertCouponBatch(coupons);


    }

    @Override
    public Coupon selectLastTimeRebate(String customerId) {
        return couponMapper.selectLastTimeRebate(customerId);
    }
}
