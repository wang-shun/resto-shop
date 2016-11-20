package com.resto.shop.web.service.impl;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.model.AppraisePraise;
import com.resto.shop.web.service.AppraisePraiseService;

/**
 * Created by carl on 2016/11/20.
 */
public class AppraisePraiseServiceImpl extends GenericServiceImpl<AppraisePraise, String> implements AppraisePraiseService {
    @Override
    public GenericDao<AppraisePraise, String> getDao() {
        return null;
    }
}
