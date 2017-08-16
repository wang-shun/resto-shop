package com.resto.shop.web.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.rocketmq.client.producer.MQProducer;
import com.resto.brand.core.entity.Result;
import com.resto.brand.core.enums.BehaviorType;
import com.resto.brand.core.enums.DetailType;
import com.resto.brand.core.util.MQSetting;
import com.resto.brand.web.model.*;
import com.resto.brand.web.service.*;
import com.resto.shop.web.producer.MQMessageProducer;
import com.resto.shop.web.util.BrandAccountSendUtil;
import org.json.JSONObject;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.DateUtil;
import com.resto.brand.core.util.SMSUtils;
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


    @Resource
	BrandAccountLogService brandAccountLogService;

    @Resource
	AccountSettingService accountSettingService;

    @Resource
	BrandAccountService brandAccountService;

    @Resource
	AccountNoticeService accountNoticeService;

    
    @Override
    public GenericDao<SmsLog, Long> getDao() {
        return smslogMapper;
    }

    
    
	@Override
	public com.alibaba.fastjson.JSONObject sendCode(String phone, String code, String brandId, String shopId, int smsLogType, Map<String,String> logMap,Boolean openBrandAccount,AccountSetting accountSetting) {
        BrandUser brandUser = brandUserService.selectOneByBrandId(brandId);
		Brand brand = brandService.selectByPrimaryKey(brandId);
		//发送阿里短信返回 默认使用阿里发送短信
		com.alibaba.fastjson.JSONObject aliResult = new com.alibaba.fastjson.JSONObject();
		if(smsLogType == SmsLogType.AUTO_CODE){
			aliResult = sendMsg(code, phone,brandUser,logMap);
		}
		SmsLog smsLog = new SmsLog();
		smsLog.setBrandId(brandId);
		smsLog.setShopDetailId(shopId);
		smsLog.setContent(code);
		smsLog.setSmsType(SmsLogType.AUTO_CODE);
		smsLog.setCreateTime(new Date());
		smsLog.setPhone(phone);
		smsLog.setSmsResult(com.alibaba.fastjson.JSONObject.toJSONString(aliResult));

		BrandSetting brandSetting = brandSettingService.selectByBrandId(brandId);
		Boolean flag = false;
		if(openBrandAccount!=null&&openBrandAccount){
			flag = true;
		}else {
			flag = brandSetting.getOpenBrandAccount()==1;
		}

		//返回值中有"success":"false"时说明商家无法发短信或者该条短信发送失败,此时不更新短信账户
		if(aliResult.getBoolean("success")){ //返回成功
			try{
				/**
				 * yz 2017/07/28 计费系统 (验证码短信发送需要扣除账户信息 记录)
				 */
				if(flag){
					log.info("该品牌开启了品牌账户信息--------");
					//获取品牌账户设置
					if(accountSetting==null){
						 accountSetting = accountSettingService.selectByBrandSettingId(brandSetting.getId());
					}
					//定义每条短信的单价
					BigDecimal sms_unit = BigDecimal.ZERO;
					if(accountSetting.getOpenSendSms()==1){
						sms_unit = accountSetting.getSendSmsValue();
					}
					BrandAccount brandAccount = brandAccountService.selectByBrandId(brandId);
					//剩余账户余额
					BigDecimal remain = brandAccount.getAccountBalance().subtract(sms_unit);
					BrandAccountLog blog = new BrandAccountLog();
					blog.setCreateTime(new Date());
					blog.setGroupName(brand.getBrandName());
					blog.setBehavior(BehaviorType.SMS);
					blog.setFoundChange(sms_unit.negate());//负数
					blog.setRemain(remain);//剩余账户余额
					blog.setDetail(DetailType.SMS_CODE);
					blog.setAccountId(brandAccount.getId());
					blog.setBrandId(brandId);
					blog.setShopId(shopId);
					blog.setSerialNumber(DateUtil.getRandomSerialNumber());//这个流水号目前使用当前时间搓+4位随机字符串
					Integer accountId = brandAccount.getId();
					brandAccount = new BrandAccount();
					brandAccount.setId(accountId);
					brandAccount.setAccountBalance(remain);
					//记录品牌账户的更新日志 + 更新账户
					brandAccountLogService.logBrandAccountAndLog(blog,accountSetting,brandAccount);
					List<AccountNotice> noticeList = accountNoticeService.selectByAccountId(brandAccount.getId());
					//判断是否需要发短信通知欠费
					Result result =  BrandAccountSendUtil.sendSms(brandAccount,noticeList,brand.getBrandName(),accountSetting);
					if(result.isSuccess()){
						Long id = accountSetting.getId();
						AccountSetting as = new AccountSetting();
						as.setId(id);
						as.setType(1);
						accountSettingService.update(as);
						//发送延时消息 24小时
						log.info("发送欠费消息后把账户设置改为已发送状态,并发送消息队列。。。");
						MQMessageProducer.sendBrandAccountSms(brandId, MQSetting.DELAY_TIME);
					}
				}else {
					log.info("该品牌未开启品牌账户 -- ");
					//更新短信账户的信息
					smsAcountService.updateByBrandId(brandId);
					//判断是否要提醒商家充值短信账户
					sendNotice(brandUser,logMap);
				}
			}catch(Exception e){
				log.error("发送短信失败:"+e.getMessage());
			}
		}
		insert(smsLog);
		log.info("短信发送结果:"+ com.alibaba.fastjson.JSONObject.toJSONString(aliResult));
		return aliResult;
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

	
	public com.alibaba.fastjson.JSONObject sendMsg(String code, String phone, BrandUser brandUser, Map<String,String> logMap){
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
			com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
			jsonObject.put("msg","当前品牌已超欠费可用额度，请充值后使用短信功能");
			jsonObject.put("success","false");
			return jsonObject;
		}else{
			//剩余短信在设置的范围内
			if(this.isHave(arrs, remindNum+"")){
                String content = sb.append(logMap.get("content")).append("在固定条数短信时发短信给商家").toString();
                logMap.put("content",content);
				//发短信提醒商家
				SMSUtils.sendNoticeToBrand(brandUser.getBrandName(), remindNum, brandUser.getPhone(),logMap);
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
