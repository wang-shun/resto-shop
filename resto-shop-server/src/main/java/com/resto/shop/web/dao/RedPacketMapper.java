package com.resto.shop.web.dao;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.AccountLog;
import com.resto.shop.web.model.RedPacket;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface RedPacketMapper extends GenericDao<RedPacket,String> {

    RedPacket selectFirstRedPacket(@Param("customerId") String customerId,@Param("redType") Integer redType);

    void updateRedRemainderMoney(@Param("id") String id,@Param("redRemainderMoney") BigDecimal redRemainderMoney);
}
