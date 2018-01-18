package com.resto.shop.web.report;


import com.resto.brand.web.dto.MemberUserDto;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface CustomerMapperReport{

    //得到品牌用户信息
    String selectBrandUser();

    List<MemberUserDto> selectListMemberUser(@Param("beginDate") Date begin, @Param("endDate") Date end);


}