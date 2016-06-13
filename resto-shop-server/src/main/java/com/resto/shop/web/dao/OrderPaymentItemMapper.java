package com.resto.shop.web.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.web.dto.IncomeReportDto;
import com.resto.shop.web.model.OrderPaymentItem;

public interface OrderPaymentItemMapper  extends GenericDao<OrderPaymentItem,String> {
    int deleteByPrimaryKey(String id);

    int insert(OrderPaymentItem record);

    int insertSelective(OrderPaymentItem record);

    OrderPaymentItem selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(OrderPaymentItem record);

    int updateByPrimaryKeyWithBLOBs(OrderPaymentItem record);

    int updateByPrimaryKey(OrderPaymentItem record);

	List<OrderPaymentItem> selectByOrderId(String orderId);

	List<OrderPaymentItem> selectpaymentByPaymentMode(@Param("shopId")String shopId,@Param("beginDate")Date beginDate, @Param("endDate")Date endDate);
	
	/**
	 * 根据时间查询  【充值订单 】状态为 1 的信息，用于报表统计时使用
	 * @param shopId
	 * @param beginDate
	 * @param endDate
	 * @author lmx
	 * @return
	 */
	OrderPaymentItem selectChargeOrderByDate(@Param("shopId")String shopId,@Param("beginDate") Date beginDate,@Param("endDate") Date endDate);
	
	/**
	 * 查询品牌下不同店铺的数据
	 * @param brandId
	 * @return
	 */
	List<IncomeReportDto> selectIncomeList(@Param("brandId")String brandId,@Param("begin")Date begin,@Param("end")Date end);

}
