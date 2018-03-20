package com.resto.shop.web.service.impl;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.WeightPackageDetailMapper;
import com.resto.shop.web.model.WeightPackageDetail;
import com.resto.shop.web.service.WeightPackageDetailService;

import javax.annotation.Resource;

public class WeightPackageDetailServiceImpl extends GenericServiceImpl<WeightPackageDetail, Long> implements WeightPackageDetailService {

    @Resource
    private WeightPackageDetailMapper weightPackageDetailMapper;

    @Override
    public GenericDao<WeightPackageDetail, Long> getDao() {
        return weightPackageDetailMapper;
    }

    @Override
    public int insertDetail(Long weightPackageId, WeightPackageDetail weightPackageDetail) {
        weightPackageDetail.setWeightPackageId(weightPackageId);
        return weightPackageDetailMapper.insert(weightPackageDetail);
    }

    @Override
    public void deleteDetails(Long weightPackageId) {
        weightPackageDetailMapper.deleteDetails(weightPackageId);
    }
}
