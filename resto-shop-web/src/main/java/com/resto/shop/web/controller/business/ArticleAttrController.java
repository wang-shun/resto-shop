package com.resto.shop.web.controller.business;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.ArticleAttr;
import com.resto.shop.web.service.ArticleAttrService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("articleattr")
public class ArticleAttrController extends GenericController {

    @Resource
    ArticleAttrService articleattrService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @RequestMapping("/list")
    public void list() {
    }

    @RequestMapping("/list_all")
    @ResponseBody
    public List<ArticleAttr> listData() {
        return articleattrService.selectListByShopId(getCurrentShopId());
    }

    @RequestMapping("list_one")
    @ResponseBody
    public Result list_one(Integer id) {
        ArticleAttr articleattr = articleattrService.selectById(id);
        return getSuccessResult(articleattr);
    }

    @RequestMapping("create")
    @ResponseBody
    public Result create(@Valid ArticleAttr articleAttr) {
        articleAttr.setShopDetailId(getCurrentShopId());
        articleattrService.create(articleAttr);
        return Result.getSuccess();
    }

    @RequestMapping("modify")
    @ResponseBody
    public Result modify(@Valid ArticleAttr brand) {
        articleattrService.updateInfo(brand);
        return Result.getSuccess();
    }

    @RequestMapping("delete")
    @ResponseBody
    public Result delete(Integer id) {
        articleattrService.deleteInfo(id);
        return Result.getSuccess();
    }
}
