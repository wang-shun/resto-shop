package com.resto.shop.web.dao;

import com.resto.shop.web.model.TableCode;
import com.resto.brand.core.generic.GenericDao;

public interface TableCodeMapper  extends GenericDao<TableCode,String> {
    int deleteByPrimaryKey(String id);

    int insert(TableCode record);

    int insertSelective(TableCode record);

    TableCode selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(TableCode record);

    int updateByPrimaryKey(TableCode record);

    TableCode selectByName(String name);

    TableCode selectByCodeNumber(String codeNumber);

    TableCode selectByPersonNumber(Integer personNumber);
}
