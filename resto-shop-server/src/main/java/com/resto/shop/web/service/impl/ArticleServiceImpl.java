package com.resto.shop.web.service.impl;

import java.sql.*;
import java.util.*;
import java.util.Date;

import javax.annotation.Resource;

import com.resto.brand.core.entity.Result;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.model.DatabaseConfig;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.shop.web.dao.ArticleMapper;
import com.resto.shop.web.dao.FreeDayMapper;
import com.resto.shop.web.dao.OrderMapper;
import com.resto.shop.web.model.*;
import com.resto.shop.web.service.ArticlePriceService;
import com.resto.shop.web.service.ArticleService;
import com.resto.shop.web.service.KitchenService;
import com.resto.shop.web.service.MealAttrService;
import com.resto.shop.web.service.SupportTimeService;

import cn.restoplus.rpc.common.util.StringUtil;
import cn.restoplus.rpc.server.RpcService;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 *
 */
@RpcService
public class ArticleServiceImpl extends GenericServiceImpl<Article, String> implements ArticleService {

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private OrderMapper orderMapper;


    @Resource
    private ArticlePriceService articlePriceServer;

    @Resource
    private SupportTimeService supportTimeService;

    @Resource
    private KitchenService kitchenService;

    @Resource
    private MealAttrService mealAttrService;

    @Resource
    private BrandSettingService brandSettingService;

    @Resource
    private FreeDayMapper freedayMapper;


    @Override
    public GenericDao<Article, String> getDao() {
        return articleMapper;
    }

    @Override
    public List<Article> selectList(String currentShopId) {
        return articleMapper.selectList(currentShopId);
    }

    @Override
    public Article save(Article article) {
        article.setId(ApplicationUtils.randomUUID());
        this.insert(article);
        kitchenService.saveArticleKitchen(article.getId(), article.getKitchenList());
        if (article.getArticleType() == Article.ARTICLE_TYPE_SIGNLE) {
            articlePriceServer.saveArticlePrices(article.getId(), article.getArticlePrices());
        } else if (article.getArticleType() == Article.ARTICLE_TYPE_MEALS) {
            mealAttrService.insertBatch(article.getMealAttrs(), article.getId());
        }
        supportTimeService.saveSupportTimes(article.getId(), article.getSupportTimes());
        return article;
    }

    @Override
    public int update(Article article) {
        kitchenService.saveArticleKitchen(article.getId(), article.getKitchenList());
        if (article.getArticleType() == Article.ARTICLE_TYPE_SIGNLE) {
            articlePriceServer.saveArticlePrices(article.getId(), article.getArticlePrices());
        } else if (article.getArticleType() == Article.ARTICLE_TYPE_MEALS) {
            mealAttrService.insertBatch(article.getMealAttrs(), article.getId());
        }
        supportTimeService.saveSupportTimes(article.getId(), article.getSupportTimes());
        return super.update(article);
    }

    @Override
    public Article selectFullById(String id,String show) {
        Article article = selectById(id);
        List<Integer> kitchenList = kitchenService.selectIdsByArticleId(id);
        article.setKitchenList(kitchenList.toArray(new Integer[0]));
        if (article.getArticleType() == Article.ARTICLE_TYPE_SIGNLE) {
            List<ArticlePrice> prices = articlePriceServer.selectByArticleId(id);
            article.setArticlePrices(prices);
        } else {
            List<MealAttr> mealAttrs = mealAttrService.selectFullByArticleId(id,show);
            article.setMealAttrs(mealAttrs);
        }
        List<Integer> supportTimesIds = supportTimeService.selectByIdsArticleId(id);
        article.setSupportTimes(supportTimesIds.toArray(new Integer[0]));


        return article;
    }

    @Override
    public List<Article> selectListFull(String currentShopId, Integer distributionModeId,String show) {
        List<Article> articleList = articleMapper.selectListByShopIdAndDistributionId(currentShopId, distributionModeId);
        Map<String, Article> articleMap = selectAllSupportArticle(currentShopId);
        for (Article a : articleList) {
            if (a.getArticleType() == Article.ARTICLE_TYPE_SIGNLE) {
                if (!StringUtil.isEmpty(a.getHasUnit())) {
                    List<ArticlePrice> prices = articlePriceServer.selectByArticleId(a.getId());
                    a.setArticlePrices(prices);
                }
            } else if (a.getArticleType() == Article.ARTICLE_TYPE_MEALS) {
                List<MealAttr> mealAttrs = mealAttrService.selectFullByArticleId(a.getId(),show);
                a.setMealAttrs(mealAttrs);
            }
            if (!articleMap.containsKey(a.getId())) {
                a.setIsEmpty(true);
            }
        }
        return articleList;
    }

    @Override
    public int delete(String id) {
        Article article = selectById(id);
        article.setState(false);
        return update(article);
    }

    private Map<String, Article> selectAllSupportArticle(String currentShopId) {
        List<SupportTime> supportTime = supportTimeService.selectNowSopport(currentShopId);
        if (supportTime.isEmpty()) {
            return new HashMap<>();
        }
        List<Integer> list = new ArrayList<>(ApplicationUtils.convertCollectionToMap(Integer.class, supportTime).keySet());
        List<Article> article = articleMapper.selectBySupportTimeId(list, currentShopId);
        Map<String, Article> articleMap = ApplicationUtils.convertCollectionToMap(String.class, article);
        return articleMap;
    }

    @Override
    public List<Article> selectListByIsEmpty(Integer isEmpty, String shopId) {
        return articleMapper.selectListByIsEmpty(isEmpty, shopId);
    }

    @Override
    public void setEmpty(Integer isEmpty, String articleId) {
        articleMapper.setEmpty(isEmpty, articleId);
    }

    @Override
    public void addLikes(String articleId) {
        articleMapper.addLikes(articleId);
    }

    @Override
    public void updateLikes(String articleId, Long likes) {
        articleMapper.updateLikes(articleId, likes);
    }


    @Override
    public void initStock() {
        /**
         * 餐品套餐库存 默认为 最低的单品
         */
        //多规格商品 库存之和 等于该品库存
        articleMapper.initSuitStock();
        articleMapper.initSize();
    }

    @Override
    public List<ArticleStock> getStock(String shopId,String familyId,Integer empty,Integer activated) {

        FreeDay day = freedayMapper.selectByDate(DateFormatUtils.format(new Date(), "yyyy-MM-dd"),shopId);
        int freeDay = 0 ;
        if(day == null){
            freeDay = 1;
        }
        List<ArticleStock> result = articleMapper.getStock(shopId,familyId,empty,freeDay,activated);
        log.error("-------  "+activated);
        return result;
    }

    @Override
    public Boolean clearStock(String articleId,String shopId) {
    	String emptyRemark = "【手动沽清】";
        articleMapper.clearStock(articleId,emptyRemark);
        articleMapper.clearPriceTotal(articleId,emptyRemark);
        articleMapper.clearPriceStock(articleId,emptyRemark);
//        articleMapper.cleanPriceAll(articleId,emptyRemark);//方法重复
        //如果有规格的
        orderMapper.setStockBySuit(shopId);
        articleMapper.initSizeCurrent();
        articleMapper.clearMain(articleId,emptyRemark);
        return true;
    }

    @Override
    public Boolean editStock(String articleId, Integer count,String shopId) {
    	String emptyRemark = count <=0 ? "【手动沽清】" : null;
        articleMapper.editStock(articleId, count ,emptyRemark);
        articleMapper.editPriceStock(articleId,count,emptyRemark);
        orderMapper.setStockBySuit(shopId);
        articleMapper.initSizeCurrent();
        articleMapper.initEmpty();
        return true;
    }

	@Override
	public Boolean setActivated(String articleId, Integer activated) {
		int row = articleMapper.setActivate(articleId, activated);
		return row > 0 ? true : false;
	}
}
