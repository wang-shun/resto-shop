package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.WeightPackage;

import java.util.List;

public interface WeightPackageService extends GenericService<WeightPackage, Long> {

    List<WeightPackage> getAllWeightPackages(String shopId);

}
