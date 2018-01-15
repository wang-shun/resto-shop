package com.resto.shop.web.report;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.DeliveryPoint;

import java.util.List;

public interface DeliveryPointMapper  extends GenericDao<DeliveryPoint,Integer> {
    int deleteByPrimaryKey(Integer id);

    int insert(DeliveryPoint record);

    int insertSelective(DeliveryPoint record);

    DeliveryPoint selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(DeliveryPoint record);

    int updateByPrimaryKey(DeliveryPoint record);

    List<DeliveryPoint> selectListById(String currentShopId);
}
