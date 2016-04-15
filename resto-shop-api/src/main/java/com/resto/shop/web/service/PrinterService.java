package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.Printer;

public interface PrinterService extends GenericService<Printer, Integer> {
	/**
	 * 根据店铺ID查询信息
	 * @return
	 */
	List<Printer> selectListByShopId(String shopId);
}
