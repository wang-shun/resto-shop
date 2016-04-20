package com.resto.shop.web;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.resto.shop.web.config.ServerConfig;

public class ShopServerRpcBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShopServerRpcBootstrap.class);

    @SuppressWarnings("resource")
	public static void main(String[] args) {
        LOGGER.debug("start server");
        for(String arg:args){
        	if(arg.contains("=")){
        		String [] kv = arg.split("=");
        		String key = kv[0].replaceAll("-D", "");
        		String value = StringUtils.trimToEmpty(kv[1]);
        		LOGGER.info("set default property: "+key+"="+value);
        		System.setProperty(key,value);
        	}
        }
        
        ApplicationContext context = new AnnotationConfigApplicationContext(ServerConfig.class);
    }
}
