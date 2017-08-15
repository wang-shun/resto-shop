package com.resto.shop.web.service;

import java.util.List;
import java.util.Map;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.Printer;

public interface PrinterService extends GenericService<Printer, Integer> {
	/**
	 * 根据店铺ID查询信息
	 * @return
	 */
	List<Printer> selectListByShopId(String shopId);

	List<Printer> selectByShopAndType(String shopId, int reception);


	Integer checkError(String shopId);

	List<Printer> selectQiantai(String shopId,Integer type);

	Map<String, Object> openCashDrawer(String orderId,String shopId);

	Map<String, Object> openCashDrawerNew(String orderId,String shopId);

	List<Printer> selectListNotSame(String shopId);

	List<Printer> selectPrintByType(String shopId, Integer type);
}
