package com.resto.shop.web.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.shop.web.constant.OrderState;
import com.resto.shop.web.dao.AppraiseMapper;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.Appraise;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.ShowPhoto;
import com.resto.shop.web.service.AccountService;
import com.resto.shop.web.service.AppraiseService;
import com.resto.shop.web.service.ArticleService;
import com.resto.shop.web.service.CustomerService;
import com.resto.shop.web.service.OrderService;
import com.resto.shop.web.service.RedConfigService;
import com.resto.shop.web.service.ShowPhotoService;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class AppraiseServiceImpl extends GenericServiceImpl<Appraise, String> implements AppraiseService {

    @Resource
    private AppraiseMapper appraiseMapper;

    @Resource
    OrderService orderService;
    
    @Resource
    ArticleService articleService;
    
    @Resource
    ShowPhotoService showPhotoService;
    
    @Resource
    RedConfigService redConfigService;
    
    @Resource
    AccountService accountService;
    
    @Resource
    CustomerService customerService;
    
    @Override
    public GenericDao<Appraise, String> getDao() {
        return appraiseMapper;
    }

	@Override
	public List<Appraise> listAppraise(String currentShopId, Integer currentPage, Integer showCount, Integer maxLevel,
			Integer minLevel) {
		return appraiseMapper.listAppraise(currentShopId, currentPage, showCount, maxLevel, minLevel);
	}

	@Override
	public Map<String, Object> appraiseCount(String currentShopId) {
	    return appraiseMapper.appraiseCount(currentShopId);
	}

	@Override
	public List<Map<String, Object>> appraiseMonthCount(String surrentShopId) {
		return appraiseMapper.appraiseMonthCount(surrentShopId);
	}

	@Override
	public Appraise saveAppraise(Appraise appraise) throws AppException {
		Order order= orderService.selectById(appraise.getOrderId());
		if(order.getAllowAppraise()){
			String pic = getPicture(appraise);
			appraise.setPictureUrl(pic);
			appraise.setId(ApplicationUtils.randomUUID());
			appraise.setCreateTime(new Date());
			appraise.setStatus((byte)1);
			BigDecimal redMoney= rewardRed(order);
			appraise.setRedMoney(redMoney);
			
			insert(appraise);
			order.setOrderState(OrderState.HASAPPRAISE);
			order.setAllowAppraise(false);
			order.setAllowCancel(false);
			orderService.update(order);
		}else{
			throw new AppException(AppException.ORDER_NOT_ALL_APPRAISE);
		}
		return appraise;
	}

	private BigDecimal rewardRed(Order order) {
		BigDecimal money = redConfigService.nextRedAmount(order);
		Customer cus = customerService.selectById(order.getCustomerId());
		if(money.compareTo(BigDecimal.ZERO)>0){
			accountService.addAccount(money,cus.getAccountId(), " 评论奖励红包:"+money);
			log.info("评论奖励红包: "+money+" 元");
		}
		return money;
	}

	private String getPicture(Appraise appraise) {
		String pic=null;
		int type = appraise.getType();
		switch (type) {
		case 1:
			String articleId = appraise.getArticleId();
			if(articleId.contains("@")){
				articleId = articleId.split("@")[0];
			}
			pic = articleService.selectById(articleId).getPhotoSmall();
			break;
		case 2:
			if(StringUtils.isNumeric(appraise.getArticleId())){
				Integer showPhotoId = Integer.parseInt(appraise.getArticleId());
				ShowPhoto showPhoto = showPhotoService.selectById(showPhotoId);
				pic = showPhoto.getPicUrl();
			}
			break;
		default:
			pic=null;
			break;
		}
		return pic ;
	}


}
