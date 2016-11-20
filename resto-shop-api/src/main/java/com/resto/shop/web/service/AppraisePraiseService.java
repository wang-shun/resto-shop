package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.AppraisePraise;

/**
 * Created by carl on 2016/11/20.
 */
public interface AppraisePraiseService extends GenericService<AppraisePraise, String> {

    void updateCancelPraise(String id);

}
