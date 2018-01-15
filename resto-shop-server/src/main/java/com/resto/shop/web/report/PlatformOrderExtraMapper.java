package com.resto.shop.web.report;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.PlatformOrderExtra;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PlatformOrderExtraMapper  extends GenericDao<PlatformOrderExtra,String> {
    int deleteByPrimaryKey(String id);

    int insert(PlatformOrderExtra record);

    int insertSelective(PlatformOrderExtra record);

    PlatformOrderExtra selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PlatformOrderExtra record);

    int updateByPrimaryKey(PlatformOrderExtra record);

    List<PlatformOrderExtra> selectByPlatformOrderId(@Param("platformOrderId") String platformOrderId);
}
