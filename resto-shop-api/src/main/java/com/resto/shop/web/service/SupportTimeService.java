package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.SupportTime;

public interface SupportTimeService extends GenericService<SupportTime, Integer> {

    List<SupportTime> selectList(String shopDetailId);

	void saveSupportTimes(String articleId, Integer[] supportTimes);

	List<Integer> selectByIdsArticleId(String articleId);
    
}
