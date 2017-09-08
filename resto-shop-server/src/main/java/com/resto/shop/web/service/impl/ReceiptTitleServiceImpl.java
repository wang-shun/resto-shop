package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.ReceiptTitleMapper;
import com.resto.shop.web.model.ReceiptTitle;
import com.resto.shop.web.service.ReceiptTitleService;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by xielc on 2017/9/5.
 */
@RpcService
public class ReceiptTitleServiceImpl extends GenericServiceImpl<ReceiptTitle,String> implements ReceiptTitleService {
    @Resource
    ReceiptTitleMapper receiptTitleMapper;

    @Override
    public GenericDao<ReceiptTitle, String> getDao() {
        return receiptTitleMapper;
    }

    @Override
    public int insertSelective(ReceiptTitle record){
        if(record.getState()==1){
            ReceiptTitle state=new ReceiptTitle();
            state.setState(0);
            receiptTitleMapper.updateByState(state);
        }
        return receiptTitleMapper.insertSelective(record);
    }

    @Override
    public int updateByPrimaryKeySelective(ReceiptTitle record){
        if(record.getState()==1){
            ReceiptTitle state=new ReceiptTitle();
            state.setState(0);
            receiptTitleMapper.updateByState(state);
        }
        return receiptTitleMapper.updateByPrimaryKeySelective(record);
    }
    @Override
    public ReceiptTitle selectByPrimaryKey(String id){
        return receiptTitleMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<ReceiptTitle> selectOneList(String customerId){
        return  receiptTitleMapper.selectOneList(customerId);
    }

    @Override
    public List<ReceiptTitle> selectTypeList(String customerId,String type){
        return receiptTitleMapper.selectTypeList(customerId,Integer.parseInt(type));
    }

}
