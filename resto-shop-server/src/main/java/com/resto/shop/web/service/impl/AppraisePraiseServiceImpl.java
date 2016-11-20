package com.resto.shop.web.service.impl;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.AppraisePraiseMapper;
import com.resto.shop.web.model.AppraisePraise;
import com.resto.shop.web.service.AppraisePraiseService;

import javax.annotation.Resource;

/**
 * Created by carl on 2016/11/20.
 */
public class AppraisePraiseServiceImpl extends GenericServiceImpl<AppraisePraise, String> implements AppraisePraiseService {

    @Resource
    private AppraisePraiseMapper appraisePraiseMapper;

    @Override
    public GenericDao<AppraisePraise, String> getDao() {
        return appraisePraiseMapper;
    }

    @Override
    public void updateCancelPraise(String id) {
        appraisePraiseMapper.updateCancelPraise(id);
    }
}
