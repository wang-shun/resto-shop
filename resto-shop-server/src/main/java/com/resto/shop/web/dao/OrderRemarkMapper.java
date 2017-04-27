package com.resto.shop.web.dao;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.OrderRemark;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderRemarkMapper extends GenericDao<OrderRemark, String>{

    List<OrderRemark> selectOrderRemarks(@Param("shopId") String shopId);
}
