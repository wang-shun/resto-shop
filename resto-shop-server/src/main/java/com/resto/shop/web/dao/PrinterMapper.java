package com.resto.shop.web.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.Printer;

public interface PrinterMapper  extends GenericDao<Printer,Integer> {
    int deleteByPrimaryKey(Integer id);

    int insert(Printer record);

    int insertSelective(Printer record);

    Printer selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Printer record);

    int updateByPrimaryKey(Printer record);
    
    /**
     * 根据店铺ID查询信息
     * @param currentShopId
     * @return
     */
    List<Printer> selectListByShopId(@Param(value = "shopId") String currentShopId);
}
