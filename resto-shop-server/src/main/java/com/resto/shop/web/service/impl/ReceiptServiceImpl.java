package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.ReceiptMapper;
import com.resto.shop.web.dto.ReceiptOrder;
import com.resto.shop.web.model.Receipt;
import com.resto.shop.web.service.ReceiptService;
import javax.annotation.Resource;
import java.util.List;

/**
 * Created by xielc on 2017/9/5.
 */
@RpcService
public class ReceiptServiceImpl extends GenericServiceImpl<Receipt,String> implements ReceiptService {
    @Resource
    ReceiptMapper receiptMapper;

    @Override
    public GenericDao<Receipt, String> getDao() {
        return receiptMapper;
    }

    @Override
    public int insertSelective(Receipt record){
        return receiptMapper.insertSelective(record);
    }

    @Override
    public List<ReceiptOrder> selectReceiptOrderList(String customerId,String state){
        return receiptMapper.selectReceiptOrderList(customerId,Integer.parseInt(state));
    }
}
