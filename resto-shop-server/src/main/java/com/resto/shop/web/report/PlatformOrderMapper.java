package com.resto.shop.web.report;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.web.dto.PlatformReportDto;
import com.resto.shop.web.model.PlatformOrder;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface PlatformOrderMapper  extends GenericDao<PlatformOrder,String> {
    int deleteByPrimaryKey(String id);

    int insert(PlatformOrder record);

    int insertSelective(PlatformOrder record);

    PlatformOrder selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PlatformOrder record);

    int updateByPrimaryKeyWithBLOBs(PlatformOrder record);

    int updateByPrimaryKey(PlatformOrder record);

    PlatformOrder selectByPlatformOrderId(@Param("platformOrderId") String platformOrderId, @Param("type") Integer type);

    //直接sql查询
    PlatformReportDto selectByshopDetailId(Map map);
    //调用存储过程查询
    PlatformReportDto proc_shopdetailId(Date beginDate, Date endDate, String shopDetailId);

    //根据店铺id，查询订单详情
    List<PlatformOrder> selectshopDetailIdList(Map map);

    //根据三方品台订单id查询该订单详细信息
    List<PlatformOrder> getPlatformOrderDetailList(String platformOrderId);

    /**
     * 查询某天的异常订单
     * @param currentShopId
     * @param dateBegin
     * @param dateEnd
     * @return
     */
    List<PlatformOrder> selectPlatFormErrorOrderList(@Param("shopId") String currentShopId, @Param("dateBegin") Date dateBegin, @Param("dateEnd") Date dateEnd);
}
