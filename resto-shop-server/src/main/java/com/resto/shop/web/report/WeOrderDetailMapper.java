package com.resto.shop.web.report;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.WeOrderDetail;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

public interface WeOrderDetailMapper  extends GenericDao<WeOrderDetail,Integer> {
    int deleteByPrimaryKey(Long id);

    int insert(WeOrderDetail record);

    int insertSelective(WeOrderDetail record);

    WeOrderDetail selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(WeOrderDetail record);

    int updateByPrimaryKey(WeOrderDetail record);

    WeOrderDetail selectWeOrderByShopIdAndTime(@Param("shopId") String shopId, @Param("createTime") Date date);
}
