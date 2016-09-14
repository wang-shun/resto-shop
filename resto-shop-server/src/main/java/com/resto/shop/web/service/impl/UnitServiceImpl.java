package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.shop.web.dao.UnitMapper;
import com.resto.shop.web.model.SupportTime;
import com.resto.shop.web.model.Unit;
import com.resto.shop.web.model.UnitDetail;
import com.resto.shop.web.model.UnitFamily;
import com.resto.shop.web.service.UnitService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by KONATA on 2016/9/11.
 */
@RpcService
public class UnitServiceImpl extends GenericServiceImpl<Unit, String> implements UnitService{

    @Autowired
    private UnitMapper unitMapper;

    @Override
    public GenericDao<Unit, String> getDao() {
        return unitMapper;
    }

    @Override
    public List<Unit> getUnits(String shopId) {
        return unitMapper.getUnits(shopId);
    }

    @Override
    public void insertFamily(Unit unit) {
        for(UnitFamily unitFamily: unit.getFamilies()){
            String familyId =  ApplicationUtils.randomUUID();
            unitFamily.setId(familyId);
            unitMapper.insertFamily(unit.getId(),unitFamily);
            if(unitFamily.getDetailList() != null ){
                for(UnitDetail detail : unitFamily.getDetailList()){
                    String detailId =  ApplicationUtils.randomUUID();
                    detail.setId(detailId);
                    unitMapper.insertDetail(familyId,detail);

                }
            }

        }
    }

    @Override
    public Unit getUnitById(String id) {
        return unitMapper.getUnitById(id);
    }

    @Override
    public void initUnit(Unit unit) {
        unitMapper.deleteFamily(unit.getId());
        for(UnitFamily unitFamily : unit.getFamilies()){
            unitMapper.deleteDetail(unitFamily.getId());
        }
    }

    @Override
    public Unit getUnitByArticleid(String articleId) {
        return unitMapper.getUnitByArticleid(articleId);
    }
}
