package com.resto.shop.web.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.PrinterMapper;
import com.resto.shop.web.model.Printer;
import com.resto.shop.web.service.PrinterService;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class PrinterServiceImpl extends GenericServiceImpl<Printer, Integer> implements PrinterService {

    @Resource
    private PrinterMapper printerMapper;

    @Override
    public GenericDao<Printer, Integer> getDao() {
        return printerMapper;
    }

	@Override
	public List<Printer> selectListByShopId(String shopId) {
		return printerMapper.selectListByShopId(shopId);
	}



	@Override
	public List<Printer> selectByShopAndType(String orderId, int reception) {
		return printerMapper.selectByShopAndType(orderId,reception);
	}


	@Override
	public Integer checkError(String shopId) {
		return printerMapper.checkError(shopId) ;
	}

	@Override
	public List<Printer> selectQiantai(String shopId) {
		return printerMapper.selectQiantai(shopId);
	}
}
