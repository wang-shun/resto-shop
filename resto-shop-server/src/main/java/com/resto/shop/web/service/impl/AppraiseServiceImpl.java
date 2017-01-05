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
import com.resto.shop.web.model.AccountLog;
import com.resto.shop.web.model.Appraise;
import com.resto.shop.web.model.Article;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderItem;
import com.resto.shop.web.model.ShowPhoto;
import com.resto.shop.web.service.AccountService;
import com.resto.shop.web.service.AppraiseService;
import com.resto.shop.web.service.ArticleService;
import com.resto.shop.web.service.CustomerService;
import com.resto.shop.web.service.OrderItemService;
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
    
    @Resource
    OrderItemService orderItemService;
    
    @Override
    public GenericDao<Appraise, String> getDao() {
        return appraiseMapper;
    }

	@Override
	public List<Appraise> updateAndListAppraise(String currentShopId, Integer currentPage, Integer showCount, Integer maxLevel,
			Integer minLevel) {
		List<Appraise> appraiseList = appraiseMapper.listAppraise(currentShopId, currentPage, showCount, maxLevel, minLevel);
		for (Appraise appraise : appraiseList) {
			if(StringUtils.isBlank(appraise.getFeedback())){
				String text = getFeedBackText(appraise);
				appraise.setFeedback(text);
				update(appraise);
			}
		}
		return appraiseList;
	}

	private String getFeedBackText(Appraise appraise) {
		String articleId = appraise.getArticleId();
		if(appraise.getType()==1){
			Article art = articleService.selectById(articleId);
			if(art!=null){
				return art.getName();
			}
		}else if(appraise.getType()==2&&StringUtils.isNumeric(articleId)){
			ShowPhoto sp = showPhotoService.selectById(Integer.parseInt(articleId));
			if(sp!=null){
				return sp.getTitle();
			}
		}
		return "";
	}

	@Override
	public Map<String, Object> appraiseCount(String currentShopId) {
	    return appraiseMapper.appraiseCount(currentShopId);
	}

	@Override
	public List<Map<String, Object>> appraiseMonthCount(String currentShopId) {
		return appraiseMapper.appraiseMonthCount(currentShopId);
	}

	@Override
	public Appraise saveAppraise(Appraise appraise) throws AppException {
		Order order= orderService.selectById(appraise.getOrderId());
		if(order.getAllowAppraise()){
//			String pic = getPicture(appraise);
//			appraise.setPictureUrl(pic);
			appraise.setId(ApplicationUtils.randomUUID());
			appraise.setCreateTime(new Date());
			appraise.setStatus((byte)1);
			appraise.setShopDetailId(order.getShopDetailId());
			BigDecimal redMoney= rewardRed(order);
			appraise.setRedMoney(redMoney);
			appraise.setBrandId(order.getBrandId());
			insert(appraise);
			order.setOrderState(OrderState.HASAPPRAISE);
			order.setAllowAppraise(false);
			order.setAllowCancel(false);
			order.setAllowContinueOrder(false);
			orderService.update(order);
		}else{
			log.error("订单不允许评论:	"+order.getId());
			throw new AppException(AppException.ORDER_NOT_ALL_APPRAISE);
		}
		return appraise;
	}

	private BigDecimal rewardRed(Order order) {
		BigDecimal money = redConfigService.nextRedAmount(order);
		Customer cus = customerService.selectById(order.getCustomerId());
		if(money.compareTo(BigDecimal.ZERO)>0){
			accountService.addAccount(money,cus.getAccountId(), " 评论奖励红包:"+money,AccountLog.SOURCE_RED_PACKAGE);
			log.info("评论奖励红包: "+money+" 元"+order.getId());
		}
		return money;
	}

	private String getPicture(Appraise appraise) {
		String pic=null;
		int type = appraise.getType();
		switch (type) {
		case 1: //好评
			String itemId = appraise.getArticleId();
			OrderItem item = orderItemService.selectById(itemId);
			String articleId  = item.getArticleId();
			if(articleId.contains("@")){
				articleId = articleId.split("@")[0];
			}
			Article article = articleService.selectById(articleId);
			if(article!=null){
				pic = article.getPhotoSmall();
				articleService.addLikes(article.getId());
				appraise.setArticleId(article.getId());
			}
			break;
		case 2:
			if(StringUtils.isNumeric(appraise.getArticleId())){
				Integer showPhotoId = Integer.parseInt(appraise.getArticleId());
				ShowPhoto showPhoto = showPhotoService.selectById(showPhotoId);
				if(showPhoto!=null){
					pic = showPhoto.getPicUrl();
				}
			}
			break;
		default:
			pic=null;
			break;
		}
		return pic ;
	}

	@Override
	public Appraise selectDetailedById(String appraiseId) {
		Appraise appraise = appraiseMapper.selectDetailedById(appraiseId);
		return appraise;
	}

	@Override
	public Appraise selectDeatilByOrderId(String orderId) {
		List<Appraise> apprises = appraiseMapper.selectDeatilByOrderId(orderId);
		if(apprises.size() > 0){
			return apprises.get(0);
		}else{
			return null;
		}
	}

	@Override
	public Appraise selectAppraiseByCustomerId(String customerId,String shopId) {
		return appraiseMapper.selectAppraiseByCustomerId(customerId,shopId);
	}

	@Override
	public List<Appraise> selectCustomerAllAppraise(String customerId, Integer currentPage, Integer showCount) {
		return appraiseMapper.selectCustomerAllAppraise(customerId, currentPage, showCount);
	}

	@Override
	public int selectByCustomerCount(String customerId) {
		return appraiseMapper.selectByCustomerCount(customerId);
	}

	@Override
	public List<Appraise> selectByGoodAppraise() {
		return appraiseMapper.selectByGoodAppraise();
	}
	
	@Override
	public Map<String, Object> selectCustomerAppraiseAvg(String customerId) {
		return appraiseMapper.selectCustomerAppraiseAvg(customerId);
	}
}
