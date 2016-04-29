package com.resto.shop.web.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.DateUtil;
import com.resto.brand.core.util.SMSUtils;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.shop.web.dao.SmsLogMapper;
import com.resto.shop.web.model.SmsLog;
import com.resto.shop.web.service.SmsLogService;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class SmsLogServiceImpl extends GenericServiceImpl<SmsLog, Long> implements SmsLogService {

    @Resource
    private SmsLogMapper smslogMapper;

    @Resource
    BrandService brandService;
    
    @Resource
    BrandSettingService brandSettingService;
    
    @Override
    public GenericDao<SmsLog, Long> getDao() {
        return smslogMapper;
    }

	@Override
	public String sendCode(String phone, String code, String brandId,String shopId) {
		Brand b = brandService.selectById(brandId);
		BrandSetting brandSetting = brandSettingService.selectByBrandId(b.getId());
		String string = SMSUtils.sendCode(brandSetting.getSmsSign(), b.getBrandName(), code, phone);
		SmsLog smsLog = new SmsLog();
		smsLog.setBrandId(brandId);
		smsLog.setShopDetailId(shopId);
		smsLog.setContent(code);
		smsLog.setSmsType(SmsLog.CODE);
		smsLog.setCreateTime(new Date());
		smsLog.setPhone(phone);
		smsLog.setSmsResult(string);
		try{
			insert(smsLog);
		}catch(Exception e){
			log.error("发送短信失败:"+e.getMessage());
		}
		return string;
	}

	@Override
	public List<SmsLog> selectListByShopId(String shopId) {
		
		return smslogMapper.selectListByShopId(shopId);
	}

	@Override
	public List<SmsLog> selectListByShopIdAndDate(String ShopId) {
		System.out.println(DateUtil.getAfterDayDate(new Date(), -2));
		Date begin = DateUtil.getDateBegin(DateUtil.getAfterDayDate(new Date(), -2));
		return smslogMapper.selectListByShopIdAndDate(ShopId,begin);
	} 

}
