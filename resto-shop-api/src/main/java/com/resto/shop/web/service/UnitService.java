package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.Unit;

import java.util.List;

/**
 * Created by KONATA on 2016/9/11.
 */
public interface UnitService extends GenericService<Unit, String> {

   List<Unit> getUnits(String shopId);

   void insertFamily(Unit unit);

   Unit getUnitById(String id);

   void initUnit(Unit unit);

   Unit getUnitByArticleid(String articleId);


}
