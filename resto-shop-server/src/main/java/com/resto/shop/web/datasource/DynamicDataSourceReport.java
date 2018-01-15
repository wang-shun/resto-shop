package com.resto.shop.web.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.resto.brand.web.model.DatabaseConfig;
import com.resto.brand.web.service.DatabaseConfigService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicDataSourceReport extends AbstractRoutingDataSource implements InitializingBean{

	@Resource
	DatabaseConfigService databaseConfigService;

	public static final Map<Object,Object> dataSourceMap = new ConcurrentHashMap<>();

	public DynamicDataSourceReport() {
		this.setTargetDataSources(dataSourceMap);	
	}

	@Override
	protected Object determineCurrentLookupKey() {
		String dataconfigId = DataSourceContextHolderReport.getDataSourceName();
		if(!dataSourceMap.containsKey(dataconfigId)){
			DatabaseConfig config = databaseConfigService.selectByBrandId(DataSourceContextHolder.getDataSourceName());
			DruidDataSource druidDataSource = new DruidDataSource();
			druidDataSource.setUrl("jdbc:mysql://139.196.222.42:3306/shop_manager?useUnicode=true&characterEncoding=utf8");
			druidDataSource.setUrl(config.getUrl().replace("rds64fw2qrd8q0eg95nmo.mysql.rds.aliyuncs.com"，"rr-uf68ruwd0571iwmf4o.mysql.rds.aliyuncs.com"));
			druidDataSource.setUsername("viewer");
			druidDataSource.setPassword("Vino2016");
			druidDataSource.setDriverClassName(config.getDriverClassName());
			druidDataSource.setInitialSize(1);
			druidDataSource.setRemoveAbandoned(true);
			druidDataSource.setRemoveAbandonedTimeout(300);
			druidDataSource.setMinIdle(1);
			druidDataSource.setMaxActive(100);
			druidDataSource.setMaxWait(60000);
			druidDataSource.setTimeBetweenEvictionRunsMillis(60000);
			druidDataSource.setMinEvictableIdleTimeMillis(300000);
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
