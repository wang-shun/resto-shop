package com.resto.shop.web.dao;

import com.resto.shop.web.model.OffLineOrder;
import com.resto.brand.core.generic.GenericDao;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface OffLineOrderMapper  extends GenericDao<OffLineOrder,String> {
    int deleteByPrimaryKey(String id);

    int insert(OffLineOrder record);

    int insertSelective(OffLineOrder record);

    OffLineOrder selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(OffLineOrder record);

    int updateByPrimaryKey(OffLineOrder record);

    /**
     * 查询当日有效的线下pos订单信息
     * @param source
     * @param shopId
     * @param begin
     * @param end
     * @return
     */
    OffLineOrder selectByTimeSourceAndShopId(@Param("source") Integer source,@Param("shopId") String shopId,@Param("beginDate") Date begin,@Param("endDate") Date end);

    /**
     * 查询当月下的所有线下pos订单
     * @param shopId
     * @param beginDate
     * @param endDate
     * @return
     */
    List<OffLineOrder> selectlistByTimeSourceAndShopId(@Param("shopId") String shopId,@Param("beginDate") Date beginDate,@Param("endDate") Date endDate,@Param("source") Integer source);

    List<OffLineOrder> selectByShopIdAndTime(@Param("shopId") String shopId, @Param("beginDate") Date beginDate, @Param("endDate")Date endDate);
}
