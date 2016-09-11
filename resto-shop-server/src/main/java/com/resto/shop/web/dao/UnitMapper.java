package com.resto.shop.web.dao;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.Unit;

import java.util.List;

/**
 * Created by KONATA on 2016/9/11.
 */
public interface UnitMapper extends GenericDao<Unit,String> {

    List<Unit> getUnits(String shopId);

}
