package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.TableGroupMapper;
import com.resto.shop.web.model.TableGroup;
import com.resto.shop.web.service.TableGroupService;

import javax.annotation.Resource;

/**
 * Created by carl on 2017/9/25.
 */
@RpcService
public class TableGroupServiceImpl extends GenericServiceImpl<TableGroup, Long> implements TableGroupService {

    @Resource
    private TableGroupMapper tableGroupMapper;

    @Override
    public GenericDao<TableGroup, Long> getDao() {
        return tableGroupMapper;
    }
}
