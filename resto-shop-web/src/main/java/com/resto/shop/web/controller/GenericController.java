package com.resto.shop.web.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.model.SysError;
import com.resto.brand.web.service.SysErrorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.resto.brand.core.entity.DataVailedException;
import com.resto.brand.core.entity.JSONResult;
import com.resto.brand.core.entity.Result;
import com.resto.brand.core.feature.orm.mybatis.Page;
import com.resto.brand.web.model.BrandUser;
import com.resto.shop.web.config.SessionKey;
import org.springframework.web.context.request.ServletWebRequest;

@Component
public abstract class GenericController{

	protected Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private SysErrorService sysErrorService;

	public Result getSuccessResult() {
		return getSuccessResult(null);
	}
	
	public Result getSuccessResult(Object data){
		if(data==null){
			return new Result(true);
		}
		JSONResult<Object> result = new JSONResult<Object>(data); 
		return result;
	}

    public Result getKcSuccessResult(Object data, BigDecimal sum,String beginDate,String endDate){
        if(data==null){
            return new Result(true);
        }
        JSONResult<Object> result = new JSONResult<Object>(data);
        result.setMessage("当前查询时间为:"+beginDate+"到"+endDate+"当前时间微信支付总额为:"+sum);
        return result;
    }


	@ExceptionHandler(value = {Exception.class})
	public void throwExceptionHandler(HttpServletRequest request,HttpServletResponse response,Exception ex){
		String shopDetailId = getCurrentShopId() == null ? null : getCurrentShopId();
		String brandId = getCurrentBrandId() == null ? null : getCurrentBrandId();
		log.error("店铺id:"+ shopDetailId+",品牌id:"+brandId+",ErrorType:"+ex.toString()+",请求url:"+request.getRequestURL().toString()
				+",ErrorMsg:"+ex.getMessage());
	}



	public HttpServletRequest getRequest(){
		return  ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest(); 
	}

	/**
	 * name:yjuany
	 * @return
	 */
	public HttpServletResponse getResponse(){


		return ((ServletWebRequest)RequestContextHolder.getRequestAttributes()).getResponse();
	}
	
	public HttpSession getSession(){
		return getRequest().getSession();
	}
	
	public Map<String,String> getParams(){
		HttpServletRequest request = getRequest();
		Map<String,String> params = new HashMap<String,String>();
		Map<String, String[]> requestParams = request.getParameterMap();
		for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = iter.next();
			String[] values =  requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			params.put(name, valueStr);
		}
		return params;
	}
	
	@SuppressWarnings("rawtypes")
	public Page<?> getPage(){
		Map<String,String> params = getParams();
		if(params.containsKey("pageSize")&&params.containsKey("pageNo")){
			Page<?> page = new Page(getInt("pageSize"),getInt("pageNo"));
			return page;
		}else{
			return null;
		}
	}
	
	public int getInt(String key){
		return getInt(key, 0);
	}
	
	public int getInt(String key,int defaultValue){
		return getObject(key)==null?defaultValue:Integer.parseInt(getObject(key).toString());
	}
	
	public Long getLong(String key,long defaultValue){
		return getObject(key)==null?defaultValue:Long.valueOf(getObject(key).toString());
	}
	
	public Long getLong(String key){
		return getLong(key, 0);
	}
	public String getString(String key){
		return getString(key, "");
	}
	
	public String getString(String key,String defaultValue){
		return getObject(key)==null?defaultValue:String.valueOf(getObject(key));
	}
	
	public Object getObject(String key){
		return getParams().get(key);
	}
	public JSONPObject getJSONPObject(Object data){
		return new JSONPObject(getRequest().getParameter("callback"), data);
	}

	public String getCurrentShopId(){
		if( getCurrentBrandUser() != null){
			return getCurrentBrandUser().getShopDetailId();
		}else{
			return null;
		}
	}

	public String getCurrentBrandId(){
		if(getCurrentBrandUser() != null){
			return getCurrentBrandUser().getBrandId();
		}else{
			return null;
		}
	}

	public BrandUser getCurrentBrandUser(){
		return (BrandUser) getRequest().getSession().getAttribute(SessionKey.USER_INFO);
	}

	public String getCurrentUserId(){
		return getCurrentBrandUser().getId();
	}

	public String getBrandName(){
		return getCurrentBrandUser().getBrandName();
	}

    public List<ShopDetail> getCurrentShopDetails(){
        return (List<ShopDetail>) getRequest().getSession().getAttribute(SessionKey.CURRENT_SHOP_NAMES);
    }
	
	public String getBaseUrl(){
		HttpServletRequest request = getRequest();
		String path = request.getContextPath();
		String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
		return basePath;
	}

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setAutoGrowCollectionLimit(Integer.MAX_VALUE);
    }

	/**
	 * 计算两个日期之间相差的天数
	 * @param smdate 较小的时间
	 * @param bdate  较大的时间
	 * @return 相差天数
	 * @throws ParseException
	 */
	public static int daysBetween(Date smdate,Date bdate) throws ParseException
	{
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		smdate=sdf.parse(sdf.format(smdate));
		bdate=sdf.parse(sdf.format(bdate));
		Calendar cal = Calendar.getInstance();
		cal.setTime(smdate);
		long time1 = cal.getTimeInMillis();
		cal.setTime(bdate);
		long time2 = cal.getTimeInMillis();
		long between_days=(time2-time1)/(1000*3600*24);

		return Integer.parseInt(String.valueOf(between_days));
	}

	/**
	 *字符串的日期格式的计算
	 */
	public static int daysBetween(String smdate,String bdate) throws ParseException{
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.setTime(sdf.parse(smdate));
		long time1 = cal.getTimeInMillis();
		cal.setTime(sdf.parse(bdate));
		long time2 = cal.getTimeInMillis();
		long between_days=(time2-time1)/(1000*3600*24);

		return Integer.parseInt(String.valueOf(between_days));
	}

	public Integer getMonthDay(String year, String month){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, Integer.parseInt(year));
		calendar.set(Calendar.MONTH, Integer.parseInt(month) - 1);
		return calendar.getActualMaximum(Calendar.DATE);
	}

	public Date getBeginDay(String year, String month, Integer day){
		Calendar beginDate = Calendar.getInstance();
		beginDate.set(Calendar.YEAR, Integer.parseInt(year));
		beginDate.set(Calendar.MONTH, Integer.parseInt(month) - 1);
		beginDate.set(Calendar.DATE, day + 1);
		beginDate.set(Calendar.HOUR_OF_DAY, 0);
		beginDate.set(Calendar.MINUTE, 0);
		beginDate.set(Calendar.SECOND,1);
		return beginDate.getTime();
	}

	public Date getEndDay(String year, String month, Integer day){
		Calendar endDate = Calendar.getInstance();
		endDate.set(Calendar.YEAR, Integer.parseInt(year));
		endDate.set(Calendar.MONTH, Integer.parseInt(month) - 1);
		endDate.set(Calendar.DATE, day + 1);
		endDate.set(Calendar.HOUR_OF_DAY, 23);
		endDate.set(Calendar.MINUTE, 59);
		endDate.set(Calendar.SECOND,59);
		return endDate.getTime();
	}
}
