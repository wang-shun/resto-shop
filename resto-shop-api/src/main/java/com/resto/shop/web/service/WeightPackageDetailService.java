package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.WeightPackage;
import com.resto.shop.web.model.WeightPackageDetail;

public interface WeightPackageDetailService extends GenericService<WeightPackageDetail, Long> {

    int insertDetail(Long weightPackageId, WeightPackageDetail weightPackageDetail);

    void deleteDetails(Long weightPackageId);

}
