package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.shop.web.dao.UnitMapper;
import com.resto.shop.web.model.Unit;
import com.resto.shop.web.model.UnitArticle;
import com.resto.shop.web.model.UnitDetail;
import com.resto.shop.web.service.UnitService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
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
    public List<Unit> getUnitsByArticleId(String shopId, String articleId) {
        return unitMapper.getUnitsByArticleId(shopId, articleId);
    }

    @Override
    public Unit insertDetail(Unit unit) {
        for (UnitDetail unitDetail : unit.getDetails()) {
            String detailId = ApplicationUtils.randomUUID();
            unitDetail.setId(detailId);
            unitMapper.insertDetail(unit.getId(), unitDetail);
        }


        return  unit;

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
    public List<Unit> getUnitByArticleidWechat(String articleId) {
        List<Unit> units = unitMapper.getUnitByArticleidWechat(articleId);
        return units;
    }

    @Override
    public void insertArticleRelation(String articleId, List<Unit> units) {
        for(Unit unit : units){
            String id = ApplicationUtils.randomUUID();
            unit.setChoiceType(unit.getChoiceType() == null ? 1 : unit.getChoiceType());
            unitMapper.insertArticleRelation(articleId,id,unit);
            for(UnitDetail unitDetail : unit.getDetails()){
                String detailID = ApplicationUtils.randomUUID();
                unitMapper.insertUnitDetailRelation(detailID,id,unitDetail);
            }
        }
    }

    @Override
    public void updateArticleRelation(String articleId, List<Unit> units) {
        unitMapper.deleteArticleUnit(articleId);
        insertArticleRelation(articleId, units);
    }

    @Override
    public void deleteUnit(String id) {
        List<String> ids = unitMapper.getUnitNew(id);
        unitMapper.deleteUnit(ids);
        unitMapper.deleteUnitNew(id);

    }

    @Override
    public void modifyUnit(Unit unit) {
        //首先得到使用该规格的菜品关系列表
        List<String> ids = unitMapper.getUnitNew(unit.getId());

        for(String id: ids){
            //删除多余的规格
            unitMapper.deleteUnitMore(id,unit.getDetails());
            //遍历每个菜品关系
            for(UnitDetail unitDetail : unit.getDetails()){
                //拿到每个明细
                int count = unitMapper.getUnitByRelation(unitDetail.getId(),id);
                if(count == 0){ //不存在
                    unitDetail.setPrice(new BigDecimal(0));
                    unitMapper.insertUnitDetailRelation(ApplicationUtils.randomUUID(),id,unitDetail);
                }
            }
        }
    }

    @Override
    public List<UnitArticle> selectUnitDetail(String id) {
        return unitMapper.selectUnitDetail(id);
    }

    @Override
    public List<Unit> selectUnitByShopId(String shopId) {
        return unitMapper.selectUnitByShopId(shopId);
    }

    @Override
    public List<UnitDetail> selectUnitDetailByShopId(String shopId) {
        return unitMapper.selectUnitDetailByShopId(shopId);
    }
}
