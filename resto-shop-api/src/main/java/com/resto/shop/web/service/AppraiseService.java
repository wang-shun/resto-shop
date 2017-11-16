package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.brand.web.dto.AppraiseShopDto;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.Appraise;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface AppraiseService extends GenericService<Appraise, String> {
 
	/**
	 * 根据当前 店铺ID 查询所有的评论数量和平均分
	 * @param currentShopId
	 * @return
	 */
	Map<String, Object> appraiseCount(String currentShopId);
	
	/**
	 * 根据当前 店铺ID 查询每个月的评论数量和平均分
	 * @param surrentShopId
	 * @return
	 */
	List<Map<String, Object>> appraiseMonthCount(String surrentShopId);

	Appraise saveAppraise(Appraise appraise) throws AppException;

	/**
	 * 查询当前店铺 的 评论列表
	 * @param currentPage	当前分页
	 * @param showCount		显示数量
	 * @param maxLevel		最大级别
	 * @param minLevel		最小级别
	 * @return
	 */
	List<Appraise> updateAndListAppraise(String currentShopId, Integer currentPage, Integer showCount, Integer maxLevel,
			Integer minLevel);

	Appraise selectDetailedById(String appraiseId);

	Appraise selectDeatilByOrderId(String orderId, String customerId);

	Appraise selectAppraiseByCustomerId(String customerId,String shopId);
	
	List<Appraise> selectCustomerAllAppraise(String customerId, Integer currentPage, Integer showCount);

	int selectByCustomerCount(String customerId);

	/**
	 * 查询每个品牌前500条好评
	 * @return
     */
	List<Appraise> selectByGoodAppraise();
	
	/**
	 * 查询用户综合评分
	 * @return
	 */
	Map<String, Object> selectCustomerAppraiseAvg(String customerId);

    /**
     * 查询时间段内的评论
     * @param begin
     * @param end
     * @param id
     * @return
     */
    List<Appraise> selectByTimeAndShopId(String  id, Date begin, Date end);

    List<AppraiseShopDto> selectAppraiseShopDto(Map<String, Object> selectMap);

	/**
	 * yz 2017/07/28
	 * 时间内品牌分数查询
	 * @param begin
	 * @param end
	 * @return
	 */
	List<Appraise> selectByTimeAndBrandId( Date begin, Date end);

	/**
	 * 该订单下某人的评论记录
	 * @param orderId
	 * @param customerId
     * @return
     */
	Appraise selectByOrderIdCustomerId(String orderId, String customerId);
}
