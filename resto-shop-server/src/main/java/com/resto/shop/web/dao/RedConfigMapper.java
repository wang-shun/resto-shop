package com.resto.shop.web.dao;

import com.resto.shop.web.model.RedConfig;
import com.resto.brand.core.generic.GenericDao;

public interface RedConfigMapper  extends GenericDao<RedConfig,Long> {
    int deleteByPrimaryKey(Long id);

    int insert(RedConfig record);

    int insertSelective(RedConfig record);

    RedConfig selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RedConfig record);

    int updateByPrimaryKey(RedConfig record);
}
