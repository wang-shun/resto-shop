package com.resto.shop.web.report;


import com.resto.brand.web.dto.CouponDto;

import java.util.List;
import java.util.Map;

public interface CouponMapperReport{

    List<CouponDto> selectCouponDto(Map<String, Object> selectMap);

}
