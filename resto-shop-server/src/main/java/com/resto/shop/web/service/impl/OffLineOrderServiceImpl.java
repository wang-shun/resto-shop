package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.DateUtil;
import com.resto.shop.web.dao.OffLineOrderMapper;
import com.resto.shop.web.dto.OrderNumDto;
import com.resto.shop.web.model.OffLineOrder;
import com.resto.shop.web.service.OffLineOrderService;
import cn.restoplus.rpc.server.RpcService;

import java.util.Date;
import java.util.List;

/**
 *
 */
@RpcService
public class OffLineOrderServiceImpl extends GenericServiceImpl<OffLineOrder, String> implements OffLineOrderService {

    @Resource
    private OffLineOrderMapper offlineorderMapper;

    @Override
    public GenericDao<OffLineOrder, String> getDao() {
        return offlineorderMapper;
    }

    @Override
    public OffLineOrder selectByTimeSourceAndShopId(Integer source, String shopId) {
        Date begin = DateUtil.getDateBegin(new Date());
        Date end  = DateUtil.getDateEnd(new Date());

        return offlineorderMapper.selectByTimeSourceAndShopId(source,shopId,begin,end);

    }

    @Override
    public List<OffLineOrder> selectByShopIdAndTime(String shopId, Date beginDate, Date endDate) {
        return offlineorderMapper.selectByShopIdAndTime(shopId,beginDate,endDate);
    }

    @Override
    public List<OffLineOrder> selectlistByTimeSourceAndShopId(String id, Date begin, Date end, int offlinePos) {
        return offlineorderMapper.selectlistByTimeSourceAndShopId(id,begin,end,offlinePos);
    }

    @Override
    public OffLineOrder selectSumByTimeSourceAndShopId(int offlinePos, String id, Date begin, Date end) {
        return offlineorderMapper.selectSumByTimeSourceAndShopId(offlinePos,id,begin,end);
    }

	@Override
	public OffLineOrder selectByTimeSourceAndShopId(int offlinePos, String id, Date dateBegin, Date dateEnd) {
		return offlineorderMapper.selectByTimeSourceAndShopId(offlinePos,id,dateBegin,dateEnd);
	}

    @Override
    public List<OrderNumDto> selectOrderNumByTimeAndBrandId(String brandId, String begin, String end) {
        Date beginDate = DateUtil.getformatBeginDate(begin);
        Date endDate = DateUtil.getformatEndDate(end);
        return offlineorderMapper.selectOrderNumByTimeAndBrandId(brandId,beginDate,endDate);
    }


}
