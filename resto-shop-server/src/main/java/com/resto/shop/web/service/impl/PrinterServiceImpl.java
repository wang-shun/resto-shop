package com.resto.shop.web.service.impl;

import java.util.*;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.shop.web.constant.TicketType;
import com.resto.shop.web.constant.TicketTypeNew;
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
	public List<Printer> selectQiantai(String shopId,Integer type) {
		return printerMapper.selectQiantai(shopId,type);
	}

	@Override
	public Map<String, Object> openCashDrawer(String orderId,String shopId) {
    	Printer printer = printerMapper.getCashPrinter(shopId);
		Map<String, Object> result = new HashMap();
		if(printer != null){
			result.put("TABLE_NO","2");
			result.put("KITCHEN_NAME",printer.getName());
			result.put("PORT",printer.getPort());
			result.put("ORDER_ID",orderId);
			result.put("IP",printer.getIp());
			String print_id = ApplicationUtils.randomUUID();
			result.put("PRINT_TASK_ID", print_id);
			result.put("ADD_TIME", new Date().getTime());
			Map<String, Object> data = new HashMap<>();
			result.put("DATA",data);
			result.put("STATUS", 0);
			result.put("TICKET_TYPE", TicketType.OPENCASHDRAW);
		}
		return result;
	}


	@Override
	public Map<String, Object> openCashDrawerNew(String orderId, String shopId) {
		Printer printer = printerMapper.getCashPrinter(shopId);
		Map<String, Object> result = new HashMap();
		if(printer != null){
			result.put("TABLE_NO","2");
			result.put("KITCHEN_NAME",printer.getName());
			result.put("PORT",printer.getPort());
			result.put("ORDER_ID",orderId);
			result.put("IP",printer.getIp());
			String print_id = ApplicationUtils.randomUUID();
			result.put("PRINT_TASK_ID", print_id);
			result.put("ADD_TIME", new Date().getTime());
			Map<String, Object> data = new HashMap<>();
			result.put("DATA",data);
			result.put("STATUS", 0);
			result.put("TICKET_TYPE", TicketTypeNew.COMMAND);
			result.put("TICKET_MODE", TicketTypeNew.OPEN_CASH_DRAWER);
		}
		return result;
	}

	@Override
	public List<Printer> selectListNotSame(String shopId) {
		List<Printer> ticket =  printerMapper.selectTicketNotSame(shopId);
		List<Printer> label =  printerMapper.selectLabelNotSame(shopId);
		List<Printer> result = new ArrayList<>();
		result.addAll(ticket);
		result.addAll(label);
		return result;
	}

	@Override
	public List<Printer> selectPrintByType(String shopId, Integer type) {
		return printerMapper.selectPrintByType(shopId, type);
	}
}
