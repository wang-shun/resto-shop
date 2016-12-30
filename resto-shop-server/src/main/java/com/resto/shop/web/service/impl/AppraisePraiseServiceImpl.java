package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.AppraisePraiseMapper;
import com.resto.shop.web.model.AppraisePraise;
import com.resto.shop.web.service.AppraisePraiseService;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by carl on 2016/11/20.
 */
@RpcService
public class AppraisePraiseServiceImpl extends GenericServiceImpl<AppraisePraise, String> implements AppraisePraiseService {

    @Resource
    private AppraisePraiseMapper appraisePraiseMapper;

    @Override
    public GenericDao<AppraisePraise, String> getDao() {
        return appraisePraiseMapper;
    }

    @Override
    public AppraisePraise updateCancelPraise(String appraiseId, String customerId, Integer isDel) {
        appraisePraiseMapper.updateCancelPraise(appraiseId, customerId, isDel);
        AppraisePraise appraisePraise = appraisePraiseMapper.selectByAppraiseIdCustomerId(appraiseId, customerId);
        return appraisePraise;
    }

    @Override
    public AppraisePraise updateCancelPraise(AppraisePraise appraisePraise) {
        appraisePraiseMapper.insertSelective(appraisePraise);
        return appraisePraise;
    }

    @Override
    public List<AppraisePraise> appraisePraiseList(String appraiseId) {
        return appraisePraiseMapper.appraisePraiseList(appraiseId);
    }

    @Override
    public AppraisePraise selectByAppraiseIdCustomerId(String appraiseId, String customerId) {
        return appraisePraiseMapper.selectByAppraiseIdCustomerId(appraiseId, customerId);
    }

    @Override
    public int selectByCustomerCount(String customerId) {
        return appraisePraiseMapper.selectByCustomerCount(customerId);
    }
}
