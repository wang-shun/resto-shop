package com.resto.shop.web.report;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.WeOrder;

public interface WeOrderMapper  extends GenericDao<WeOrder,Integer> {
    int deleteByPrimaryKey(Long id);

    int insert(WeOrder record);

    int insertSelective(WeOrder record);

    WeOrder selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(WeOrder record);

    int updateByPrimaryKey(WeOrder record);
}
