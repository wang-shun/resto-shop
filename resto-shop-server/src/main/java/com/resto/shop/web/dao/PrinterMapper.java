package com.resto.shop.web.dao;

import com.resto.shop.web.model.Printer;
import com.resto.brand.core.generic.GenericDao;

public interface PrinterMapper  extends GenericDao<Printer,Integer> {
    int deleteByPrimaryKey(Integer id);

    int insert(Printer record);

    int insertSelective(Printer record);

    Printer selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Printer record);

    int updateByPrimaryKey(Printer record);
}
