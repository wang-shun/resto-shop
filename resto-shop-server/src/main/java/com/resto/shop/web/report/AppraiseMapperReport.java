package com.resto.shop.web.report;

import com.resto.brand.web.dto.AppraiseShopDto;
import java.util.List;
import java.util.Map;

public interface AppraiseMapperReport{

    List<AppraiseShopDto> selectAppraiseShopDto(Map<String, Object> selectMap);


}
