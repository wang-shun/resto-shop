package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.DateUtil;
import com.resto.shop.web.dao.TableCodeMapper;
import com.resto.shop.web.model.TableCode;
import com.resto.shop.web.service.TableCodeService;
import cn.restoplus.rpc.server.RpcService;

import java.util.Date;

/**
 *
 */
@RpcService
public class TableCodeServiceImpl extends GenericServiceImpl<TableCode, String> implements TableCodeService {

    @Resource
    private TableCodeMapper tablecodeMapper;

    @Override
    public GenericDao<TableCode, String> getDao() {
        return tablecodeMapper;
    }

    @Override
    public void insertTableCode(TableCode tablecode) {

        //做后台验证
        if(tablecode.getMinNumber()>tablecode.getMaxNumber()){
            return;
        }

        //插入时间
        tablecode.setCreateTime(new Date());
        tablecode.setEndTime(tablecode.getCreateTime());
    }

    @Override
    public void updateTableCode(TableCode tablecode) {
        //更新时间
        tablecode.setEndTime(new Date());
    }

    @Override
    public TableCode selectByName(String name) {
        return tablecodeMapper.selectByName(name);
    }

    @Override
    public TableCode selectByCodeNumber(String codeNumber) {
        return tablecodeMapper.selectByCodeNumber(codeNumber);
    }


}
