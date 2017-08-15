package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.OffLineOrder;

import java.util.Date;
import java.util.List;

public interface OffLineOrderService extends GenericService<OffLineOrder, String> {
    OffLineOrder  selectByTimeSourceAndShopId(Integer source,String shopId);

    List<OffLineOrder> selectByShopIdAndTime(String id, Date beginDate, Date endDate);

    List<OffLineOrder> selectlistByTimeSourceAndShopId(String id, Date begin, Date end, int offlinePos);

    /**
     * 查询一段时间内的订单的各项之和
     * @param offlinePos
     * @param id
     * @param todayBegin
     * @param todayEnd
     * @return
     */
    OffLineOrder selectSumByTimeSourceAndShopId(int offlinePos, String id, Date todayBegin, Date todayEnd);

	OffLineOrder selectByTimeSourceAndShopId(int offlinePos, String id, Date dateBegin, Date dateEnd);
}
