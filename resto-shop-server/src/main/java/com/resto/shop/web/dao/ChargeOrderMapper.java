package com.resto.shop.web.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.resto.brand.web.dto.RechargeLogDto;
import com.resto.brand.web.dto.RedPacketDto;
import com.resto.shop.web.model.RedPacket;
import org.apache.ibatis.annotations.Param;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.ChargeOrder;

public interface ChargeOrderMapper  extends GenericDao<ChargeOrder,String> {
    int deleteByPrimaryKey(String id);

    int insert(ChargeOrder record);

    int insertSelective(ChargeOrder record);

    ChargeOrder selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ChargeOrder record);

    int updateByPrimaryKey(ChargeOrder record);

	BigDecimal selectTotalBalance(String customerId);

	void updateBalance(@Param("id")String id, @Param("useCharge")BigDecimal useCharge, @Param("useReward")BigDecimal useReward);

	ChargeOrder selectFirstBalanceOrder(String customerId);

	void refundCharge(BigDecimal payValue, String id);

	void refundReward(BigDecimal payValue, String id);

    List<ChargeOrder> selectByDateAndShopId(@Param("beginDate") Date begin,@Param("endDate") Date end, @Param("shopId") String shopId);

    List<ChargeOrder> selectByDateAndBrandId(@Param("beginDate") Date begin, @Param("endDate") Date end,@Param("brandId") String brandId);
    List<ChargeOrder>  shopChargeCodes(@Param("shopDetailId")String shopDetailId,@Param("beginDate")Date beginDate, @Param("endDate")Date endDate);

    List<Map<String, Object>> selectByShopToDay(Map<String, Object> selectMap);

    List<RedPacketDto> selectChargeRedPacket(Map<String, Object> selectMap);

    List<ChargeOrder> selectListByDateAndShopId(@Param("beginDate") Date begin, @Param("endDate") Date end,@Param("shopId") String id);

    List<ChargeOrder> selectByCustomerIdAndBrandId(@Param("customerId") String customerId, @Param("brandId") String brandId);

    List<ChargeOrder> selectMonthDto(Map<String, Object> selectMap);

    RechargeLogDto selectRechargeLog(@Param("begin")Date begin, @Param("end")Date end, @Param("brandId")String brandId);

    RechargeLogDto selectShopRechargeLog(@Param("begin")Date begin,@Param("end")Date end,@Param("shopId")String shopId);

    List<Map<String, Object>> getChargeSumInfo(Map<String, Object> selectMap);

    List<String> selectCustomerChargeOrder(List<String> customerIds);
}
