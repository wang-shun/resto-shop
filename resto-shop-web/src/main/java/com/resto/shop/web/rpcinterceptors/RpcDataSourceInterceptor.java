package com.resto.shop.web.rpcinterceptors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.resto.shop.web.config.SessionKey;

import cn.restoplus.rpc.common.bean.RpcRequest;
import cn.restoplus.rpc.common.listener.SendInterceptor;



public class RpcDataSourceInterceptor implements SendInterceptor{
	Logger log = LoggerFactory.getLogger(getClass());

	@Override
    public void beforeSend(RpcRequest request) {
        String interfaceName = request.getInterfaceName();
        if(interfaceName.matches("^com.resto.shop.web.service.*") || interfaceName.matches("^com.resto.scm.web.service.*")){
            HttpServletRequest httpRequest = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
            String brandId = (String) httpRequest.getSession().getAttribute(SessionKey.CURRENT_BRAND_ID);
            request.setRequestHead(brandId);
            //生成接口文档 以后前后端分离从redis里面取出  模拟
            //request.setRequestHead("31946c940e194311b117e3fff5327215");
            //httpRequest.getSession().setAttribute(SessionKey.CURRENT_SHOP_ID,"31164cebcc4b422685e8d9a32db12ab8");
            if(log.isInfoEnabled()){
                log.info(request.getInterfaceName()+" add head:"+request.getRequestHead());
            }
        }
    }


	public static void main(String[] args) {
		boolean b = "com.resto.shop.web.service.AdvertService".matches("^com.resto.shop.web.service.*");
					 
		System.out.println(b);
	}
	
}
