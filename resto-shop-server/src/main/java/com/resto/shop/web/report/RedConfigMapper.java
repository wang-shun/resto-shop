package com.resto.shop.web.report;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.RedConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RedConfigMapper  extends GenericDao<RedConfig,Long> {
    int deleteByPrimaryKey(Long id);

    int insert(RedConfig record);

    int insertSelective(RedConfig record);

    RedConfig selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RedConfig record);

    int updateByPrimaryKey(RedConfig record);
    
    //根据店铺ID查询信息
    List<RedConfig> selectListByShopId(@Param(value = "shopId") String currentShopId);
}
