package com.resto.shop.web.datasource;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.alibaba.druid.pool.DruidDataSource;
import com.resto.brand.web.model.DatabaseConfig;
import com.resto.brand.web.service.DatabaseConfigService;

public class DynamicDataSource extends AbstractRoutingDataSource implements InitializingBean{

	@Resource
	DatabaseConfigService databaseConfigService;
	
	public static final Map<Object,Object> dataSourceMap = new HashMap<>();
	
	public DynamicDataSource() {
		this.setTargetDataSources(dataSourceMap);	
	}

	@Override
	protected Object determineCurrentLookupKey() {
		String dataconfigId = DataSourceContextHolder.getDataSourceName();
		if(!dataSourceMap.containsKey(dataconfigId)){
			DatabaseConfig config = databaseConfigService.selectByBrandId(DataSourceContextHolder.getDataSourceName());
			DruidDataSource druidDataSource = new DruidDataSource();
			druidDataSource.setUrl(config.getUrl());
			druidDataSource.setUsername(config.getUsername());
			druidDataSource.setPassword(config.getPassword());
			druidDataSource.setDriverClassName(config.getDriverClassName());
			dataSourceMap.put(dataconfigId, druidDataSource);
			super.setTargetDataSources(dataSourceMap);
			super.afterPropertiesSet();
		}
		return dataconfigId;
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
	}
}
