package com.resto.shop.web.dao;

import com.resto.shop.web.model.NewEmployee;
import com.resto.brand.core.generic.GenericDao;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NewEmployeeMapper  extends GenericDao<NewEmployee,String> {
    int deleteByPrimaryKey(String id);

    int insert(NewEmployee record);

    int insertSelective(NewEmployee record);

    NewEmployee selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(NewEmployee record);

    int updateByPrimaryKey(NewEmployee record);

    List<NewEmployee> selectByIds(List<String> ids);

    List<NewEmployee> selectByShopId(@Param("shopId") String shopId);
}
