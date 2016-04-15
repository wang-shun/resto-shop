package com.resto.shop.web.config;

import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;

import com.resto.brand.web.service.DatabaseConfigService;
import com.resto.shop.web.datasource.DynamicDataSource;

import cn.restoplus.rpc.client.RpcProxy;

@Configurable
@ImportResource({"classpath:applicationContext.xml"})
public class ServerConfig {
	@Resource
	RpcProxy proxy;
	
	@Bean
	public DatabaseConfigService databaseConfigService(){
		return proxy.create(DatabaseConfigService.class);
	}
	
	
	
	@Bean
	public DynamicDataSource dataSource(){
		return new DynamicDataSource();
	}
}
