package com.resto.shop.web.datasource;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.alibaba.druid.pool.DruidDataSource;
import com.resto.brand.web.model.DatabaseConfig;
import com.resto.brand.web.service.DatabaseConfigService;

public class DynamicDataSource extends AbstractRoutingDataSource{

	@Resource
	DatabaseConfigService databaseConfigService;
	
	public static final Map<Object,Object> dataSourceMap = new HashMap<>();
	
	public DynamicDataSource(DruidDataSource defaultDataSource) {
		dataSourceMap.put("defaultDataSource", defaultDataSource);
		this.setTargetDataSources(dataSourceMap);	}

	@Override
	protected Object determineCurrentLookupKey() {
		if(dataSourceMap.get(DataSourceContextHolder.getDataSourceName())==null){
			DatabaseConfig config = databaseConfigService.selectByBrandId(DataSourceContextHolder.getDataSourceName());
			DruidDataSource dataSource = new DruidDataSource();
			dataSource.setUrl(config.getUrl());
			dataSource.setUsername(config.getUsername());
			dataSource.setPassword(config.getPassword());
			dataSource.setDriverClassName(config.getDriverClassName());
			dataSourceMap.put(DataSourceContextHolder.getDataSourceName(), dataSource);
			super.setTargetDataSources(dataSourceMap);
		}
		return DataSourceContextHolder.getDataSourceName();
	}

}
