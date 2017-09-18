package com.resto.shop.web.controller.business;


import com.resto.brand.core.entity.Result;
import com.resto.brand.core.util.PinyinUtil;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.PlatformService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.constant.ArticleType;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.Article;
import com.resto.shop.web.model.ArticlePrice;
import com.resto.shop.web.model.ArticleRecommendPrice;
import com.resto.shop.web.model.MealItem;
import com.resto.shop.web.service.*;
import com.resto.shop.web.util.LogTemplateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


@Controller
@RequestMapping("article")
public class ArticleController extends GenericController {

    @Resource
    FreedayService freedayService;

    @Resource
    ArticleService articleService;

    @Resource
    ArticlePriceService articlePriceService;

    @Resource
    BrandSettingService brandSettingService;

    @Resource
    BrandService brandService;

    @Resource
    ShopDetailService shopDetailService;

    @Autowired
    private UnitService unitService;

    @Autowired
    private ArticleRecommendService articleRecommendService;

    @Resource
    private MealItemService mealItemService;

    @Autowired
    private PlatformService platformService;

    @RequestMapping("/list")
    public void list() {
    }

    @RequestMapping("/list_all")
    @ResponseBody
    public List<Article> listData() {
        List<Article> articles = articleService.selectList(getCurrentShopId());
        return articles;
    }

    @RequestMapping("list_one")
    @ResponseBody
    public Result list_one(String id) {
        Article article = articleService.selectById(id);
        return getSuccessResult(article);
    }

    @RequestMapping("list_one_full")
    @ResponseBody
    public Result list_one_full(String id) {
        Article article = articleService.selectFullById(id, "");
        article.setUnits(unitService.getUnitByArticleid(id));
//        List<Platform> platforms =  platformService.selectByBrandId(getCurrentBrandId());
//        article.setPlatforms(platforms);
        return getSuccessResult(article);
    }


    @RequestMapping("save")
    @ResponseBody
    public Result create(@Valid @RequestBody Article article) {
        article.setShopDetailId(getCurrentShopId());
        article.setUpdateUserId(getCurrentUserId());
        article.setUpdateTime(new Date());
        String id = article.getId();
        if (StringUtils.isEmpty(article.getId())) {
            article.setCreateUserId(getCurrentUserId());
            article.setInitials(PinyinUtil.getPinYinHeadChar(article.getName()));
            id = articleService.save(article).getId();
            unitService.insertArticleRelation(id, article.getUnits());
        } else {
            article.setInitials(PinyinUtil.getPinYinHeadChar(article.getName()));
            articleService.update(article);
            //修改单品的时候如果存在推荐餐包 联动修改
            if (article.getArticleType() == ArticleType.SIMPLE_ARTICLE) {
                List<ArticleRecommendPrice> articleRecommendPrice = articleRecommendService.selectByRecommendArticleInfo(article.getId());
                for (ArticleRecommendPrice ar : articleRecommendPrice) {
                    articleRecommendService.updatePriceById(article.getFansPrice() != null ? article.getFansPrice() : article.getPrice(), ar.getId());
                }
                //联动修改套餐子品名称
                List<MealItem> mealItemList = mealItemService.selectByArticleId(article.getId());
                if(mealItemList.size() > 0){
                    for(MealItem mealItem : mealItemList){
                        mealItem.setArticleName(article.getName());
                        mealItemService.updateArticleNameById(article.getName(), mealItem.getId());
                    }
                }
            }

            List<ArticlePrice> list = articlePriceService.selectByArticleId(article.getId());
            if (article.getIsEmpty() == true) {
                articleService.clearStock(article.getId(), getCurrentShopId());
            } else {
                if (article.getArticleType() == 1 && list.size() == 0) {
                    if (freedayService.selectExists(new Date(), article.getShopDetailId())) {
                        articleService.editStock(article.getId(), article.getStockWeekend(), getCurrentShopId());
                    } else {
                        articleService.editStock(article.getId(), article.getStockWorkingDay(), getCurrentShopId());
                    }
                } else if (article.getArticleType() == 1 && list.size() != 0) {
                    if (freedayService.selectExists(new Date(), article.getShopDetailId())) {
                        for (ArticlePrice ap : list) {
                            articleService.editStock(ap.getId(), ap.getStockWeekend(), getCurrentShopId());
                        }
                    } else {
                        for (ArticlePrice ap : list) {
                            articleService.editStock(ap.getId(), ap.getStockWorkingDay(), getCurrentShopId());
                        }
                    }
                }
            }
            if (article.getActivated() == true) {
                articleService.setActivated(article.getId(), 1);
            } else {
                articleService.setActivated(article.getId(), 0);
            }
            unitService.updateArticleRelation(id, article.getUnits());
        }
//        articleService.initStock();
        Brand brand = brandService.selectByPrimaryKey(getCurrentBrandId());
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(getCurrentShopId());
        LogTemplateUtils.articleEdit(brand.getBrandName(), shopDetail.getName(), getCurrentBrandUser().getUsername());
        return Result.getSuccess();
    }

    @RequestMapping("delete")
    @ResponseBody
    public Result delete(String id) {
        Article article = articleService.selectById(id);
        if (article.getArticleType() == ArticleType.SIMPLE_ARTICLE) {
            //单品时校验
            List<Article> articles = articleService.delCheckArticle(id);
            if (articles.size() != 0) {
                StringBuffer mess = new StringBuffer();
                for (Article art : articles) {
                    mess.append(art.getName() + "，");
                }
                Result result = new Result();
                result.setSuccess(false);
                result.setMessage("删除失败，在" + mess.toString().substring(0, mess.toString().length() - 1) + "套餐存在！");
                result.setStatusCode(100);
                return result;
            }
        }
        articleService.delete(id);
        //联动删除在推荐餐品包中的id
        articleRecommendService.deleteRecommendByArticleId(id);
        Brand brand = brandService.selectByPrimaryKey(getCurrentBrandId());
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(getCurrentShopId());
        LogTemplateUtils.articleEdit(brand.getBrandName(), shopDetail.getName(), getCurrentBrandUser().getUsername());
        return Result.getSuccess();
    }

    public boolean IsFreeday(Date time) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        int dayForWeek = 0;
        if (cal.get(Calendar.DAY_OF_WEEK) == 1) {
            dayForWeek = 7;
        } else {
            dayForWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        }
        if (dayForWeek > 5) {
            return false;
        }
        return true;
    }

    @RequestMapping("singo_article")
    @ResponseBody
    public List<Article> getSingoList() {
        List<Article> result = articleService.getSingoArticle(getCurrentShopId());
        return result;
    }

    @RequestMapping("singo_article_all")
    @ResponseBody
    public List<Article> getSingoListAll() {
        List<Article> result = articleService.getSingoArticleAll(getCurrentShopId());
        return result;
    }

    @RequestMapping("/selectsingleItem")
    @ResponseBody
    public Result selectsingleItem() {
        List<Article> list = null;
        try {
            list = articleService.selectsingleItem(getCurrentShopId());
            return getSuccessResult(list);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result(false);
        }
    }
}
