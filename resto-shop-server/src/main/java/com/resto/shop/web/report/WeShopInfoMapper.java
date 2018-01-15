package com.resto.shop.web.report;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.WeShopInfo;

public interface WeShopInfoMapper  extends GenericDao<WeShopInfo,Integer> {
    int deleteByPrimaryKey(Long id);

    int insert(WeShopInfo record);

    int insertSelective(WeShopInfo record);

    WeShopInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(WeShopInfo record);

    int updateByPrimaryKey(WeShopInfo record);
}
