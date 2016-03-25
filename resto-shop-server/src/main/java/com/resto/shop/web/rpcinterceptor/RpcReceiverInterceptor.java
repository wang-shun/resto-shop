package com.resto.shop.web.rpcinterceptor;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.resto.brand.web.service.DatabaseConfigService;
import com.resto.shop.web.datasource.DataSourceContextHolder;

import cn.restoplus.rpc.common.bean.RpcRequest;
import cn.restoplus.rpc.common.listener.ReceiverInterceptor;

@Component
public class RpcReceiverInterceptor implements ReceiverInterceptor{
	
	@Resource
	DatabaseConfigService databaseConfigService;
	
	@Override
	public void receiver(RpcRequest request) {
		if(request.getRequestHead()!=null){
			System.err.println("Receiver request Head:");
			System.err.println(request.getRequestHead());
			
			DataSourceContextHolder.setDataSourceName(request.getRequestHead());
		}
	}

}
