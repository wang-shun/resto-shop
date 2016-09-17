package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.shop.web.dao.UnitMapper;
import com.resto.shop.web.model.Unit;
import com.resto.shop.web.model.UnitDetail;
import com.resto.shop.web.service.UnitService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by KONATA on 2016/9/11.
 */
@RpcService
public class UnitServiceImpl extends GenericServiceImpl<Unit, String> implements UnitService {

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
    public void insertDetail(Unit unit) {
        for (UnitDetail unitDetail : unit.getDetails()) {
            String detailId = ApplicationUtils.randomUUID();
            unitDetail.setId(detailId);
            unitMapper.insertDetail(unit.getId(), unitDetail);
        }
    }

    @Override
    public Unit getUnitById(String id) {
        return unitMapper.getUnitById(id);
    }

    @Override
    public void initUnit(Unit unit) {
        unitMapper.deleteDetail(unit.getId());
    }

    @Override
    public List<Unit> getUnitByArticleid(String articleId) {
        return unitMapper.getUnitByArticleid(articleId);
    }

    @Override
    public void insertArticleRelation(String articleId, List<Unit> units) {
        for(Unit unit : units){
            String id = ApplicationUtils.randomUUID();
            unitMapper.insertArticleRelation(articleId,id,unit);
            for(UnitDetail unitDetail : unit.getDetails()){
                String detailID = ApplicationUtils.randomUUID();
                unitMapper.insertUnitDetailRelation(detailID,id,unitDetail);
            }
        }
    }
}
