package com.resto.shop.web.controller.scm;

import com.resto.brand.core.entity.Result;
import com.resto.scm.web.dto.CategoryOne;
import com.resto.scm.web.model.MdCategory;
import com.resto.scm.web.service.CategoryService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.util.Assertion;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("scmCategory")
public class ScmCategoryController extends GenericController {

    @Autowired
    private CategoryService categoryService;

    @RequestMapping("/list")
    public void list() {
    }

    @RequestMapping("/query")
    @ResponseBody
    public Result queryAll() {
        List<CategoryOne> list = categoryService.queryAll(this.getCurrentShopId());
        return getSuccessResult(list);
    }

    @RequestMapping("/list_all")
    @ResponseBody
    public Result listData() {
        List<MdCategory> list = categoryService.queryCategories();
        return getSuccessResult(list);
    }

    @RequestMapping("/list_categoryHierarchy")
    @ResponseBody
    public Result list_categoryHierarchy(Integer categoryHierarchy) {
        List<MdCategory> list = categoryService.queryByCategoryHierarchy(categoryHierarchy);
        return getSuccessResult(list);
    }

    @RequestMapping("/list_one")
    @ResponseBody
    public Result list_one(Long id) {
        MdCategory mdCategory = categoryService.queryById(id);
        return getSuccessResult(mdCategory);
    }

    @RequestMapping("look_down")
    @ResponseBody
    public Result look_down(Integer hierarchyId) {
        if (hierarchyId == null) {
            return new Result("hierarchyId不能为空", 5000, false);
        } else if (hierarchyId >= 3) {
            return new Result("已经是最小层级", 5000, false);
        } else {
            List<MdCategory> list = categoryService.queryByCategoryHierarchy(hierarchyId + 1);
            return getSuccessResult(list);
        }
    }

    @RequestMapping("create")
    @ResponseBody
    public Result create(@Valid @RequestBody MdCategory mdCategory) {
        try{
            Assertion.isNull(mdCategory,"新增参数不能为空");
            Assertion.isEmpty(mdCategory.getCategoryName(),"名称不能为空");
            Assertion.isPositive(mdCategory.getParentId(),"父类id不能为空");
            Assertion.isPositive(mdCategory.getCategoryHierarchy(),"层级id不能为空");
            Assertion.isPositive(mdCategory.getSort(),"排序不能为空");
            Assertion.isEmpty(mdCategory.getCategoryDesc(),"分类备注不能为空");

            mdCategory.setShopDetailId(this.getCurrentShopId());
            mdCategory.setBrandId(this.getCurrentBrandId());
            int i = categoryService.addCategory(mdCategory);
            if (i > 0) {
                return Result.getSuccess();
            }
        }catch (Exception e){
            return new Result(e.getMessage(), 5000, false);
        }
        return new Result("保存失败", 5000, false);
    }

    @RequestMapping("modify")
    @ResponseBody
    public Result modify(@Valid MdCategory mdCategory) {
        Integer row = categoryService.updateMdCategory(mdCategory);
        return Result.getSuccess();
    }

    @RequestMapping("delete")
    @ResponseBody
    public Result delete(Long id) {
        Integer row = categoryService.deleteById(id);
        return Result.getSuccess();
    }

}
