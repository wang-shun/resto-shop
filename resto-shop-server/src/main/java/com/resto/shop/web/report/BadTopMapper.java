package com.resto.shop.web.report;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.BadTop;

public interface BadTopMapper  extends GenericDao<BadTop,Long> {
    int deleteByPrimaryKey(Long id);

    int insert(BadTop record);

    int insertSelective(BadTop record);

    BadTop selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(BadTop record);

    int updateByPrimaryKey(BadTop record);
}
