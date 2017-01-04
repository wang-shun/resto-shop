package com.resto.shop.web.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.Appraise;

public interface AppraiseMapper  extends GenericDao<Appraise,String> {
    int deleteByPrimaryKey(String id);

    int insert(Appraise record);

    int insertSelective(Appraise record);

    Appraise selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Appraise record);

    int updateByPrimaryKey(Appraise record);
    
    List<Appraise> listAppraise(@Param(value = "currentShopId") String currentShopId,@Param(value = "currentPage") Integer currentPage,@Param(value = "showCount") Integer showCount,@Param(value = "maxLevel") Integer maxLevel,@Param(value = "minLevel") Integer minLevel);
    
    Map<String, Object> appraiseCount(@Param(value="currentShopId") String currentShopId);
    
    List<Map<String, Object>> appraiseMonthCount(String currentShopId);

	Appraise selectDetailedById(String appraiseId);

    List<Appraise> selectDeatilByOrderId(String orderId);

    Appraise selectAppraiseByCustomerId(@Param("customerId")String customerId,@Param("shopId") String shopId);

    Appraise selectAppraiseByCustomer(@Param("customerId")String customerId,@Param("shopId") String shopId);
    
    List<Appraise> selectCustomerAllAppraise(@Param(value = "customerId") String customerId, @Param(value = "currentPage") Integer currentPage, @Param(value = "showCount") Integer showCount);

    int selectByCustomerCount(String customerId);

    List<Appraise> selectByGoodAppraise();
}
