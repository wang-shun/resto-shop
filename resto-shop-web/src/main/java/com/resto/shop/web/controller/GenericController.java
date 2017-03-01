package com.resto.shop.web.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
}
