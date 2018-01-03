package com.resto.shop.web.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.resto.brand.web.dto.AppraiseShopDto;
import com.resto.shop.web.constant.RedType;
import com.resto.shop.web.model.*;
import com.resto.shop.web.producer.MQMessageProducer;
import com.resto.shop.web.service.*;
import org.apache.commons.lang3.StringUtils;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.shop.web.constant.OrderState;
import com.resto.shop.web.dao.AppraiseMapper;
import com.resto.shop.web.exception.AppException;

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

    @Resource
    RedPacketService redPacketService;

	@Resource
	ParticipantService participantService;

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
		if(order.getAllowAppraise() && (order.getGroupId() == null || "".equals(order.getGroupId()))){
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
			orderService.update(order);
		}else if(order.getAllowAppraise() && order.getGroupId() != null && !"".equals(order.getGroupId())){
			//判断用户是否已经评论
			Appraise a = appraiseMapper.selectByOrderIdCustomerId(appraise.getOrderId(), appraise.getCustomerId());
			if(a != null){
				log.error("订单不允许评论:	"+order.getId());
				throw new AppException(AppException.ORDER_NOT_ALL_APPRAISE);
			}
			appraise.setId(ApplicationUtils.randomUUID());
			appraise.setCreateTime(new Date());
			appraise.setStatus((byte)1);
			appraise.setShopDetailId(order.getShopDetailId());
			BigDecimal redMoney= rewardRed(order);
			appraise.setRedMoney(redMoney);
			appraise.setBrandId(order.getBrandId());
			insert(appraise);
			//仅修改  够餐组下面的单人的领取红包记录
			participantService.updateAppraiseByOrderIdCustomerId(order.getId(), appraise.getCustomerId());
			//查询该够餐组 下面是否还存在未领取红包的记录   如没有则订单状态变成11
			List<Participant> participants = participantService.selectNotAppraiseByGroupId(order.getGroupId(), order.getId());
			if(participants.size() == 0){
				order.setOrderState(OrderState.HASAPPRAISE);
				order.setAllowAppraise(false);
			}
			order.setAllowCancel(false);
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
		String uuid = ApplicationUtils.randomUUID();
		if(money.compareTo(BigDecimal.ZERO)>0){
//			accountService.addAccount(money,cus.getAccountId(), " 评论奖励红包:"+money,AccountLog.APPRAISE_RED_PACKAGE,order.getShopDetailId());
            RedPacket redPacket = new RedPacket();
            redPacket.setId(uuid);
            redPacket.setRedMoney(money);
            redPacket.setCreateTime(new Date());
            redPacket.setCustomerId(cus.getId());
            redPacket.setBrandId(order.getBrandId());
            redPacket.setShopDetailId(order.getShopDetailId());
            redPacket.setRedRemainderMoney(money);
            redPacket.setRedType(RedType.APPRAISE_RED);
			redPacket.setOrderId(order.getId());
			redPacket.setState(0);
            redPacketService.insert(redPacket);
			log.info("评论奖励红包: "+money+" 元"+order.getId());
			RedPacket rp = redPacketService.selectById(uuid);
			MQMessageProducer.sendShareGiveMoneyMsg(rp,5);
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
	public Appraise selectDeatilByOrderId(String orderId, String customerId) {
		List<Appraise> apprises = appraiseMapper.selectDeatilByOrderId(orderId, customerId);
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

    @Override
    public List<Appraise> selectByTimeAndShopId(String shopId,Date begin, Date end) {
        return appraiseMapper.selectByTimeAndShopId(shopId, begin, end);
    }

    @Override
    public List<AppraiseShopDto> selectAppraiseShopDto(Map<String, Object> selectMap) {
        return appraiseMapper.selectAppraiseShopDto(selectMap);
    }

	@Override
	public List<Appraise> selectByTimeAndBrandId(Date begin, Date end) {
		return appraiseMapper.selectByTimeAndBrandId(begin,end);
	}

	@Override
	public Appraise selectByOrderIdCustomerId(String orderId, String customerId) {
		return appraiseMapper.selectByOrderIdCustomerId(orderId, customerId);
	}

	@Override
	public List<Appraise> selectAllAppraiseByShopIdAndCustomerId(String shopId, String customerId) {
		return appraiseMapper.selectAllAppraiseByShopIdAndCustomerId(shopId,customerId);
	}
}
