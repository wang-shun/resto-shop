package com.resto.shop.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.resto.shop.web.config.ServerConfig;

public class ShopServerRpcBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShopServerRpcBootstrap.class);

    @SuppressWarnings("resource")
	public static void main(String[] args) {
        LOGGER.debug("start server");
        new AnnotationConfigApplicationContext(ServerConfig.class);
    }
}
