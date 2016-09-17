package com.resto.shop.web.dao;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.Unit;
import com.resto.shop.web.model.UnitDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by KONATA on 2016/9/11.
 */
public interface UnitMapper extends GenericDao<Unit, String> {

    List<Unit> getUnits(String shopId);

    int insertDetail(@Param("unitId") String unitId, @Param("detail") UnitDetail unitDetail);

//    int insertDetail(@Param("familyId") String familyId, @Param("detail")UnitDetail unitDetail);

    Unit getUnitById(String id);

    int deleteFamily(String id);

    int deleteDetail(String unitId);

    List<Unit> getUnitByArticleid(String articleId);
}
