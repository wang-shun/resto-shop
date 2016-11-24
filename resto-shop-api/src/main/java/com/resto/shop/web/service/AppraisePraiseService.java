package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.AppraisePraise;

import java.util.List;

/**
 * Created by carl on 2016/11/20.
 */
public interface AppraisePraiseService extends GenericService<AppraisePraise, String> {

    void updateCancelPraise(String appraiseId, String customerId, Integer isDel);

    void updateCancelPraise(AppraisePraise appraisePraise);

    List<AppraisePraise> appraisePraiseList(String appraiseId);

    AppraisePraise selectByAppraiseIdCustomerId(String appraiseId, String customerId);

    int selectByCustomerCount(String customerId);
}
