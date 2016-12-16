package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.TableQrcodeMapper;
import com.resto.shop.web.model.TableQrcode;
import com.resto.shop.web.service.TableQrcodeService;

import javax.annotation.Resource;

/**
 * Created by carl on 2016/12/16.
 */
@RpcService
public class TableQrcodeServiceImpl extends GenericServiceImpl<TableQrcode, Long> implements TableQrcodeService {

    @Resource
    private TableQrcodeMapper tableQrcodeMapper;

    @Override
    public GenericDao<TableQrcode, Long> getDao() {
        return tableQrcodeMapper;
    }
}
