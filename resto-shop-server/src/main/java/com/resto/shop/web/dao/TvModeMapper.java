package com.resto.shop.web.dao;

import com.resto.shop.web.model.TvMode;
import com.resto.brand.core.generic.GenericDao;

public interface TvModeMapper  extends GenericDao<TvMode,Integer> {
    int insert(TvMode record);

    int insertSelective(TvMode record);
}
