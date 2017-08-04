package com.resto.shop.web.controller.business;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.resto.brand.core.util.StringUtils;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.service.*;
import com.resto.shop.web.util.RedisUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.resto.brand.core.entity.Result;
import com.resto.brand.web.model.DistributionMode;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.DistributionModeService;
import com.resto.shop.web.constant.TimeConsType;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.NewCustomCoupon;

@Controller
@RequestMapping("newcustomcoupon")
public class NewCustomCouponController extends GenericController{

    @Resource
    NewCustomCouponService newcustomcouponService;

    @Resource
    DistributionModeService distributionmodeService;

    @Resource
    BrandService brandService;

    @Resource
    OrderService orderService;

    @Resource
    ChargeOrderService chargeOrderService;

    @Resource
    CustomerService customerService;

    @Resource
    CouponService couponService;

    @RequestMapping("/list")
    public void list(){
    }

    /**
     * 查询所有的优惠券设置
     * @return
     */
    @RequestMapping("/list_all")
    @ResponseBody
    public List<NewCustomCoupon> listData(){
        return newcustomcouponService.selectListByBrandId(getCurrentBrandId(),getCurrentShopId());
    }

    /**
     * 查询当前店铺的优惠券
     * @return
     */
    @RequestMapping("/list_all_shopId")
    @ResponseBody
    public List<NewCustomCoupon> listDataByShopId(){
        return newcustomcouponService.selectListShopId(getCurrentShopId());
    }

    @RequestMapping("list_one")
    @ResponseBody
    public Result list_one(Long id){
        NewCustomCoupon newcustomcoupon = newcustomcouponService.selectById(id);
        return getSuccessResult(newcustomcoupon);
    }

    @RequestMapping("create")
    @ResponseBody
    public Result create(@Valid NewCustomCoupon newCustomCoupon, HttpServletRequest request){
        //选择优惠券时间类型1时,日期需要填写
        if(TimeConsType.TYPENUM==newCustomCoupon.getTimeConsType()){
            if(newCustomCoupon.getCouponValiday()==null||"".equals(newCustomCoupon.getCouponValiday())){
                log.debug("日期不能为空");
                return new Result("日期不能为空",false);
            }
            //选择优惠券时间类型2时，开始和结束时间必须填
        }else if(TimeConsType.TYPETIME==newCustomCoupon.getTimeConsType()){
            if(newCustomCoupon.getBeginDateTime()==null||newCustomCoupon.getEndDateTime()==null){
                log.debug("优惠券开始或者结束时间不能为空");
                return new Result("优惠券开始或者结束时间不能为空",false);
            }
        }else {
            if((newCustomCoupon.getBeginTime()!=null&&newCustomCoupon.getEndTime()!=null)||(newCustomCoupon.getBeginDateTime()!=null&&newCustomCoupon.getEndDateTime()!=null)){
                if(newCustomCoupon.getBeginTime().compareTo(newCustomCoupon.getEndTime())>0){
                    log.debug("开始时间大于结束时间");
                    return new Result("开始时间大于结束时间",false);
                }else if(newCustomCoupon.getBeginDateTime().compareTo(newCustomCoupon.getEndDateTime())>0){
                    log.debug("优惠券的开始时间不能大于结束时间");
                    return new Result("优惠券的开始时间不能大于结束时间",false);
                }
            }
        }
        newCustomCoupon.setBrandId(getCurrentBrandId());
        //如果是店铺优惠券
        if(newCustomCoupon.getIsBrand()==0){
            newCustomCoupon.setShopDetailId(getCurrentShopId());
        }
        newCustomCoupon.setCreateTime(new Date());
        newcustomcouponService.insertNewCustomCoupon(newCustomCoupon);
        if(RedisUtil.get(getCurrentBrandId()+"newCustomCoupon") != null){
            RedisUtil.remove(getCurrentBrandId()+"newCustomCoupon");
        }

        return Result.getSuccess();
    }

    @RequestMapping("modify")
    @ResponseBody
    public Result modify(@Valid NewCustomCoupon newCustomCoupon){
        if(TimeConsType.TYPENUM==newCustomCoupon.getTimeConsType()){
            if(newCustomCoupon.getCouponValiday()==null){
                log.info("日期不能为空");
                return new Result("日期不能为空",false);
            }

        }else if(TimeConsType.TYPETIME==newCustomCoupon.getTimeConsType()){
            if(newCustomCoupon.getBeginDateTime()==null||newCustomCoupon.getEndDateTime()==null){
                log.info("优惠券开始或者结束时间不能为空");
                return new Result("优惠券开始或者结束时间不能为空",false);
            }
        }else {

            if((newCustomCoupon.getBeginTime()!=null&&newCustomCoupon.getEndTime()!=null)||(newCustomCoupon.getBeginDateTime()!=null&&newCustomCoupon.getEndDateTime()!=null)){
                if(newCustomCoupon.getBeginTime().compareTo(newCustomCoupon.getEndTime())>0){
                    log.info("开始时间大于结束时间");
                    return new Result("开始时间大于结束时间",false);
                }else if(newCustomCoupon.getBeginDateTime().compareTo(newCustomCoupon.getEndDateTime())>0){
                    log.info("优惠券的开始时间不能大于结束时间");
                    return new Result("优惠券的开始时间不能大于结束时间",false);
                }
            }
        }
        newCustomCoupon.setBrandId(getCurrentBrandId());
        if(newCustomCoupon.getIsBrand()==1){//如果改成品牌则设置店铺id为空
            newCustomCoupon.setShopDetailId(null);
        }else if(newCustomCoupon.getIsBrand()==0){//如果是店铺专有
            newCustomCoupon.setShopDetailId(getCurrentShopId());
        }

        newcustomcouponService.update(newCustomCoupon);
        if(RedisUtil.get(getCurrentBrandId()+"newCustomCoupon") != null){
            RedisUtil.remove(getCurrentBrandId()+"newCustomCoupon");
        }
        return Result.getSuccess();
    }

    @RequestMapping("delete")
    @ResponseBody
    public Result delete(Long id){
        newcustomcouponService.delete(id);
        if(RedisUtil.get(getCurrentBrandId()+"newCustomCoupon") != null){
            RedisUtil.remove(getCurrentBrandId()+"newCustomCoupon");
        }
        return Result.getSuccess();
    }

    @RequestMapping("distributionmode/list_all")
    @ResponseBody
    public List<DistributionMode> lists(){
        return distributionmodeService.selectList();
    }
    @RequestMapping("distributionMode/list_one")
    @ResponseBody
    public DistributionMode listOne(Integer id){
        return distributionmodeService.selectById(id);
    }

    @RequestMapping("/goToGrant")
    public String goToGrant(String couponId, HttpServletRequest request){
        request.setAttribute("couponId", couponId);
        return "newcustomcoupon/grantCoupon";
    }

    /**
     * 根据条件查询发放流失唤醒优惠卷的用户
     * @param selectMap
     * @return
     */
    @RequestMapping("/selectCustomer")
    @ResponseBody
    public Result selectCustomer(@RequestParam Map<String, String> selectMap){
        try{
            //封装最后的返回值
            List<JSONObject> array = new ArrayList<>();
            JSONObject object = new JSONObject();
            //根据查询条件先得到所有用户
            Map<String, Object> objectMap = new HashMap<>();
            for (Map.Entry map : selectMap.entrySet()){
                objectMap.put(map.getKey().toString(), map.getValue());
            }
            List<Customer> customers = customerService.selectBySelectMap(objectMap);
            //得到当前时间的字符串
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String newDateString = format.format(new Date());
            //初始差距天数
            Integer daysBetween;
            //声明标识判断该用户是否满足订单条件默认不满足
            boolean meetOrder;
            List<String> customerIds = new ArrayList<>();
            for (Customer customer : customers){
                object.put("customerId", customer.getId());
                object.put("customerType", customer.getIsBindPhone() ? "注册" : "未注册");
                object.put("isValue", "否");
                object.put("nickname", customer.getNickname());
                String sex = "男";
                if(customer.getSex() == 2) {
                    sex = "女";
                }else if (customer.getSex() == 0){
                    sex = "未知";
                }
                object.put("sex", sex);
                object.put("telephone", customer.getTelephone() == null ? "--" : customer.getTelephone());
                object.put("birthday", customer.getCustomerDetail() != null && customer.getCustomerDetail().getBirthDate() != null
                        ? new SimpleDateFormat("yyyy-MM-dd").format(customer.getCustomerDetail().getBirthDate()) : "--");
                object.put("orderCount", 0);
                object.put("orderMoney", 0);
                object.put("AVGOrderMoney", 0);
                object.put("useOrder", customer.getUseOrder());
                object.put("chargeOrder", customer.getChargeOrder());
                array.add(object);
                object = new JSONObject();
                customerIds.add(customer.getId());
            }
            //获取所有下过单的用户
            List<Map<String, String>> orderList = orderService.selectCustomerOrderCount(customerIds);
            //获取充值过的所有用户
            List<String>  chargeOrderList = chargeOrderService.selectCustomerChargeOrder(customerIds);
            Iterator<JSONObject> iterator = array.iterator();
            array = new ArrayList<>();
            while (iterator.hasNext()){
                object = iterator.next();
                meetOrder = false;
                if (object.getBoolean("useOrder")) {
                    for (Map orderMap : orderList) {
                        if (object.get("customerId").toString().equalsIgnoreCase(orderMap.get("customerId").toString())) {
                            daysBetween = daysBetween(orderMap.get("lastOrderTime").toString(), newDateString);
                            //判断该用户满不满足订单条件
                            if (Integer.valueOf(orderMap.get("orderCount").toString()).compareTo(Integer.valueOf((StringUtils.isBlank(selectMap.get("orderCount")) ? "0" : selectMap.get("orderCount")))) > 0
                                    && new BigDecimal(orderMap.get("orderTotal").toString()).compareTo(new BigDecimal((StringUtils.isBlank(selectMap.get("orderTotal")) ? "0" : selectMap.get("orderTotal")))) > 0
                                    && new BigDecimal(orderMap.get("avgOrderMoney").toString()).compareTo(new BigDecimal((StringUtils.isBlank(selectMap.get("avgOrderMoney")) ? "0" : selectMap.get("avgOrderMoney")))) > 0
                                    && daysBetween.compareTo(Integer.valueOf((StringUtils.isBlank(selectMap.get("lastOrderDay")) ? "0" : selectMap.get("lastOrderDay")))) > 0) {
                                meetOrder = true;
                            }
                            object.put("orderCount", orderMap.get("orderCount").toString());
                            object.put("orderMoney", orderMap.get("orderTotal").toString());
                            object.put("AVGOrderMoney", orderMap.get("avgOrderMoney").toString());
                            orderList.remove(orderMap);
                            break;
                        }
                    }
                }
                //如果录如过订单条件但该用户没有满足订单条件则将该用户从列表中移除掉
                if (!meetOrder && (StringUtils.isNotBlank(selectMap.get("orderCount")) || StringUtils.isNotBlank(selectMap.get("orderTotal"))
                        || StringUtils.isNotBlank(selectMap.get("avgOrderMoney")) || StringUtils.isNotBlank(selectMap.get("lastOrderDay")))) {
                    iterator.remove();
                    continue;
                }
                //判断是否录如过储值条件，有则进行筛选
                if (object.getBoolean("chargeOrder")) {
                    for (String customerId : chargeOrderList) {
                        if (customerId.equalsIgnoreCase(object.get("customerId").toString())) {
                            object.put("isValue", "是");
                            chargeOrderList.remove(customerId);
                            break;
                        }
                    }
                }
                //判断如果有录入过是否储值的条件，如有则移除没有满足条件的用户
                if ("1".equalsIgnoreCase(selectMap.get("isValue")) && object.get("isValue").toString().equalsIgnoreCase("否")){
                    iterator.remove();
                    continue;
                }else if ("0".equalsIgnoreCase(selectMap.get("isValue")) && object.get("isValue").toString().equalsIgnoreCase("是")){
                    iterator.remove();
                    continue;
                }
                array.add(object);
                iterator.remove();
            }
            return getSuccessResult(array);
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            return new Result(false);
        }
    }

    /**
     * 向用户发放流失唤醒优惠卷
     * @param customerId
     * @param couponId
     * @return
     */
    @RequestMapping("/grantCoupon")
    @ResponseBody
    public Result grantCoupon(String customerId, String couponId){
        try{
            String[] customerIds = customerId.split(",");
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put("customerIds", customerIds);
            List<Customer> customerList = customerService.selectBySelectMap(objectMap);
            List<NewCustomCoupon> newCustomCoupons = new ArrayList<>();
            newCustomCoupons.add(newcustomcouponService.selectById(Long.valueOf(couponId)));
            for (Customer customer : customerList){
                couponService.addRealTimeCoupon(newCustomCoupons, customer);
            }
            return getSuccessResult();
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            return new Result(false);
        }
    }
}	
