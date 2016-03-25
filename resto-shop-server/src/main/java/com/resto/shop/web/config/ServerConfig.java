package com.resto.shop.web.config;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;

import com.alibaba.druid.pool.DruidDataSource;
import com.resto.brand.web.service.DatabaseConfigService;
import com.resto.shop.web.datasource.DynamicDataSource;

import cn.restoplus.rpc.client.RpcProxy;

@Configurable
@ImportResource({"classpath:applicationContext*.xml"})
public class ServerConfig {
	@Resource
	RpcProxy proxy;
	
	@Resource
	DruidDataSource defaultDataSource;
	
	@Bean
	public DatabaseConfigService databaseConfigService(){
		return proxy.create(DatabaseConfigService.class);
	}
	
	@Bean
	public DataSource dataSource(){
		DynamicDataSource dynamicDataSource = new DynamicDataSource(defaultDataSource);
		dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);
		return dynamicDataSource;
	}
}
