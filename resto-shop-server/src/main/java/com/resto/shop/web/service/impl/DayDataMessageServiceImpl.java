package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.DateUtil;
import com.resto.shop.web.dao.DayDataMessageMapper;
import com.resto.shop.web.model.DayDataMessage;
import com.resto.shop.web.service.DayDataMessageService;
import cn.restoplus.rpc.server.RpcService;

import java.util.Date;
import java.util.List;

/**
 *
 */
@RpcService
public class DayDataMessageServiceImpl extends GenericServiceImpl<DayDataMessage, String> implements DayDataMessageService {

    @Resource
    private DayDataMessageMapper daydatamessageMapper;

    @Override
    public GenericDao<DayDataMessage, String> getDao() {
        return daydatamessageMapper;
    }

    @Override
    public List<DayDataMessage> selectListByTime(int normal, String date, int dayMessage) {
        Date dateTime = DateUtil.fomatDate(date);
        return daydatamessageMapper.selectListByTime(normal,dayMessage,dateTime);
    }
}
