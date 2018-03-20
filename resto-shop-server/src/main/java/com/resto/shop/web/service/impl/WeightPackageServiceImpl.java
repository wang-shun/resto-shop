package com.resto.shop.web.service.impl;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.shop.web.dao.WeightPackageMapper;
import com.resto.shop.web.model.Unit;
import com.resto.shop.web.model.UnitDetail;
import com.resto.shop.web.model.WeightPackage;
import com.resto.shop.web.model.WeightPackageDetail;
import com.resto.shop.web.service.WeightPackageDetailService;
import com.resto.shop.web.service.WeightPackageService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

public class WeightPackageServiceImpl extends GenericServiceImpl<WeightPackage, Long> implements WeightPackageService {

    @Resource
    private WeightPackageMapper weightPackageMapper;

    @Resource
    private WeightPackageDetailService weightPackageDetailService;

    @Override
    public GenericDao<WeightPackage, Long> getDao() {
        return weightPackageMapper;
    }

    @Override
    public List<WeightPackage> getAllWeightPackages(String shopId) {
        return weightPackageMapper.getAllWeightPackages(shopId);
    }

    @Override
    public WeightPackage insertDetail(WeightPackage weightPackage) {
        for(WeightPackageDetail weightPackageDetail : weightPackage.getDetails()){
            weightPackageDetailService.insertDetail(weightPackage.getId(), weightPackageDetail);
        }
        return weightPackage;
    }

    @Override
    public void initWeightPackageDetail(WeightPackage weightPackage) {
        weightPackageDetailService.deleteDetails(weightPackage.getId());
    }
}
