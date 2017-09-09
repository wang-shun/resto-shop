package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.core.util.DateUtil;
import com.resto.brand.core.util.MQSetting;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.model.TableQrcode;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.brand.web.service.TableQrcodeService;
import com.resto.shop.web.constant.Common;
import com.resto.shop.web.constant.PrinterType;
import com.resto.shop.web.constant.TicketType;
import com.resto.shop.web.constant.TicketTypeNew;
import com.resto.shop.web.dao.ReceiptMapper;
import com.resto.shop.web.dto.ReceiptOrder;
import com.resto.shop.web.dto.ReceiptPos;
import com.resto.shop.web.dto.ReceiptPosOrder;
import com.resto.shop.web.model.*;
import com.resto.shop.web.service.AreaService;
import com.resto.shop.web.service.PrinterService;
import com.resto.shop.web.service.ReceiptService;
import com.resto.shop.web.service.ReceiptTitleService;
import com.resto.shop.web.util.RedisUtil;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.resto.brand.core.util.HttpClient.doPostAnsc;
import static com.resto.brand.core.util.LogUtils.url;

/**
 * Created by xielc on 2017/9/5.
 */
@RpcService
public class ReceiptServiceImpl extends GenericServiceImpl<Receipt,String> implements ReceiptService {

    @Resource
    ReceiptMapper receiptMapper;

    @Resource
    ShopDetailService shopDetailService;

    @Resource
    PrinterService printerService;

    @Resource
    BrandService brandService;

    @Resource
    ReceiptTitleService receiptTitleService;

    @Autowired
    private TableQrcodeService tableQrcodeService;

    @Autowired
    private AreaService areaService;

    @Override
    public GenericDao<Receipt, String> getDao() {
        return receiptMapper;
    }

    @Override
    public int insertSelective(Receipt record){
        return receiptMapper.insertSelective(record);
    }

    @Override
    public int updateByPrimaryKeySelective(Receipt record){
        return receiptMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateState(Receipt record){
        return receiptMapper.updateState(record);
    }

    @Override
    public List<ReceiptOrder> selectReceiptOrderList(String customerId,String state){
        if(state==null||state.equals("")){
            return receiptMapper.selectApplyReceiptOrderList(customerId);
        }else{
            return receiptMapper.selectReceiptOrderList(customerId,Integer.parseInt(state));
        }
    }
    @Override
    public ReceiptPosOrder getReceiptOrderList(String receiptId){
        return receiptMapper.getReceiptOrderList(Integer.parseInt(receiptId));
    }
    @Override
    public List<ReceiptPos> getReceiptList(String shopId,String state){
        List<ReceiptPos> rlist=new ArrayList<>();
        if(state==null||state.equals("")){
            //查询当天的所有发票
            List<ReceiptPos> now = receiptMapper.getNowReceiptList(shopId,null);
            rlist.addAll(now);
            //查询以前的所有发票
            List<ReceiptPos> before = receiptMapper.getBeforeReceiptList(shopId);
            rlist.addAll(before);
        }else if(state.equals("0")){
            List<ReceiptPos> now = receiptMapper.getNowReceiptList(shopId,Integer.parseInt(state));
            rlist.addAll(now);
            List<ReceiptPos> before = receiptMapper.getBeforeReceiptList(shopId);
            rlist.addAll(before);
        }else {
            List<ReceiptPos> now = receiptMapper.getNowReceiptList(shopId,Integer.parseInt(state));
            rlist.addAll(now);
        }
        return rlist;
    }

    /**
     * 发票打印
     * @return
     */
    @Override
    public List<Map<String, Object>> printReceiptOrder(String receiptId,String ShopId){
        ReceiptPosOrder receiptPosOrder = receiptMapper.getReceiptOrderList(Integer.parseInt(receiptId));
        ShopDetail shopDetail = shopDetailService.selectById(ShopId);
        List<Map<String, Object>> printTask = new ArrayList<>();
        List<Printer> ticketPrinter=new ArrayList<>();
        ticketPrinter.addAll(printerService.selectPrintByType(ShopId,PrinterType.RECEPTION));
        for (Printer printer : ticketPrinter) {
            getReceiptOrderModel(receiptPosOrder, printer,shopDetail, printTask);
        }
        Brand brand = brandService.selectById(shopDetail.getBrandId());
        JSONArray json = new JSONArray(printTask);
        Map map = new HashMap(4);
        map.put("brandName", brand.getBrandName());
        map.put("fileName", shopDetail.getName());
        map.put("type", "posAction");
        map.put("content", "发票订单:" + receiptPosOrder.getOrderNumber() + "返回打印发票模版" + json.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(url, map);
        return printTask;
    }

    private void getReceiptOrderModel(ReceiptPosOrder receiptPosOrder, Printer printer,ShopDetail shopDetail, List<Map<String, Object>> printTask){
        String modeText="增值税发票";
        Map<String, Object> print = new HashMap<String, Object>();
        print.put("PORT", printer.getPort());
        print.put("OID", receiptPosOrder.getReceiptId());
        print.put("IP", printer.getIp());
        String print_id = ApplicationUtils.randomUUID();
        print.put("PRINT_TASK_ID", print_id);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("ORDER_ID", receiptPosOrder.getOrderNumber());
        data.put("DATETIME", DateUtil.formatDate(receiptPosOrder.getPayTime(), "yyyy-MM-dd HH:mm:ss"));
        data.put("DISTRIBUTION_MODE", modeText+receiptPosOrder.getTableNumber());
        //data.put("TABLE_NUMBER", "");
        //添加当天打印订单的序号
        TableQrcode tableQrcode = tableQrcodeService.selectByTableNumberShopId(shopDetail.getId(), Integer.valueOf(receiptPosOrder.getTableNumber()));
        if (tableQrcode == null) {
            //data.put("ORDER_NUMBER",  "---");
            data.put("TABLE_NUMBER", "---");
        } else {
            if (tableQrcode.getAreaId() == null) {
                //data.put("ORDER_NUMBER", "---");
                data.put("TABLE_NUMBER", "---");
            } else {
                Area area = areaService.selectById(tableQrcode.getAreaId());
                if (area == null) {
                    //data.put("ORDER_NUMBER", "---");
                    data.put("TABLE_NUMBER", "---");
                } else {
                    //data.put("ORDER_NUMBER", area.getName());
                    data.put("TABLE_NUMBER", area.getName());
                }
            }

        }
        ReceiptTitle receiptTitle=receiptTitleService.selectByPrimaryKey(receiptPosOrder.getReceiptTitleId());
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        Map<String, Object> itemMoney = new HashMap<String, Object>();
        itemMoney.put("ARTICLE_COUNT","订单金额:");
        itemMoney.put("ARTICLE_NAME",receiptPosOrder.getOrderMoney()+"元");
        items.add(itemMoney);
        Map<String, Object> itemName = new HashMap<String, Object>();
        itemName.put("ARTICLE_COUNT","公司抬头:");
        itemName.put("ARTICLE_NAME",receiptPosOrder.getName());
        items.add(itemName);
        Map<String, Object> itemDutyParagraph = new HashMap<String, Object>();
        itemDutyParagraph.put("ARTICLE_COUNT","    税号:");
        itemDutyParagraph.put("ARTICLE_NAME",receiptPosOrder.getDutyParagraph());
        items.add(itemDutyParagraph);
        Map<String, Object> itemCompanyAddress = new HashMap<String, Object>();
        itemCompanyAddress.put("ARTICLE_COUNT","单位地址:");
        itemCompanyAddress.put("ARTICLE_NAME",receiptTitle.getCompanyAddress());
        items.add(itemCompanyAddress);
        Map<String, Object> itemMobileNo = new HashMap<String, Object>();
        itemMobileNo.put("ARTICLE_COUNT","公司电话:");
        itemMobileNo.put("ARTICLE_NAME",receiptTitle.getMobileNo());
        items.add(itemMobileNo);
        Map<String, Object> itemBankOfDeposit = new HashMap<String, Object>();
        itemBankOfDeposit.put("ARTICLE_COUNT","开户银行:");
        itemBankOfDeposit.put("ARTICLE_NAME",receiptTitle.getBankOfDeposit());
        items.add(itemBankOfDeposit);
       Map<String, Object> itemBankNumber = new HashMap<String, Object>();
        itemBankNumber.put("ARTICLE_COUNT","银行账号:");
        itemBankNumber.put("ARTICLE_NAME",receiptTitle.getBankNumber());
        items.add(itemBankNumber);
        data.put("ITEMS", items);
        data.put("CUSTOMER_SATISFACTION", "");
        data.put("CUSTOMER_SATISFACTION_DEGREE", "");
        data.put("CUSTOMER_PROPERTY", "");
        print.put("DATA", data);
        print.put("STATUS", "0");
        print.put("TICKET_TYPE", TicketType.KITCHEN);
        //添加到 打印集合
        printTask.add(print);
        RedisUtil.set(print_id, print);
    }
}
