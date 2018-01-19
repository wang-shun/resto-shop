package com.resto.shop.web.report;


import com.resto.brand.web.dto.RedPacketDto;

import java.util.List;
import java.util.Map;

public interface RedPacketMapperReport{

    List<RedPacketDto> selectRedPacketLog(Map<String, Object> selectMap);

    Map<String, Object> selectUseRedOrder(Map<String, Object> selectMap);

}
