package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.common.util.StringUtil;
import cn.restoplus.rpc.server.RpcService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.core.util.MQSetting;
import com.resto.brand.web.dto.ArticleSellDto;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.constant.ArticleType;
import com.resto.shop.web.constant.Common;
import com.resto.shop.web.dao.ArticleMapper;
import com.resto.shop.web.dao.FreeDayMapper;
import com.resto.shop.web.dao.OrderMapper;
import com.resto.shop.web.model.*;
import com.resto.shop.web.producer.MQMessageProducer;
import com.resto.shop.web.service.*;
import com.resto.shop.web.util.RedisUtil;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.resto.brand.core.util.HttpClient.doPostAnsc;
import static com.resto.brand.core.util.LogUtils.url;

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
    private BrandService brandService;

    @Resource
    private ArticlePriceService articlePriceServer;

    @Resource
    private SupportTimeService supportTimeService;

    @Resource
    private KitchenService kitchenService;


    @Resource
    private BrandSettingService brandSettingService;

    @Resource
    private FreeDayMapper freedayMapper;

    @Autowired
    private ArticleFamilyService articleFamilyService;

    @Autowired
    private MealAttrService mealAttrService;

    @Autowired
    private MealItemService mealItemService;


    @Autowired
    private ArticleAttrService articleAttrService;

    @Autowired
    private ArticleUnitService articleUnitService;

    @Autowired
    private ShopDetailService shopDetailService;

    @Resource
    private RecommendCategoryService recommendCategoryService;


    @Override
    public GenericDao<Article, String> getDao() {
        return articleMapper;
    }

    @Override
    public List<Article> selectList(String currentShopId) {
        Map<String, Article> discountMap = selectAllSupportArticle(currentShopId);
        List<Article> articleList = articleMapper.selectList(currentShopId);
        for (Article article : articleList) {
            if (discountMap.containsKey(article.getId())) {
                article.setDiscount(discountMap.get(article.getId()).getDiscount());
            }
        }
        return articleList;
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
    public Article selectFullById(String id, String show) {
        Article article = selectById(id);
        List<Integer> kitchenList = kitchenService.selectIdsByArticleId(id);
        article.setKitchenList(kitchenList.toArray(new Integer[0]));
        if (article.getArticleType() == Article.ARTICLE_TYPE_SIGNLE) {
            List<ArticlePrice> prices = articlePriceServer.selectByArticleId(id);
            article.setArticlePrices(prices);
        } else {
            List<MealAttr> mealAttrs = mealAttrService.selectFullByArticleId(id, show);
            article.setMealAttrs(mealAttrs);
        }
        List<Integer> supportTimesIds = supportTimeService.selectByIdsArticleId(id);
        article.setSupportTimes(supportTimesIds.toArray(new Integer[0]));
        return article;
    }

    @Override
    public List<Article> selectListFull(String currentShopId, Integer distributionModeId, String show) {
        List<Article> articleList = articleMapper.selectListByShopIdAndDistributionId(currentShopId, distributionModeId);
        //当前时间的年月
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String dateNowStr = sdf.format(d);
        for (Article article : articleList) {
            article.setMonthlySales(articleMapper.selectSumByMonthlySales(article.getId(), dateNowStr));
//            Integer count = (Integer) RedisUtil.get(article.getId() + Common.KUCUN);
            Integer count = (Integer) RedisUtil.get(article.getId() + Common.KUCUN);
            if (count != null) {
                article.setCurrentWorkingStock(count);
            }
        }
        getArticleDiscount(currentShopId, articleList, show);
        return articleList;
    }

    @Override
    public List<Article> selectListByShopIdRecommendCategory(String currentShopId, String recommendCcategoryId, String show) {
        List<Article> articleList = articleMapper.selectListByShopIdRecommendCategory(currentShopId, recommendCcategoryId);
        //当前时间的年月
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String dateNowStr = sdf.format(d);
        for (Article article : articleList) {
            Integer count = (Integer) RedisUtil.get(article.getId() + Common.KUCUN);
            if (count != null) {
                article.setCurrentWorkingStock(count);
            }
            article.setMonthlySales(articleMapper.selectSumByMonthlySales(article.getId(), dateNowStr));
            article.setRecommendCategoryId(recommendCcategoryId);
        }
        getArticleDiscount(currentShopId, articleList, show);
        RecommendCategory recommendCategory=recommendCategoryService.selectById(recommendCcategoryId);
        if(recommendCategory!=null){
            if(recommendCategory.getType()==0){
                Collections.sort(articleList, new Comparator<Article>() {
                    public int compare(Article o1, Article o2) {
                        Integer a = o1.getMonthlySales();
                        Integer b = o2.getMonthlySales();
                        // 升序
                        //return a.compareTo(b);
                        // 降序
                         return b.compareTo(a);
                    }
                });
            }
        }
        return articleList;
    }

    @Override
    public List<Article> getArticleListByFamily(String shopId, String articleFamilyId, Integer currentPage, Integer showCount) {
        List<SupportTime> supportTime = supportTimeService.selectNowSopport(shopId);
        if (supportTime.isEmpty()) {
            return null;
        }
        List<Integer> list = new ArrayList<>(ApplicationUtils.convertCollectionToMap(Integer.class, supportTime).keySet());
        List<Article> articles = articleMapper.getArticleListByFamily(list, shopId, articleFamilyId, currentPage, showCount);
        getArticleDiscount(shopId, articles, "wechat");
        return articles;
    }

    @Override
    public void updateInitialsById(String initials, String articleId) {
        articleMapper.updateInitialsById(initials, articleId);
    }

    @Override
    public List<Article> selectArticleList() {
        return articleMapper.selectArticleList();
    }

    public void getArticleDiscount(String shopId, List<Article> articles, String show) {
        Map<String, Article> articleMap = selectAllSupportArticle(shopId);
        for (Article a : articles) {
            if (a.getArticleType() == Article.ARTICLE_TYPE_SIGNLE) {//单品
                if (!StringUtil.isEmpty(a.getHasUnit())) {
                    List<ArticlePrice> prices = articlePriceServer.selectByArticleId(a.getId());
                    for (ArticlePrice price : prices) {
//                        Integer ck = (Integer) RedisUtil.get(price.getId() + Common.KUCUN);
                        Integer ck = (Integer) RedisUtil.get(price.getId() + Common.KUCUN);
                        if (ck != null) {
                            price.setCurrentWorkingStock(ck);
                        }
                    }
                    a.setArticlePrices(prices);
                }
            } else if (a.getArticleType() == Article.ARTICLE_TYPE_MEALS) {//套餐
                List<MealAttr> mealAttrs = mealAttrService.selectFullByArticleId(a.getId(), show);
                a.setMealAttrs(mealAttrs);
            }
            if (!articleMap.containsKey(a.getId())) {
                a.setIsEmpty(true);
            } else {
                //设置菜品的折扣百分比
                a.setDiscount(articleMap.get(a.getId()).getDiscount());
            }
        }
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
    public void changeEmpty(Integer isEmpty, String articleId) {
        articleMapper.changeEmpty(isEmpty, articleId);
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
//        articleMapper.initSuitStock();
        articleMapper.initSize();
    }

    @Override
    public List<ArticleStock> getStock(String shopId, String familyId, Integer empty, Integer activated) {
        FreeDay day = freedayMapper.selectByDate(DateFormatUtils.format(new Date(), "yyyy-MM-dd"), shopId);
        int freeDay = 0;
        if (day == null) {
            freeDay = 1;
        }
        List<ArticleStock> result = articleMapper.getStock(shopId, familyId, empty, freeDay, activated);
        List<ArticleStock> array = new ArrayList<>();
        for (ArticleStock articleStock : result) {
//            Integer ck = (Integer) RedisUtil.get(articleStock.getId() + Common.KUCUN);
            Integer ck = (Integer) RedisUtil.get(articleStock.getId() + Common.KUCUN);
            if (ck != null) {
                articleStock.setCurrentStock(ck);
            }
            if (empty != null && empty == Common.YES) {
                //售罄
                if (articleStock.getCurrentStock() == 0) {
                    array.add(articleStock);
                }
            } else {
                array.add(articleStock);
            }

        }
        return array;
    }

    @Override
    public Boolean clearStock(String articleId, String shopId) {
        Article article = null;
        ShopDetail shopDetail = shopDetailService.selectById(shopId);
        Brand brand = brandService.selectById(shopDetail.getBrandId());
        String emptyRemark = "【手动沽清】";
//        RedisUtil.set(articleId + Common.KUCUN, 0);
        RedisUtil.set(articleId + Common.KUCUN, 0);
        String baseArticleId = articleId;
        if (articleId.indexOf("@") > -1) {
            String aid = articleId.substring(0, articleId.indexOf("@"));
            article = articleMapper.selectByPrimaryKey(aid);
            articlePriceServer.clearPriceStock(articleId, emptyRemark);
            baseArticleId = aid;

        } else {
            article = articleMapper.selectByPrimaryKey(articleId);
            articleMapper.clearStock(articleId, emptyRemark);
            articlePriceServer.clearPriceTotal(articleId, emptyRemark);
        }


        List<ArticlePrice> articlePrices = articlePriceServer.selectByArticleId(baseArticleId);
        int sum = 0;
        if (!CollectionUtils.isEmpty(articlePrices)) {
            for (ArticlePrice articlePrice : articlePrices) {
                Integer ck = (Integer) RedisUtil.get(articlePrice.getId() + Common.KUCUN);
                if (ck != null) {
                    sum += ck;
                } else {
                    sum += articlePrice.getCurrentWorkingStock();
                }
            }
            RedisUtil.set(baseArticleId + Common.KUCUN, sum);
            if (sum == 0) {
                articleMapper.setEmpty(baseArticleId);
            } else {
                articleMapper.setEmptyFail(baseArticleId);
            }
        }


//        List<Article> taocan = orderMapper.getStockBySuit(shopDetail.getId());
//        for(Article tc : taocan){
//            Integer suit = (Integer) RedisUtil.get(tc.getId()+Common.KUCUN);
//            if(suit != null){
//                if(suit == 0 && tc.getCount() > 0){
//                    orderMapper.setEmptyFail(tc.getId());
//                }
//                RedisUtil.set(tc.getId()+Common.KUCUN,tc.getCount());
//            }else{
//                if(tc.getIsEmpty() && tc.getCount() > 0){
//                    orderMapper.setEmptyFail(tc.getId());
//                }
//                RedisUtil.set(tc.getId()+Common.KUCUN,tc.getCount());
//            }
//        }


//        articleMapper.cleanPriceAll(articleId,emptyRemark);//方法重复
        //如果有规格的

//        orderMapper.setStockBySuit(shopId);
//        articleMapper.initSizeCurrent();
//        articleMapper.clearMain(articleId, emptyRemark);

        Map map = new HashMap(4);
        map.put("brandName", brand.getBrandName());
        map.put("fileName", shopDetail.getName());
        map.put("type", "posAction");
        map.put("content", "店铺:" + shopDetail.getName() + "在pos端沽清了菜品(" + article.getName() + ")Id为:" + articleId + ",请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(url, map);
        return true;
    }

    @Override
    public Boolean editStock(String articleId, Integer count, String shopId) {
        Article article = null;
        String baseArticleId = articleId;
        Boolean moreType = false;
        String aid = articleId;
        if (articleId.indexOf("@") > -1) {
            moreType = true;
            aid = articleId.substring(0, articleId.indexOf("@"));
            article = articleMapper.selectByPrimaryKey(aid);
        } else {
            article = articleMapper.selectByPrimaryKey(articleId);
        }
        ShopDetail shopDetail = shopDetailService.selectById(shopId);
        Brand brand = brandService.selectById(shopDetail.getBrandId());
        String emptyRemark = count <= 0 ? "【手动沽清】" : null;
//        RedisUtil.set(articleId + Common.KUCUN, count);
            RedisUtil.set(articleId + Common.KUCUN, count);
        if (article.getIsEmpty()) {
            if (moreType && count > 0) {
                articlePriceServer.setArticlePriceEmptyFail(baseArticleId);
            } else if (!moreType && count > 0) {
                articleMapper.setEmptyFail(articleId);
            }
        } else {
            if (moreType && count == 0) {
                articlePriceServer.setArticlePriceEmpty(baseArticleId);
            } else if (!moreType && count == 0) {
                articleMapper.setEmpty(articleId);
            } else if (moreType && count > 0) {
                articlePriceServer.setArticlePriceEmptyFail(baseArticleId);
            } else if (!moreType && count > 0) {
                articleMapper.setEmptyFail(articleId);
            }
        }

        List<ArticlePrice> articlePrices = articlePriceServer.selectByArticleId(aid);
        int sum = 0;
        if (!CollectionUtils.isEmpty(articlePrices)) {
            for (ArticlePrice articlePrice : articlePrices) {
                Integer ck = (Integer) RedisUtil.get(articlePrice.getId() + Common.KUCUN);
                if (ck != null) {
                    sum += ck;
                } else {
                    sum += articlePrice.getCurrentWorkingStock();
                }
            }
            RedisUtil.set(aid + Common.KUCUN, sum);
            if (sum == 0) {
                articleMapper.setEmpty(aid);
            } else {
                articleMapper.setEmptyFail(aid);
            }
        }


//        List<Article> taocan = orderMapper.getStockBySuit(shopDetail.getId());
//        for(Article tc : taocan){
//            Integer suit = (Integer) RedisUtil.get(tc.getId()+Common.KUCUN);
//            if(suit != null){
//                if(suit == 0 && tc.getCount() > 0){
//                    orderMapper.setEmptyFail(tc.getId());
//                }
//                RedisUtil.set(tc.getId()+Common.KUCUN,tc.getCount());
//            }else{
//                if(tc.getIsEmpty() && tc.getCount() > 0){
//                    orderMapper.setEmptyFail(tc.getId());
//                }
//                RedisUtil.set(tc.getId()+Common.KUCUN,tc.getCount());
//            }
//        }
//        articleMapper.editStock(articleId, count, emptyRemark);
//        articleMapper.editPriceStock(articleId, count, emptyRemark);
//        orderMapper.setStockBySuit(shopId);
//        articleMapper.initSizeCurrent();

//        articleMapper.initEmpty();
        Map map = new HashMap(4);
        map.put("brandName", brand.getBrandName());
        map.put("fileName", shopDetail.getName());
        map.put("type", "posAction");
        map.put("content", "店铺:" + shopDetail.getName() + "修改菜品(" + article.getName() + ")Id为:" + articleId + "的库存为:" + count + ",请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(url, map);
        return true;
    }

    @Override
    public Boolean setActivated(String articleId, Integer activated) {
        Article article = articleMapper.selectByPrimaryKey(articleId);
        ShopDetail shopDetail = shopDetailService.selectById(article.getShopDetailId());
        Brand brand = brandService.selectById(shopDetail.getBrandId());
        int row = articleMapper.setActivate(articleId, activated);
        Map map = new HashMap(4);
        map.put("brandName", brand.getBrandName());
        map.put("fileName", shopDetail.getName());
        map.put("type", "posAction");
        if (activated.equals(0)) {
            map.put("content", "店铺:" + shopDetail.getName() + "在pos端下架了菜品(" + article.getName() + ")Id为:" + articleId + ",请求服务器地址为:" + MQSetting.getLocalIP());
        } else {
            map.put("content", "店铺:" + shopDetail.getName() + "在pos端上架了菜品(" + article.getName() + ")Id为:" + articleId + ",请求服务器地址为:" + MQSetting.getLocalIP());
        }
        doPostAnsc(url, map);
        return row > 0 ? true : false;
    }

    @Override
    public List<Article> getSingoArticle(String shopId) {
        return articleMapper.getSingoArticle(shopId);
    }

    @Override
    public List<Article> getSingoArticleAll(String shopId) {
        return articleMapper.getSingoArticleAll(shopId);
    }

    @Override
    public void deleteRecommendId(String recommendId) {
        articleMapper.deleteRecommendId(recommendId);
    }


    @Override
    public void assignArticle(String[] shopList, String[] articleList) {
        for (String articleId : articleList) { //遍历要复制的菜品
            Article article = articleMapper.selectByPrimaryKey(articleId); //得到要复制的菜品

            //得到要复制的菜品分类
            ArticleFamily articleFamily = articleFamilyService.selectById(article.getArticleFamilyId());
            //循环店铺
            for (String shopId : shopList) {
                //将菜品的店铺设置为要复制的店铺
                article.setId(ApplicationUtils.randomUUID());
                article.setShopDetailId(shopId);

                //判断该店铺下是否已有菜品分类
                ArticleFamily family = articleFamilyService.checkSame(shopId, articleFamily.getName());
                if (family == null) {
                    //如果没有,那么生成
                    String familyId = ApplicationUtils.randomUUID();
                    articleFamily.setId(familyId);
                    articleFamily.setShopDetailId(shopId);
                    articleFamilyService.copyBrandArticleFamily(articleFamily);
                    article.setArticleFamilyId(familyId);
                } else {
                    //如果有,那么覆盖
                    //todo
                    articleFamily.setId(family.getId());
                    articleFamily.setShopDetailId(shopId);
                    articleFamilyService.update(articleFamily);
                    article.setArticleFamilyId(family.getId());
                }

                //判断下是不是有规格单品
                List<ArticleAttr> articleAttrs = articleAttrService.selectListByArticleId(articleId);
                StringBuilder hasUnit = new StringBuilder();

                if (!CollectionUtils.isEmpty(articleAttrs)) {
                    for (ArticleAttr articleAttr : articleAttrs) {
                        //得到要复制的规格
                        List<ArticleUnit> articleUnits = articleUnitService.selectListByAttrId(articleAttr.getId());
                        ArticleAttr same = articleAttrService.selectSame(articleAttr.getName(), shopId);
                        //不存在相同规格属性
                        if (same == null) {
                            articleAttr.setId(null);
                            articleAttr.setShopDetailId(shopId);
                            articleAttrService.insertByAuto(articleAttr);
                        } else {
                            //存在相同规格属性,覆盖
                            articleAttr.setId(same.getId());
                            articleAttr.setShopDetailId(shopId);
                            articleAttrService.update(articleAttr);
                        }
                        for (ArticleUnit articleUnit : articleUnits) {
                            ArticlePrice articlePrice = articlePriceServer.selectByArticle(articleId, articleUnit.getId());
                            if (articlePrice == null) {
                                continue;
                            }
                            ArticleUnit sameUnit = articleUnitService.selectSame(articleUnit.getName(), articleAttr.getId().toString());
                            if (sameUnit == null) {
                                articleUnit.setTbArticleAttrId(articleAttr.getId());
                                articleUnit.setId(null);
                                articleUnitService.insertByAuto(articleUnit);
                            } else {
                                articleUnit.setId(sameUnit.getId());
                                articleUnit.setTbArticleAttrId(articleAttr.getId());
                                articleUnitService.update(articleUnit);
                            }
                            hasUnit.append(articleUnit.getId()).append(",");

                            ArticlePrice copy = articlePriceServer.selectById(article.getId() + "@" + articlePrice.getUnitIds());
                            if (copy != null) {
                                articlePrice.setArticleId(article.getId());
                                articlePrice.setUnitIds(articleUnit.getId().toString());
                                articlePrice.setId(article.getId() + "@" + articlePrice.getUnitIds());
                                articlePriceServer.update(articlePrice);
                            } else {
                                articlePrice.setArticleId(article.getId());
                                articlePrice.setUnitIds(articleUnit.getId().toString());
                                articlePrice.setId(article.getId() + "@" + articlePrice.getUnitIds());
                                articlePriceServer.insert(articlePrice);
                            }
                        }
                    }
                }
                if (!StringUtils.isEmpty(hasUnit.toString())) {
                    article.setHasUnit(hasUnit.toString().substring(0, hasUnit.length() - 1));
                }
                article.setpId(articleId);
                //判断要复制的菜品是否已经在该店铺下生成过
                Article copy = articleMapper.selectByPid(articleId, shopId);
                if (copy != null) {
                    //如果生成过,那么覆盖
                    //todo
                    article.setId(copy.getId());
                    articleMapper.updateByPrimaryKeySelective(article);
                } else {
                    articleMapper.insert(article);
                }
            }
        }
    }

    @Override
    public void assignTotal(String[] shopList, String[] articleList) {
        for (String articleId : articleList) { //遍历菜品
            Article article = articleMapper.selectByPrimaryKey(articleId); //得到菜品
            //循环店铺
            for (String shopId : shopList) {
                if (article.getArticleType().equals(ArticleType.TOTAL_ARTICLE)) { //套餐
                    //如果是套餐的话，先获取套餐下的全部单品
                    List<Article> articles = articleMapper.getArticleByMeal(articleId);
                    for (Article art : articles) {
                        art.setShopDetailId(shopId);
                        art.setpId(art.getId());
                        //得到菜品分类
                        ArticleFamily family = articleFamilyService.selectById(art.getArticleFamilyId());
                        ArticleFamily articleFamily = articleFamilyService.checkSame(shopId, family.getName());
                        if (articleFamily == null) {
                            //每个店铺生成自己的菜品分类
                            String familyId = ApplicationUtils.randomUUID();
                            family.setId(familyId);
                            family.setShopDetailId(shopId);
                            articleFamilyService.copyBrandArticleFamily(family);
                            art.setArticleFamilyId(familyId);
                        } else {
                            articleFamily.setId(articleFamily.getId());
                            articleFamily.setShopDetailId(shopId);
                            articleFamilyService.update(articleFamily);
                            art.setArticleFamilyId(articleFamily.getId());
                        }

                        //判断每个单品是不是全部已经引入
                        Article copy = articleMapper.selectByPid(art.getId(), shopId);
                        if (copy != null) {
                            //如果生成过,那么覆盖
                            //todo
                            art.setId(copy.getId());
                            articleMapper.updateByPrimaryKeySelective(art);

                        } else {
                            art.setId(ApplicationUtils.randomUUID());
                            articleMapper.insert(art);
                        }
                    }
//                    //得到要复制的套餐属性
                    List<MealAttr> attrs = mealAttrService.selectList(articleId);
                    for (MealAttr attr : attrs) {
                        //循环旧的套餐属性
                        List<MealItem> mealItems = mealItemService.selectByAttrId(attr.getId());
                        attr.setId(null);
                        attr.setArticleId(articleMapper.selectByPid(article.getId(), shopId).getId());
                        mealAttrService.insert(attr);
                        for (MealItem mealItem : mealItems) {
                            mealItem.setMealAttrId(attr.getId());
                            mealItem.setArticleId(articleMapper.selectByPid(mealItem.getArticleId(), shopId).getId());
                            mealItem.setId(null);
                            mealItemService.insert(mealItem);
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<Article> delCheckArticle(String id) {
        return articleMapper.delCheckArticle(id);
    }

    @Override
    public void updatePhotoSquare(@Param("id") String id, @Param("photoSquare") String photoSquare) {
        articleMapper.updatePhotoSquare(id, photoSquare);
    }

    @Override
    public void updateArticleImg(Article article) {
        articleMapper.updateByPrimaryKeySelective(article);
    }

    @Override
    public void addArticleLikes(String articleId) {
        articleMapper.addArticleLikes(articleId);
    }

    @Override
    public List<Article> selectsingleItem(String shopId) {
        return articleMapper.selectsingleItem(shopId);
    }

    @Override
    public List<ArticleSellDto> callOrderArtcile(Map<String, Object> selectMap) {
        return articleMapper.queryOrderArtcile(selectMap);
    }


    @Override
    public List<ArticleSellDto> selectArticleByType(Map<String, Object> selectMap) {
        return articleMapper.selectArticleByType(selectMap);
    }

    @Override
    public Map<String, Object> callArticleOrderCount(Map<String, Object> selectMap) {
        return articleMapper.selectArticleOrderCount(selectMap);
    }

    @Override
    public List<String> selectArticleSort(Map<String, Object> selectMap) {
        return articleMapper.selectArticleSort(selectMap);
    }

    @Override
    public List<Article> selectnewPosListByFamillyId(String shopId, Integer page, Integer size, String familyId) {
        List<SupportTime> supportTime = supportTimeService.selectNowSopport(shopId);
        if (supportTime.isEmpty()) {
            return null;
        }
        List<Integer> list = new ArrayList<>(ApplicationUtils.convertCollectionToMap(Integer.class, supportTime).keySet());
        PageHelper.startPage(page, size);
        List<Article> articleList = articleMapper.selectnewPosListByFamillyId(list, shopId, familyId);
        PageInfo<Article> pageInfo = new PageInfo<>(articleList);
        return pageInfo.getList();

    }

    @Override
    public List<Article> selectHasResourcePhotoList(String currentBrandId) {
        return articleMapper.selectHasResourcePhotoList(currentBrandId);
    }

    @Override
    public Boolean setEmpty(String articleId) {
        return articleMapper.setEmpty(articleId);
    }

    @Override
    public Boolean setEmptyFail(String articleId) {
        return articleMapper.setEmptyFail(articleId);
    }
    @Override
    public List<Article> selectArticleByShopId(String shopId) {
        return articleMapper.selectArticleByShopId(shopId);
    }

    @Override
    public void sendMsgToLocalPosUpdateData(String brandId, String shopId) {
        MQMessageProducer.sendUpdateDateToLocalPos(brandId,shopId);
    }

}
