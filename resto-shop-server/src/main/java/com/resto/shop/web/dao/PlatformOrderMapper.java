package com.resto.shop.web.dao;

import com.resto.shop.web.model.PlatformOrder;
import com.resto.brand.core.generic.GenericDao;
import org.apache.ibatis.annotations.Param;

public interface PlatformOrderMapper  extends GenericDao<PlatformOrder,String> {
    int deleteByPrimaryKey(String id);

    int insert(PlatformOrder record);

    int insertSelective(PlatformOrder record);

    PlatformOrder selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PlatformOrder record);

    int updateByPrimaryKeyWithBLOBs(PlatformOrder record);

    int updateByPrimaryKey(PlatformOrder record);

    PlatformOrder selectByPlatformOrderId(@Param("platformOrderId") String platformOrderId, @Param("type") Integer type);
}
