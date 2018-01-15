package com.resto.shop.web.report;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.PosUser;

public interface PosUserMapper  extends GenericDao<PosUser,Long> {
    int deleteByPrimaryKey(Long id);

    int insert(PosUser record);

    int insertSelective(PosUser record);

    PosUser selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PosUser record);

    int updateByPrimaryKey(PosUser record);
}
