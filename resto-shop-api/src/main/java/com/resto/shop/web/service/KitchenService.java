package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.Kitchen;

public interface KitchenService extends GenericService<Kitchen, Integer> {
	/**
	 * 根据店铺ID查询信息
	 * @return
	 */
	List<Kitchen> selectListByShopId(String shopId);
}
