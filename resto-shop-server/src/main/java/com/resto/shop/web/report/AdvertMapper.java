package com.resto.shop.web.report;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.Advert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdvertMapper  extends GenericDao<Advert,Integer> {
    int deleteByPrimaryKey(Integer id);

    int insert(Advert record);

    int insertSelective(Advert record);

    Advert selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Advert record);

    int updateByPrimaryKeyWithBLOBs(Advert record);

    int updateByPrimaryKey(Advert record);
    
    /**
     * 根据店铺ID查询信息
     * @return
     */
    List<Advert> selectListByShopId(@Param(value = "shopId") String currentShopId);
}
