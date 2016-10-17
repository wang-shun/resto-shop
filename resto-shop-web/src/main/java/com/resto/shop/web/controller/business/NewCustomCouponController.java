package com.resto.shop.web.controller.business;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.brand.web.model.DistributionMode;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.DistributionModeService;
import com.resto.shop.web.config.SessionKey;
import com.resto.shop.web.constant.TimeConsType;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.NewCustomCoupon;
import com.resto.shop.web.service.NewCustomCouponService;

@Controller
@RequestMapping("newcustomcoupon")
public class NewCustomCouponController extends GenericController{

    @Resource
    NewCustomCouponService newcustomcouponService;

    @Resource
    DistributionModeService distributionmodeService;

    @Resource
    BrandService brandService;

    @RequestMapping("/list")
    public void list(){
    }

    /**
     * 查询所有的优惠券
     * @return
     */
    @RequestMapping("/list_all")
    @ResponseBody
    public List<NewCustomCoupon> listData(){
        return newcustomcouponService.selectListByBrandId(getCurrentBrandId());
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
                return new Result(false);
            }
            //选择优惠券时间类型2时，开始和结束时间必须填
        }else if(TimeConsType.TYPETIME==newCustomCoupon.getTimeConsType()){
            if(newCustomCoupon.getBeginDateTime()==null||newCustomCoupon.getEndDateTime()==null){
                log.debug("优惠券开始或者结束时间不能为空");
                return new Result(false);
            }
        }else {
            if((newCustomCoupon.getBeginTime()!=null&&newCustomCoupon.getEndTime()!=null)||(newCustomCoupon.getBeginDateTime()!=null&&newCustomCoupon.getEndDateTime()!=null)){
                if(newCustomCoupon.getBeginTime().compareTo(newCustomCoupon.getEndTime())>0){
                    log.debug("开始时间大于结束时间");
                    return new Result(false);
                }else if(newCustomCoupon.getBeginDateTime().compareTo(newCustomCoupon.getEndDateTime())>0){
                    log.debug("优惠券的开始时间不能大于结束时间");
                    return new Result(false);
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
        return Result.getSuccess();
    }

    @RequestMapping("modify")
    @ResponseBody
    public Result modify(@Valid NewCustomCoupon newCustomCoupon){
        if(TimeConsType.TYPENUM==newCustomCoupon.getTimeConsType()){
            if(newCustomCoupon.getCouponValiday()==null){
                log.info("日期不能为空");
                return new Result(false);
            }

        }else if(TimeConsType.TYPETIME==newCustomCoupon.getTimeConsType()){
            if(newCustomCoupon.getBeginDateTime()==null||newCustomCoupon.getEndDateTime()==null){
                log.info("优惠券开始或者结束时间不能为空");
                return new Result(false);
            }
        }else {

            if((newCustomCoupon.getBeginTime()!=null&&newCustomCoupon.getEndTime()!=null)||(newCustomCoupon.getBeginDateTime()!=null&&newCustomCoupon.getEndDateTime()!=null)){
                if(newCustomCoupon.getBeginTime().compareTo(newCustomCoupon.getEndTime())>0){
                    log.info("开始时间大于结束时间");
                    return new Result(false);
                }else if(newCustomCoupon.getBeginDateTime().compareTo(newCustomCoupon.getEndDateTime())>0){
                    log.info("优惠券的开始时间不能大于结束时间");
                    return new Result(false);
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
        return Result.getSuccess();
    }

    @RequestMapping("delete")
    @ResponseBody
    public Result delete(Long id){
        newcustomcouponService.delete(id);
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

}	
