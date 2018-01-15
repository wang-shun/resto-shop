package com.resto.shop.web.report;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.ChargeSetting;

import java.util.List;

public interface ChargeSettingMapper  extends GenericDao<ChargeSetting,String> {
    int deleteByPrimaryKey(String id);

    int insert(ChargeSetting record);

    int insertSelective(ChargeSetting record);

    ChargeSetting selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ChargeSetting record);

    int updateByPrimaryKey(ChargeSetting record);

    List<ChargeSetting> selectListByShopId();

    List<ChargeSetting> selectListByShopIdAll();

    List<ChargeSetting> selectListByShopIdAndType(int flag);
}
