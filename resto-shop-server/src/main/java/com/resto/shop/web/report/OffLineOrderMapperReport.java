package com.resto.shop.web.report;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.dto.OrderNumDto;
import com.resto.shop.web.dto.UnderLineOrderDto;
import com.resto.shop.web.model.OffLineOrder;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface OffLineOrderMapperReport{

    List<OrderNumDto> selectOrderNumByTimeAndBrandId(@Param("brandId") String brandId, @Param("beginDate") Date beginDate, @Param("endDate") Date endDate);

}
