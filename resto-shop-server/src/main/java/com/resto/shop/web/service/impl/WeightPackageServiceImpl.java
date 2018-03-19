package com.resto.shop.web.service.impl;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.WeightPackageMapper;
import com.resto.shop.web.model.WeightPackage;
import com.resto.shop.web.service.WeightPackageService;

import javax.annotation.Resource;

public class WeightPackageServiceImpl extends GenericServiceImpl<WeightPackage, Long> implements WeightPackageService {

    @Resource
    private WeightPackageMapper weightPackageMapper;

    @Override
    public GenericDao<WeightPackage, Long> getDao() {
        return weightPackageMapper;
    }
}
