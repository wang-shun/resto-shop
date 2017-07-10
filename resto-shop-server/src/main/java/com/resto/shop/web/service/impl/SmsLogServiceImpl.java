package com.resto.shop.web.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.resto.shop.web.model.Customer;
import org.json.JSONObject;

import com.resto.brand.core.entity.Result;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.DateUtil;
import com.resto.brand.core.util.SMSUtils;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.model.BrandUser;
import com.resto.brand.web.model.SmsAcount;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.BrandUserService;
import com.resto.brand.web.service.SmsAcountService;
import com.resto.shop.web.constant.SmsLogType;
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
    BrandUserService brandUserService;
    
    @Resource
    BrandSettingService brandSettingService;
    
    @Resource
    SmsAcountService smsAcountService;
    
    
    
    @Override
    public GenericDao<SmsLog, Long> getDao() {
        return smslogMapper;
    }

    
    
	@Override
	public String sendCode(String phone, String code, String brandId, String shopId, int smsLogType,Map<String,String> logMap) {
        BrandUser brandUser = brandUserService.selectOneByBrandId(brandId);
		//发送阿里短信返回 默认使用阿里发送短信
		String string = null;
		if(smsLogType == SmsLogType.AUTO_CODE){
			string = sendMsg(code, phone,brandUser,logMap);
		}
		SmsLog smsLog = new SmsLog();
		smsLog.setBrandId(brandId);
		smsLog.setShopDetailId(shopId);
		smsLog.setContent(code);
		smsLog.setSmsType(SmsLogType.AUTO_CODE);
		smsLog.setCreateTime(new Date());
		smsLog.setPhone(phone);
		smsLog.setSmsResult(string);
		JSONObject obj = new JSONObject(string);
		
		//返回值中有"success":"false"时说明商家无法发短信或者该条短信发送失败,此时不更新短信账户
			if(obj.getBoolean("success")){ //返回成功
				try{
					insert(smsLog);
					//更新短信账户的信息
					smsAcountService.updateByBrandId(brandId);
					//判断是否要提醒商家充值短信账户
					sendNotice(brandUser,logMap);
				}catch(Exception e){
					log.error("发送短信失败:"+e.getMessage());
				}
			}
                //短信发送失败不更新短信账户
				insert(smsLog);

		log.info("短信发送结果:"+string);
		return string;
	}

	private void sendNotice(BrandUser brandUser,Map<String,String>logMap) {
		SmsAcount smsAcount = smsAcountService.selectByBrandId(brandUser.getBrandId());
		//获取短信账户短信提醒
		String str = smsAcount.getSmsRemind();
		String[] arrs = str.split(",");
		//获取商家短信剩余数量
		int remindNum = smsAcount.getRemainderNum();
		//判断是否需要提醒
		if(this.isHave(arrs, remindNum+"")){
			//提醒商家充值
			SMSUtils.sendNoticeToBrand(brandUser.getBrandName(),remindNum, brandUser.getPhone(),logMap);
		}
	}

	
	public String sendMsg(String code,String phone,BrandUser brandUser,Map<String,String> logMap){
		//判断该品牌账户的余额是否充足
		SmsAcount smsAcount = smsAcountService.selectByBrandId(brandUser.getBrandId());
		//获取剩余短信条数
		int remindNum = smsAcount.getRemainderNum();
		String [] arrs = smsAcount.getSmsRemind().split(",");
        StringBuilder sb = new StringBuilder();
		//判断剩余短信条数是否大于设定的最小可发短信值
		if(remindNum<this.getMinStr(arrs)){
			//我们提醒商家充值
            String content = sb.append(logMap.get("content")).append("商家剩余条数不足").toString();
            logMap.put("content",content);
			SMSUtils.sendNoticeToBrand(brandUser.getBrandName(),smsAcount.getRemainderNum(), brandUser.getPhone(),logMap);
			log.info("剩余短信为"+remindNum+"条无法发短信");
			//返回false标记让商家无法发短信
			return "{'msg':'当前品牌已超欠费可用额度，请充值后使用短信功能','success':'false'}";
		}else{
			//剩余短信在设置的范围内
			if(this.isHave(arrs, remindNum+"")){
                String content = sb.append(logMap.get("content")).append("在固定条数短信时发短信给商家").toString();
                logMap.put("content",content);
				//发短信提醒商家
				SMSUtils.sendNoticeToBrand(brandUser.getBrandName(), remindNum, brandUser.getPhone(),logMap);
				SMSUtils.sendNoticeToBrand(serviceName, remindNum, brandUser.getPhone(),logMap);
			}
		}
		//商家给客户发短信
		if(logMap != null){
			String content = sb.append(logMap.get("content")).append("商家给客户发短信").toString();
			logMap.put("content",content);
		}

		return SMSUtils.sendCode(brandUser.getBrandName(), code, phone,logMap);
	}

	@Override
	public List<SmsLog> selectListByShopId(String shopId) {
		
		return smslogMapper.selectListByShopId(shopId);
	}

	@Override
	public List<SmsLog> selectListByShopIdAndDate(String ShopId) {
		Date begin = DateUtil.getDateBegin(DateUtil.getAfterDayDate(new Date(), -2));
		return smslogMapper.selectListByShopIdAndDate(ShopId,begin);
	}

	@Override
	public List<SmsLog> selectListWhere(String begin,String end,String shopIds) {
        if(("".equals(begin)||begin==null)&&(end==null||"".equals(""))){
            begin = DateUtil.formatDate(new Date(),"yyyy-MM-dd");
            end = DateUtil.formatDate(new Date(),"yyyy-MM-dd");
        }


		Date beginDate = DateUtil.getformatBeginDate(begin);
		Date endDate = DateUtil.getformatEndDate(end);
		String[] temp = shopIds.split(",");
		//查询短信记录
		List<SmsLog> list =  smslogMapper.selectListByWhere(beginDate, endDate, temp);
		for (SmsLog smsLog : list) {
			smsLog.setSmsLogTyPeName(SmsLogType.getSmsLogTypeName(smsLog.getSmsType()));
		}

		return list;

	}

	@Override
	public List<SmsLog> selecByBrandId(String brandId) {
		List<SmsLog> list = smslogMapper.selectListByBrandId(brandId);
		for (SmsLog smsLog : list) {
			smsLog.setSmsLogTyPeName(SmsLogType.getSmsLogTypeName(smsLog.getSmsType()));
		}
		return list;
	}
	
	
	/**
	 * String数组获取最小值
	 * @param arrs
	 * @return
	 */
	
	public int getMinStr(String[] arrs){
		if(arrs == null ||arrs.length<=0){
			 throw new IllegalArgumentException("空数组无法获取最小值");
		}
		int min = Integer.parseInt(arrs[0]);
		for(int i=0;i<arrs.length;i++){
			int temp = Integer.parseInt(arrs[i]);
			if(min>temp){
				min = temp;
			}
		}
		return min;
	}
	
	public  boolean isHave(String[] strs,String s){
		  /*此方法有两个参数，第一个是要查找的字符串数组，第二个是要查找的字符或字符串
		   * */
		  for(int i=0;i<strs.length;i++){
		   if(strs[i].indexOf(s)!=-1){//循环查找字符串数组中的每个字符串中是否包含所有查找的内容
		    return true;//查找到了就返回真，不在继续查询
		   }
		  }
		  return false;//没找到返回false
		 }

    @Override
    public SmsLog selectByMap(Map<String, Object> selectMap) {
        return smslogMapper.selectByMap(selectMap);
    }
}
